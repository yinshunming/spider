package com.nju.spider;

import com.nju.spider.bean.Report;
import com.nju.spider.crawler.*;
import com.nju.spider.download.DownloadStrategy;
import com.nju.spider.download.DownloadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    private final static int crawlThreadsNum = 50;

    private final static int downloadThredsNum = 10; //初期不宜过大，防止被反爬

    private final static long downloadSleepInterval = 10 * 60 * 1000;

    private final static long initDelay = 5 * 1000;

    public static void main(String [] args) {
        //更新任务一般量不大，单线程足矣，也可以防止爬的太厉害把网站弄down掉
        List<BaseCrawler> crawlerList = new ArrayList<>();
        crawlerList.add(new GartnerReportCrawler());
        crawlerList.add(new AccentureCrawler());
        crawlerList.add(new DeloitteCnCrawler());
        crawlerList.add(new PwccnCrawler());

        ScheduledExecutorService es = Executors.newScheduledThreadPool(crawlThreadsNum);
        for (BaseCrawler baseCrawler : crawlerList) {
            es.scheduleWithFixedDelay(baseCrawler::run, initDelay, baseCrawler.getIntervalTime(), TimeUnit.MILLISECONDS);
        }

        //查看是否需要使用proxy去下载pdf
        List<String> needToUsingProxyDownloadOrgName = new ArrayList<>();
        for (BaseCrawler baseCrawler : crawlerList) {
            if (baseCrawler.needProxyToDownload()) {
                needToUsingProxyDownloadOrgName.add(baseCrawler.getCrawlName());
            }
        }

        //TODO 抽出来做一个类
        ExecutorService downloadEs = Executors.newFixedThreadPool(downloadThredsNum);
        while(true) {
            List<Report> reportToDowloadList = DownloadStrategy.getReportsToDownload();
            //乱序提交，防止饥饿情况(pdf下载时间很长)发生
            Collections.shuffle(reportToDowloadList);

            for (Report report : reportToDowloadList) {
                //这边可以做个策略
                if (needToUsingProxyDownloadOrgName.contains(report.getOrgName())) {
                    downloadEs.submit(new DownloadTask(report, true));
                } else {
                    downloadEs.submit(new DownloadTask(report, false));
                }
            }
            try {
                Thread.sleep(downloadSleepInterval);
            } catch (InterruptedException e) {
            }
        }

    }
}
