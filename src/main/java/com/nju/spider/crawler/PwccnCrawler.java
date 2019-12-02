package com.nju.spider.crawler;


/**
 * 普华永道中国爬虫
 */
public class PwccnCrawler extends BaseCrawler{
    private static final String orgName = "pwccn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 6 * 3600 * 1000;  //6h间隔抓取时间

    //一级页，获取行业名称
    private static final String firstIndex = "https://www.pwccn.com/zh/industries.html";

    @Override
    public String getCrawlName() {
        return orgName;
    }

    @Override
    public void crawl() {

    }

    @Override
    public long getIntervalTime() {
        return 0;
    }
}
