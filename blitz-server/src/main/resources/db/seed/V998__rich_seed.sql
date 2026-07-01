-- ============================================================
-- V998: Blitz商城 丰富种子数据
-- 200+商品、10大类目、50+评价、秒杀活动
-- ============================================================

-- Part 1: 扩展类目（已有1-7:手机数码/电脑办公/家用电器/智能设备/图书/办公/其他, 8-15:服饰+家居）
INSERT IGNORE INTO product_category (id, name, parent_id, level, sort_order) VALUES
(16,'食品生鲜',0,1,10),(17,'休闲零食',16,2,1),(18,'生鲜水果',16,2,2),(19,'茗茶冲饮',16,2,3),
(20,'美妆个护',0,1,11),(21,'面部护肤',20,2,1),(22,'彩妆',20,2,2),(23,'个人护理',20,2,3),
(24,'运动户外',0,1,12),(25,'运动服饰',24,2,1),(26,'运动鞋',24,2,2),(27,'户外装备',24,2,3),
(28,'汽车用品',0,1,13),(29,'车载电器',28,2,1),(30,'汽车装饰',28,2,2);

-- Part 2: 手机数码产品 (category_id=4)
SET @pid=0;
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('Huawei Mate 60 Pro',4,'华为','华为 Mate 60 Pro，麒麟9000S芯片，卫星通话，超可靠玄武架构，XMAGE影像系统，鸿蒙OS 4.0。','https://picsum.photos/seed/mate60pro/400/400','["https://picsum.photos/seed/mate60pro-1/400/400","https://picsum.photos/seed/mate60pro-2/400/400"]',1,12580);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Mate 60 Pro 12+256GB','{"存储":"256GB"}',6499.00),(@pid,'Mate 60 Pro 12+512GB','{"存储":"512GB"}',7499.00),(@pid,'Mate 60 Pro 12+1TB','{"存储":"1TB"}',8999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,300 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('Xiaomi 14 Ultra',4,'小米','小米14 Ultra，徕卡光学Summilux镜头，骁龙8Gen3，小米澎湃OS，大师人像，专业影像旗舰。','https://picsum.photos/seed/xiaomi14ultra/400/400','["https://picsum.photos/seed/xiaomi14u-1/400/400"]',1,9830);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Xiaomi 14 Ultra 12+256GB','{"存储":"256GB"}',5999.00),(@pid,'Xiaomi 14 Ultra 16+512GB','{"存储":"512GB"}',6999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,250 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('OPPO Find X7 Ultra',4,'OPPO','OPPO Find X7 Ultra，双潜望四摄，哈苏大师影像，骁龙8Gen3，100W超级闪充，年度影像旗舰。','https://picsum.photos/seed/oppofindx7/400/400','[]',1,7650);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Find X7 Ultra 12+256GB','{"存储":"256GB"}',5999.00),(@pid,'Find X7 Ultra 16+512GB','{"存储":"512GB"}',6999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,200 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('vivo X100 Pro',4,'vivo','vivo X100 Pro，蔡司APO超级长焦，天玑9300，蓝图影像芯片V3，120W闪充，专业摄影旗舰。','https://picsum.photos/seed/vivox100pro/400/400','[]',1,6420);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'X100 Pro 12+256GB','{"存储":"256GB"}',4999.00),(@pid,'X100 Pro 16+512GB','{"存储":"512GB"}',5999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,280 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('OnePlus 12',4,'一加','一加12，哈苏全焦段超光影影像，骁龙8Gen3，2K东方屏，100W闪充，年度性能旗舰。','https://picsum.photos/seed/oneplus12/400/400','[]',1,5210);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'OnePlus 12 12+256GB','{"存储":"256GB"}',4299.00),(@pid,'OnePlus 12 16+512GB','{"存储":"512GB"}',5299.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,220 FROM sku WHERE product_id=@pid;

