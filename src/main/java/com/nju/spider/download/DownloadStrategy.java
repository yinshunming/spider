package com.nju.spider.download;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.FormatUtils;

import java.util.List;
import java.util.stream.Collectors;

public class DownloadStrategy {

    private static final long notDownloadBeforeTime = 40 * 3600 * 1000;

    public static String downloadFolderBase = "D:/reports";

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
}
