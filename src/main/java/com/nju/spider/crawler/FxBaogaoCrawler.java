package com.nju.spider.crawler;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName FxBaogaoCrawler
 * @Description 发现报告爬虫
 * @Author UPC
 * @Date 2019/12/21 11:21
 * @Version 1.0
 */
@Slf4j
public class FxBaogaoCrawler extends BaseCrawler{

    private static final String orgName = "FxBaogao";

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