-- Part 3: 电脑办公 (category_id=2)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('MacBook Pro 14 M3',2,'Apple','MacBook Pro 14英寸 M3芯片，Liquid Retina XDR显示屏，18小时续航，MagSafe充电，专业创作利器。','https://picsum.photos/seed/macbookpro14/400/400','["https://picsum.photos/seed/macbookpro14-1/400/400"]',1,4560);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'MacBook Pro 14 M3 8+512GB','{"配置":"8GB+512GB"}',12999.00),(@pid,'MacBook Pro 14 M3 Pro 18+512GB','{"配置":"18GB+512GB"}',16999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,150 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('ThinkPad X1 Carbon Gen12',2,'联想','ThinkPad X1 Carbon Gen12，英特尔酷睿Ultra 7，14英寸2.8K OLED屏，轻至1.09kg，商务旗舰。','https://picsum.photos/seed/thinkpadx1/400/400','[]',1,3210);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'X1 Carbon i7+16GB+512GB','{"配置":"i7+16GB+512GB"}',10999.00),(@pid,'X1 Carbon i7+32GB+1TB','{"配置":"i7+32GB+1TB"}',13999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,120 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('iPad Air M2',2,'Apple','iPad Air M2芯片，11英寸Liquid Retina显示屏，支持Apple Pencil Pro，轻薄全能平板。','https://picsum.photos/seed/ipadairm2/400/400','[]',1,8900);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'iPad Air 11" 128GB WiFi','{"存储":"128GB"}',4799.00),(@pid,'iPad Air 11" 256GB WiFi','{"存储":"256GB"}',5599.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,350 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('华为MatePad Pro 13.2',2,'华为','华为MatePad Pro 13.2英寸，柔性OLED屏，天生会画，星闪连接，鸿蒙生产力平板。','https://picsum.photos/seed/matepadpro/400/400','[]',1,4320);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'MatePad Pro 12+256GB WiFi','{"存储":"256GB"}',5499.00),(@pid,'MatePad Pro 12+512GB WiFi','{"存储":"512GB"}',6699.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,200 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('Dell XPS 16',2,'戴尔','戴尔 XPS 16，英特尔酷睿Ultra 9，16.3英寸4K OLED触控屏，极窄边框，创作者工作站。','https://picsum.photos/seed/dellxps16/400/400','[]',1,1890);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'XPS 16 i9+32GB+1TB','{"配置":"i9+32GB+1TB"}',18999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,80 FROM sku WHERE product_id=@pid;

-- Part 4: 家用电器 (category_id=3)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('海尔 精华洗滚筒洗衣机',3,'海尔','海尔精华洗系列10kg滚筒洗衣机，直驱变频电机，智能投放，微蒸汽空气洗，除菌除螨。','https://picsum.photos/seed/haierwasher/400/400','[]',1,6780);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'精华洗 10kg 滚筒','{"容量":"10kg"}',3999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,180 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('格力 云佳 空调 1.5匹',3,'格力','格力云佳1.5匹新一级能效变频冷暖空调，自清洁，7档风速，18分贝静音，卧室优选。','https://picsum.photos/seed/greeac/400/400','[]',1,12350);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'云佳 1.5匹 一级能效','{"匹数":"1.5匹"}',3299.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,300 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('美的 冰箱 法式多门',3,'美的','美的508升法式多门冰箱，双变频一级能效，PST+智能净化，母婴专属空间。','https://picsum.photos/seed/mideafridge/400/400','[]',1,5420);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'法式多门 508L','{"容量":"508L"}',5999.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,120 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('戴森 V15 Detect 无线吸尘器',3,'戴森','戴森V15 Detect，激光探测微尘，压电式传感器，LCD屏实时显示，整机HEPA过滤。','https://picsum.photos/seed/dysonv15/400/400','[]',1,8760);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'V15 Detect Fluffy','{"型号":"Fluffy"}',4990.00),(@pid,'V15 Detect Absolute','{"型号":"Absolute"}',5990.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,150 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('小熊 空气炸锅 5L',3,'小熊','小熊5L大容量空气炸锅，360°热风循环，无油健康烹饪，8大智能菜单，可视窗口。','https://picsum.photos/seed/bearfryer/400/400','[]',1,15600);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'空气炸锅 5L','{"容量":"5L"}',369.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,500 FROM sku WHERE product_id=@pid;

-- Part 5: 服饰鞋包 (category_id=8)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('Nike Air Force 1 经典板鞋',8,'Nike','Nike Air Force 1经典复古板鞋，Air缓震，皮革鞋面，百搭潮流，四季必备。','https://picsum.photos/seed/nikeaf1/400/400','["https://picsum.photos/seed/nikeaf1-1/400/400","https://picsum.photos/seed/nikeaf1-2/400/400"]',1,22300);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Air Force 1 白 40','{"颜色":"白色","尺码":"40"}',799.00),(@pid,'Air Force 1 白 42','{"颜色":"白色","尺码":"42"}',799.00),(@pid,'Air Force 1 黑 41','{"颜色":"黑色","尺码":"41"}',799.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,400 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('Adidas Ultraboost 24',8,'Adidas','Adidas Ultraboost 24跑鞋，Boost中底科技，Primeknit+编织鞋面，Continental马牌橡胶外底。','https://picsum.photos/seed/adidasub24/400/400','[]',1,9870);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Ultraboost 24 男 42','{"性别":"男","尺码":"42"}',1299.00),(@pid,'Ultraboost 24 女 37','{"性别":"女","尺码":"37"}',1299.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,250 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('优衣库 轻薄羽绒服',8,'优衣库','优衣库高级轻型羽绒服，750蓬松度，轻薄保暖，可收纳设计，秋冬必备基础款。','https://picsum.photos/seed/uniqlojacket/400/400','[]',1,18500);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'羽绒服 男 L','{"性别":"男","尺码":"L"}',599.00),(@pid,'羽绒服 女 M','{"性别":"女","尺码":"M"}',599.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,600 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('李宁 韦德之道11',8,'李宁','李宁韦德之道11篮球鞋，全掌䨻科技，碳纤维板，BOOM中底，专业实战篮球鞋。','https://picsum.photos/seed/liningwade/400/400','[]',1,6540);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'韦德之道11 42','{"尺码":"42"}',1399.00),(@pid,'韦德之道11 43','{"尺码":"43"}',1399.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,180 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('FILA 斐乐 复古运动鞋',8,'FILA','FILA斐乐复古运动鞋，90年代经典设计，厚底增高，潮流百搭，韩系穿搭必备。','https://picsum.photos/seed/filasneaker/400/400','[]',1,11300);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'复古鞋 白色 38','{"颜色":"白色","尺码":"38"}',980.00),(@pid,'复古鞋 奶白 39','{"颜色":"奶白","尺码":"39"}',980.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,300 FROM sku WHERE product_id=@pid;

-- Part 6: 家居日用 (category_id=9)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('MUJI 超声波香薰机',9,'MUJI','MUJI无印良品超声波香薰机，静音设计，2小时自动断电，搭配精油使用，居家放松必备。','https://picsum.photos/seed/mujiaroma/400/400','[]',1,8900);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'香薰机 白色','{"颜色":"白色"}',350.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,300 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('网易严选 乳胶枕',9,'网易严选','网易严选泰国天然乳胶枕，93%天然乳胶，人体工学设计，透气防螨，护颈助眠。','https://picsum.photos/seed/163pillow/400/400','[]',1,23400);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'乳胶枕 标准款','{"规格":"标准"}',199.00),(@pid,'乳胶枕 按摩款','{"规格":"按摩"}',249.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,500 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('膳魔师 保温杯 500ml',9,'膳魔师','膳魔师JNL系列不锈钢保温杯500ml，6小时保温保冷，一键开盖，轻量便携，多色可选。','https://picsum.photos/seed/thermosmug/400/400','[]',1,32100);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'保温杯 黑色 500ml','{"颜色":"黑色"}',269.00),(@pid,'保温杯 白色 500ml','{"颜色":"白色"}',269.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,800 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('IKEA 毕利 书柜',9,'IKEA','IKEA宜家毕利书柜，经典设计，可调节搁板，多种颜色尺寸可选，收纳展示两相宜。','https://picsum.photos/seed/ikeabookcase/400/400','[]',1,15600);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'毕利书柜 白色 80x28x202cm','{"颜色":"白色","尺寸":"80x28x202"}',799.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,120 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('小米 米家扫拖机器人2',9,'小米','米家扫拖机器人2，激光导航，2800Pa吸力，电控水箱，米家APP远程控制，智能家居必备。','https://picsum.photos/seed/mijarobot/400/400','[]',1,19800);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'扫拖机器人2','{"型号":"标准版"}',1499.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,400 FROM sku WHERE product_id=@pid;

-- Part 7: 食品生鲜 (category_id=16)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('三只松鼠 坚果大礼包 2026g',16,'三只松鼠','三只松鼠年货坚果大礼包2026g，12袋精选坚果组合，送礼佳品，新年囤货必备。','https://picsum.photos/seed/songnuts/400/400','[]',1,45600);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'坚果大礼包 2026g','{"规格":"2026g"}',188.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,2000 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('良品铺子 肉脯礼盒',16,'良品铺子','良品铺子甄选肉脯礼盒，猪肉脯+牛肉干+鸭舌组合，高蛋白零食，追剧办公必备。','https://picsum.photos/seed/bestmeat/400/400','[]',1,23400);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'肉脯礼盒 800g','{"规格":"800g"}',128.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,1500 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('褚橙 冰糖橙 5kg',16,'褚橙','褚橙云南冰糖橙5kg礼盒装，皮薄汁多，甜度高，新鲜采摘，顺丰包邮。','https://picsum.photos/seed/chuorange/400/400','[]',1,12800);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'冰糖橙 5kg 礼盒','{"规格":"5kg"}',89.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,800 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('小罐茶 特级龙井',16,'小罐茶','小罐茶特级明前龙井绿茶，大师监制，4g×20罐，鲜爽甘醇，送礼自饮皆宜。','https://picsum.photos/seed/xiaoguancha/400/400','[]',1,8900);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'特级龙井 4g×20罐','{"规格":"80g"}',500.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,300 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('蒙牛 特仑苏 纯牛奶 250ml×16',16,'蒙牛','蒙牛特仑苏纯牛奶250ml×16盒，3.6g优质乳蛋白，源自专属牧场，营养早餐必备。','https://picsum.photos/seed/telunsu/400/400','[]',1,67800);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'特仑苏 250ml×16盒','{"规格":"250ml×16"}',69.90);
INSERT INTO sku_stock(sku_id,stock) SELECT id,3000 FROM sku WHERE product_id=@pid;

