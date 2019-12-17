package com.nju.spider.download;

import com.nju.spider.bean.Report;
import com.nju.spider.db.ReportDaoUtils;
import com.nju.spider.utils.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DownloadTask implements Runnable {
    private Report report;
    private boolean usingProxy;
    private CountDownLatch countDownLatch;

    @Override
    public void run() {
        //TODO 存储优化，上传oss (本地只需存在tmp目录，上传后删除即可) or 本地存(是否需要分子文件夹存，看的更舒服)
        try {
            boolean downloadSucc = false;
            if (!usingProxy) {
                downloadSucc = HttpUtils.doDownload(report);
            } else {
                downloadSucc = HttpUtils.doDownloadWithProxy(report);
            }

            if (downloadSucc) {
                ReportDaoUtils.updateDownloadSatus(report);
            }
        } catch (Exception ex) {
            log.error("downing encounts error ", ex);
        } finally {
            countDownLatch.countDown();
        }
    }
}
