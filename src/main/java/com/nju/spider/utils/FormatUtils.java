package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;

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

            }

            if (date != null) {
                return date;
            }
        }
        return date;
    }

    public  static void main(String [] args) {
        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat dateformat2 = new SimpleDateFormat("");
        String dateStr = "June 05, 2017";
        String dateStr1 = "2017-06-15T04:00:00Z";
        Date date = FormatUtils.parseDateByMutilDateFormate(dateStr, dateformat);
        System.out.println(date);
    }
}
