package com.nju.spider.download;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DownloadTask implements Runnable {
    private Report report;
    private boolean usingProxy;

    @Override
    public void run() {
        //TODO 存储优化，上传oss (本地只需存在tmp目录，上传后删除即可) or 本地存(是否需要分子文件夹存，看的更舒服)
        String filePath = DownloadStrategy.getFilePath(report);
        report.setFileUrl(filePath);
        boolean downloadSucc = false;
        if (!usingProxy) {
            downloadSucc = HttpUtils.doDownload(report.getUrl(), filePath);
        } else {
            downloadSucc = HttpUtils.doDownloadWithProxy(report.getUrl(), filePath);
        }

        if (downloadSucc) {
            ReportDaoUtils.updateDownloadSatus(report);
        }
    }
}
