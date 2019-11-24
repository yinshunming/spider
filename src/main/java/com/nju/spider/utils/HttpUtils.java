package com.nju.spider.utils;

import com.google.common.base.Strings;
import lombok.extern.java.Log;
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
import java.util.regex.Pattern;

@Slf4j
public class HttpUtils {
    private static final String defaulUserAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36";

    private static final Pattern fileNamePattern = Pattern.compile("filename=\"(.*)\"");


    public static String doGetWithRetry(String url, int retryCount) {
        for (int i = 0 ; i < retryCount; i++) {
            String res = doGet(url);
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

    public static String doGetOrDownload(String url) {
        String resStr = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(15000).build();
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent", defaulUserAgent);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            Header contentDisposition = response.getFirstHeader("Content-Disposition");
            if (contentDisposition != null) {
                String fileName = contentDisposition.getValue();

            }
        } catch (Exception e) {
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
        String resStr = null;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
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

    public static boolean doDownload(String url, String filePath) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
        httpGet.setHeader("User-Agent", defaulUserAgent);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
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
        } catch (Exception e) {
            log.error("download file encounts error ", e);
            return false;
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

        return true;
    }
}
