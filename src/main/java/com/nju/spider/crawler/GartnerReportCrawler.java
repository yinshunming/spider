package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.JDBCUtils;
import com.nju.spider.db.ReportDao;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class GartnerReportCrawler extends BaseCrawler{
    private static final String indexUrl = "https://www.gartner.com/en/products/special-reports";
    private static final String baseUrl = "https://www.gartner.com";
    private static final String indexUrl2 = "https://www.gartner.com/ngw/syspath-bin/gartner/dynamiccontent?" +
            "requestType=select-by-tags&designType=tiles&nPage=1&pageSize=78&languageCode=en&showLocalizedContent=false&" +
            "filterCodes=&randomSeed=&currentPagePath=/en/products/special-reports&tags=emt%3Apage%2Ftype%2Fspecial-reports%2Cemt%3Apage%2Fcontent-type%2Fspecial-reports";
    private static final String industryName = "Gartner";
    private static final int retryTimes = 5;
    private static final int intervalTime = 12 * 3600 * 1000;  //12h间隔抓取时间

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public void crawl() {
        String res = HttpUtils.doGetWithRetry(indexUrl, retryTimes);
        try {
            TagNode rootNode = MyHtmlCleaner.clean(res);
            Object[] articeUrlElementHrefs =  rootNode.evaluateXPath("//a[@data-type='research']/@href");
            for (Object articleUrlElementHref : articeUrlElementHrefs) {
                try {
                    String articleUrl = baseUrl +  articleUrlElementHref;
                    String content = HttpUtils.doGetWithRetry(articleUrl, retryTimes);
                    TagNode rootNodeContent = MyHtmlCleaner.clean(content);
                    Object[] downloadResourceElements =  rootNodeContent.evaluateXPath("//a/i[@id='eloqua-final-submit-loading']/../@href");
                    List<Report> reportList = new ArrayList<>();
                    for (Object pdfUrl : downloadResourceElements) {
                        Report report = new Report();
                        report.setUrl((String) pdfUrl);
                        report.setIndustryName(industryName);
                        //TODO 还需要补充其他的信息
                        reportList.add(report);
                    }

                    ReportDao.insertReports(reportList);
                } catch (Exception ex) {
                    log.error("dealing with article url encounts error ", ex);
                }
            }
        } catch (XPatherException e) {
        }
    }



}
