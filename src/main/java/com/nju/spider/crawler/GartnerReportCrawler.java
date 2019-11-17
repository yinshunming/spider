package com.nju.spider.crawler;

import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;


@Slf4j
public class GartnerReportCrawler extends BaseCrawler{
    private static final String indexUrl = "https://www.gartner.com/en/products/special-reports";
    private static final String baseUrl = "https;//www.gartner.com/";
    private static final int retryTimes = 5;
    private static final int intervalTime = 12 * 3600 * 1000;  //12h间隔抓取时间

    @Override
    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public void crawl() {
        String res = HttpUtils.doGetWithRetry(indexUrl, retryTimes);
        try {
            TagNode rootNode = MyHtmlCleaner.clean(res);
            TagNode[] articeUrlElements = (TagNode[]) rootNode.evaluateXPath("//a[@data-type='research']");
            for (TagNode articleUrlElement : articeUrlElements) {

                try {
                    String articleUrl = baseUrl + articleUrlElement.getAttributeByName("href");
                    System.out.println("articleUrl " + articleUrl);
                } catch (Exception ex) {
                    log.error("dealing with article url encounts error ", ex);
                }
            }
        } catch (XPatherException e) {
        }
    }



}
