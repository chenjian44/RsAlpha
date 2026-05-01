CREATE TABLE `Counters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL DEFAULT '1',
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `DcChannelMessages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channelId` varchar(255) NOT NULL,
  `channelName` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user` varchar(255) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contentMd5` varchar(32) NOT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_md5` (`contentMd5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `daily_summaries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL COMMENT '日期',
  `content` text NOT NULL COMMENT '总结全文',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日总结表';

CREATE TABLE IF NOT EXISTS `blogger_sentiments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` varchar(10) NOT NULL COMMENT '日期，格式：YYYY-MM-DD',
  `ticker` varchar(20) NOT NULL COMMENT '股票代码',
  `blogger` varchar(100) NOT NULL COMMENT '大V名字',
  `sentiment_score` int(11) NOT NULL COMMENT '情绪量化分值',
  `horizon` varchar(100) DEFAULT NULL COMMENT '交易周期',
  `strategy` text COMMENT '详细策略',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_ticker_blogger` (`date`, `ticker`, `blogger`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大V情绪分析表';

CREATE TABLE IF NOT EXISTS `blogger_raw_sentiments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` varchar(10) NOT NULL COMMENT '日期，格式：YYYY-MM-DD',
  `ticker` varchar(20) NOT NULL COMMENT '股票代码',
  `blogger` varchar(100) NOT NULL COMMENT '大V名字',
  `sentiment_score` int(11) NOT NULL COMMENT '情绪量化分值',
  `horizon` varchar(100) DEFAULT NULL COMMENT '交易周期',
  `strategy` text COMMENT '详细策略',
  `raw_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '原始帖子内容',
  `channel_id` varchar(255) DEFAULT NULL COMMENT '频道ID',
  `channel_name` varchar(255) DEFAULT NULL COMMENT '频道名称',
  `message_time` timestamp NULL DEFAULT NULL COMMENT '原始消息时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_ticker_blogger_raw` (`date`, `ticker`, `blogger`),
  KEY `idx_blogger` (`blogger`),
  KEY `idx_ticker` (`ticker`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大V原始帖子情感分析表';
