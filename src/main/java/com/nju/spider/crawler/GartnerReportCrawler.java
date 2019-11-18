package com.nju.spider.crawler;

import com.nju.spider.db.JDBCUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@Slf4j
public class GartnerReportCrawler extends BaseCrawler{
    private static final String indexUrl = "https://www.gartner.com/en/products/special-reports";
    private static final String baseUrl = "https://www.gartner.com";
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
                    for (Object pdfUrl : downloadResourceElements) {
                        Connection conn = JDBCUtils.getConn();
                        PreparedStatement pstmt = conn.prepareStatement("select * from test");
                        ResultSet rs =   pstmt.executeQuery();
                        System.out.println(rs);
                    }
                } catch (Exception ex) {
                    log.error("dealing with article url encounts error ", ex);
                }
            }
        } catch (XPatherException e) {
        }
    }



}
