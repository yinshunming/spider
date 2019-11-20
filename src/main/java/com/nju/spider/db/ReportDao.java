package com.nju.spider.db;

import com.nju.spider.bean.Report;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
public class ReportDao {

    public static void insertReports(List<Report> reports) {
        for (Report report : reports) {
            Connection conn = null;
            try {
                conn = JDBCUtils.getConn();
                String sql = "INSERT INTO report (title, publish_time, org_name, industry_name, file_url, url, authors) " +
                        "VALUES(?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, report.getTitle());
                ps.setDate(2, new Date(report.getPublishTime().getTime()));
                ps.setString(3, report.getOrgName());
                ps.setString(4, report.getIndustryName());
                ps.setString(5, report.getFileUrl());
                ps.setString(6, report.getUrl());
                ps.setString(7, report.getAuthors());
                ps.executeUpdate();

            } catch (Exception ex) {
                log.error("inserting report into db encounts error ", ex);
            }
        }
    }
}
