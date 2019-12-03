package com.nju.spider.crawler;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 普华永道中国爬虫
 */
@Slf4j
public class PwccnCrawler extends BaseCrawler{
    private static final String orgName = "pwccn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 6 * 3600 * 1000;  //6h间隔抓取时间

    //一级页，获取行业名称
    private static final String firstIndex = "https://www.pwccn.com/zh/industries.html";

    //研究与洞察页面的url
    private static final String historyIndexUrl2 = "https://www.pwccn.com/content/pwc/cn/zh/research-and-insights/jcr:content/" +
            "dynamic-content-par/collection_v2.rebrand-filter-dynamic.html?currentPagePath=/content/pwc/cn/zh/research-and-insights&" +
            "list=%7B%7D&searchText=&defaultImagePath=/content/dam/pwc/network/collection-fallback-images&page=";

    private static final String baseUrl = "https://www.pwccn.com";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("mm月 yyyy", Locale.SIMPLIFIED_CHINESE));


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

        try {
            String resIndex = HttpUtils.doGetWithRetryUsingProxy(firstIndex, retryTimes);
            TagNode indexNode = MyHtmlCleaner.clean(resIndex);
            Object [] industryEls = indexNode.evaluateXPath("//div[@class='link-index__content']//a");

            for (Object industryEl : industryEls) {
                try {
                    TagNode industryTagNode = (TagNode) industryEl;
                    String href = baseUrl + industryTagNode.getAttributeByName("href");
                    String industryName = industryTagNode.getText().toString().trim();
                    System.out.println(href + " " + industryName);
                } catch (Exception ex) {
                }

            }
        } catch (XPatherException ex) {
            log.error("geting article url from index encounts error ", ex);
        }


        //从研究与洞察下载的pdf，有部分和行业中重合，先下行业，再用这边补充
        for (int i = 0; i <= 40; i+=8) {
            String historyUrlToCrawl = historyIndexUrl2 + i;
            try {
                String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl, retryTimes);
                JSONObject jo = JSONObject.parseObject(res);
                JSONArray elements = jo.getJSONArray("elements");
                for (int j = 0; j < elements.size(); j++) {
                    try {
                        JSONObject elJO = elements.getJSONObject(j);
                        String articleUrl = elJO.getString("href");
                        String publishDateStr = elJO.getString("publishDate");
                        Date date = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                        String title = elJO.getString("title");

                    } catch (Exception ex) {
                        log.error("dealing with article encounts error ", ex);
                    }
                }
            } catch (Exception ex) {
                log.error("dealing to find index encounts error ", ex);
            }
        }

    }


}
