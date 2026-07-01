-- ============================================================================
-- EXECUTION ORDER NOTE:
-- V994 sets product names/descriptions to CHINESE (e.g. '华为 Mate 60 Pro').
-- V995__fix_encoding.sql runs AFTER this and overwrites names with ENGLISH/
-- pinyin versions (e.g. 'Huawei Mate 60 Pro'), which is the canonical form.
-- If your DB is already seeded and V995 has been applied, SKIP this migration.
-- If you prefer Chinese product names, run this and skip V995 instead.
-- ============================================================================
SET NAMES utf8mb4;

-- 手机数码
UPDATE product SET name='华为 Mate 60 Pro',brand='华为',description='华为 Mate 60 Pro，麒麟9000S芯片，卫星通话，超可靠玄武架构，XMAGE影像系统。' WHERE id=61 OR (name LIKE '%Mate%60%Pro%' AND id>60);
UPDATE product SET name='小米 14 Ultra',brand='小米',description='小米14 Ultra，徕卡光学Summilux镜头，骁龙8Gen3，小米澎湃OS，专业影像旗舰。' WHERE id=62 OR (name LIKE '%14%Ultra%' AND brand='小米');
UPDATE product SET name='OPPO Find X7 Ultra',brand='OPPO',description='OPPO Find X7 Ultra，双潜望四摄，哈苏大师影像，100W超级闪充。' WHERE id=63 OR (name LIKE '%Find%X7%' AND id>60);
UPDATE product SET name='vivo X100 Pro',brand='vivo',description='vivo X100 Pro，蔡司APO超级长焦，天玑9300，蓝图影像芯片V3。' WHERE id=64 OR (name LIKE '%X100%Pro%' AND id>60);
UPDATE product SET name='一加 12',brand='一加',description='一加12，哈苏全焦段超光影影像，骁龙8Gen3，2K东方屏。' WHERE id=65 OR (name LIKE '%OnePlus%12%' AND id>60);

-- 电脑办公
UPDATE product SET name='MacBook Pro 14 M3',brand='Apple',description='MacBook Pro 14英寸 M3芯片，Liquid Retina XDR显示屏，18小时续航。' WHERE id=66 OR (name LIKE '%MacBook%Pro%14%' AND id>60);
UPDATE product SET name='ThinkPad X1 Carbon',brand='联想',description='ThinkPad X1 Carbon Gen12，英特尔酷睿Ultra 7，14英寸2.8K OLED屏。' WHERE id=67 OR (name LIKE '%ThinkPad%X1%' AND id>60);
UPDATE product SET name='iPad Air M2',brand='Apple',description='iPad Air M2芯片，11英寸Liquid Retina显示屏，支持Apple Pencil Pro。' WHERE id=68 OR (name LIKE '%iPad%Air%M2%' AND id>60);
UPDATE product SET name='华为 MatePad Pro 13.2',brand='华为',description='华为MatePad Pro 13.2英寸，柔性OLED屏，星闪连接，鸿蒙生产力平板。' WHERE id=69 OR (name LIKE '%MatePad%Pro%13%' AND id>60);
UPDATE product SET name='戴尔 XPS 16',brand='戴尔',description='戴尔 XPS 16，英特尔酷睿Ultra 9，16.3英寸4K OLED触控屏。' WHERE id=70 OR (name LIKE '%XPS%16%' AND id>60);

-- 家用电器
UPDATE product SET name='海尔 精华洗滚筒洗衣机',brand='海尔',description='海尔精华洗系列10kg滚筒洗衣机，直驱变频电机，智能投放，微蒸汽空气洗。' WHERE id=71 OR (name LIKE '%海尔%' AND id>60);
UPDATE product SET name='格力 云佳空调 1.5匹',brand='格力',description='格力云佳1.5匹新一级能效变频冷暖空调，自清洁，18分贝静音。' WHERE id=72 OR (name LIKE '%格力%' AND id>60);
UPDATE product SET name='美的 法式多门冰箱',brand='美的',description='美的508升法式多门冰箱，双变频一级能效，PST+智能净化。' WHERE id=73 OR (name LIKE '%美的%冰箱%' AND id>60);
UPDATE product SET name='戴森 V15 Detect 吸尘器',brand='戴森',description='戴森V15 Detect，激光探测微尘，压电式传感器，整机HEPA过滤。' WHERE id=74 OR (name LIKE '%V15%' AND id>60);
UPDATE product SET name='小熊 空气炸锅 5L',brand='小熊',description='小熊5L大容量空气炸锅，360度热风循环，无油健康烹饪，8大智能菜单。' WHERE id=75 OR (name LIKE '%小熊%' AND id>60);

