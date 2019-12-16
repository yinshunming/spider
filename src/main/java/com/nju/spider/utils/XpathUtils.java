package com.nju.spider.utils;

import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName XpathUtils
 * @Description TODO
 * @Author UPC
 * @Date 2019/12/4 21:20
 * @Version 1.0
 */
@Slf4j
public class XpathUtils {

    public static String getStringFromXpathUsingSpecialXpath(TagNode tagNode, String xpath) {
        String ret = null;
        try {
            Document docTmpContent = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
            XPath newXpath = XPathFactory.newInstance().newXPath();
            ret = newXpath.evaluate(xpath, docTmpContent);
        } catch (Exception e) {
            log.error("getting string from xpath encounts error ");
        }
        return ret;
    }


    public static String getStringFromXpathUsingSpecialXpath(String res, String xpath) {
        String ret = null;
        try {
            TagNode tagNode = MyHtmlCleaner.clean(res);
            ret = getStringFromXpathUsingSpecialXpath(tagNode, xpath);
        } catch (Exception e) {
            log.error("getting string from xpath encounts error ");
        }

        return ret;
    }

    public static List<String> getStringListFromXpath(String res, String xpath) {
        List<String> retList = new ArrayList<>();
        try {
            TagNode tagNode = MyHtmlCleaner.clean(res);
            retList = getStringListFromXpath(tagNode, xpath);
        } catch (Exception ex) {
            log.error("getting string list from xpath encounts error ", ex);
        }
        return retList;
    }

    public static List<String> getStringListFromXpath(TagNode tagNode, String xpath) {
        List<String> retList = new ArrayList<>();
        try {
            Object[] objs = tagNode.evaluateXPath(xpath);
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof String) {
                    retList.add(((String)objs[i]).replace("&nbsp;", "").trim());
                } else {
                    retList.add(objs[i].toString().replace("&nbsp;", "").trim());
                }
            }
        } catch (Exception ex) {
            log.error("getting string list from xpath encounts error ", ex);
        }
        return retList;
    }


    public static List<TagNode> getTagNodeListFromXpath(String res, String xpath) {
        List<TagNode> retList = new ArrayList<>();
        try {
            TagNode tagNode = MyHtmlCleaner.clean(res);
            Object[] objs = tagNode.evaluateXPath(xpath);

            for (int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof TagNode) {
                    retList.add((TagNode) objs[i]);
                }
            }
        } catch (Exception ex) {
            log.error("getting string list from xpath encounts error ", ex);
        }
        return retList;
    }

    public static String getStringFromXpath(String res, String xpath) {
        String ret = null;
        try {
            TagNode tagNode = MyHtmlCleaner.clean(res);
            ret = getStringFromXpath(tagNode, xpath);
        } catch (Exception e) {
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
                ret = ret.replace("&nbsp;", "").trim();
            }
        } catch (XPatherException e) {
            log.error("getting string from xpath encounts error ", e);
        }

        return ret;
    }

}
