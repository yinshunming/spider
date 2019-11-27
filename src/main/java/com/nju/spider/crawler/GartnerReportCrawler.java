package com.nju.spider.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@Slf4j
public class GartnerReportCrawler extends BaseCrawler{
    private static final String baseUrl = "https://www.gartner.com";
    private static final String indexUrl = "https://www.gartner.com/en/products/special-reports";
    private static final String indexUrl2 = "https://www.gartner.com/ngw/syspath-bin/gartner/dynamiccontent?" +
            "requestType=select-by-tags&designType=tiles&nPage=1&pageSize=78&languageCode=en&showLocalizedContent=false&" +
            "filterCodes=&randomSeed=&currentPagePath=/en/products/special-reports&tags=emt%3Apage%2Ftype%2Fspecial-reports%2Cemt%3Apage%2Fcontent-type%2Fspecial-reports";

    private static final String updateIndex = "https://www.gartner.com/ngw/syspath-bin/gartner/dynamiccontent?requestType" +
            "=select-by-tags&designType=tiles&nPage=1&pageSize=9&languageCode=en&showLocalizedContent=false&filterCodes=&" +
            "randomSeed=&currentPagePath=/en/products/special-reports&tags=emt%3Apage%2Ftype%2Fspecial-reports%2Cemt%3Apage%2Fcontent-type%2Fspecial-reports";

    private static final String orgName = "Gartner";
    private static final int retryTimes = 5;
    private static final long intervalTime = 6 * 3600 * 1000;  //6h间隔抓取时间

    private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("MMM dd, yyyy", Locale.US));

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public String getCrawlName() {
        return orgName;
    }

    @Override
    public void crawl() {
        /**
         * 首页中Featured Reports不用爬取，都在indexUrl2中，直接用json取就好
         */
//        String res = HttpUtils.doGetWithRetry(indexUrl, retryTimes);
//        try {
//            TagNode rootNode = MyHtmlCleaner.clean(res);
//            Object[] articeUrlElementHrefs =  rootNode.evaluateXPath("//a[@data-type='research']/@href");
//            for (Object articleUrlElementHref : articeUrlElementHrefs) {
//                try {
//                    String articleUrl = baseUrl +  articleUrlElementHref;
//                    String content = HttpUtils.doGetWithRetry(articleUrl, retryTimes);
//                    TagNode rootNodeContent = MyHtmlCleaner.clean(content);
//                    Object[] downloadResourceElements =  rootNodeContent.evaluateXPath("//a/i[@id='eloqua-final-submit-loading']/../@href");
//                    List<Report> reportList = new ArrayList<>();
//                    for (Object pdfUrl : downloadResourceElements) {
//                        Report report = new Report();
//                        report.setUrl((String) pdfUrl);
//                        report.setIndustryName(industryName);
//                        reportList.add(report);
//                    }
//
//                    ReportDao.insertReports(reportList);
//                } catch (Exception ex) {
//                    log.error("dealing with article url encounts error ", ex);
//                }
//            }
//        } catch (XPatherException e) {
//        }

        String res = HttpUtils.doGetWithRetry(indexUrl2, retryTimes);
        JSONObject jo = JSON.parseObject(res);
        JSONArray docs = jo.getJSONObject("data").getJSONArray("docs");
        List<Report> reportList = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            try {
                JSONObject doc = docs.getJSONObject(i);
                Report report = new Report();
                Date publishDate = FormatUtils.parseDateByMutilDateFormate(doc.getString("date"), simpleDateFormatThreadLocal.get());
                report.setPublishTime(publishDate);
                report.setOrgName(orgName);
                report.setAuthors(doc.getString("authors"));
                report.setTitle(doc.getString("title"));
                report.setExtra(doc.toJSONString());
                //report.setIndexUrl(indexUrl2);
                report.setIndexUrl(updateIndex);
                String articleUrl = baseUrl + doc.getString("url");
                report.setArticleUrl(articleUrl);
                String content = HttpUtils.doGetWithRetry(articleUrl, retryTimes);
                TagNode rootNodeContent = MyHtmlCleaner.clean(content);
                Object[] downloadResourceElements =  rootNodeContent.evaluateXPath("//a/i[@id='eloqua-final-submit-loading']/../@href");

                String downloadUrl = ((String)downloadResourceElements[0]).trim();
                if (!downloadUrl.endsWith("pdf")) {
                    //需要再访问一层
                    String tmpUrl = baseUrl + downloadUrl;
                    String tmpContent = HttpUtils.doGetWithRetry(tmpUrl, retryTimes);
                    TagNode rootNodeTmpContent = MyHtmlCleaner.clean(tmpContent);
                    Document docTmpContent = new DomSerializer(new CleanerProperties()).createDOM(rootNodeTmpContent);
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    downloadUrl = xpath.evaluate("//div[contains(@class, 'cmp-globalsite-button')]//a/@href", docTmpContent);
                }

                if (downloadUrl.equals("/en/trends/top-insights")) {
                    return;
                }

                //只需要取一个就行
                report.setUrl(downloadUrl);

                reportList.add(report);
            } catch (Exception ex) {
                log.error("dealing with article url encounts error ", ex);
            }
        }

        ReportDaoUtils.insertReports(reportList);
    }



}
