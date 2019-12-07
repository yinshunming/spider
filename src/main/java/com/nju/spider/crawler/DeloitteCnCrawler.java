package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 德勤中国网站爬虫
 * 主要pdf分布在两处，一处是 新闻稿， 一处是 文章 (较少)
 */
@Slf4j
public class DeloitteCnCrawler extends BaseCrawler{
    private static final String orgName = "DeloitteChina";
    private static final int retryTimes = 15;

    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

    private static final String historyIndexUrl = "https://www2.deloitte.com/cn/zh/footerlinks/pressreleasespage.html?i=1;" +
            "q=*;rank=rank-search;sp_q_18=%22%E6%96%B0%E9%97%BB%E7%A8%BF%22;sp_s=date-published%7Ctitle;sp_x_18=content-type;view=xml&pageNumber=2;page=";

    private static final String history2IndexUrl = "https://www2.deloitte.com/cn/zh/footerlinks/pressreleasespage.html?i=1;" +
            "pageNumber=2;q=*;rank=rank-search;sp_q_18=%22%E6%96%87%E7%AB%A0%22;sp_s=date-published%7Ctitle;sp_x_18=content-type;view=xml&pageNumber=3;page=";

    private static final String baseUrl = "https://www2.deloitte.com";

    private static final Pattern titlePattern = Pattern.compile("《(.*)》");

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd MMM yyyy", Locale.SIMPLIFIED_CHINESE));

    @Override
    public String getCrawlName() {
        return orgName;
    }

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }


    @Override
    public boolean needProxyToDownload() {
        return true;
    }

    @Override
    public void crawl() {
        //爬新闻稿
        for (int i = 1; i <= 1; i++) {
            String historyUrlToCrawl = historyIndexUrl + i;
            doCrawl(historyUrlToCrawl);
        }

        //爬文章
        for (int i = 1; i <= 1; i++) {
            String historyUrlToCrawl = history2IndexUrl + i;
            doCrawl(historyUrlToCrawl);
        }
    }

    private void doCrawl(String historyUrlToCrawl) {
        try {
            log.info("starting to crawl url: " + historyUrlToCrawl);
            String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl, retryTimes);
            TagNode rootNode = MyHtmlCleaner.clean(res);
//                Object [] articleUrls = rootNode.evaluateXPath("//ul//li[@class='press-release']//h2//a//@href");
            Object [] articleElements = rootNode.evaluateXPath("//div[@class='release-text-container']");
            List<Report> reportList = new ArrayList<>();
            for (Object articleEOb : articleElements) {
                try {
                    TagNode articleTagNode = (TagNode) articleEOb;
                    Object[] articleUrlObs = articleTagNode.evaluateXPath("//h2//a/@href");
                    String articleUrl = baseUrl + articleUrlObs[0];
                    Object[] publishDateObs = articleTagNode.evaluateXPath("//p[@class='release-date']/text()");
                    Date publishDate = null;
                    if (publishDateObs != null && publishDateObs.length > 0) {
                        String publishDateStr = publishDateObs[0].toString().trim();
                        publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                    }

                    String res2 = HttpUtils.doGetWithRetryUsingProxy(articleUrl, retryTimes);
                    TagNode articleNode = MyHtmlCleaner.clean(res2);
                    Object [] downloadHrefs = articleNode.evaluateXPath("//div[@class='downloadpromo section']//a/@href");

                    if (downloadHrefs.length > 0) {
                        String url = baseUrl + downloadHrefs[0];

                        String title = null;
                        Object[] title1Objs = articleNode.evaluateXPath("//div[@class='downloadpromo section']//button/text()");  //结合正则表达式

                        if (title1Objs.length > 0) {
                            String title1All = title1Objs[0].toString();
                            Matcher matcher1 = titlePattern.matcher(title1All);
                            if (matcher1.find()) {
                                title = matcher1.group(1);
                            }
                        }

                        //第二种方式，直接在正文中找到第一个链接的文字，作为title，可能误判
                        if (title == null) {
                            String title2 = XpathUtils.getStringFromXpath(articleNode, "//div[@class='contentpagecolctrl section']//a/text()");
                            if (StringUtils.isNotBlank(title2)) {
                                title = title2.replaceAll("[《》<<>>]", "");
                            }
                        }

                        Report report = new Report();
                        report.setPublishTime(publishDate);
                        report.setUrl(url);
                        report.setArticleUrl(articleUrl);
                        report.setIndexUrl(historyUrlToCrawl);
                        report.setOrgName(orgName);
                        report.setTitle(title);
                        reportList.add(report);
                    }
                } catch (Exception ex) {
                    log.error("dealing article elements encounts errpr", ex);
                }
            }

            ReportDaoUtils.insertReports(reportList);
        } catch (Exception ex) {
            log.error("crawling " + historyUrlToCrawl + " encounts error", ex);
        }
    }
}
