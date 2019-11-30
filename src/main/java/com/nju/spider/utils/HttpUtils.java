package com.nju.spider.utils;

import com.nju.spider.bean.Report;
import com.nju.spider.download.DownloadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HttpUtils {
    private static final String defaulUserAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36";

    private static final Pattern fileNamePattern = Pattern.compile("filename=\"(.*)\"");



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
            HttpHost proxy = new HttpHost("localhost", 8589, "http");
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
            HttpHost proxy = new HttpHost("localhost", 8589, "http");
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

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).build();
        if (useProxy) {
            HttpHost proxy = new HttpHost("localhost", 8589, "http");
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
            log.error("download file encounts error ", e);
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

    public static void main(String [] args) {
        String filePath = "D:/test/123.pdf";
        String fileName = "rra.pdf";
        String ret = filePath.replaceAll("[^/]+\\.pdf", fileName);
        System.out.println(ret);
    }
}