package com.nju.spider.crawler.other;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.nju.spider.crawler.BaseCrawler;
import com.nju.spider.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PailixiangCrawler extends BaseCrawler {

    private static final String siteName = "Pailixiang";

    private static final long intervalTime = 1 * 3600 * 1000;  //1h间隔抓取时间

    private static final String albumId = "98da3ba4-5721-4e33-a47a-80b0917bedce";

    private static final String pailixiangUrl = "https://www.pailixiang.com/Portal/Services/AlbumDetail.ashx?t=2&rid=reqid2okck0cbkq";
    @Override
    public String getCrawlName() {
        return siteName;
    }

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public void crawl() {
        log.info("starting to crawl url: " + pailixiangUrl);
        Map<String, String> urlHeaders = new HashMap<String, String>();
        urlHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        String postBody = "albumId=" + albumId +"&groupId=&len=1000&from=&order=0&accessType=1&nw=";
        String res = HttpUtils.doPost(pailixiangUrl, postBody, urlHeaders);
        JSONObject jo = JSON.parseObject(res);
        JSONArray ja = jo.getJSONArray("Data");
        for (int i = 0; i < ja.size(); i++) {
            log.info("start to crawl: {}, total {}", i + 1, ja.size());
            JSONObject joImages = ja.getJSONObject(i);
            String dowloadImageUrl = joImages.getString("DownloadImageUrl");
            String fileName = (i + 1) + ".jpg";
            HttpUtils.doDownload(dowloadImageUrl, fileName, "D:\\youxi\\pailixiang");
        }
    }

    public static void main(String [] args) {
        PailixiangCrawler crawler = new PailixiangCrawler();
        crawler.crawl();
    }
}
