CREATE TABLE `report` (
                          `id` int(11) NOT NULL AUTO_INCREMENT,
                          `title` varchar(500) DEFAULT NULL,
                          `publish_time` date DEFAULT NULL,
                          `org_name` varchar(255) DEFAULT NULL,
                          `industry_name` varchar(255) DEFAULT NULL,
                          `file_url` varchar(512) DEFAULT NULL,
                          `url` varchar(512) DEFAULT NULL,
                          `authors` varchar(255) DEFAULT NULL,
                          `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `download_status` tinyint(1) NOT NULL DEFAULT '0',
                          `extra` text,
                          `indexUrl` varchar(512) DEFAULT NULL,
                          `articleUrl` varchar(512) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `url_UNIQUE` (`url`),
                          KEY `download_status` (`download_status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4