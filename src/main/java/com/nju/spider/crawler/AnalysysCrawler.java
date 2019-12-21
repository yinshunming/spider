package com.nju.spider.crawler;

import com.alibaba.fastjson.JSON;
import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName AnalysysCrawler
 * @Description 易观网站爬虫
 * @Author UPC
 * @Date 2019/12/15 21:43
 * @Version 1.0
 */
@Slf4j
public class AnalysysCrawler extends BaseCrawler {
    private static final String orgName = "Analysys";

    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

    private static final String crawlUrlTemplate = "https://www.analysys.cn/article/analysis/%s";

    private static final String downloadBaseUrl = "https://www.analysys.cn/article/download/%s";

    private static final String baseUrl = "https://www.analysys.cn";

    //经测试，无需登录就可以下载
//    private static final String userName = "";
//    private static final String password = "";

    private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.US));


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
        for (int i = 1; i <= 1; i++) {
            String historyUrlToCrawl = String.format(crawlUrlTemplate, i);
            try {
                log.info("starting to crawl url: " + historyUrlToCrawl);
                String res = HttpUtils.doGetWithRetry(historyUrlToCrawl);
                List<Report> reportList = new ArrayList<>();
                List<TagNode> tagNodeList = XpathUtils.getTagNodeListFromXpath(res, "//ul[@class='news_left']//li[@class='clearfix']");
                for (TagNode tagNode : tagNodeList) {
                    try {
                        String articleHref = XpathUtils.getStringFromXpath(tagNode, "//div/a/@href");
                        String summary = XpathUtils.getStringFromXpath(tagNode, "//div/p/@title");
                        String title = XpathUtils.getStringFromXpath(tagNode, "//div/a/h1/text()");
                        String industry1 = XpathUtils.getStringFromXpath(tagNode, "//div//span[@class='ehn'][1]/text()");
                        String industry2 = XpathUtils.getStringFromXpath(tagNode, "//div//span[@class='ehn'][3]/text()");
                        String publishDateStr = XpathUtils.getStringFromXpath(tagNode, "//div//span[@class='ehn'][2]/text()");
                        String industry = StringUtils.isNoneBlank(industry1) ? (StringUtils.isNotBlank(industry2) ? industry1 + "-" + industry2 : industry1 ): industry2;
                        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr.replace("\"", ""), simpleDateFormatThreadLocal.get());
                        String articleUrl = baseUrl + articleHref;

                        String res2 = HttpUtils.doGetWithRetry(articleUrl);
                        String result = XpathUtils.getStringFromXpath(res2, "//div[@class='title_btn']//div[@data-login]/text()");
                        if (StringUtils.isNotBlank(result)) {
                            String[] hrefArrays = articleHref.split("/");
                            String articleId = hrefArrays[hrefArrays.length - 1];
                            if (StringUtils.isNumeric(articleId)) {
                                String pdfUrl = String.format(downloadBaseUrl, articleId);
                                Report report = new Report();
                                report.setUrl(pdfUrl);
                                report.setPublishTime(publishDate);
                                report.setIndustryName(industry);
                                report.setTitle(title);
                                Map<String, String> summaryMap = new HashMap<>();
                                summaryMap.put("short_desc", summary);
                                String summaryJSON = JSON.toJSONString(summaryMap);
                                report.setExtra(summaryJSON);
                                report.setIndexUrl(historyUrlToCrawl);
                                report.setArticleUrl(articleUrl);
                                report.setOrgName(orgName);
                                reportList.add(report);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("error crawling article indexs", ex);
                    }
                }

                ReportDaoUtils.insertReports(reportList);
            } catch (Exception ex) {
                log.error("error doing crawl ", ex);
            }
        }
    }

    public static void main(String [] args) {
        new AnalysysCrawler().crawl();
    }
}
