INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (1, 'siteName', '站点名称');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (5, 'storageStrategy', '当前启用存储引擎');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (6, 'username', '管理员账号');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (7, 'password', '管理员密码');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (8, 'domain', '站点域名');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (11, 'customCss', '自定义 CSS');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (12, 'customJs', '自定义 JS (可用于统计代码)');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (13, 'tableSize', '表格大小');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (14, 'showOperator', '是否显示操作按钮');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (15, 'showDocument', '是否显示文档');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (16, 'announcement', '网站公告');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (17, 'showAnnouncement', '是否显示网站公告');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`, `REMARK`) VALUES (18, 'layout', '页面布局');
INSERT INTO SYSTEM_CONFIG (`ID`, `k`,`VALUE`,`REMARK`) VALUES (19, 'root', '/home/application/zfile/data','本地存储根路径');
-- INSERT INTO SYSTEM_CONFIG (`ID`, `k`,`VALUE`,`REMARK`) VALUES (19, 'root', 'd:/zfile','本地存储根路径');
INSERT INTO SYS_ROLE (`ID`,`ROLE`) VALUES (1, 'ROLE_ADMIN');
INSERT INTO SYS_ROLE (`ID`,`ROLE`) VALUES (2, 'ROLE_USER');
INSERT INTO SYS_USER_ROLE (`ID`,`rid`,`uid`) VALUES (1, 1, 1);
INSERT INTO SYS_USER_ROLE (`ID`,`rid`,`uid`) VALUES (2, 2, 2);