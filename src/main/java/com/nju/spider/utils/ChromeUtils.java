package com.nju.spider.utils;

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
public class ChromeUtils {
    public void openChromeTest() {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("chromedriver.exe").getFile());
        System.out.println(file.getAbsolutePath());

        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        ChromeOptions chromeOptions = new ChromeOptions();
//        设置为 headless 模式 （必须）
        String proxy_str = "localhost:8589";
        Proxy proxy = new Proxy().setHttpProxy(proxy_str).setSslProxy(proxy_str);
        chromeOptions.setProxy(proxy);
        chromeOptions.addArguments("--headless");

////        设置浏览器窗口打开大小  （非必须）
//        chromeOptions.addArguments("--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get("https://www.pwccn.com/zh/industries/aircraft-leasing.html");
        //driver.get("https://www.google.com.hk");
        String title = driver.getPageSource();
        System.out.println(title);
        driver.quit();
    }

    public static void main(String [] args) {
        ChromeUtils chromeUtils = new ChromeUtils();
        chromeUtils.openChromeTest();
    }
}
