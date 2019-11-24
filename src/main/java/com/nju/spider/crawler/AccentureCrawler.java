package com.nju.spider.crawler;

import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Slf4j
public class AccentureCrawler extends BaseCrawler{
    private static final String orgName = "Accenture";
    private static final int retryTimes = 8;
    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间

    private static final String historyIndexUrl = "https://newsroom.accenture.com/?page=%s";
    private static final String updateIndexUrl = "https://newsroom.accenture.com/";
    private static final String baseUrl = "https://newsroom.accenture.com";

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
        //先爬历史
        for (int i = 1; i <= 10; i++) {
            String historyUrlToCrawl = String.format(historyIndexUrl, i);
            try {
                log.info("starting to crawl url: " + historyUrlToCrawl);
                String res = HttpUtils.doGetWithRetry(historyUrlToCrawl, retryTimes);
                TagNode rootNode = MyHtmlCleaner.clean(res);
                //获得文章页地址
                Object [] articleUrlObs = rootNode.evaluateXPath("//div[@id='tek-wrap-centerwell']//h4/a[@data-analytics-content-type='engagement']/@href");
//                //publishDate还是从文章里取吧
//                Document doc = new DomSerializer(new CleanerProperties()).createDOM(rootNode);
//                XPath xpath = XPathFactory.newInstance().newXPath();
//                NodeList nodeList = (NodeList) xpath.evaluate("//div[@id='tek-wrap-centerwell']//div[contains(@id, 'wrap-article-listing')]", doc, XPathConstants.NODESET);
                for (Object articleUrlOb: articleUrlObs) {
                    try {
                        String articleUrl = baseUrl + (String) articleUrlOb;
                        String articleContent = HttpUtils.doGetWithRetry(articleUrl, retryTimes);
                        TagNode articleRootNode = MyHtmlCleaner.clean(articleContent);
                        Document articleDoc = new DomSerializer(new CleanerProperties()).createDOM(articleRootNode);
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        String publishDate = xpath.evaluate("//div[contains(@class, ' rel-date')]", articleDoc);
                        Object[] hrefs = articleRootNode.evaluateXPath("//article//a/@href");
                        for (Object href : hrefs) {
                            try {
                                //对链接进行分析
                            } catch (Exception ex) {
                                log.error("dealing with href " + href + " encounts error", ex);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("dealing with articleUrl " + articleUrlOb + " encounts error", ex);
                    }
                }
            } catch (Exception e) {
                log.error("dealing history url encounts error " + historyUrlToCrawl, e);
            }
        }

    }
}
