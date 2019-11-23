package com.nju.spider.utils;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class MyHtmlCleaner {

    private static HtmlCleaner cleaner = new HtmlCleaner();
    static {
        //此处自定义一些clean规则，暂时使用默认的
    }

    public static TagNode clean(String htmlContent) {
        TagNode tagNode =  cleaner.clean(htmlContent);
        return tagNode;
    }
}