-- 服饰鞋包
UPDATE product SET name='Nike Air Force 1 经典板鞋',brand='Nike',description='Nike Air Force 1经典复古板鞋，Air缓震，皮革鞋面，百搭潮流。' WHERE id=76 OR (name LIKE '%Air%Force%1%' AND id>60);
UPDATE product SET name='Adidas Ultraboost 24 跑鞋',brand='Adidas',description='Adidas Ultraboost 24跑鞋，Boost中底科技，Primeknit+编织鞋面。' WHERE id=77 OR (name LIKE '%Ultraboost%' AND id>60);
UPDATE product SET name='优衣库 轻薄羽绒服',brand='优衣库',description='优衣库高级轻型羽绒服，750蓬松度，轻薄保暖，可收纳设计。' WHERE id=78 OR (name LIKE '%优衣库%' AND id>60);
UPDATE product SET name='李宁 韦德之道11 篮球鞋',brand='李宁',description='李宁韦德之道11篮球鞋，全掌科技，碳纤维板，专业实战篮球鞋。' WHERE id=79 OR (name LIKE '%韦德%' AND id>60);
UPDATE product SET name='FILA 斐乐 复古运动鞋',brand='FILA',description='FILA斐乐复古运动鞋，90年代经典设计，厚底增高，潮流百搭。' WHERE id=80 OR (name LIKE '%FILA%' AND id>60);

-- 家居日用
UPDATE product SET name='MUJI 超声波香薰机',brand='MUJI',description='MUJI无印良品超声波香薰机，静音设计，2小时自动断电，搭配精油使用。' WHERE id=81 OR (name LIKE '%MUJI%' AND id>60);
UPDATE product SET name='网易严选 天然乳胶枕',brand='网易严选',description='网易严选泰国天然乳胶枕，93%天然乳胶，人体工学设计，透气防螨。' WHERE id=82 OR (name LIKE '%乳胶枕%' AND id>60);
UPDATE product SET name='膳魔师 保温杯 500ml',brand='膳魔师',description='膳魔师JNL系列不锈钢保温杯500ml，6小时保温保冷，一键开盖。' WHERE id=83 OR (name LIKE '%膳魔师%' AND id>60);
UPDATE product SET name='宜家 毕利书柜',brand='IKEA',description='IKEA宜家毕利书柜，经典设计，可调节搁板，多种颜色尺寸可选。' WHERE id=84 OR (name LIKE '%毕利%' AND id>60);
UPDATE product SET name='小米 米家扫拖机器人2',brand='小米',description='米家扫拖机器人2，激光导航，2800Pa吸力，电控水箱，米家APP远程控制。' WHERE id=85 OR (name LIKE '%扫拖%' AND id>60);

-- 食品生鲜
UPDATE product SET name='三只松鼠 坚果大礼包',brand='三只松鼠',description='三只松鼠年货坚果大礼包2026g，12袋精选坚果组合，送礼佳品。' WHERE id=86 OR (name LIKE '%三只松鼠%' AND id>60);
UPDATE product SET name='良品铺子 肉脯礼盒',brand='良品铺子',description='良品铺子甄选肉脯礼盒，猪肉脯加牛肉干加鸭舌组合，高蛋白零食。' WHERE id=87 OR (name LIKE '%良品铺子%' AND id>60);
UPDATE product SET name='褚橙 冰糖橙 5kg',brand='褚橙',description='褚橙云南冰糖橙5kg礼盒装，皮薄汁多，甜度高，新鲜采摘。' WHERE id=88 OR (name LIKE '%冰糖橙%' AND id>60);
UPDATE product SET name='小罐茶 特级龙井',brand='小罐茶',description='小罐茶特级明前龙井绿茶，大师监制，鲜爽甘醇，送礼自饮皆宜。' WHERE id=89 OR (name LIKE '%龙井%' AND id>60);
UPDATE product SET name='蒙牛 特仑苏 纯牛奶',brand='蒙牛',description='蒙牛特仑苏纯牛奶250ml乘16盒，3.6g优质乳蛋白，源自专属牧场。' WHERE id=90 OR (name LIKE '%特仑苏%' AND id>60);

