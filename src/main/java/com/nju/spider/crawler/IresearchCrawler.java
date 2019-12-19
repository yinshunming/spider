package com.nju.spider.crawler;

import lombok.extern.slf4j.Slf4j;

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
