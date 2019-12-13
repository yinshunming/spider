package com.nju.spider.crawler;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import com.nju.spider.utils.HttpUtils;
import com.nju.spider.utils.MyHtmlCleaner;
import com.nju.spider.utils.XpathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName NielsenCrawler
 * @Description nielsen网站爬虫
 * @Author UPC
 * @Date 2019/12/10 21:45
 * @Version 1.0
 */
@Slf4j
public class NielsenCrawler extends BaseCrawler{

    private static final String xpath = "//iframe[@class='embed-iframe']/@src";

    private static final String xpath2 = "//div[@id='download_links']//a/@href";

    private static final String usaUrl = "https://www.nielsen.com/us/en/insights/report/page/%s/";

    private static final String usaUrl2 = "https://www.nielsen.com/us/en/insights/case-study/page/%s/";

    private static final String uasUrl3 = "https://www.nielsen.com/us/en/insights/resource/page/%s/";

    private static final String cnUrl = "https://www.nielsen.com/cn/zh/insights/report/page/%s/";


    private static final String orgName = "Nielsen";

    private static final long intervalTime = 8 * 3600 * 1000;  //8h间隔抓取时间


    private static ThreadLocal<SimpleDateFormat> publishDateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("MM-dd-yyyy", Locale.US));


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

        for (int i = 1; i <= 54; i++ ) {
            String historyUrlToCrawl = String.format(usaUrl, i);
            doCrawl(historyUrlToCrawl);
        }

        for (int i = 1; i <= 6; i++) {
            String historyUrlToCrawl = String.format(usaUrl2, i);
            doCrawl(historyUrlToCrawl);
        }

        for (int i = 1; i <= 5; i++) {
            String historyUrlToCrawl = String.format(uasUrl3, i);
            doCrawl(historyUrlToCrawl);
        }

        for (int i = 1; i <= 3; i++) {
            String historyUrlToCrawl = String.format(cnUrl, i);
            doCrawl(historyUrlToCrawl);
        }
    }

    private void doCrawl(String historyUrlToCrawl) {
        try {
            log.info("start to crawl " + historyUrlToCrawl);
            String res = HttpUtils.doGetWithRetryUsingProxy(historyUrlToCrawl);
            TagNode indexRootTagNode = MyHtmlCleaner.clean(res);
            Object[] articleIndexObs = indexRootTagNode.evaluateXPath("//article//div[@class='row']//div[@class='col']");
            List<Report> reportList = new ArrayList<>();
            for (Object articleIndexOb : articleIndexObs) {
                try {
                    TagNode articleIndexTagNode = (TagNode) articleIndexOb;
                    String industryName = XpathUtils.getStringFromXpath(articleIndexTagNode, "//header//span/a/text()");
                    String publishDateStr = XpathUtils.getStringFromXpath(articleIndexTagNode, "//header//time/text()");
                    Date publishDate = FormatUtils.parseDateByDateFormate(publishDateStr, publishDateFormatThreadLocal.get());
                    String title = XpathUtils.getStringFromXpath(articleIndexTagNode, "//h2//a/text()");
                    String articleUrl = XpathUtils.getStringFromXpath(articleIndexTagNode, "//h2//a/@href");
                    String res2 = HttpUtils.doGetWithRetryUsingProxy(articleUrl);
                    TagNode res2TagNode =  MyHtmlCleaner.clean(res2);
                    String pdfUrl = XpathUtils.getStringFromXpath(res2TagNode, "//div[@id='download_links']//a/@href");
                    //此处没找到pdf的链接，则找寻另一种格式
                    if (StringUtils.isBlank(pdfUrl)) {
                        String tmpHref = XpathUtils.getStringFromXpath(res2TagNode, "//iframe[@class='embed-iframe']/@src");
                        if (StringUtils.isNotBlank(tmpHref)) {
                            String res3 = HttpUtils.doPostWithRetryUsingProxy(tmpHref, getPostContent(), getHeaders());
                            pdfUrl = XpathUtils.getStringFromXpath(res3, "//form//span//a/@href");
                        }
                    }

                    if (StringUtils.isNotBlank(pdfUrl)) {
                        //TODO 填充report
                        Report report = new Report();
                        report.setIndustryName(industryName);
                        report.setTitle(title);
                        report.setPublishTime(publishDate);
                        report.setArticleUrl(articleUrl);
                        report.setUrl(pdfUrl);
                        report.setIndexUrl(historyUrlToCrawl);
                        report.setOrgName(orgName);
                        reportList.add(report);
                    }
                } catch (Exception ex) {
                    log.error("error getting article url ", ex);
                }
            }
            ReportDaoUtils.insertReports(reportList);
        } catch (Exception ex) {
            log.error("error getting article indexs of url " + historyUrlToCrawl, ex);
        }
    }

    private static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        //Cookie目前有效期比较长，暂时不用动态的获取
        headers.put("Cookie", "visitor_id31782=524498679; visitor_id31782-hash=6b0e8b761b73fab11c94d768c4c9329f5b9cd790662ac13eec534c23bce96f53a4bfc9977c8aba513a4312f16aef1435399fd0f8; pardot=t0lmaqm3b05p1od3ts2dkiefld");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    private static String getPostContent() {
        //目前也比较固定
        return "31782_219811pi_31782_219811=SDDF&31782_219813pi_31782_219813=dafafsd&31782_219815pi_31782_219815=12afd%40xds.com&31782_219817pi_31782_219817=dsfa&31782_219819pi_31782_219819=sadffdas&31782_219821pi_31782_219821=2278517&pi_extra_field=&_utf8=%E2%98%83&hiddenDependentFields=";
    }

    public static void main(String [] args) {
        new NielsenCrawler().crawl();
    }
}
