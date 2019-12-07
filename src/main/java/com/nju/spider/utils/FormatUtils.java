package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
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
            dateStr = dateStr.replace("一", "1").replace("二", "2").replace("三", "3").replace("四", "4").replace("五", "5").replace("六", "6")
                    .replace("七", "7").replace("八", "8").replace("九", "9").replace("十", "10").replace("十一", "11").replace("十二", "12");
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
                date = parseDateByDateFormate(dateStr, smdf);
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
//        String str = "《中国创新崛起——中国创新生态发展报告2019》";
//        String res = str.replaceAll("[《》<<>>]", "");
//        System.out.println(res);
        String dateStr = "十月 29, 2019";
        dateStr = dateStr.replace("一", "1").replace("二", "2").replace("三", "3").replace("四", "4").replace("五", "5").replace("六", "6")
        .replace("七", "7").replace("八", "8").replace("九", "9").replace("十", "10").replace("十一", "11").replace("十二", "12");
        SimpleDateFormat df = new SimpleDateFormat("MM月 dd, yyyy", Locale.SIMPLIFIED_CHINESE);
        Date d = null;
        try {
            d = df.parse(dateStr);
            System.out.println(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
//        SimpleDateFormat dateformat2 = new SimpleDateFormat("");
//        String dateStr =
//                "            June 08, 2010   ";
//        String dateStr1 = "2017-06-15T04:00:00Z";
//        Date date = FormatUtils.parseDateByMutilDateFormate(dateStr, dateformat);
//        System.out.println(date);
//        String articleUrl = "https://newsroom.accenture.com/news/the-un-global-compact-and-accenture-identify-business-opportunities-of-sustainable-energy.htm";
//        String articleContent = HttpUtils.doGetWithRetryUsingProxy(articleUrl, 8);
//        TagNode articleRootNode = MyHtmlCleaner.clean(articleContent);
//        Document articleDoc = null;
//        try {
//            articleDoc = new DomSerializer(new CleanerProperties()).createDOM(articleRootNode);
//            XPath xpath = XPathFactory.newInstance().newXPath();
//            String title = xpath.evaluate("//article//div[contains(@style, 'center')]//strong/text() | //article//strong/center/text()" +
//                    " | //div[@id='art-hero']//h1/text()" +
//                    " | //div[@id='content-details']//div[@align='center']/b/text()", articleDoc);
//            System.out.println(title);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
