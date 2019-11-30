package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Slf4j
public class FormatUtils {

    private static ThreadLocal<SimpleDateFormat> publishDateSdfThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public static String publishDateToString(Date publishDate) {
        return publishDateSdfThreadLocal.get().format(publishDate);
    }

    public static Date parseDateByDateFormate(String dateStr, SimpleDateFormat dateFormat) {
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        }  catch (Exception ex) {
            log.error("parsing date encounts error ", ex);
        }

        return date;
    }


    public static Date parseDateByMutilDateFormate(String dateStr, SimpleDateFormat ... dateFormats) {
        Date date = null;
        for (SimpleDateFormat smdf : dateFormats) {
            try {
                date = smdf.parse(dateStr);
            } catch (Exception ex) {
                log.error("parsing date encounts error ", ex);
            }

            if (date != null) {
                return date;
            }
        }
        return date;
    }

    public  static void main(String [] args) {
//        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
//        SimpleDateFormat dateformat2 = new SimpleDateFormat("");
//        String dateStr =
//                "            June 08, 2010   ";
//        String dateStr1 = "2017-06-15T04:00:00Z";
//        Date date = FormatUtils.parseDateByMutilDateFormate(dateStr, dateformat);
//        System.out.println(date);
        String articleUrl = "https://newsroom.accenture.com/news/the-un-global-compact-and-accenture-identify-business-opportunities-of-sustainable-energy.htm";
        String articleContent = HttpUtils.doGetWithRetryUsingProxy(articleUrl, 8);
        TagNode articleRootNode = MyHtmlCleaner.clean(articleContent);
        Document articleDoc = null;
        try {
            articleDoc = new DomSerializer(new CleanerProperties()).createDOM(articleRootNode);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String title = xpath.evaluate("//article//div[contains(@style, 'center')]//strong/text() | //article//strong/center/text()" +
                    " | //div[@id='art-hero']//h1/text()" +
                    " | //div[@id='content-details']//div[@align='center']/b/text()", articleDoc);
            System.out.println(title);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
