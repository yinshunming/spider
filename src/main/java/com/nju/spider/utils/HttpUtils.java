package com.nju.spider.utils;

import com.nju.spider.bean.Report;
import com.nju.spider.download.DownloadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HttpUtils {
    private static final String defaulUserAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36";

    private static final String postDefaultContentType = "application/x-www-form-urlencoded";

    private static final Pattern fileNamePattern = Pattern.compile("filename=\"(.*)\"");

    public static final String proxyHost = "localhost";

    public static final Integer proxyPort = 8589;

    private static final Integer defaultRetryTimes = 3;


    public static HttpHost getProxy() {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
        return proxy;
    }


    public static String doGetWithRetry(String url) {
        return doGetWithRetry(url, defaultRetryTimes);
    }


    public static String doGetWithRetry(String url, int retryCount) {
        return doGetWithRetry(url, retryCount, false);
    }

    public static String doGetWithRetry(String url, int retryCount, boolean usingProxy) {
        for (int i = 0 ; i < retryCount; i++) {
            String res = doGet(url, usingProxy);
            if (StringUtils.isNotBlank(res)) {
                return res;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return null;
    }

    public static String doGetWithRetryUsingProxy(String url) {
        return doGetWithRetryUsingProxy(url, defaultRetryTimes);
    }

    public static String doGetWithRetryUsingProxy(String url, int retryCount) {
        return doGetWithRetry(url, retryCount, true);
    }

    public static String doGetOrDownloadWithRetryTime(Report report, int retryCount) {
        for (int i = 0 ; i < retryCount; i++) {
            String res = doGetOrDownload(report);
            if (StringUtils.isNotBlank(res)) {
                return res;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        return null;
    }


    //这个方法其实不太好，写了就写了吧
    public static String doGetOrDownload(Report report) {
        String url = report.getUrl();

        String resStr = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(15000).build();
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent", defaulUserAgent);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            String filePath = DownloadStrategy.changeFileNameAccordingToResponse(response, report);

            Header contentType = response.getFirstHeader("Content-Type");
            //暂时只处理pdf类型
            if (contentType != null && contentType.getValue().contains("pdf")) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                File file = new File(filePath);
                file.getParentFile().mkdirs();
                FileOutputStream fileout = new FileOutputStream(file);
                /**
                 * 根据实际运行效果 设置缓冲区大小
                 */
                byte[] buffer = new byte[1024 * 30];
                int ch = 0;
                while ((ch = is.read(buffer)) != -1) {
                    fileout.write(buffer, 0, ch);
                }
                is.close();
                fileout.flush();
                fileout.close();
                return "pdf";
            } else {
                //非下载文件，则正常返回String
                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    resStr = EntityUtils.toString(responseEntity);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
            }
        }
        return resStr;
    }

    public static String judgeUrlIfPdfDownloadWithRetryTimes(String url, int retryTimes, boolean usingProxy) {
        for (int i = 0 ; i < retryTimes; i++) {
            String res = judgeUrlIfPdfDownload(url, usingProxy);
            if (StringUtils.isNotBlank(res)) {
                return res;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        return null;
    }

    public static String judgeUrlIfPdfDownload(String url, boolean usingProxy) {
        String resStr = null;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(15000).build();
        if (usingProxy) {
            HttpHost proxy = getProxy();
            requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).setProxy(proxy).build();
        }

        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent", defaulUserAgent);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                Header contentType = response.getFirstHeader("Content-Type");
                //暂时只处理pdf类型
                if (contentType != null && contentType.getValue().contains("pdf")) {
                    return "pdf";
                } else {
                    resStr = EntityUtils.toString(responseEntity);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
            }
        }
        return resStr;
    }



    public static String doGet(String url) {
        return doGet(url, false);
    }

    public static String doGet(String url, boolean usingProxy) {
        String resStr = null;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig =  RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).build();
        if (usingProxy) {
            HttpHost proxy = getProxy();
            requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(15000).setProxy(proxy).build();
        }
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent", defaulUserAgent);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                resStr = EntityUtils.toString(responseEntity);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
            }
        }
        return resStr;
    }


    public static boolean doDownload(Report report) {
        return doDownload(report, false);
    }


    public static boolean doDownloadWithProxy(Report report) {
        return doDownload(report, true);
    }

    public static boolean doDownload(Report report, boolean useProxy) {
        String url = report.getUrl();
        url = url.replaceAll(" ","%20").replaceAll("\\^", "5e");

        //.setRedirectStrategy(new DefaultRedirectStrategy()).
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).build();
        if (useProxy) {
            HttpHost proxy = getProxy();
            requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).setProxy(proxy).build();
        }
        httpGet.setHeader("User-Agent", defaulUserAgent);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);

            String filePath = DownloadStrategy.changeFileNameAccordingToResponse(response, report);

            HttpEntity entity = response.getEntity();
            if (entity != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                InputStream is = entity.getContent();
                File file = new File(filePath);
                file.getParentFile().mkdirs();
                FileOutputStream fileout = new FileOutputStream(file);
                /**
                 * 根据实际运行效果 设置缓冲区大小
                 */
                byte[] buffer = new byte[1024 * 30];
                int ch = 0;
                while ((ch = is.read(buffer)) != -1) {
                    fileout.write(buffer, 0, ch);
                }
                is.close();
                fileout.flush();
                fileout.close();
                return true;
            }
        } catch (Exception e) {
            log.error("download file encounts error " + url, e);
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
            }
        }

        return false;
    }

    public static String doPostWithRetryUsingProxy(String url, String strEntity) {
        return doPostWithRetry(url, strEntity, null, defaultRetryTimes, true);
    }

    public static String doPostWithRetryUsingProxy(String url, String strEntity, int retryTimes) {
        return doPostWithRetry(url, strEntity, null, retryTimes, true);
    }

    public static String doPostWithRetryUsingProxy(String url, String strEntity, Map<String, String> headersMap) {
        return doPostWithRetry(url, strEntity, headersMap, defaultRetryTimes, true);
    }

    public static String doPostWithRetryUsingProxy(String url, String strEntity, Map<String, String> headersMap, int retryTimes) {
        return doPostWithRetry(url, strEntity, headersMap, retryTimes, true);
    }

    public static String doPostWithRetry(String url, String strEntity) {
        return doPostWithRetry(url, strEntity, null, defaultRetryTimes);
    }

    public static String doPostWithRetry(String url, String strEntity, int retryTimes) {
        return doPostWithRetry(url, strEntity, null, retryTimes);
    }


    public static String doPostWithRetry(String url, String strEntity, Map<String, String> headersMap) {
        return doPostWithRetry(url, strEntity, headersMap, defaultRetryTimes);
    }


    public static String doPostWithRetry(String url, String strEntity, Map<String, String> headersMap, int retryTimes) {
        return doPostWithRetry(url, strEntity, headersMap, retryTimes, false);
    }


    public static String doPostWithRetry(String url, String strEntity, Map<String, String> headersMap, int retryTimes, boolean usingProxy) {
        for (int i = 0 ; i < retryTimes; i++) {
            String res = doPost(url, strEntity, headersMap, usingProxy);
            if (StringUtils.isNotBlank(res)) {
                return res;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        return null;
    }


    public static String doPost(String url, String strEntity, Map<String, String> headersMap) {
        return doPost(url, strEntity, headersMap, false);
    }


    public static String doPost(String url, String strEntity, Map<String, String> headersMap, boolean usingProxy) {
        String resStr = null;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig =  RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).build();
        if (usingProxy) {
            HttpHost proxy = getProxy();
            requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(15000).setProxy(proxy).build();
        }

        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("User-Agent", defaulUserAgent);
            httpPost.setHeader("Content-Type", postDefaultContentType);

            if (headersMap != null && headersMap.size() > 0) {
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            if (StringUtils.isNotBlank(strEntity)) {
                httpPost.setEntity(new StringEntity(strEntity));
            }

            response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                resStr = EntityUtils.toString(responseEntity);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
            }
        }
        return resStr;
    }

    public static void main(String [] args) {
//        String filePath = "D:/test/123.pdf";
//        String fileName = "rra.pdf";
//        String ret = filePath.replaceAll("[^/]+\\.pdf", fileName);
//        System.out.println(ret);
//        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        NameValuePair pair = new BasicNameValuePair("31782_219811pi_31782_219811", "SDDF");
//        NameValuePair pair2 = new BasicNameValuePair("31782_219813pi_31782_219813", "dafafsd");
//        NameValuePair pair3 = new BasicNameValuePair("31782_219815pi_31782_219815", "12afd@xds.com");
//        NameValuePair pair4 = new BasicNameValuePair("31782_219817pi_31782_219817", "dsfa");
//        NameValuePair pair5 = new BasicNameValuePair("31782_219821pi_31782_219821", "2278517");
//        NameValuePair pair6 = new BasicNameValuePair("pi_extra_field", "");
//        NameValuePair pair7 = new BasicNameValuePair("_utf8", "%E2%98%83");
//        NameValuePair pair8 = new BasicNameValuePair("hiddenDependentFields", "");
//        params.add(pair);
//        params.add(pair2);
//        params.add(pair3);
//        params.add(pair4);
//        params.add(pair5);
//        params.add(pair6);
//        params.add(pair7);
//        params.add(pair8);

        String res = HttpUtils.doPostWithRetry("https://www4.nielsen.com/l/31782/2019-09-20/jdwfvn", "31782_219811pi_31782_219811=SDDF&31782_219813pi_31782_219813=dafafsd&31782_219815pi_31782_219815=12afd%40xds.com&31782_219817pi_31782_219817=dsfa&31782_219819pi_31782_219819=sadffdas&31782_219821pi_31782_219821=2278517&pi_extra_field=&_utf8=%E2%98%83&hiddenDependentFields=");
        System.out.println(res);
    }
}