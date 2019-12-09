package com.nju.spider.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nju.spider.bean.Report;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName KpmgCnCrawler
 * @Description 毕马威中国爬虫
 * @Author UPC
 * @Date 2019/12/7 17:56
 * @Version 1.0
 */
@Slf4j
public class KpmgCnCrawler extends BaseCrawler{
    private static final String orgName = "MckinseyCn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 7 * 3600 * 1000;  //7h间隔抓取时间

    private static final String indexUrl = "https://home.kpmg/search/?all_sites=false&facets=false&i=1&language=zh" +
            "&q=&q1=Insights&q10=All&q11=All&q2=All&q3=All&q4=All&q5=All&q6=All&q7=All&q8=All&q9=All&rank=KPMG_Last_Mod_Long&site=cn_zh" +
            "&sort=KPMG_Filter_Date&sp_dedupe_field=KPMG_Document_ID&sp_k=cn_zh&x1=KPMG_Tab_Type&x10=KPMG_Ser_Path_Loc_pa&x11=KPMG_Topic_Path_pa" +
            "&x2=KPMG_Cont_Mem_Firm&x3=KPMG_Geo_Rel_Path&x4=KPMG_Ind_Path_Loc_pa&x5=KPMG_Topic_Path&x6=KPMG_Insights_Path&x7=KPMG_Cont_Type_Path" +
            "&x8=KPMG_Filter_Year&x9=KPMG_Market_Path&page=";

    private static final String baseUrl = "https://assets.kpmg";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MMM-yyyy", Locale.US));


    @Override
    public String getCrawlName() {
        return null;
    }

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public void crawl() {
        //一共96页
        for (int i = 1; i <= 96; i++) {
            try {
                String crawlUrl = indexUrl + i;
                log.info("start to crawl " + crawlUrl);
                String res = HttpUtils.doGetWithRetry(crawlUrl, retryTimes);
                JSONArray ja = JSON.parseObject(res).getJSONObject("customer-results").getJSONObject("resultset").getJSONObject("results").getJSONArray("result");
                List<Report> reportList = new ArrayList<>();
                for (int j = 0; j < ja.size(); j++) {
                    try {
                        JSONObject jo = ja.getJSONObject(j);
                        String title = jo.getString("KPMG_Title");
                        String articleUrl = jo.getString("url");
                        String pdfUrl = baseUrl + jo.getString("KPMG_PDF");
                        String publishDateStr = jo.getString("KPMG_Article_Date");
                        Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());

                        Report report = new Report();
                        report.setOrgName(orgName);
                        report.setUrl(pdfUrl);
                        report.setIndexUrl(crawlUrl);
                        report.setArticleUrl(articleUrl);
                        report.setPublishTime(publishDate);
                        report.setTitle(title);
                        report.setExtra(jo.toJSONString());
                        reportList.add(report);
                    } catch (Exception ex) {
                        log.error("getting pdf encounts error ", ex);
                    }
                }
                System.out.println(reportList);
            } catch (Exception ex) {
                log.error("getting index url encounts error ", ex);
            }


        }
    }

    public static void main(String [] args) {
        KpmgCnCrawler kpmgCnCrawler = new KpmgCnCrawler();
        kpmgCnCrawler.crawl();
    }
}
