package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

/**
 * @ClassName ChromeUtils
 * @Description TODO
 * @Author UPC
 * @Date 2019/12/5 21:51
 * @Version 1.0
 */
@Slf4j
public class ChromeUtils {

    public static String doChromeCrawlWithRetryTimesUsingProxy(String url, int retryTimes) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = doChromeCrawlUsingProxy(url);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
        }
        return null;
    }


    public static String doChromeCrawlUsingProxy(String url) {
        return doChromeCrawl(url, true);
    }

    public static String doChromeCrawlWithRetryTime(String url, int retryTimes) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = doChromeCrawl(url);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
        }
        return null;
    }


    public static String doChromeCrawl(String url) {
        return doChromeCrawl(url, false);
    }


    public static String doChromeCrawl(String url, boolean usingProxy) {
        WebDriver driver = null;
        try {
            ClassLoader classLoader = ChromeUtils.class.getClassLoader();
            File file = new File(classLoader.getResource("chromedriver.exe").getFile());
            System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
            ChromeOptions chromeOptions = new ChromeOptions();
            //设置为 headless 模式 （必须）
            if (usingProxy) {
                String proxy_str = HttpUtils.proxyHost + ":" + HttpUtils.proxyPort;
                Proxy proxy = new Proxy().setHttpProxy(proxy_str).setSslProxy(proxy_str);
                chromeOptions.setProxy(proxy);
            }
            chromeOptions.addArguments("--headless");

            driver = new ChromeDriver(chromeOptions);
            driver.get(url);
            String pageSource = driver.getPageSource();
            return pageSource;
        }  catch(Exception ex) {
            log.error("getting html using chrome encounts error", ex);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return null;
    }

    public static void main(String [] args) {
        String res = ChromeUtils.doChromeCrawl("https://www.google.com.hk", true);
        System.out.println(res);
    }
}
