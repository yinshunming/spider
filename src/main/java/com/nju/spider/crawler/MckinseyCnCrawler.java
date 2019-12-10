package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName MckinseyCrawler
 * @Description 麦肯锡中国爬虫
 * @Author UPC
 * @Date 2019/12/7 17:50
 * @Version 1.0
 */
@Slf4j
public class MckinseyCnCrawler extends BaseCrawler{
    private static final String orgName = "MckinseyCn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 5 * 3600 * 1000;  //5h间隔抓取时间

    private static final String firstIndexUrl = "https://www.mckinsey.com.cn/insights/";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("MMM dd, yyyy", Locale.SIMPLIFIED_CHINESE));


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
        try {
            log.info("start to crawl " + firstIndexUrl);
            String indexRes = HttpUtils.doGetWithRetry(firstIndexUrl, retryTimes);
            TagNode indexRootTagNode = MyHtmlCleaner.clean(indexRes);
            Object [] indexObs1 = indexRootTagNode.evaluateXPath("//ul[@id='menu-menu-left']//li");
            Object [] indexObs2 = indexRootTagNode.evaluateXPath("//ul[@id='menu-menu-right']//li");
            Object [] indexObs =  ArrayUtils.addAll(indexObs1, indexObs2);
            for (Object indexOb : indexObs) {
                try {
                    TagNode indexTagNode = (TagNode) indexOb;
                    String industryHref = XpathUtils.getStringFromXpath(indexTagNode, "/a/@href");
                    String industryName = XpathUtils.getStringFromXpath(indexTagNode, "/a/text()");
                    String industryRes = HttpUtils.doGetWithRetry(industryHref, retryTimes);
                    TagNode industryTagNode = MyHtmlCleaner.clean(industryRes);
                    Object [] articleIndexObs = industryTagNode.evaluateXPath("//div[@class='recent-posts-content']");
                    List<Report> reportList = new ArrayList<>();
                    for (Object articleIndexOb : articleIndexObs) {
                        try {
                            TagNode articleIndexNode = (TagNode) articleIndexOb;
                            String title = XpathUtils.getStringFromXpath(articleIndexNode, "//h4/a/text()");
                            String articleUrl = XpathUtils.getStringFromXpath(articleIndexNode, "//h4/a/@href");
                            String publishDateStr = XpathUtils.getStringFromXpath(articleIndexNode, "//span[@class='date']/text()");
                            Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());

                            String articleRes = HttpUtils.doGetWithRetry(articleUrl, retryTimes);
                            TagNode articleTagNode = MyHtmlCleaner.clean(articleRes);

                            String authors = XpathUtils.getStringFromXpath(articleTagNode, "//article//p[@style='text-align: right;']/text()");
                            if (authors != null) {
                                authors = authors.trim().replace("作者：", "");
                                //太长则表示取错了,置空
                                if (authors.length() >= 50) {
                                    authors = null;
                                }
                            }

                            List<String> hrefList = XpathUtils.getStringListFromXpath(articleTagNode, "//article//div[@class='post-content']//a/@href");
                            for (String href: hrefList) {
                                if (href.endsWith(".pdf")) {
                                    Report report = new Report();
                                    report.setUrl(href);
                                    report.setTitle(title);
                                    report.setAuthors(authors);
                                    report.setArticleUrl(articleUrl);
                                    report.setIndustryName(industryName);
                                    report.setOrgName(orgName);
                                    report.setIndexUrl(industryHref);
                                    report.setPublishTime(publishDate);
                                    report.setPublishTime(publishDate);
                                    reportList.add(report);
                                }
                            }
                        } catch (Exception ex) {
                            log.error("getting article href encounts error ", ex);
                        }
                    }
                    ReportDaoUtils.insertReports(reportList);
                } catch (Exception e) {
                    log.error("getting industry urls encounts error");
                }
            }
        } catch (Exception e) {
        }

    }

    public static void main(String [] args) {
        new MckinseyCnCrawler().crawl();
    }
}
