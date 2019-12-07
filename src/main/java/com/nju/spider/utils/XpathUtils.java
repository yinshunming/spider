package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

/**
 * @ClassName XpathUtils
 * @Description TODO
 * @Author UPC
 * @Date 2019/12/4 21:20
 * @Version 1.0
 */
@Slf4j
public class XpathUtils {

    public static String getStringFromXpath(String res, String xpath) {
        String ret = null;
        try {
            TagNode tagNode = MyHtmlCleaner.clean(res);
            Object [] objs = tagNode.evaluateXPath(xpath);
            if (objs.length > 0) {
                if (objs[0] instanceof String) {
                    ret = (String) objs[0];
                } else {
                    ret = objs[0].toString();
                }
                ret = ret.trim();
            }
        } catch (XPatherException e) {
            log.error("getting string from xpath encounts error ");
        }

        return ret;
    }


    public static String getStringFromXpath(TagNode tagNode , String xpath) {
        String ret = null;

        try {
            Object [] objs = tagNode.evaluateXPath(xpath);
            if (objs.length > 0) {
                if (objs[0] instanceof String) {
                    ret = (String) objs[0];
                } else {
                    ret = objs[0].toString();
                }
            }
        } catch (XPatherException e) {
            log.error("getting string from xpath encounts error ", e);
        }

        return ret;
    }
}
