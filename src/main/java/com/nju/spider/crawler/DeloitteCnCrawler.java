package com.nju.spider.crawler;

import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 德勤中国网站爬虫
 * TODO 历史爬虫35页后部分格式不一致，因为pdf比较老，数量比较少，暂时不处理了
 */
@Slf4j
public class DeloitteCnCrawler extends BaseCrawler{
    private static final String orgName = "DeloitteChina";
    private static final int retryTimes = 5;

    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

    private static final String historyIndexUrl = "https://www2.deloitte.com/cn/zh/footerlinks/pressreleasespage.html?i=1;page=%s;" +
            "q=*;rank=rank-search;sp_q_18=%22%E6%96%B0%E9%97%BB%E7%A8%BF%22;sp_s=date-published%7Ctitle;sp_x_18=content-type;view=xml&pageNumber=2";

    private static final String baseUrl = "https://www2.deloitte.com";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd MMM yyyy", Locale.SIMPLIFIED_CHINESE));

    @Override
    public String getCrawlName() {
        return orgName;
    }

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }


    @Override
    public boolean needProxyToDownload() {
        return true;
    }

    @Override
    public void crawl() {

        for (int i = 1; i <= 41; i++) {

            String historyUrlToCrawl = String.format(historyIndexUrl, i);
            try {
                log.info("starting to crawl url: " + historyUrlToCrawl);
                String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl, retryTimes);
                TagNode rootNode = MyHtmlCleaner.clean(res);
//                Object [] articleUrls = rootNode.evaluateXPath("//ul//li[@class='press-release']//h2//a//@href");
                Object [] articleElements = rootNode.evaluateXPath("//div[@class='release-text-container']");
                for (Object articleEOb : articleElements) {
                    try {
                        TagNode articleTagNode = (TagNode) articleEOb;
                        Object[] articleUrlObs = articleTagNode.evaluateXPath("//h2//a//@href");
                        String articleUrl = baseUrl + articleUrlObs[0];
                        Object[] publishDateObs = articleTagNode.evaluateXPath("//p[@class='release-date']/text()");
                        Date publishDate = null;
                        if (publishDateObs != null && publishDateObs.length > 0) {
                            String publishDateStr = ((String) publishDateObs[0]).trim();
                            publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                        }

                        String res2 = HttpUtils.doGetWithRetryUsingProxy(articleUrl, retryTimes);
                        TagNode articleNode = MyHtmlCleaner.clean(res);
                        String downloadXpath = "//div[@class='downloadpromo section']//a/@href";
                        String title1 = "//div[@class='downloadpromo section']//button/text()";

                        String title2 = "//div[@class='contentpagecolctrl section']//a/text()"; //有可能误判
                    } catch (Exception ex) {
                        log.error("deaing article elements encounts errpr", ex);
                    }
                }
            } catch (Exception ex) {
                log.error("crawling " + historyUrlToCrawl + " encounts error", ex);
            }
        }
    }
}
