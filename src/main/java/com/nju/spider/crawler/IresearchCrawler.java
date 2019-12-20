package com.nju.spider.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName IresearchCrawler
 * @Description 艾瑞咨询爬虫
 * @Author UPC
 * @Date 2019/12/19 21:27
 * @Version 1.0
 */
@Slf4j
public class IresearchCrawler extends BaseCrawler{
    private static final String orgName = "IResearch";
    private static final long intervalTime = 6 * 3600 * 1000;  //6h间隔抓取时间

    //可以一次拿到所有的数据，后续爬更新就少爬点
    private static final String crawlHistoryUrl = "https://www.iresearch.com.cn/products/GetReportList?classId=&fee=0&date=&lastId=&pageSize=1500";

    private static final String crawlUpdateUrl = "https://www.iresearch.com.cn/products/GetReportList?classId=&fee=0&date=&lastId=&pageSize=9";

    private static final String articleUrlTemplate = "https://www.iresearch.com.cn/Detail/report?id=%s&isfree=0";

    private static final String pdfUrlTemplate = "http://www.iresearch.cn/include/ajax/user_ajax.ashx?work=idown&rid=%s";

    private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.US));

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
            String urlToCrawl = crawlUpdateUrl;
            String res = HttpUtils.doGetWithRetry(urlToCrawl);
            JSONObject jo = JSON.parseObject(res);
            JSONArray ja = jo.getJSONArray("List");
            List<Report> reportList = new ArrayList<>();
            for (int i = 0; i < ja.size(); i++) {
                try {
                    JSONObject article = ja.getJSONObject(i);
                    String title = article.getString("Title");
                    String publishDateStr = article.getString("Uptime");
                    Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, simpleDateFormatThreadLocal.get());
                    Integer id = article.getInteger("NewsId");
                    String industry = article.getString("industry");
                    String articleUrl = String.format(articleUrlTemplate, id);
                    String pdfUrl = String.format(pdfUrlTemplate, id);

                    Report report = new Report();
                    report.setOrgName(orgName);
                    report.setIndexUrl(urlToCrawl);
                    report.setArticleUrl(articleUrl);
                    report.setTitle(title);
                    report.setIndustryName(industry);
                    report.setPublishTime(publishDate);
                    report.setUrl(pdfUrl);
                    report.setExtra(article.toJSONString());
                    reportList.add(report);
                } catch (Exception ex) {
                    log.error("error crawling one article ", ex);
                }
            }
            ReportDaoUtils.insertReports(reportList);
        } catch (Exception ex) {
            log.error("error crawling url ", ex);
        }
    }

    public static void main(String [] args) {
        new IresearchCrawler().crawl();
    }
}
