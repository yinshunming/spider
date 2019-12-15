package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName TalkingDataCrawler
 * @Description TaklkingData网站爬虫
 * @Author UPC
 * @Date 2019/12/13 22:08
 * @Version 1.0
 */
@Slf4j
public class TalkingDataCrawler extends BaseCrawler{
    private static final String crawlUrlTemplate = "http://mi.talkingdata.com/reports.html?category=all&tag=all&page=%s";

    private static final String orgName = "TalkingData";

    private static final long intervalTime = 4 * 3600 * 1000;  //4h间隔抓取时间

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.US));


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
        //更新只要第一页
        for (int i = 1; i<= 1; i++) {
            String historyCrawlUrl = String.format(crawlUrlTemplate, i);
            log.info("start to crawl " + historyCrawlUrl);
            try {
                String res = HttpUtils.doGetWithRetry(historyCrawlUrl);
                List<TagNode> tagNodeList = XpathUtils.getTagNodeListFromXpath(res, "//div[@class='operate-book']");
                List<Report> reportList = new ArrayList<>();
                for (TagNode tagNode : tagNodeList) {
                    try {
                        String title = XpathUtils.getStringFromXpath(tagNode, "/a/@title");
                        String publishDateStr = XpathUtils.getStringFromXpath(tagNode, "/p/text()");
                        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                        String articleHref = XpathUtils.getStringFromXpath(tagNode, "/a/@href");
                        String res2 = HttpUtils.doGetWithRetry(articleHref);
                        String pdfUrl = XpathUtils.getStringFromXpath(res2, "//div[@class='operate-verify']//button/@data-url");
                        Report report = new Report();
                        report.setOrgName(orgName);
                        report.setIndexUrl(historyCrawlUrl);
                        report.setArticleUrl(articleHref);
                        report.setPublishTime(publishDate);
                        report.setTitle(title);
                        report.setUrl(pdfUrl);
                        reportList.add(report);
                    } catch (Exception ex) {
                        log.error("getting one article info encounts error ", ex);
                    }
                }

                ReportDaoUtils.insertReports(reportList);
            } catch (Exception ex) {
                log.error("crawling index page encounts error ", ex);
            }
        }
    }

    public static void main(String [] args) {
        new TalkingDataCrawler().crawl();
    }
}
