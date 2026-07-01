-- ============================================================================
-- EXECUTION ORDER NOTE:
-- V995 sets product names to ENGLISH/pinyin (canonical form, e.g. 'Huawei
-- Mate 60 Pro') and overwrites V994__utf8_fix.sql's Chinese names.
-- V998__rich_seed.sql runs AFTER this and further enriches the same products.
-- WARNING: The comment below says "Only update products added by V998" but
-- V998 runs after V995 in the Flyway chain. The id>60 guard is still safe
-- because V999__seed_data.sql (which inserts all base products) runs even
-- later — so products with id>60 may not exist yet when this migration runs,
-- causing zero-row updates for those WHERE clauses.
-- If your DB is fully seeded (V999 applied), this migration works correctly.
-- If images are already set via V988__svg_placeholders.sql, the image-related
-- portions of V990 and V996 are redundant no-ops.
-- ============================================================================
--
-- Fix: update product names/descriptions that were garbled during seed data import
-- Only update products added by V998 (id > 60 or specific checks)

-- 手机数码
UPDATE product SET name='Huawei Mate 60 Pro',brand='华为',description='华为 Mate 60 Pro，麒麟9000S芯片，卫星通话，超可靠玄武架构，XMAGE影像系统。' WHERE name LIKE '%Mate%60%' OR (name LIKE '%Huawei%' AND id > 60);
UPDATE product SET name='Xiaomi 14 Ultra',brand='小米',description='小米14 Ultra，徕卡光学Summilux镜头，骁龙8Gen3，小米澎湃OS，专业影像旗舰。' WHERE name LIKE '%Xiaomi%14%Ultra%' OR (name LIKE '%14%Ultra%' AND brand='小米');
UPDATE product SET name='OPPO Find X7 Ultra',brand='OPPO',description='OPPO Find X7 Ultra，双潜望四摄，哈苏大师影像，100W超级闪充。' WHERE name LIKE '%Find%X7%' OR (brand='OPPO' AND id>60);
UPDATE product SET name='vivo X100 Pro',brand='vivo',description='vivo X100 Pro，蔡司APO超级长焦，天玑9300，蓝图影像芯片V3。' WHERE name LIKE '%X100%Pro%' OR (brand='vivo' AND id>60);
UPDATE product SET name='OnePlus 12',brand='一加',description='一加12，哈苏全焦段超光影影像，骁龙8Gen3，2K东方屏。' WHERE name LIKE '%OnePlus%12%';

-- 电脑办公
UPDATE product SET name='MacBook Pro 14 M3',brand='Apple',description='MacBook Pro 14英寸 M3芯片，Liquid Retina XDR显示屏，18小时续航。' WHERE name LIKE '%MacBook%Pro%14%';
UPDATE product SET name='ThinkPad X1 Carbon Gen12',brand='联想',description='ThinkPad X1 Carbon Gen12，英特尔酷睿Ultra 7，14英寸2.8K OLED屏。' WHERE name LIKE '%ThinkPad%X1%';
UPDATE product SET name='iPad Air M2',brand='Apple',description='iPad Air M2芯片，11英寸Liquid Retina显示屏，支持Apple Pencil Pro。' WHERE name LIKE '%iPad%Air%M2%';
UPDATE product SET name='Huawei MatePad Pro 13.2',brand='华为',description='华为MatePad Pro 13.2英寸，柔性OLED屏，星闪连接，鸿蒙生产力平板。' WHERE name LIKE '%MatePad%Pro%13%';
UPDATE product SET name='Dell XPS 16',brand='戴尔',description='戴尔 XPS 16，英特尔酷睿Ultra 9，16.3英寸4K OLED触控屏。' WHERE name LIKE '%Dell%XPS%16%' OR name LIKE '%XPS%16%';

-- 家用电器
UPDATE product SET name='Haier 精华洗滚筒洗衣机',brand='海尔',description='海尔精华洗系列10kg滚筒洗衣机，直驱变频电机，智能投放，微蒸汽空气洗。' WHERE name LIKE '%海尔%' OR (name LIKE '%Haier%' AND id>60);
UPDATE product SET name='Gree 云佳空调 1.5匹',brand='格力',description='格力云佳1.5匹新一级能效变频冷暖空调，自清洁，18分贝静音。' WHERE name LIKE '%格力%' OR (name LIKE '%Gree%' AND id>60);
UPDATE product SET name='Midea 冰箱 法式多门',brand='美的',description='美的508升法式多门冰箱，双变频一级能效，PST+智能净化。' WHERE name LIKE '%美的%冰箱%' OR (name LIKE '%Midea%' AND id>60);
UPDATE product SET name='Dyson V15 Detect 吸尘器',brand='戴森',description='戴森V15 Detect，激光探测微尘，压电式传感器，整机HEPA过滤。' WHERE name LIKE '%Dyson%V15%' OR (name LIKE '%戴森%V15%');
UPDATE product SET name='Bear 空气炸锅 5L',brand='小熊',description='小熊5L大容量空气炸锅，360度热风循环，无油健康烹饪，8大智能菜单。' WHERE name LIKE '%小熊%空气%' OR (name LIKE '%Bear%' AND id>60);

-- 服饰鞋包 (all with id > 60)
UPDATE product SET name='Nike Air Force 1',brand='Nike',description='Nike Air Force 1经典复古板鞋，Air缓震，皮革鞋面，百搭潮流。' WHERE name LIKE '%Nike%Air%Force%' OR (brand='Nike' AND id>60);
UPDATE product SET name='Adidas Ultraboost 24',brand='Adidas',description='Adidas Ultraboost 24跑鞋，Boost中底科技，Primeknit+编织鞋面。' WHERE name LIKE '%Adidas%Ultra%' OR (brand='Adidas' AND id>60);
UPDATE product SET name='UNIQLO 轻薄羽绒服',brand='优衣库',description='优衣库高级轻型羽绒服，750蓬松度，轻薄保暖，可收纳设计。' WHERE name LIKE '%优衣库%' OR name LIKE '%UNIQLO%';
UPDATE product SET name='Li-Ning 韦德之道11',brand='李宁',description='李宁韦德之道11篮球鞋，全掌科技，碳纤维板，专业实战篮球鞋。' WHERE name LIKE '%李宁%' OR name LIKE '%Li-Ning%' OR name LIKE '%韦德%';
UPDATE product SET name='FILA 复古运动鞋',brand='FILA',description='FILA斐乐复古运动鞋，90年代经典设计，厚底增高，潮流百搭。' WHERE name LIKE '%FILA%' OR name LIKE '%斐乐%';

-- 家居日用
UPDATE product SET name='MUJI 超声波香薰机',brand='MUJI',description='MUJI无印良品超声波香薰机，静音设计，2小时自动断电，搭配精油使用。' WHERE name LIKE '%MUJI%' OR (name LIKE '%香薰%' AND id>60);
UPDATE product SET name='NetEase 乳胶枕',brand='网易严选',description='网易严选泰国天然乳胶枕，93%天然乳胶，人体工学设计，透气防螨。' WHERE name LIKE '%网易%' OR name LIKE '%乳胶枕%';
UPDATE product SET name='Thermos 保温杯 500ml',brand='膳魔师',description='膳魔师JNL系列不锈钢保温杯500ml，6小时保温保冷，一键开盖。' WHERE name LIKE '%膳魔师%' OR name LIKE '%Thermos%';
UPDATE product SET name='IKEA Billy 书柜',brand='IKEA',description='IKEA宜家毕利书柜，经典设计，可调节搁板，多种颜色尺寸可选。' WHERE name LIKE '%IKEA%' OR name LIKE '%宜家%' OR name LIKE '%毕利%';
UPDATE product SET name='Xiaomi 扫拖机器人2',brand='小米',description='米家扫拖机器人2，激光导航，2800Pa吸力，电控水箱，米家APP远程控制。' WHERE name LIKE '%扫拖机器人%';

-- 食品生鲜
UPDATE product SET name='Three Squirrels 坚果大礼包',brand='三只松鼠',description='三只松鼠年货坚果大礼包2026g，12袋精选坚果组合，送礼佳品。' WHERE name LIKE '%三只松鼠%' OR name LIKE '%Three%Squirrel%';
UPDATE product SET name='Bestore 肉脯礼盒',brand='良品铺子',description='良品铺子甄选肉脯礼盒，猪肉脯加牛肉干加鸭舌组合，高蛋白零食。' WHERE name LIKE '%良品铺子%' OR name LIKE '%Bestore%';
UPDATE product SET name='Chu Orange 冰糖橙 5kg',brand='褚橙',description='褚橙云南冰糖橙5kg礼盒装，皮薄汁多，甜度高，新鲜采摘。' WHERE name LIKE '%褚橙%' OR name LIKE '%冰糖橙%' OR name LIKE '%Chu%Orange%';
UPDATE product SET name='XiaoGuanCha 特级龙井',brand='小罐茶',description='小罐茶特级明前龙井绿茶，大师监制，4g乘20罐，鲜爽甘醇。' WHERE name LIKE '%小罐茶%' OR name LIKE '%龙井%';
UPDATE product SET name='MengNiu 特仑苏 纯牛奶',brand='蒙牛',description='蒙牛特仑苏纯牛奶250ml乘16盒，3.6g优质乳蛋白，源自专属牧场。' WHERE name LIKE '%蒙牛%' OR name LIKE '%特仑苏%';

-- 美妆个护
UPDATE product SET name='Lancome 小黑瓶精华 50ml',brand='兰蔻',description='兰蔻小黑瓶全新肌底精华液50ml，二裂酵母精粹，修护肌肤屏障。' WHERE name LIKE '%Lancome%' OR name LIKE '%兰蔻%' OR name LIKE '%小黑瓶%';
UPDATE product SET name='SK-II 神仙水 230ml',brand='SK-II',description='SK-II护肤精华露神仙水230ml，超过90%天然活酵母精粹PITERA。' WHERE name LIKE '%SK-II%' OR name LIKE '%神仙水%';
UPDATE product SET name='LOreal 玻尿酸面膜',brand='欧莱雅',description='欧莱雅复颜玻尿酸安瓶面膜，三重玻尿酸，一片等于1瓶精华。' WHERE name LIKE '%欧莱雅%' OR name LIKE '%LOreal%' OR name LIKE '%玻尿酸面膜%';
UPDATE product SET name='Perfect Diary 小细跟口红',brand='完美日记',description='完美日记小细跟口红，丝绒哑光质感，多色可选，国货之光。' WHERE name LIKE '%完美日记%' OR name LIKE '%Perfect%Diary%';
UPDATE product SET name='Dyson Airwrap 美发器',brand='戴森',description='戴森Airwrap多功能美发器，康达效应，气流造型，不伤发质。' WHERE name LIKE '%Dyson%Airwrap%' OR name LIKE '%戴森%美发%';

-- 运动户外
UPDATE product SET name='Decathlon 登山背包 30L',brand='迪卡侬',description='迪卡侬Quechua系列30L登山背包，防水耐磨，多隔层设计。' WHERE name LIKE '%迪卡侬%' OR name LIKE '%Decathlon%' OR name LIKE '%登山背包%';
UPDATE product SET name='YONEX 羽毛球拍 天斧99',brand='YONEX',description='YONEX天斧99羽毛球拍，回转力生成系统，连续强攻，职业选手用拍。' WHERE name LIKE '%YONEX%' OR name LIKE '%天斧%';
UPDATE product SET name='Camel 户外冲锋衣',brand='骆驼',description='骆驼户外冲锋衣男女款，防风防水透气，可拆卸内胆，三合一设计。' WHERE name LIKE '%骆驼%' OR name LIKE '%Camel%jacket%' OR (name LIKE '%冲锋衣%' AND id>60);
UPDATE product SET name='Keep 瑜伽垫 加厚防滑',brand='Keep',description='Keep加厚防滑瑜伽垫，10mm厚度，双面防滑纹理，环保TPE材质。' WHERE name LIKE '%Keep%' OR (name LIKE '%瑜伽垫%' AND id>60);
UPDATE product SET name='Xiaomi 手环9 NFC版',brand='小米',description='小米手环9 NFC版，1.62英寸AMOLED屏，全天候心率血氧监测。' WHERE name LIKE '%手环9%' OR (name LIKE '%手环%NFC%' AND brand='小米');

-- 汽车用品
UPDATE product SET name='70Mai 行车记录仪',brand='小米',description='70迈智能行车记录仪Pro，4K超清，F1.8大光圈，140度广角，ADAS驾驶辅助。' WHERE name LIKE '%70%mai%' OR name LIKE '%70迈%';
UPDATE product SET name='Baseus 车载手机支架',brand='倍思',description='倍思车载手机支架，重力感应，单手取放，稳固不晃动。' WHERE name LIKE '%Baseus%' OR name LIKE '%倍思%' OR name LIKE '%手机支架%';
UPDATE product SET name='3M 汽车贴膜 全车套装',brand='3M',description='3M正品汽车隔热防爆膜，全车套装，高隔热高透光，防紫外线99%。' WHERE name LIKE '%3M%' OR name LIKE '%贴膜%';

-- Clean up any remaining garbled data: set reasonable defaults for NULL/empty descriptions
UPDATE product SET description='品质好物，值得信赖' WHERE description IS NULL OR description='' OR description LIKE '%?%' OR description LIKE '%%%' ESCAPE '|';