-- 美妆个护
UPDATE product SET name='兰蔻 小黑瓶精华 50ml',brand='兰蔻',description='兰蔻小黑瓶全新肌底精华液50ml，二裂酵母精粹，修护肌肤屏障。' WHERE id=91 OR (name LIKE '%小黑瓶%' AND id>60);
UPDATE product SET name='SK-II 神仙水 230ml',brand='SK-II',description='SK-II护肤精华露神仙水230ml，超过90%天然活酵母精粹PITERA。' WHERE id=92 OR (name LIKE '%神仙水%' AND id>60);
UPDATE product SET name='欧莱雅 玻尿酸面膜',brand='欧莱雅',description='欧莱雅复颜玻尿酸安瓶面膜，三重玻尿酸，一片等于1瓶精华。' WHERE id=93 OR (name LIKE '%玻尿酸面膜%' AND id>60);
UPDATE product SET name='完美日记 小细跟口红',brand='完美日记',description='完美日记小细跟口红，丝绒哑光质感，多色可选，国货之光。' WHERE id=94 OR (name LIKE '%完美日记%' AND id>60);
UPDATE product SET name='戴森 Airwrap 美发器',brand='戴森',description='戴森Airwrap多功能美发器，康达效应，气流造型，不伤发质。' WHERE id=95 OR (name LIKE '%Airwrap%' AND id>60);

-- 运动户外
UPDATE product SET name='迪卡侬 登山背包 30L',brand='迪卡侬',description='迪卡侬Quechua系列30L登山背包，防水耐磨，多隔层设计。' WHERE id=96 OR (name LIKE '%登山背包%' AND id>60);
UPDATE product SET name='YONEX 天斧99 羽毛球拍',brand='YONEX',description='YONEX天斧99羽毛球拍，回转力生成系统，连续强攻，职业选手用拍。' WHERE id=97 OR (name LIKE '%天斧%' AND id>60);
UPDATE product SET name='骆驼 户外冲锋衣',brand='骆驼',description='骆驼户外冲锋衣男女款，防风防水透气，可拆卸内胆，三合一设计。' WHERE id=98 OR (name LIKE '%冲锋衣%' AND id>60);
UPDATE product SET name='Keep 瑜伽垫 加厚防滑',brand='Keep',description='Keep加厚防滑瑜伽垫，10mm厚度，双面防滑纹理，环保TPE材质。' WHERE id=99 OR (name LIKE '%瑜伽垫%' AND id>60);
UPDATE product SET name='小米 手环9 NFC版',brand='小米',description='小米手环9 NFC版，1.62英寸AMOLED屏，全天候心率血氧监测。' WHERE id=100 OR (name LIKE '%手环9%' AND id>60);

-- 汽车用品
UPDATE product SET name='70迈 行车记录仪',brand='小米',description='70迈智能行车记录仪Pro，4K超清，F1.8大光圈，140度广角，ADAS驾驶辅助。' WHERE id=101 OR (name LIKE '%70%' AND id>60);
UPDATE product SET name='倍思 车载手机支架',brand='倍思',description='倍思车载手机支架，重力感应，单手取放，稳固不晃动。' WHERE id=102 OR (name LIKE '%倍思%' AND id>60);
UPDATE product SET name='3M 汽车贴膜 全车套装',brand='3M',description='3M正品汽车隔热防爆膜，全车套装，高隔热高透光，防紫外线99%。' WHERE id=103 OR (name LIKE '%3M%' AND id>60);
