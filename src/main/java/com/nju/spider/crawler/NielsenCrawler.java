package com.nju.spider.crawler;

/**
 * @ClassName NielsenCrawler
 * @Description nielsen网站爬虫
 * @Author UPC
 * @Date 2019/12/10 21:45
 * @Version 1.0
 */
public class NielsenCrawler extends BaseCrawler{

    private static final String xpath = "//iframe[@class='embed-iframe']";

    private static final String xpath2 = "//div[@id='download_links']//a/@href";

    private static final String usaUrl = "https://www.nielsen.com/us/en/insights/report/";

    private static final String cnUrl = "https://www.nielsen.com/cn/zh/insights/report/";


    @Override
    public String getCrawlName() {
        return null;
    }

    @Override
    public long getIntervalTime() {
        return 0;
    }

    @Override
    public void crawl() {

    }
}