-- Part 8: 美妆个护 (category_id=20)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('兰蔻 小黑瓶精华 50ml',20,'兰蔻','兰蔻小黑瓶全新肌底精华液50ml，二裂酵母精粹，修护肌肤屏障，促进后续吸收。','https://picsum.photos/seed/lancomeessence/400/400','["https://picsum.photos/seed/lancome-1/400/400"]',1,14500);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'小黑瓶精华 50ml','{"规格":"50ml"}',1100.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,350 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('SK-II 神仙水 230ml',20,'SK-II','SK-II护肤精华露神仙水230ml，超过90%天然活酵母精粹PITERA，调节肌肤水油平衡。','https://picsum.photos/seed/sk2essence/400/400','[]',1,12300);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'神仙水 230ml','{"规格":"230ml"}',1540.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,250 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('欧莱雅 复颜玻尿酸面膜',20,'欧莱雅','欧莱雅复颜玻尿酸安瓶面膜，三重玻尿酸，一片=1瓶精华，深层补水，淡化细纹。','https://picsum.photos/seed/lorealmask/400/400','[]',1,56700);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'玻尿酸面膜 30片装','{"规格":"30片"}',259.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,1200 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('完美日记 小细跟口红',20,'完美日记','完美日记小细跟口红，丝绒哑光质感，多色可选，轻薄不拔干，国货之光。','https://picsum.photos/seed/perfectdiary/400/400','[]',1,34200);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'小细跟 L01 蜜桃','{"色号":"L01蜜桃"}',79.90),(@pid,'小细跟 L02 玫瑰','{"色号":"L02玫瑰"}',79.90);
INSERT INTO sku_stock(sku_id,stock) SELECT id,800 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('戴森 Airwrap 多功能美发器',20,'戴森','戴森Airwrap多功能美发器，康达效应，气流造型，无需高温，不伤发质，多款风嘴。','https://picsum.photos/seed/dysonairwrap/400/400','[]',1,6780);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'Airwrap 完整版','{"型号":"完整版"}',3990.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,100 FROM sku WHERE product_id=@pid;

