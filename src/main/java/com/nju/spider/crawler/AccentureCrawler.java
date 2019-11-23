package com.nju.spider.crawler;

public class AccentureCrawler extends BaseCrawler{
    private static final String orgName = "Accenture";
    private static final int retryTimes = 8;
    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

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
        
    }
}
