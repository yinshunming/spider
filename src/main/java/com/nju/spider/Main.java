package com.nju.spider;

import com.nju.spider.bean.Report;
import com.nju.spider.crawler.BaseCrawler;
import com.nju.spider.crawler.GartnerReportCrawler;
import com.nju.spider.db.JDBCUtils;
import com.nju.spider.download.DownloadStrategy;
import com.nju.spider.download.DownloadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private final static int crawlThreadsNum = 50;

    private final static int downloadThredsNum = 10; //初期不宜过大，防止被反爬

    private final static long downloadSleepInterval = 5 * 60 * 1000;

    public static void main(String [] args) {
        //更新任务一般量不大，单线程足矣，也可以防止爬的太厉害把网站弄down掉
        List<BaseCrawler> crawlerList = new ArrayList<>();
        crawlerList.add(new GartnerReportCrawler());

        ExecutorService es = Executors.newFixedThreadPool(crawlThreadsNum);
        for (BaseCrawler baseCrawler : crawlerList) {
            es.submit(baseCrawler::run);
        }


        //TODO 抽出来做一个类
        ExecutorService downloadEs = Executors.newFixedThreadPool(downloadThredsNum);
        while(true) {
            List<Report> reportToDowloadList = DownloadStrategy.getReportsToDownload();
            //乱序提交，防止饿死情况发生
            Collections.shuffle(reportToDowloadList);

            for (Report report : reportToDowloadList) {
                downloadEs.submit(new DownloadTask(report));
            }
            try {
                Thread.sleep(downloadSleepInterval);
            } catch (InterruptedException e) {
            }
        }

    }
}
