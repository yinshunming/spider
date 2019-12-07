package com.nju.spider.crawler;

/**
 * @ClassName KpmgCnCrawler
 * @Description 毕马威中国爬虫
 * @Author UPC
 * @Date 2019/12/7 17:56
 * @Version 1.0
 */
public class KpmgCnCrawler extends BaseCrawler{
    private static final String orgName = "MckinseyCn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 7 * 3600 * 1000;  //7h间隔抓取时间


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

    }
}
