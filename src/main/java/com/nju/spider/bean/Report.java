package com.nju.spider.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private int id;
    private String title;
    private Date publishTime;
    private String orgName;
    private String industryName;
    private String fileUrl;
    private String url;
    private String authors;
    private Date insertTime;
    private Date updateTime;
}
