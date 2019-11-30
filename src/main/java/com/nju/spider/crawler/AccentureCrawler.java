package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.JDBCUtils;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.download.DownloadStrategy;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
public class AccentureCrawler extends BaseCrawler{
    private static final String orgName = "Accenture";
    private static final int retryTimes = 8;
    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

    private static final String historyIndexUrl = "https://newsroom.accenture.com/?page=%s";
    private static final String updateIndexUrl = "https://newsroom.accenture.com/";
    private static final String baseUrl = "https://newsroom.accenture.com";

    private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("MMM dd, yyyy", Locale.US));

    @Override
    public String getCrawlName() {
        return orgName;
    }


    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public void crawl() {
        //先爬历史
        for (int i = 315; i <= 433; i++) {
            String historyUrlToCrawl = String.format(historyIndexUrl, i);
            try {
                log.info("starting to crawl url: " + historyUrlToCrawl);
                List<Report> reportList = new ArrayList<>();
                String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl, retryTimes);
                TagNode rootNode = MyHtmlCleaner.clean(res);
                //获得文章页地址
                Object [] articleUrlObs = rootNode.evaluateXPath("//div[@id='tek-wrap-centerwell']//h4/a[@data-analytics-content-type='engagement']/@href");
//                //publishDate还是从文章里取吧
//                Document doc = new DomSerializer(new CleanerProperties()).createDOM(rootNode);
//                XPath xpath = XPathFactory.newInstance().newXPath();
//                NodeList nodeList = (NodeList) xpath.evaluate("//div[@id='tek-wrap-centerwell']//div[contains(@id, 'wrap-article-listing')]", doc, XPathConstants.NODESET);
                for (Object articleUrlOb: articleUrlObs) {
                    try {
                        String articleUrl = baseUrl + articleUrlOb;
                        String articleContent = HttpUtils.doGetWithRetryUsingProxy(articleUrl, retryTimes);
                        TagNode articleRootNode = MyHtmlCleaner.clean(articleContent);
                        Document articleDoc = new DomSerializer(new CleanerProperties()).createDOM(articleRootNode);
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        String publishDateStr = xpath.evaluate("//div[contains(@class, 'rel-date')]", articleDoc);
                        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr.trim(), simpleDateFormatThreadLocal.get());

                        String title = xpath.evaluate("//article//div[contains(@style, 'center')]//strong/text() | //article//strong/center/text()" +
                                " | //div[@id='art-hero']//h1/text()" +
                                " | //div[@id='content-details']//div[@align='center']/b/text()", articleDoc);

                        Object[] hrefs = articleRootNode.evaluateXPath("//article//a/@href");
                        for (Object hrefObj : hrefs) {
                            try {
                                //对链接进行分析
                                String href = ((String) hrefObj).trim();
                                if (href.contains("mailto") || href.trim().equals("http://www.accenture.com/")
                                        || href.trim().equals("http://www.accenture.com") || !href.contains("accenture")) {
                                    continue;
                                }

                                if (href.startsWith("//")) {
                                    href = "https:" + href;
                                }


                                Report tmpReport = new Report();
                                tmpReport.setOrgName(orgName);
                                tmpReport.setPublishTime(publishDate);
                                tmpReport.setUrl(href);
                                tmpReport.setTitle(title);
                                tmpReport.setIndexUrl(historyUrlToCrawl);
                                tmpReport.setArticleUrl(href);

                                //只处理本站内的链接
                                if (href.contains(".pdf")) {
                                    tmpReport.setArticleUrl(articleUrl);
                                    //确定是一篇pdf，则下载
                                    reportList.add(tmpReport);
                                    continue;
                                }

                                //继续判断是否是pdf下载
                                String res2 = HttpUtils.judgeUrlIfPdfDownloadWithRetryTimes(href, retryTimes, true);

                                if (StringUtils.equals(res2, "pdf")) {
                                    tmpReport.setArticleUrl(articleUrl);
                                    reportList.add(tmpReport);
                                    continue;
                                }

                                if (StringUtils.isBlank(res2)) {
                                    continue;
                                }

                                TagNode res2RootNode = MyHtmlCleaner.clean(res2);
                                Document res2Doc = new DomSerializer(new CleanerProperties()).createDOM(res2RootNode);
                                XPath xpath2 = XPathFactory.newInstance().newXPath();
                                String reportUrl = xpath2.evaluate("//a[contains(@data-analytics-link-name, 'FULL REPORT')]/@href " +
                                        "| //a[contains(@data-analytics-link-name, 'full report')]/@href  " +
                                        "|  //a[contains(@data-analytics-link-name, 'read the report')]/@href " +
                                        "| //a[contains(@data-analytics-link-name, 'READ THE REPORT')]/@href " +
                                        "| //a[contains(@title, 'full article')]/@href ", res2Doc);
                                if (StringUtils.isNotBlank(reportUrl)) {
                                    if (reportUrl.startsWith("//")) {
                                        reportUrl = "https:" + reportUrl;
                                    }
                                      tmpReport.setUrl(reportUrl);
                                      reportList.add(tmpReport);
//                                        //宁愿多访问一次网络，确保确实是pdf
//                                        String res3 = HttpUtils.judgeUrlIfPdfDownloadWithRetryTimes(reportUrl, retryTimes, true);
//                                        if (StringUtils.equals(res3, "pdf")) {
//                                            tmpReport.setUrl(reportUrl);
//                                            reportList.add(tmpReport);
//                                            continue;
//                                        }


                                }
                            } catch (Exception ex) {
                                log.error("dealing with href " + hrefObj + " encounts error", ex);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("dealing with articleUrl " + articleUrlOb + " encounts error", ex);
                    }
                }

                ReportDaoUtils.insertReports(reportList);

            } catch (Exception e) {
                log.error("dealing history url encounts error " + historyUrlToCrawl, e);
            }


        }

    }
}
