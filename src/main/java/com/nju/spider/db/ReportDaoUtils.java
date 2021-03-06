package com.nju.spider.db;

import com.nju.spider.bean.Report;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReportDaoUtils {

    public static List<Report> getReportsToDownload() {
        List<Report> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JDBCUtils.getConn();
            String sql = "SELECT id,url,insert_time,publish_time,org_name FROM report WHERE download_status = 0";
            ps = conn.prepareStatement(sql);
            r = ps.executeQuery();
            while (r.next()) {
                Report report = new Report();
                report.setId(r.getInt("id"));
                report.setUrl(r.getString("url"));
                report.setInsertTime(r.getDate("insert_time"));
                report.setPublishTime(r.getDate("publish_time"));
                report.setOrgName(r.getString("org_name"));
                reports.add(report);
            }
        } catch (Exception ex) {
            log.error("inserting report into db encounts error ", ex);
        } finally {
            JDBCUtils.close(r, ps, conn);
        }
        return reports;
    }


    public static void insertReports(List<Report> reports) {
        for (Report report : reports) {
            if (StringUtils.isBlank(report.getUrl())) {
                continue;
            }
            Connection conn = null;
            PreparedStatement ps1 = null;
            ResultSet r1 = null;
            PreparedStatement ps2 = null;
            try {
                conn = JDBCUtils.getConn();
                String sql1 = "SELECT COUNT(1) FROM report WHERE url = ?";
                ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, report.getUrl());
                r1 = ps1.executeQuery();
                boolean haveCrawled = false;
                while (r1.next()) {
                    int count = r1.getInt(1);
                    if (count > 0) {
                        haveCrawled = true;
                        break;
                    }
                }

                if (haveCrawled) {
                    continue;
                }

                String sql2 = "INSERT INTO report (title, publish_time, org_name, industry_name, file_url, url, authors," +
                        " extra, indexUrl, articleUrl) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?)";
                ps2 = conn.prepareStatement(sql2);
                ps2.setString(1, report.getTitle());
                if (report.getPublishTime() != null) {
                    ps2.setDate(2, new Date(report.getPublishTime().getTime()));
                } else {
                    ps2.setDate(2, null);
                }
                ps2.setString(3, report.getOrgName());
                ps2.setString(4, report.getIndustryName());
                ps2.setString(5, report.getFileUrl());
                ps2.setString(6, report.getUrl());
                ps2.setString(7, report.getAuthors());
                ps2.setString(8, report.getExtra());
                ps2.setString(9, report.getIndexUrl());
                ps2.setString(10, report.getArticleUrl());
                ps2.executeUpdate();
            } catch (Exception ex) {
                log.error("inserting report into db encounts error ", ex);
            } finally {
                JDBCUtils.close(r1, ps1, null);
                JDBCUtils.close(ps2, conn);
            }
        }
    }

    public static void updateDownloadSatus(Report report) {
        Connection conn = null;
        PreparedStatement p = null;
        try {
            conn = JDBCUtils.getConn();
            String sql = "UPDATE report SET download_status = 1, file_url = ? WHERE id = ?";
            p = conn.prepareStatement(sql);
            p.setString(1, report.getFileUrl());
            p.setInt(2, report.getId());
            p.executeUpdate();
        } catch (Exception ex) {
            log.error("updating status encounts error", ex);
        } finally {
            JDBCUtils.close(p, conn);
        }
    }

    public static void updateDeloitteCnTitle(String newTitle, int id) {
        Connection conn = null;
        PreparedStatement p = null;
        try {
            conn = JDBCUtils.getConn();
            String sql = "UPDATE report SET title = ? WHERE id = ?";
            p = conn.prepareStatement(sql);
            p.setString(1, newTitle);
            p.setInt(2, id);
            p.executeUpdate();
        } catch (Exception ex) {
            log.error("updating status encounts error", ex);
        } finally {
            JDBCUtils.close(p, conn);
        }
    }

    public static List<Report> getDeloitteCnCrawlerReportListToUpdate() {
        String sql = "SELECT id, title, articleUrl FROM test.report where org_name = 'DeloitteChina' and title is null";

        List<Report> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {

            conn = JDBCUtils.getConn();
            ps = conn.prepareStatement(sql);
            r = ps.executeQuery();
            while (r.next()) {
                Report report = new Report();
                report.setId(r.getInt("id"));
                report.setTitle(r.getString("title"));
                report.setArticleUrl(r.getString("articleUrl"));
                reports.add(report);
            }
        } catch (Exception ex) {
            log.error("inserting report into db encounts error ", ex);
        } finally {
            JDBCUtils.close(r, ps, conn);
        }
        return reports;
    }
}
