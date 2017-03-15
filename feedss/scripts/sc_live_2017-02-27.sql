# ************************************************************
# Sequel Pro SQL dump
# Version 4529
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 180.76.174.126 (MySQL 5.7.17)
# Database: live
# Generation Time: 2017-02-27 00:50:52 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table button
# ------------------------------------------------------------

DROP TABLE IF EXISTS `button`;

CREATE TABLE `button` (
  `uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ext_attr` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon_selected_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  `remark` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `tags` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parameter` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `position` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `view_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_7qf6j6i5tiac9423nfjrkt5q5` (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `button` WRITE;
/*!40000 ALTER TABLE `button` DISABLE KEYS */;

INSERT INTO `button` (`uuid`, `created`, `creator`, `description`, `ext_attr`, `id`, `name`, `icon_url`, `icon_selected_url`, `rank`, `remark`, `sort`, `status`, `tags`, `type`, `updated`, `updater`, `parameter`, `parent_id`, `position`, `view_url`)
VALUES
	('02f0f13b-1764-42a4-aaaf-398fc970e094','2016-12-20 18:37:00',NULL,NULL,NULL,8,'直播榜','http://image.sc.feedss.com/button/rankList.png','http://image.sc.feedss.com/button/rankList.png',0,NULL,3,1,NULL,'native','2017-02-22 08:24:31',NULL,NULL,'02f0f13b-1764-42a4-aaaf-398fc970e097','centers','RankList'),
	('02f0f13b-1764-42a4-aaaf-398fc970e097','2016-12-21 12:37:29',NULL,NULL,NULL,11,'中间',NULL,NULL,0,NULL,NULL,1,NULL,NULL,'2016-12-21 12:38:45',NULL,NULL,NULL,'centers',NULL),
	('02f0f13b-1764-42a4-aaaf-398fc970e098','2016-12-21 12:38:04',NULL,NULL,NULL,12,'底部tab',NULL,NULL,0,NULL,NULL,1,NULL,NULL,'2016-12-21 12:38:46',NULL,NULL,NULL,'tabs',NULL),
	('338cdce9-0de0-4be5-998d-d82b673fffe9','2016-12-20 18:33:59',NULL,NULL,NULL,7,'发现','http://image.sc.feedss.com/button/discover.png','http://image.sc.feedss.com/button/discover.png',0,NULL,2,1,NULL,'native','2017-02-22 08:24:39',NULL,NULL,'02f0f13b-1764-42a4-aaaf-398fc970e097','centers','Discover'),
	('5da4d107-76dd-41e7-9c52-c87fc5a801b8','2016-12-20 18:22:37',NULL,NULL,NULL,2,'消息','http://image.sc.feedss.com/button/message.png','http://image.sc.feedss.com/button/message.png',0,NULL,NULL,1,NULL,'native','2017-02-22 08:24:46',NULL,NULL,NULL,'rightTop','Message'),
	('64b4e074-7d62-4098-854d-a539f256f1b0','2016-12-20 18:31:01',NULL,NULL,NULL,5,'我','http://image.sc.feedss.com/button/userCenter.png','http://image.sc.feedss.com/button/userCenterSelected.png',0,NULL,3,1,NULL,'native','2017-02-22 08:24:53',NULL,NULL,'02f0f13b-1764-42a4-aaaf-398fc970e098','tabs','UserCenter'),
	('6545875c-f55a-454f-96a5-49de137d7759','2016-12-20 18:29:37',NULL,NULL,NULL,4,'发布','http://image.sc.feedss.com/button/publish.png','http://image.sc.feedss.com/button/publish.png',0,NULL,2,1,NULL,'native','2017-02-22 08:25:00',NULL,NULL,'02f0f13b-1764-42a4-aaaf-398fc970e098','tabs','Publish'),
	('893dc278-6dbe-4f2e-881a-afa75cc9b713','2017-02-24 18:20:42',NULL,NULL,NULL,13,'热播','http://image.sc.feedss.com/button/top.png','http://image.sc.feedss.com/button/top.png',0,NULL,NULL,1,NULL,'native','2017-02-24 12:53:59',NULL,NULL,NULL,'mainTop',''),
	('893dc278-6dbe-4f2e-881a-afa75cc9b714','2017-02-24 18:20:42',NULL,NULL,NULL,14,'主播秀','http://image.sc.feedss.com/button/host.png','http://image.sc.feedss.com/button/host.png',0,NULL,NULL,1,NULL,'native','2017-02-24 12:54:07',NULL,NULL,NULL,'mainHost',''),
	('893dc278-6dbe-4f2e-881a-afa75cc9b7ef','2016-12-20 18:20:42',NULL,NULL,NULL,1,'搜索','http://image.sc.feedss.com/button/search.png','http://image.sc.feedss.com/button/search.png',0,NULL,NULL,1,NULL,'native','2017-02-22 08:25:08',NULL,NULL,NULL,'leftTop','Search'),
	('a1ba16a1-5a1f-4c17-a1de-f4395268e7a3','2016-12-20 18:33:21',NULL,NULL,NULL,6,'预告','http://image.sc.feedss.com/button/trailer.png','http://image.sc.feedss.com/button/trailer.png',0,NULL,1,1,NULL,'native','2017-02-22 08:25:15',NULL,NULL,'02f0f13b-1764-42a4-aaaf-398fc970e097','centers','Trailer'),
	('c63e987f-ea6d-4338-ac2c-2e42554abffb','2016-12-20 18:27:46',NULL,NULL,NULL,3,'广场','http://image.sc.feedss.com/button/content.png','http://image.sc.feedss.com/button/contentSelected.png',0,NULL,1,1,NULL,'native','2017-02-22 08:25:21',NULL,'c0d37690-8250-4133-b08f-fc328877a581','02f0f13b-1764-42a4-aaaf-398fc970e098','tabs','Content');

/*!40000 ALTER TABLE `button` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table category
# ------------------------------------------------------------

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `uuid` varchar(36) NOT NULL,
  `created` datetime DEFAULT NULL,
  `creator` varchar(36) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `ext_attr` varchar(1024) DEFAULT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `remark` varchar(128) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `tags` varchar(128) DEFAULT NULL,
  `type` varchar(36) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `updater` varchar(36) DEFAULT NULL,
  `parent_id` varchar(255) DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  `visiable` bit(1) NOT NULL,
  `back_count` int(11) NOT NULL,
  `stream_count` int(11) NOT NULL,
  `show_in_home_page_model` bit(1) NOT NULL,
  `show_in_right_model` bit(1) NOT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_f1ng7d3ff5ix4pm82145gdv13` (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;

INSERT INTO `category` (`uuid`, `created`, `creator`, `description`, `ext_attr`, `id`, `name`, `rank`, `remark`, `status`, `tags`, `type`, `updated`, `updater`, `parent_id`, `sort`, `visiable`, `back_count`, `stream_count`, `show_in_home_page_model`, `show_in_right_model`)
VALUES
	('33e17b26-3cea-4538-a46f-f5a7af23d612','2016-10-28 17:23:40','',NULL,NULL,15,'test',0,NULL,0,'CreateCategory','ALL','2016-10-28 17:23:40','',NULL,36,b'1',0,0,b'0',b'0'),
	('47b4bdf4-0ff1-4442-829f-6f8033cca658','2016-10-28 17:28:45','',NULL,NULL,16,'校园普法',0,NULL,0,'CreateCategory','ALL','2016-10-28 17:28:51','','33e17b26-3cea-4538-a46f-f5a7af23d612',12,b'1',0,0,b'0',b'0'),
	('88133594-36ce-4198-8681-51cc76f1eeda','2016-11-09 16:25:12','',NULL,NULL,17,'11111',0,NULL,0,'CreateCategory','ALL','2016-11-09 16:25:12','',NULL,36,b'1',0,0,b'0',b'0'),
	('8efe92a6-3b9c-4030-b8e1-d275282f223d','2016-11-17 14:06:08','',NULL,NULL,19,'家居',0,NULL,0,'CreateCategory','ALL','2016-11-17 14:34:01','','33e17b26-3cea-4538-a46f-f5a7af23d612',18,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a581','2016-08-08 12:11:00','1','测试','',10,'广场',1,'1',1,'Popular','ALL',NULL,NULL,'33e17b26-3cea-4538-a46f-f5a7af23d612',1,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a582','2016-08-08 12:11:00','1','测试','',4,'VR直播',1,'1',1,'CreateCategory','ALL',NULL,NULL,'33e17b26-3cea-4538-a46f-f5a7af23d612',3,b'1',1,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a583','2016-08-08 12:11:00','1','测试','',5,'边看边买',1,'1',1,'CreateCategory','ALL',NULL,NULL,'33e17b26-3cea-4538-a46f-f5a7af23d612',5,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a584','2016-08-08 12:11:00','1','测试','',6,'教育',1,'1',1,'CreateCategory','ALL',NULL,NULL,'88133594-36ce-4198-8681-51cc76f1eeda',9,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a585','2016-08-08 12:11:00','1','测试','测试',7,'美丽',1,'1',1,'CreateCategory','ALL',NULL,NULL,'88133594-36ce-4198-8681-51cc76f1eeda',25,b'0',19,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a586','2016-08-08 12:11:00','1','测试','测试',2,'健康',1,'1',1,'CreateCategory','ALL','2016-10-28 21:25:00','','88133594-36ce-4198-8681-51cc76f1eeda',7,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a587','2016-08-08 12:11:00','1','测试','测试',8,'公益普法',1,'1',1,'CreateCategory','ALL','2016-10-28 17:06:40','','88133594-36ce-4198-8681-51cc76f1eeda',10,b'0',29,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a589','2016-08-08 12:10:00','1','测试','测试',1,'其他',1,'1',1,'CreateCategory','ALL','2016-11-23 10:15:28','46acb8d0-4194-4e21-81f6-43759a878d44','88133594-36ce-4198-8681-51cc76f1eeda',36,b'1',7,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a590','2016-08-08 12:11:00','1','测试','测试',9,'理财',1,'1',1,'CreateCategory','ALL','2016-11-17 14:33:46','','88133594-36ce-4198-8681-51cc76f1eeda',13,b'1',0,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a591','2016-08-08 12:11:00','1','测试','测试',11,'游学',1,'1',1,'CreateCategory','ALL',NULL,NULL,'88133594-36ce-4198-8681-51cc76f1eeda',35,b'0',14,0,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a592','2016-08-08 12:11:00','1','测试','测试',12,'恋爱',1,'1',1,'CreateCategory','ALL','2016-09-18 16:16:06','','88133594-36ce-4198-8681-51cc76f1eeda',40,b'0',9,1,b'0',b'0'),
	('c0d37690-8250-4133-b08f-fc328877a593','2016-08-08 12:11:00','1','测试','测试',13,'中介',1,'1',1,'CreateCategory','ALL',NULL,NULL,'88133594-36ce-4198-8681-51cc76f1eeda',42,b'0',9,0,b'0',b'0'),
	('d874641c-78a1-4a83-85d4-1aea8f1d029e','2016-11-22 17:04:03','46acb8d0-4194-4e21-81f6-43759a878d44',NULL,NULL,32,'测试1',0,NULL,0,'CreateCategory','ALL','2016-11-22 17:04:03','46acb8d0-4194-4e21-81f6-43759a878d44',NULL,1,b'1',0,0,b'0',b'0'),
	('e596a90d-0d05-4183-a054-b2238e34f1cd','2016-09-18 16:19:41','',NULL,NULL,14,'逑友',0,NULL,0,'CreateCategory','ALL','2016-11-17 14:34:08','','88133594-36ce-4198-8681-51cc76f1eeda',23,b'1',0,0,b'0',b'0');

/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table configure
# ------------------------------------------------------------

DROP TABLE IF EXISTS `configure`;

CREATE TABLE `configure` (
  `uuid` varchar(36) NOT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '',
  `value` text,
  `category` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_otqqy1um2f6rvc1866q3ge8hf` (`id`),
  UNIQUE KEY `UK_o084n1axk8u4mgxawdutc3ogc` (`name`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `configure` WRITE;
/*!40000 ALTER TABLE `configure` DISABLE KEYS */;

INSERT INTO `configure` (`uuid`, `id`, `name`, `value`, `category`, `type`, `description`)
VALUES
	('08974f7b-01f6-4636-bc66-64c24ed1bffd',6,'qcloud_systemmessage_faceurl','http://image.sc.feedss.com/icon/system.png',NULL,NULL,'腾讯云消息中系统消息头像'),
	('24f4006e-517d-4708-ac3b-0b3f76bf549e',1,'qcloud_admin_identifier','admin',NULL,NULL,'腾讯云消息中配置的管理员账号'),
	('2ce2d139-6471-46f5-998f-8c30cbf9c9f8',9,'AddGroupMemmber','#nickname#进入了房间',NULL,NULL,'直播间消息模版'),
	('32735813-260c-4544-b188-324bcb6c979b',21,'site_integration_name','',NULL,NULL,NULL),
	('56ce3b74-ecb8-4c76-9bea-d6ac28e2495',11,'ShutUpGroupMemmber','#nickname#被主播禁言',NULL,NULL,'直播间消息模版'),
	('598da382-7a85-46ad-a615-a7e475ff14ab',4,'qcloud_systemmessage_identifier','systemMessageAccount',NULL,NULL,'腾讯云消息中系统消息账号'),
	('796e420c-807d-4e0d-bde7-e4dac8b20c29',2,'qcloud_nosession_identifier','nosessionAccount',NULL,NULL,'腾讯云消息中配置的直播间消息发送者账号'),
	('7fe03265-0cec-4887-a916-3eacf11ad8a3',23,'site_title','直播管理平台',NULL,NULL,NULL),
	('8995d998-63ab-48b0-8f4e-749ce0f3c054',24,'site_banner_url','',NULL,NULL,NULL),
	('b5f7a08c-d71e-46f3-8480-0cc7c46e9b75',25,'site_logo_url','',NULL,NULL,NULL),
	('c529d9d8-547d-47e2-a237-80199b33ba36',10,'GetOutGroupMemmber','#nickname#被主播踢出房间',NULL,NULL,'直播间消息模版'),
	('d9aa83cf-7700-457f-b9e2-9ad7a64769b7',8,'qcloud_pubstr','-----BEGIN PUBLIC KEY-----\nMFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEH0DmYZnbMVtYa7pZDPWkYp0vQGMEHWVS\ncPnFSXBOFi4KTx7AvLEhyfseQGoKf9SQj7mkWVDHPC+4kGoNP2JdmQ==\n-----END PUBLIC KEY-----\n',NULL,NULL,'腾讯云消息公钥'),
	('d9aa83cf-7700-457f-b9e2-9ad7a64769b8',7,'qcloud_privstr','-----BEGIN PRIVATE KEY-----\nMIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQgOeRmDDgoTnzUCc04ZFCD\nA2e0BhsjKteF99ZBGdkYwNuhRANCAAQfQOZhmdsxW1hrulkM9aRinS9AYwQdZVJw\n+cVJcE4WLgpPHsC8sSHJ+x5Aagp/1JCPuaRZUMc8L7iQag0/Yl2Z\n-----END PRIVATE KEY-----',NULL,NULL,'腾讯云消息私钥'),
	('d9aa83cf-7700-457f-b9e2-9ad7a64769b9',3,'qcloud_sdkappid','1400025235',NULL,NULL,'腾讯云消息中app id'),
	('dc07aa78-260b-4177-9a85-4524143e9068',68,'publishDomain','rtmp://live.feedss.com',NULL,0,'推流服务url'),
	('dc07aa78-260b-4177-9a85-4524143e9070',70,'playDomain','rtmp://live.feedss.com',NULL,0,'播流服务url'),
	('dc07aa78-260b-4177-9a85-4524143e9071',71,'playbackDomain','http://live.feedss.com/live.feedss.com',NULL,0,'回放url'),
	('dc07aa78-260b-4177-9a85-4524143e9072',72,'hlsDomain','http://live.feedss.com/hls_for_live',NULL,0,'hls播放地址，直播h5'),
	('dc07aa78-260b-4177-9a85-4524143e9073',73,'srsApi','http://live.feedss.com:1985',NULL,0,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9074',74,'appName','shuangchuangdahui',NULL,0,'应用名称'),
	('dc07aa78-260b-4177-9a85-4524143e9075',75,'roomOutTime','180',NULL,0,'推流超时时间，单位为s'),
	('dc07aa78-260b-4177-9a85-4524143e9076',76,'Sms.host','http://sms.feedss.com/sms/send',NULL,0,'短信服务，发送接口'),
	('dc07aa78-260b-4177-9a85-4524143e9077',77,'Sms.appid','shuangchuang',NULL,0,'短信服务业务id'),
	('dc07aa78-260b-4177-9a85-4524143e9078',78,'Sms.apptoken','94a405f5-1acc-4505-8e73-1ff0157903b0',NULL,0,'短信业务服务token'),
	('dc07aa78-260b-4177-9a85-4524143e9079',79,'Project.Version','2.0.0',NULL,0,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9081',81,'download.url','http://a.app.qq.com/o/simple.jsp?pkgname=cn.zgcinno.live',NULL,0,'腾讯应用宝下载链接'),
	('dc07aa78-260b-4177-9a85-4524143e9082',82,'ios.url','https://itunes.apple.com/cn/app/vr-yun-nu-yan/id1207703091?mt=8',NULL,0,'appstore 应用地址'),
	('dc07aa78-260b-4177-9a85-4524143e9083',83,'android.url','http://sj.qq.com/myapp/detail.htm?apkName=cn.zgcinno.live',NULL,0,'腾讯应用宝android下载地址'),
	('dc07aa78-260b-4177-9a85-4524143e9084',84,'agreement.url','http://image.sc.feedss.com/h5/userAgreement.html',NULL,0,'用户协议链接'),
	('dc07aa78-260b-4177-9a85-4524143e9085',85,'cloud.url','',NULL,0,'众品云用户链接'),
	('dc07aa78-260b-4177-9a85-4524143e9086',86,'mallDomain','',NULL,0,'第三方交易接口'),
	('dc07aa78-260b-4177-9a85-4524143e9087',87,'temp.dir','/tmp',NULL,0,'临时文件夹'),
	('dc07aa78-260b-4177-9a85-4524143e9088',88,'Image.host','http://image.sc.feedss.com/',NULL,0,'文件url前缀'),
	('dc07aa78-260b-4177-9a85-4524143e9089',89,'Image.path','/data/upload/image',NULL,0,'文件保存路径'),
	('dc07aa78-260b-4177-9a85-4524143e9090',90,'REGISTER_CONTENT','验证码：{code}，不能告诉别人噢！【双创大会】',NULL,0,'注册验证码'),
	('dc07aa78-260b-4177-9a85-4524143e9091',91,'LOGIN_CONTENT','验证码：{code}，不能告诉别人噢！【双创大会】',NULL,0,'登录短信验证码'),
	('dc07aa78-260b-4177-9a85-4524143e9092',92,'MOBILE_BIND_CONTENT','验证码：{code}，不能告诉别人噢！【双创大会】',NULL,0,'手机号绑定验证码'),
	('dc07aa78-260b-4177-9a85-4524143e9093',93,'Account.buylist','10,100,1000,10000',NULL,0,'购买虚拟币类别'),
	('dc07aa78-260b-4177-9a85-4524143e9094',94,'Recharge.ratio','1:10',NULL,0,'rmb与虚拟币兑换比例'),
	('dc07aa78-260b-4177-9a85-4524143e9095',95,'WeChat.appname','双创大会',NULL,0,'微信支付应用名称'),
	('dc07aa78-260b-4177-9a85-4524143e9096',96,'WeChat.AppId','wx6ddc93f2f37e9bfb',NULL,0,'微信支付appid'),
	('dc07aa78-260b-4177-9a85-4524143e9097',97,'WeChat.key','63e7e801f523dbe8e8e8456f01d2fe57',NULL,0,'微信支付app 签名'),
	('dc07aa78-260b-4177-9a85-4524143e9098',98,'WeChat.merchantId','1440569302',NULL,0,'微信支付'),
	('dc07aa78-260b-4177-9a85-4524143e9099',99,'WeChat.notifyUrl','http://sc.feedss.com/pay/wechat/paynotify',NULL,0,'微信支付'),
	('dc07aa78-260b-4177-9a85-4524143e9100',100,'WeChat.unifiedOrder','https://api.mch.weixin.qq.com/pay/unifiedorder',NULL,0,'微信支付'),
	('dc07aa78-260b-4177-9a85-4524143e9101',101,'WeChat.orderQuery','https://api.mch.weixin.qq.com/pay/orderquery',NULL,0,'微信支付'),
	('dc07aa78-260b-4177-9a85-4524143e9102',102,'Account.transaction.hasExpired','0',NULL,0,'是否有交易有效期,影响余额计算'),
	('dc07aa78-260b-4177-9a85-4524143e9103',103,'Account.transaction.systemPresent.expired','30',NULL,0,'系统赠送积分的有效时长，单位为天'),
	('dc07aa78-260b-4177-9a85-4524143e9104',104,'Register.notempty.field','nickname,avatar,gender',NULL,0,'注册非空字段'),
	('dc07aa78-260b-4177-9a85-4524143e9105',105,'Register.reward.count','0',NULL,0,'注册奖励虚拟币数'),
	('dc07aa78-260b-4177-9a85-4524143e9106',106,'Profile.reward.field','',NULL,0,'资料奖励字段'),
	('dc07aa78-260b-4177-9a85-4524143e9107',107,'Profile.reward.count','0',NULL,0,'资料奖励虚拟币数'),
	('dc07aa78-260b-4177-9a85-4524143e9108',108,'Profile.articleDetail.count','0',NULL,0,'看文章奖励'),
	('dc07aa78-260b-4177-9a85-4524143e9109',109,'Profile.login.count','0',NULL,0,' 用户登录奖励'),
	('dc07aa78-260b-4177-9a85-4524143e9e78',5,'qcloud_systemmessage_nickname','系统消息',NULL,NULL,'腾讯云消息中系统消息昵称'),
	('dc07aa78-260b-4177-9a85-4524143e9e82',41,'app_leftmodel_name','双创大会',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e83',42,'app_leftmodel_url','',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e84',43,'app_rightmodel_name','双创大会',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e85',44,'app_rightmodel_url','',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e86',46,'rule_content_for_BonusTask','1、支付完任务金额即视为向主播发布了1个任务<br />\n\r2、发布任务后，若主播确认您的任务，则您支\r付的任务金额将立即转入主播账户。若主播拒绝\r您的任务，则您支付的任务金额将返还到您的账\r户中。若主播七天内没有响应您的任务，则七天\r后自动视为拒绝且返还您的任务金额。<br />\r\r3、发布任务后，将无法撤销操作，若您误操作\r或有其他原因想要撤销该任务，请主动联系平台客\r服人员。<br />\n',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e87',47,'rule_content_for_Bid','1、支付完竞标金额即视为参与竞标 <br />\n\r2、参与竞标后，若主播确认您的竞标，则您支\r付的竞标金额将立即转入主播账户。若主播拒绝\r您的竞标，则您支付的竞标金额将返还到您的账\r户中。若主播七天内没有响应您的竞标，则七天\r后自动视为拒绝且返还您的竞标金额。 <br />\n\r3、参与竞标后，将无法撤销操作，若您误操作\r或有其他原因想要撤销竞标，请主动联系平台客\r服人员。 <br />',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e88',48,'thanksForVisitSmsContent','【双创大会】感谢您申请参观title，我们的客服人员会联系您，期待您的参与。',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e89',49,'appointmentNoticeTemplate','nickname预约了您的title，手机号：mobile',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e90',50,'interactionOverdue','1',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e91',51,'interactionNewFromDescription','发布$type$description',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e92',52,'interactionNewToDescription','确认$type$description',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e93',53,'interactionRejectFromDescription','拒绝$type$description',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e94',54,'interactionRejectToDescription','$type$description被拒绝',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e95',55,'interactionOverduaFromDescription','$type$description逾期未处理',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e96',56,'interactionOverduaToDescription','您发布的$type$description逾期未被处理',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e97',57,'streamSummaryDescription','您的直播已结束，直播开始于：start，结束于：end，时长：duration，观看人次：playCount，投标数：bidNum，任务数：bonusTaskNum，去个人中心继续处理>>',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e98',58,'template_h5','/black',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9e99',59,'search_keyword_suggest','双创,创业,会议,教育,公益',NULL,NULL,'搜索关键词列表'),
	('dc07aa78-260b-4177-9a85-4524143e9f01',61,'shareDesc','为创新创业理想而奋斗',NULL,1,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f02',62,'iosAppId','1207703091',NULL,NULL,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f03',63,'imSdkAppId','1400025235',NULL,1,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f04',64,'imSdkAccountType','10738',NULL,1,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f05',65,'miPushAppId','',NULL,1,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f06',67,'miPushAppKey','',NULL,1,NULL),
	('dc07aa78-260b-4177-9a85-4524143e9f07',113,'virtualCoinName','创币',NULL,1,'虚拟币名称'),
	('dc07aa78-260b-4177-9a85-4524143e9f08',114,'corporationName','北京众品云兮科技有限公司',NULL,1,'公司名称（关于中）'),
	('dc07aa78-260b-4177-9a85-4524143e9f09',115,'corporationCopyright','Copyright @ 2016',NULL,1,'公司Copyright'),
	('dc07aa78-260b-4177-9a85-4524143e9f10',116,'corporationDesc','为响应克强总理关于”大众创业，万众创新“的号召，由中关村阳光双创会发起的“中国互联网+双创大会”应运而生。双创大会App旨在陪伴创业者创业成功，为创业者提供更有价值的信息和资源，助创业者们为创新创业理想而奋斗。',NULL,1,'公司简介');

/*!40000 ALTER TABLE `configure` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table module
# ------------------------------------------------------------

DROP TABLE IF EXISTS `module`;

CREATE TABLE `module` (
  `uuid` varchar(36) CHARACTER SET utf8 NOT NULL,
  `created` datetime DEFAULT NULL,
  `creator` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `description` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `ext_attr` varchar(1024) CHARACTER SET utf8 DEFAULT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `remark` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `status` int(11) NOT NULL,
  `tags` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `type` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `updater` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `code` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `ioc` varchar(500) CHARACTER SET utf8 DEFAULT NULL,
  `url` varchar(500) CHARACTER SET utf8 DEFAULT NULL,
  `execute_type` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_n3j7khwwxh3njaw1adgkkvyhc` (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;

INSERT INTO `module` (`uuid`, `created`, `creator`, `description`, `ext_attr`, `id`, `name`, `rank`, `remark`, `status`, `tags`, `type`, `updated`, `updater`, `code`, `ioc`, `url`, `execute_type`, `sort`)
VALUES
	('d2a4e980-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,8,'礼物',0,NULL,0,NULL,'Base',NULL,NULL,'0000','http://image.sc.feedss.com/icon/gift.png','http://sc.feedss.com/h5/myGifts','H5',NULL),
	('d2a4f51a-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,9,'动态',0,NULL,0,NULL,'Base',NULL,NULL,'0001','http://image.sc.feedss.com/icon/shop.png','http://sc.feedss.com/h5/skillList','H5',NULL),
	('d2a4fc90-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,10,'直播',0,NULL,0,NULL,'Base',NULL,NULL,'0002','http://image.sc.feedss.com/icon/play.png',NULL,'Native',NULL),
	('d2a4fe68-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,11,'账户',0,NULL,0,NULL,'Base',NULL,NULL,'0003','http://image.sc.feedss.com/icon/card.png',NULL,'Native',NULL),
	('d2a4ff78-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,12,'任务',0,NULL,1,NULL,'Base',NULL,NULL,'0004','http://image.sc.feedss.com/icon/task.png','http://sc.feedss.com/h5/task/publishList','H5',NULL),
	('d2a500b9-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,13,'加V',0,NULL,0,NULL,'Base',NULL,NULL,'0005','http://image.sc.feedss.com/icon/V.png','','Native',NULL),
	('d2a501d1-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,14,'心愿单',0,NULL,0,NULL,'Base',NULL,NULL,'0006','http://image.sc.feedss.com/icon/wish.png',NULL,'Native',NULL),
	('d2a502d1-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,15,'我的任务',0,NULL,0,NULL,'Base',NULL,NULL,'0007','http://image.sc.feedss.com/icon/task.png',NULL,'Native',NULL),
	('d2a503d1-5d49-11e6-8b01-52540084282a',NULL,NULL,NULL,NULL,16,'我的投标',0,NULL,0,NULL,'Base',NULL,NULL,'0008','http://image.sc.feedss.com/icon/bid.png',NULL,'Native',NULL);

/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table role
# ------------------------------------------------------------

DROP TABLE IF EXISTS `role`;

CREATE TABLE `role` (
  `uuid` varchar(36) CHARACTER SET utf8 NOT NULL,
  `created` datetime DEFAULT NULL,
  `creator` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `description` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `ext_attr` varchar(1024) CHARACTER SET utf8 DEFAULT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `remark` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `status` int(11) NOT NULL,
  `tags` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `type` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `updater` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `code` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_fo6wf0tra1bdkib74mxcq3itc` (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;

INSERT INTO `role` (`uuid`, `created`, `creator`, `description`, `ext_attr`, `id`, `name`, `rank`, `remark`, `status`, `tags`, `type`, `updated`, `updater`, `code`, `sort`)
VALUES
	('3270a1db-599e-11e6-8b01-52540084282a','2016-08-04 01:17:46',NULL,NULL,NULL,1,'user',0,NULL,0,NULL,'0',NULL,NULL,'0000',NULL),
	('3270a982-599e-11e6-8b01-52540084282a','2016-08-04 01:17:46',NULL,NULL,NULL,2,'V',0,NULL,0,NULL,'0',NULL,NULL,'0001',NULL),
	('f7515aaa-6376-11e6-8b01-52540084282a','2016-08-16 14:02:08',NULL,NULL,NULL,3,'admin',0,NULL,0,NULL,'0',NULL,NULL,'0002',NULL),
	('f7515bbb-6376-11e6-8b01-52540084282a','2016-12-02 14:02:08',NULL,NULL,NULL,4,'host',0,NULL,0,NULL,NULL,NULL,NULL,'0003',NULL);

/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table usr_product
# ------------------------------------------------------------

DROP TABLE IF EXISTS `usr_product`;

CREATE TABLE `usr_product` (
  `uuid` varchar(36) CHARACTER SET utf8 NOT NULL,
  `created` datetime DEFAULT NULL,
  `creator` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `description` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `ext_attr` varchar(1024) CHARACTER SET utf8 DEFAULT NULL,
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `remark` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `status` int(11) NOT NULL,
  `tags` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `type` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `updater` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `pic` varchar(800) CHARACTER SET utf8 NOT NULL,
  `price` int(11) NOT NULL,
  `sort` int(11) DEFAULT NULL,
  `category` varchar(36) DEFAULT NULL,
  `stocks` int(11) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_2gi03oht571gsnsvllxu1q19x` (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `usr_product` WRITE;
/*!40000 ALTER TABLE `usr_product` DISABLE KEYS */;

INSERT INTO `usr_product` (`uuid`, `created`, `creator`, `description`, `ext_attr`, `id`, `name`, `rank`, `remark`, `status`, `tags`, `type`, `updated`, `updater`, `pic`, `price`, `sort`, `category`, `stocks`)
VALUES
	('1','2016-08-24 16:44:52',NULL,NULL,NULL,3,'棒棒糖',1,NULL,1,NULL,'GIFT',NULL,NULL,'http://image.sc.feedss.com/product/gift/bbt.png',1,NULL,NULL,NULL),
	('2','2016-08-25 16:45:36',NULL,NULL,NULL,4,'鲜花',1,NULL,1,NULL,'GIFT',NULL,NULL,'http://image.sc.feedss.com/product/gift/xianhua.png',10,NULL,NULL,NULL),
	('3','2016-08-31 16:46:57',NULL,NULL,NULL,5,'蛋糕',1,NULL,1,NULL,'GIFT',NULL,NULL,'http://image.sc.feedss.com/product/gift/dangao.png',100,NULL,NULL,NULL),
	('4','2016-08-31 20:51:17',NULL,NULL,NULL,6,'跑车',1,NULL,1,NULL,'GIFT',NULL,NULL,'http://image.sc.feedss.com/product/gift/paoche.png',1000,NULL,NULL,NULL),
	('5','2016-08-30 20:53:03',NULL,NULL,NULL,7,'游艇',1,NULL,1,NULL,'GIFT',NULL,NULL,'http://image.sc.feedss.com/product/gift/youting.png',10000,NULL,NULL,NULL);

/*!40000 ALTER TABLE `usr_product` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
