package com.nju.spider.crawler;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 普华永道中国爬虫
 */
@Slf4j
public class PwccnCrawler extends BaseCrawler{
    private static final String orgName = "pwccn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 6 * 3600 * 1000;  //6h间隔抓取时间

    //一级页，获取行业名称
    private static final String firstIndex = "https://www.pwccn.com/zh/industries.html";

    //研究与洞察页面的url
    private static final String historyIndexUrl2 = "https://www.pwccn.com/content/pwc/cn/zh/research-and-insights/jcr:content/" +
            "dynamic-content-par/collection_v2.rebrand-filter-dynamic.html?currentPagePath=/content/pwc/cn/zh/research-and-insights&" +
            "list=%7B%7D&searchText=&defaultImagePath=/content/dam/pwc/network/collection-fallback-images&page=";

    private static final String baseUrl = "https://www.pwccn.com";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("mm月 yyyy", Locale.SIMPLIFIED_CHINESE));


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
        try {
            String resIndex = HttpUtils.doGetWithRetryUsingProxy(firstIndex, retryTimes);
            TagNode indexNode = MyHtmlCleaner.clean(resIndex);
            Object [] industryEls = indexNode.evaluateXPath("//div[@class='link-index__content']//a");

            for (Object industryEl : industryEls) {
                List<Report> reportList = new ArrayList<>();
                try {
                    TagNode industryTagNode = (TagNode) industryEl;
                    String industryHref = baseUrl + industryTagNode.getAttributeByName("href");
                    String industryName = industryTagNode.getText().toString().trim();

                    //此处是js生成的html，用模拟浏览器才可以正确的找到dom tree
                    String resIndustry = ChromeUtils.doChromeCrawlWithRetryTimesUsingProxy(industryHref, retryTimes);
                    //String resIndustry = HttpUtils.doGetWithRetryUsingProxy(industryHref, retryTimes);
                    TagNode industryNode = MyHtmlCleaner.clean(resIndustry);
                    Object [] viewMoreButtonObs = industryNode.evaluateXPath("//a[@ng-disabled='contentList.showLoading']/@href");
                    if (viewMoreButtonObs.length > 0) {
                        //此页pdf比较多，用查看全部按钮的链接找到文章
                        String articlePubUrl = (String) viewMoreButtonObs[0];
                        String articlePubRes = ChromeUtils.doChromeCrawlWithRetryTimesUsingProxy(articlePubUrl, retryTimes);
                        TagNode articlePubTagNode = MyHtmlCleaner.clean(articlePubRes);
                        Object [] articleTagNodes = articlePubTagNode.evaluateXPath("//div[@class='row collectionv2__content']//article");
                        for (Object articleTagNodeOb : articleTagNodes) {
                            try {
                                TagNode articleTagNode = (TagNode) articleTagNodeOb;
                                Report report = getReportInfoFromIndexPage(articleTagNode);
                                report.setIndustryName(industryName);
                                report.setIndexUrl(articlePubUrl);
                                reportList.add(report);
                            } catch (Exception ex) {
                                log.error("getting from pubs page encounts error ", ex);
                            }
                        }
                    } else {
                        //此页pdf比较少没有查看全部按钮
                        Object [] articleObs = industryNode.evaluateXPath("//article");
                        for (Object articleOb : articleObs) {
                            try {
                                TagNode articleTagNode = (TagNode) articleOb;
                                Report report = getReportInfoFromIndexPage(articleTagNode);
                                report.setIndustryName(industryName);
                                report.setIndexUrl(industryHref);
                                reportList.add(report);
                            } catch (Exception ex) {
                                log.error("getting from index page encounts error ", ex);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error("getting from industry page encounts error ", ex);
                }

                ReportDaoUtils.insertReports(reportList);
            }
        } catch (XPatherException ex) {
            log.error("geting article url from index encounts error ", ex);
        }


        //从研究与洞察下载的pdf，有部分和行业中重合，先下行业，再用这边补充
        for (int i = 0; i <= 40; i+=8) {
            String historyUrlToCrawl = historyIndexUrl2 + i;
            try {
                String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl, retryTimes);
                JSONObject jo = JSONObject.parseObject(res);
                JSONArray elements = jo.getJSONArray("elements");

                List<Report> reportList = new ArrayList<>();
                for (int j = 0; j < elements.size(); j++) {
                    try {
                        JSONObject elJO = elements.getJSONObject(j);
                        String articleUrl = elJO.getString("href");
                        String publishDateStr = elJO.getString("publishDate");
                        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                        String title = elJO.getString("title");
                        String pdfUrl = baseUrl + getPdfUrlFromArticlePage(articleUrl);

                        Report report = new Report();
                        report.setTitle(title);
                        report.setUrl(pdfUrl);
                        report.setPublishTime(publishDate);
                        report.setArticleUrl(articleUrl);
                        report.setIndexUrl(historyUrlToCrawl);
                        report.setOrgName(orgName);
                        reportList.add(report);
                    } catch (Exception ex) {
                        log.error("dealing with article encounts error ", ex);
                    }
                }

                ReportDaoUtils.insertReports(reportList);
            } catch (Exception ex) {
                log.error("dealing to find index encounts error ", ex);
            }
        }
    }

    private Report getReportInfoFromIndexPage(TagNode tagNode) {
        String articleHref = XpathUtils.getStringFromXpath(tagNode, "//a/@href");
        String publishDateStr = XpathUtils.getStringFromXpath(tagNode, "//a//p//time/text()");
        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
        String title = XpathUtils.getStringFromXpath(tagNode, "//a//h4/span/text()");
        String pdfUrl = baseUrl + getPdfUrlFromArticlePage(articleHref);
        Report report = new Report();
        report.setPublishTime(publishDate);
        report.setUrl(pdfUrl);
        report.setTitle(title);
        report.setArticleUrl(articleHref);
        report.setOrgName(orgName);
        return report;
    }

    private String getPdfUrlFromArticlePage(String articleUrl) {
        String articleRes = HttpUtils.doGetWithRetryUsingProxy(articleUrl, retryTimes);
        String pdfUrl = XpathUtils.getStringFromXpath(articleRes, "//a[@class='cta-download__link']/@href");
        return pdfUrl;
    }


}
