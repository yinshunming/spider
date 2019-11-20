package com.nju.spider;

import com.nju.spider.crawler.BaseCrawler;
import com.nju.spider.crawler.GartnerReportCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private final static int downloadThredsNum = 10; //初期不宜过大，防止被反爬

    public static void main(String [] args) {
        List<BaseCrawler> crawlerList = new ArrayList<>();
        crawlerList.add(new GartnerReportCrawler());
        ExecutorService es = Executors.newFixedThreadPool(crawlerList.size());
        for (BaseCrawler baseCrawler : crawlerList) {
            es.submit(baseCrawler::run);
        }

        ExecutorService downloadES = Executors.newFixedThreadPool(downloadThredsNum);


    }
}
