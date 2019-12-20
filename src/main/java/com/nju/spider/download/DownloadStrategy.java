package com.nju.spider.download;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DownloadStrategy {

    private static final long notDownloadBeforeTime = 90 * 3600 * 1000;  //临时调大点

    private static String downloadFolderBase = "D:/reports";

    private static final Pattern fileNamePattern = Pattern.compile("file[nN]ame=\"?(.*)\"?");

    public static List<Report> getReportsToDownload() {
        List<Report> reportList = ReportDaoUtils.getReportsToDownload();
        //插入太久却没有下载下来的report就不下了
        long currentTime = System.currentTimeMillis();
        List<Report> filterList = reportList.stream().filter(report -> (currentTime - report.getInsertTime().getTime()) <= notDownloadBeforeTime)
                .collect(Collectors.toList());
        return filterList;
    }

    public static String getFilePath(Report report) {
        String url = report.getUrl();
        String [] pdfFileNames = url.split("/");
        String pdfFileName = pdfFileNames[pdfFileNames.length - 1];
        //处理某些文件名 .pdf#xxx之类
        pdfFileName = pdfFileName.replaceAll("\\.pdf.*", ".pdf");
        //是否直接拼上pdf并不好呢?
        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName = pdfFileName + ".pdf";
        }
        String publishDateStr = FormatUtils.publishDateToString(report.getPublishTime());
        String filePath = downloadFolderBase + "/" + report.getOrgName() + "/" + publishDateStr + "/" + pdfFileName;
        return filePath;
    }

    public static String changeFileNameAccordingToResponse(CloseableHttpResponse response, Report report) {
        String filePath = DownloadStrategy.getFilePath(report);
        try {

            Header contentDisposition = response.getFirstHeader("Content-Disposition");
            //如果头中有文件名，以头中的文件名为准
            if (contentDisposition != null) {
                String fileName = contentDisposition.getValue();
                Matcher matcher = fileNamePattern.matcher(fileName);
                if (matcher.find()) {
                    String newFileName = matcher.group(1);
                    //TODO 默认utf-8解码，实际上可能多种
                    newFileName = URLDecoder.decode(newFileName, "utf-8").trim().replace("\t", "");
                    //TODO 根据content-type设置类型，暂时只处理pdf类型
                    filePath = filePath.replaceAll("[^/]+\\.pdf", newFileName).replace("\"", "").trim();
                }
            }
        } catch (Exception ex) {
        }

        report.setFileUrl(filePath);
        return filePath;
    }

    public static void main(String [] args) throws UnsupportedEncodingException {
        String filePath = "D:/reports/123.pdf";
//        String contentDispositionValue = "inline; filename=\"Accenture-2017CostCybercrime-US-FINAL.pdf\"";
        String contentDispositionValue = "filename=2017%e5%b9%b4%e4%b8%ad%e5%9b%bd%e5%a4%8d%e8%b4%ad%e6%b1%bd%e8%bd%a6%e7%94%a8%e6%88%b7%e7%a0%94%e7%a9%b6%e6%8a%a5%e5%91%8a%09.pdf";
        Matcher matcher = fileNamePattern.matcher(contentDispositionValue);
        if (matcher.find()) {
            String newFileName = matcher.group(1);

            newFileName = URLDecoder.decode(newFileName, "utf-8").replace("\t", "");
            filePath = filePath.replaceAll("[^/]+\\.pdf", newFileName).replace("\"", "");
        }
        System.out.println(filePath);
    }
}