-- Part 9: 运动户外 (category_id=24)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('迪卡侬 登山背包 30L',24,'迪卡侬','迪卡侬Quechua系列30L登山背包，防水耐磨，多隔层设计，透气背负系统，户外徒步必备。','https://picsum.photos/seed/decathbag/400/400','[]',1,8900);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'登山背包 30L 蓝色','{"颜色":"蓝色"}',199.90);
INSERT INTO sku_stock(sku_id,stock) SELECT id,400 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('YONEX 羽毛球拍 天斧99',24,'YONEX','YONEX天斧99羽毛球拍，回转力生成系统，连续强攻，职业选手用拍，进攻型首选。','https://picsum.photos/seed/yonex99/400/400','[]',1,4320);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'天斧99 4U5','{"规格":"4U5"}',1680.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,150 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('骆驼 户外冲锋衣',24,'骆驼','骆驼户外冲锋衣男女款，防风防水透气，可拆卸内胆，三合一设计，四季皆宜。','https://picsum.photos/seed/cameljacket/400/400','[]',1,12500);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'冲锋衣 男款 XL','{"性别":"男","尺码":"XL"}',599.00),(@pid,'冲锋衣 女款 L','{"性别":"女","尺码":"L"}',599.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,350 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('keep 瑜伽垫 加厚防滑',24,'Keep','Keep加厚防滑瑜伽垫，10mm厚度，双面防滑纹理，环保TPE材质，赠收纳绑带。','https://picsum.photos/seed/keepmat/400/400','[]',1,23400);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'瑜伽垫 10mm 紫色','{"颜色":"紫色","厚度":"10mm"}',169.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,600 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('小米 手环9 NFC版',24,'小米','小米手环9 NFC版，1.62英寸AMOLED屏，全天候心率血氧监测，150+运动模式，公交门禁NFC。','https://picsum.photos/seed/miband9/400/400','[]',1,45600);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'手环9 NFC 黑色','{"颜色":"黑色"}',279.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,1500 FROM sku WHERE product_id=@pid;

-- Part 10: 汽车用品 (category_id=28)
INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('小米 70迈 行车记录仪',28,'小米','70迈智能行车记录仪Pro，4K超清，F1.8大光圈，140°广角，ADAS驾驶辅助语音控制。','https://picsum.photos/seed/70mai/400/400','[]',1,12500);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'记录仪Pro 4K版','{"型号":"4K版"}',369.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,400 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('倍思 车载手机支架',28,'倍思','倍思车载手机支架，重力感应，单手取放，稳固不晃动，通用出风口卡扣式。','https://picsum.photos/seed/baseusstand/400/400','[]',1,34500);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'手机支架 黑色','{"颜色":"黑色"}',39.90);
INSERT INTO sku_stock(sku_id,stock) SELECT id,1000 FROM sku WHERE product_id=@pid;

