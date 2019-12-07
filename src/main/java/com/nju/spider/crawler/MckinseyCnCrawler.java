package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName MckinseyCrawler
 * @Description 麦肯锡中国爬虫
 * @Author UPC
 * @Date 2019/12/7 17:50
 * @Version 1.0
 */
@Slf4j
public class MckinseyCnCrawler extends BaseCrawler{
    private static final String orgName = "MckinseyCn";
    private static final int retryTimes = 5;
    private static final long intervalTime = 5 * 3600 * 1000;  //5h间隔抓取时间

    private static final String firstIndexUrl = "https://www.mckinsey.com.cn/insights/";

    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("MM月 dd, yyyy", Locale.SIMPLIFIED_CHINESE));


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
        try {
            String indexRes = HttpUtils.doGetWithRetry(firstIndexUrl, retryTimes);
            TagNode indexRootTagNode = MyHtmlCleaner.clean(indexRes);
            Object [] indexObs = indexRootTagNode.evaluateXPath("//ul[@id='menu-menu-left']//li");
            for (Object indexOb : indexObs) {
                try {
                    TagNode indexTagNode = (TagNode) indexOb;
                    String industryHref = XpathUtils.getStringFromXpath(indexTagNode, "/a/@href");
                    String industryName = XpathUtils.getStringFromXpath(indexTagNode, "/a/text()");
                    String industryRes = HttpUtils.doGetWithRetry(industryHref, retryTimes);
                    TagNode industryTagNode = MyHtmlCleaner.clean(industryRes);
                    Object [] articleIndexObs = industryTagNode.evaluateXPath("//div[@class='recent-posts-content']");
                    for (Object articleIndexOb : articleIndexObs) {
                        try {
                            TagNode articleIndexNode = (TagNode) articleIndexOb;
                            String title = XpathUtils.getStringFromXpath(articleIndexNode, "//h4/a/text()");
                            String articleUrl = XpathUtils.getStringFromXpath(articleIndexNode, "//h4/a/@href");
                            String publishDateStr = XpathUtils.getStringFromXpath(articleIndexNode, "//div[@class='recent-posts-content']//span[@class='date']/text()");
                            Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());

                            String articleRes = HttpUtils.doGetWithRetryUsingProxy(articleUrl, retryTimes);
                            TagNode articleTagNode = MyHtmlCleaner.clean(articleRes);

                            String authors = XpathUtils.getStringFromXpath(articleTagNode, "//article//p[@style='text-align: right;']/text()").trim().replace("作者：", "");
                            //太长则表示取错了,置空
                            if (authors.length() >= 50) {
                                authors = null;
                            }

                            List<String> hrefList = XpathUtils.getStringListFromXpath(articleTagNode, "//a/@href");
                            for (String href: hrefList) {
                                if (href.endsWith(".pdf")) {
                                    Report report = new Report();
                                    //TODO 待填充数据
                                }
                            }
                        } catch (Exception ex) {
                            log.error("getting article href encounts error ", ex);
                        }
                    }
                } catch (Exception e) {
                    log.error("getting industry urls encounts error");
                }
            }
        } catch (Exception e) {
        }
    }
}
