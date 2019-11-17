package com.nju.spider.crawler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseCrawler {
    public abstract void crawl();

    public abstract long getIntervalTime();

    public void run() {
        while(true) {
            try {
                crawl();
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