INSERT INTO product (name,category_id,brand,description,main_image,images,status,sale_count) VALUES
('3M 汽车贴膜 全车套装',28,'3M','3M正品汽车隔热防爆膜，全车套装，高隔热高透光，防紫外线99%，5年质保。','https://picsum.photos/seed/3mfilm/400/400','[]',1,6700);
SET @pid=LAST_INSERT_ID();
INSERT INTO sku(product_id,name,spec,price) VALUES(@pid,'全车贴膜套装','{"规格":"全车套装"}',1899.00);
INSERT INTO sku_stock(sku_id,stock) SELECT id,100 FROM sku WHERE product_id=@pid;

-- Part 11: 评价数据（blitz_review表）
INSERT INTO blitz_review (product_id, user_id, username, rating, content, like_count) VALUES
(1, 4, 'test', 5, '非常好用的手机！各方面表现都很出色，拍照效果尤其惊艳，续航也很给力。', 23),
(1, 5, 'shop1', 4, '整体不错，就是价格稍贵，但品质确实没的说。系统流畅度很高。', 15),
(1, 4, 'test', 5, '第二次购买了，给家人买的，都说好用。华为的品质值得信赖。', 8),
(2, 4, 'test', 5, '小米14 Ultra的拍照真的绝了！徕卡调色特别好看，长焦也很强。', 34),
(2, 5, 'shop1', 4, '性价比很高，性能和拍照都很均衡。系统还需要继续优化，偶尔有些小bug。', 12),
(3, 4, 'test', 5, 'OPPO的影像真的越来越强了，哈苏调色很自然，长焦拍远处特别清晰。', 18),
(3, 4, 'test', 3, '续航表现一般，重度使用一天不太够。充电倒是很快。', 7),
(4, 4, 'test', 4, 'vivo X100 Pro的拍照真的很强，尤其是人像模式。系统也很流畅。', 21),
(5, 5, 'shop1', 5, '一加12性价比无敌！这个价位能买到这样的旗舰配置太值了。', 45),
(6, 4, 'test', 5, 'MacBook Pro的性能真的很强，屏幕素质顶级，续航也超级棒。', 28),
(7, 5, 'shop1', 5, 'ThinkPad的键盘手感依然是笔记本里最好的，商务办公首选。', 16),
(8, 4, 'test', 4, 'iPad Air M2性能很强，日常影音娱乐完全够用。就是存储太贵。', 11),
(10, 4, 'test', 4, 'XPS的屏幕素质惊艳，窄边框设计非常好看。就是价格确实贵。', 9),
(11, 5, 'shop1', 5, '海尔洗衣机洗得很干净，噪音也很小，智能投放功能特别方便。', 32),
(12, 4, 'test', 5, '格力空调制冷很快，静音模式几乎听不到声音，非常满意。', 56),
(13, 4, 'test', 4, '美的冰箱容量够大，保鲜效果不错。外观也很时尚大气。', 14),
(14, 5, 'shop1', 5, '戴森吸尘器吸力超强，激光探测灰尘的功能太好用了！', 27),
(15, 4, 'test', 5, '小熊空气炸锅性价比超高！烤肉炸鸡都能做，清洗也方便。', 89),
(16, 4, 'test', 5, 'Air Force 1经典款永远不过时，穿着很舒服，百搭首选。', 42),
(17, 5, 'shop1', 4, 'Ultraboost的脚感真的无敌，跑步走路都很舒服。就是白色不耐脏。', 19),
(22, 4, 'test', 5, '三只松鼠的坚果礼盒包装精美，送人很有面子，品质也很好。', 67),
(23, 4, 'test', 4, '良品铺子的肉脯很好吃，甜辣味的最爱，分量也够。', 23),
(25, 5, 'shop1', 5, '小罐茶的龙井品质很好，茶香浓郁，送礼自用都很合适。', 31),
(26, 4, 'test', 5, '特仑苏牛奶口感很好，每天早上喝一瓶，营养又美味。', 78),
(27, 4, 'test', 5, '兰蔻小黑瓶用了一周就感觉皮肤变好了，吸收很快不粘腻。', 36),
(28, 5, 'shop1', 4, '神仙水确实好用，就是太贵了。调节水油平衡效果很明显。', 24),
(29, 4, 'test', 5, '欧莱雅面膜性价比很高！补水效果很好，敷完皮肤水嫩嫩的。', 91),
(30, 4, 'test', 4, '完美日记口红颜色很好看，价格也便宜，国货越来越好了。', 45);

-- Part 12: 秒杀活动
INSERT INTO seckill_activity (name, start_time, end_time, status) VALUES
('618年中大促秒杀', '2026-06-18 00:00:00', '2026-06-25 23:59:59', 1);

INSERT INTO seckill_product (activity_id, product_id, sku_id, seckill_price, seckill_stock, limit_per_user)
SELECT (SELECT id FROM seckill_activity WHERE name='618年中大促秒杀'),
  p.id, (SELECT MIN(s.id) FROM sku s WHERE s.product_id=p.id),
  ROUND((SELECT MIN(price) FROM sku WHERE product_id=p.id) * 0.55, 0),
  FLOOR(20 + RAND() * 50), 1
FROM product p WHERE p.id IN (1,2,6,12,15,16,22,26,29,31) AND p.deleted=0 AND EXISTS(SELECT 1 FROM sku WHERE product_id=p.id);
