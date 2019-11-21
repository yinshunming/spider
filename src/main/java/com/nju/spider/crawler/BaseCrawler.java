package com.nju.spider.crawler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseCrawler {
    public abstract String getCrawlName();

    public abstract void crawl();

    public abstract long getIntervalTime();

    public void run() {

        while(true) {
            try {
                long startTime = System.currentTimeMillis();
                log.info("start " + getCrawlName() + " crawl.");
                crawl();
                log.info("end " + getCrawlName() + " crawl.");
                long costTime = System.currentTimeMillis() - startTime;
                log.info("cost time " + costTime + "ms.");
            } catch (Exception ex) {
                log.error("running crawl tasks encounts exception ", ex);
            }
            try {
                Thread.sleep(getIntervalTime());
            } catch (InterruptedException e) {
            }
        }
    }
}
