-- ============================================================
-- V999: Blitz Mall Comprehensive Seed Data
-- Expands from 5 self-branded products to 54 products across
-- multiple categories and brands, like Taobao/JD.
-- ============================================================

-- ============================================================
-- Part 1: Add new product categories (服饰鞋包 + 家居生活)
-- ============================================================
INSERT INTO product_category (id, name, parent_id, level, sort_order) VALUES
(8,  '服饰鞋包', 0, 1, 8),
(9,  '家居生活', 0, 1, 9),
(10, '男装',     8, 2, 1),
(11, '女装',     8, 2, 2),
(12, '鞋靴',     8, 2, 3),
(13, '厨房用品', 9, 2, 1),
(14, '家居家纺', 9, 2, 2),
(15, '灯具照明', 9, 2, 3);

-- ============================================================
-- Part 2: Update existing Blitz products (ids 1-5)
-- ============================================================
UPDATE product SET brand = 'Xiaomi',  name = 'Xiaomi Civi 4 Pro',             description = 'Xiaomi Civi 4 Pro 轻薄潮流手机，搭载骁龙8s Gen3处理器，莱卡光学影像，前置双柔光灯，自拍旗舰之选。' WHERE id = 1;
UPDATE product SET brand = 'Blitz',   name = 'Blitz Tab Ultra',               description = 'Blitz Tab Ultra 12.4英寸OLED旗舰平板，支持手写笔与键盘，影音办公全能搭档。' WHERE id = 2;
UPDATE product SET brand = 'Huawei',  name = 'Huawei MateBook 14',            description = 'Huawei MateBook 14 轻薄金属机身，2K触控全面屏，第13代酷睿处理器，超级终端智慧互联。' WHERE id = 3;
UPDATE product SET brand = 'Samsung', name = 'Samsung Galaxy Watch7',         description = 'Samsung Galaxy Watch7 智能手表，BioActive传感器健康监测，蓝宝石玻璃镜面， Wear OS生态。' WHERE id = 4;
UPDATE product SET brand = 'Blitz',   name = 'Blitz Phone Z1',                description = 'Blitz Phone Z1 性价比之选，5000mAh大电池，120Hz高刷屏，日常使用流畅不卡顿。' WHERE id = 5;

-- ============================================================
-- Part 3: New products — 智能手机 (category_id=4) — 8 products
-- ============================================================

-- Product 1: Apple iPhone 15 Pro Max
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('iPhone 15 Pro Max', 4, 'Apple',
        'iPhone 15 Pro Max，钛金属设计，A17 Pro芯片，4800万像素主摄，支持USB-C接口与5倍光学变焦，史上最强iPhone。',
        'https://picsum.photos/seed/iphone-15-pro-max/400/400',
        '["https://picsum.photos/seed/iphone-15-pro-max-1/400/400","https://picsum.photos/seed/iphone-15-pro-max-2/400/400","https://picsum.photos/seed/iphone-15-pro-max-3/400/400"]',
        1, 8742);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'iPhone 15 Pro Max 256GB 原色钛金属', '{"颜色":"原色钛金属","存储":"256GB"}', 8999.00),
(@pid, 'iPhone 15 Pro Max 512GB 蓝色钛金属', '{"颜色":"蓝色钛金属","存储":"512GB"}', 10999.00),
(@pid, 'iPhone 15 Pro Max 1TB 黑色钛金属',   '{"颜色":"黑色钛金属","存储":"1TB"}',   12999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock) VALUES
((SELECT id FROM sku WHERE product_id = @pid AND spec LIKE '%256GB%'), 350, 0),
((SELECT id FROM sku WHERE product_id = @pid AND spec LIKE '%512GB%'), 280, 0),
((SELECT id FROM sku WHERE product_id = @pid AND spec LIKE '%1TB%'),   150, 0);

-- Product 2: Samsung Galaxy S24 Ultra
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Samsung Galaxy S24 Ultra', 4, 'Samsung',
        '三星 Galaxy S24 Ultra，钛金属框架，骁龙8 Gen3 For Galaxy处理器，2亿像素主摄，内置S Pen，Galaxy AI智慧体验。',
        'https://picsum.photos/seed/galaxy-s24-ultra/400/400',
        '["https://picsum.photos/seed/galaxy-s24-ultra-1/400/400","https://picsum.photos/seed/galaxy-s24-ultra-2/400/400"]',
        1, 6210);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Galaxy S24 Ultra 12+256GB 钛灰', '{"颜色":"钛灰色","存储":"256GB","内存":"12GB"}', 8999.00),
(@pid, 'Galaxy S24 Ultra 12+512GB 钛黑', '{"颜色":"钛黑色","存储":"512GB","内存":"12GB"}', 10999.00),
(@pid, 'Galaxy S24 Ultra 12+1TB 钛黄',   '{"颜色":"钛黄色","存储":"1TB","内存":"12GB"}',   11999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 300, 0 FROM sku WHERE product_id = @pid;

-- Product 3: Huawei Mate 60 Pro+
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Huawei Mate 60 Pro+', 4, 'Huawei',
        '华为 Mate 60 Pro+，搭载麒麟9000S芯片，卫星通话功能，超可靠玄武架构，昆仑玻璃，XMAGE影像。',
        'https://picsum.photos/seed/huawei-mate-60-pro-plus/400/400',
        '["https://picsum.photos/seed/huawei-mate-60-pro-plus-1/400/400","https://picsum.photos/seed/huawei-mate-60-pro-plus-2/400/400"]',
        1, 10530);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Mate 60 Pro+ 16+512GB 砚黑', '{"颜色":"砚黑","存储":"512GB","内存":"16GB"}', 7999.00),
(@pid, 'Mate 60 Pro+ 16+1TB 宣白',   '{"颜色":"宣白","存储":"1TB","内存":"16GB"}',   8999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 250, 0 FROM sku WHERE product_id = @pid;

-- Product 4: Xiaomi 14 Ultra
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Xiaomi 14 Ultra', 4, 'Xiaomi',
        '小米 14 Ultra，骁龙8 Gen3，Summilux徕卡光学镜头，1英寸大底传感器，大师人像模式，安卓影像新标杆。',
        'https://picsum.photos/seed/xiaomi-14-ultra/400/400',
        '["https://picsum.photos/seed/xiaomi-14-ultra-1/400/400","https://picsum.photos/seed/xiaomi-14-ultra-2/400/400"]',
        1, 7890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Xiaomi 14 Ultra 12+256GB 经典黑', '{"颜色":"经典黑","存储":"256GB","内存":"12GB"}', 5999.00),
(@pid, 'Xiaomi 14 Ultra 16+512GB 龙晶蓝', '{"颜色":"龙晶蓝","存储":"512GB","内存":"16GB"}', 6499.00),
(@pid, 'Xiaomi 14 Ultra 16+1TB 陶瓷白',   '{"颜色":"陶瓷白","存储":"1TB","内存":"16GB"}',   7499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 320, 0 FROM sku WHERE product_id = @pid;

-- Product 5: Apple iPhone 15
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('iPhone 15', 4, 'Apple',
        'iPhone 15，灵动岛设计，A16仿生芯片，4800万像素双摄，USB-C接口，多彩配色，日常使用理想之选。',
        'https://picsum.photos/seed/iphone-15/400/400',
        '["https://picsum.photos/seed/iphone-15-1/400/400","https://picsum.photos/seed/iphone-15-2/400/400"]',
        1, 12350);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'iPhone 15 128GB 粉色', '{"颜色":"粉色","存储":"128GB"}', 5999.00),
(@pid, 'iPhone 15 256GB 蓝色', '{"颜色":"蓝色","存储":"256GB"}', 6999.00),
(@pid, 'iPhone 15 512GB 黑色', '{"颜色":"黑色","存储":"512GB"}', 7999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 400, 0 FROM sku WHERE product_id = @pid;

-- Product 6: Samsung Galaxy Z Flip6
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Samsung Galaxy Z Flip6', 4, 'Samsung',
        '三星 Galaxy Z Flip6，掌心折叠设计，FlexWindow大尺寸外屏，5000万像素主摄，IP48防水，潮流折叠标杆。',
        'https://picsum.photos/seed/galaxy-z-flip6/400/400',
        '["https://picsum.photos/seed/galaxy-z-flip6-1/400/400","https://picsum.photos/seed/galaxy-z-flip6-2/400/400"]',
        1, 4950);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Galaxy Z Flip6 12+256GB 星夜银', '{"颜色":"星夜银","存储":"256GB"}', 7499.00),
(@pid, 'Galaxy Z Flip6 12+512GB 夏沫蓝', '{"颜色":"夏沫蓝","存储":"512GB"}', 8999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 220, 0 FROM sku WHERE product_id = @pid;

-- Product 7: Xiaomi Redmi Note 13 Pro+
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Redmi Note 13 Pro+', 4, 'Xiaomi',
        'Redmi Note 13 Pro+，2亿像素主摄，120W超级快充，1.5K高光屏，IP68防水，中端价格旗舰体验。',
        'https://picsum.photos/seed/redmi-note-13-pro-plus/400/400',
        '["https://picsum.photos/seed/redmi-note-13-pro-plus-1/400/400","https://picsum.photos/seed/redmi-note-13-pro-plus-2/400/400"]',
        1, 15680);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Redmi Note 13 Pro+ 8+256GB 子夜黑',  '{"颜色":"子夜黑","存储":"256GB","内存":"8GB"}',  1999.00),
(@pid, 'Redmi Note 13 Pro+ 12+512GB 时光蓝', '{"颜色":"时光蓝","存储":"512GB","内存":"12GB"}', 2399.00),
(@pid, 'Redmi Note 13 Pro+ 16+512GB 浅梦紫', '{"颜色":"浅梦紫","存储":"512GB","内存":"16GB"}', 2799.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 500, 0 FROM sku WHERE product_id = @pid;

-- Product 8: Huawei Pura 70 Ultra
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Huawei Pura 70 Ultra', 4, 'Huawei',
        '华为 Pura 70 Ultra，超聚光伸缩镜头，1英寸感光元件，北斗卫星图片消息，HarmonyOS 4.2，美学与影像巅峰。',
        'https://picsum.photos/seed/huawei-pura-70-ultra/400/400',
        '["https://picsum.photos/seed/huawei-pura-70-ultra-1/400/400","https://picsum.photos/seed/huawei-pura-70-ultra-2/400/400"]',
        1, 6890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Pura 70 Ultra 16+512GB 香颂绿', '{"颜色":"香颂绿","存储":"512GB","内存":"16GB"}', 7999.00),
(@pid, 'Pura 70 Ultra 16+1TB 摩卡棕',   '{"颜色":"摩卡棕","存储":"1TB","内存":"16GB"}',   9999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 200, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 4: 平板电脑 (category_id=5) — 4 products
-- ============================================================

-- Product 9: Apple iPad Pro M4 12.9"
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('iPad Pro M4 12.9英寸', 5, 'Apple',
        'iPad Pro M4芯片，Ultra Retina XDR显示屏，Apple Pencil Pro支持，轻薄极致性能，创意工作者的终极工具。',
        'https://picsum.photos/seed/ipad-pro-m4/400/400',
        '["https://picsum.photos/seed/ipad-pro-m4-1/400/400","https://picsum.photos/seed/ipad-pro-m4-2/400/400"]',
        1, 4320);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'iPad Pro M4 12.9 256GB WiFi 深空黑', '{"颜色":"深空黑","存储":"256GB","网络":"WiFi"}',   8999.00),
(@pid, 'iPad Pro M4 12.9 512GB WiFi 银色',   '{"颜色":"银色","存储":"512GB","网络":"WiFi"}',   10999.00),
(@pid, 'iPad Pro M4 12.9 1TB 5G 深空黑',     '{"颜色":"深空黑","存储":"1TB","网络":"5G+WiFi"}', 14999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 180, 0 FROM sku WHERE product_id = @pid;

-- Product 10: Samsung Galaxy Tab S9 Ultra
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Samsung Galaxy Tab S9 Ultra', 5, 'Samsung',
        '三星 Galaxy Tab S9 Ultra，14.6英寸Dynamic AMOLED 2X屏，骁龙8 Gen2，IP68防水，内置S Pen。',
        'https://picsum.photos/seed/galaxy-tab-s9-ultra/400/400',
        '["https://picsum.photos/seed/galaxy-tab-s9-ultra-1/400/400","https://picsum.photos/seed/galaxy-tab-s9-ultra-2/400/400"]',
        1, 2150);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Tab S9 Ultra 12+256GB WiFi 石墨灰', '{"颜色":"石墨灰","存储":"256GB","网络":"WiFi"}',   6999.00),
(@pid, 'Tab S9 Ultra 12+512GB WiFi 米色',   '{"颜色":"米色","存储":"512GB","网络":"WiFi"}',     8999.00),
(@pid, 'Tab S9 Ultra 16+1TB 5G 石墨灰',     '{"颜色":"石墨灰","存储":"1TB","网络":"5G+WiFi"}', 10999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 150, 0 FROM sku WHERE product_id = @pid;

-- Product 11: Huawei MatePad Pro 13.2"
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Huawei MatePad Pro 13.2英寸', 5, 'Huawei',
        '华为 MatePad Pro 13.2"，柔性OLED全面屏，星闪手写笔，天生会画，移动办公与创作旗舰平板。',
        'https://picsum.photos/seed/huawei-matepad-pro/400/400',
        '["https://picsum.photos/seed/huawei-matepad-pro-1/400/400","https://picsum.photos/seed/huawei-matepad-pro-2/400/400"]',
        1, 3680);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'MatePad Pro 13.2 12+256GB 星河蓝', '{"颜色":"星河蓝","存储":"256GB","内存":"12GB"}', 4299.00),
(@pid, 'MatePad Pro 13.2 12+512GB 晶钻白', '{"颜色":"晶钻白","存储":"512GB","内存":"12GB"}', 5299.00),
(@pid, 'MatePad Pro 13.2 16+1TB 砚黑',     '{"颜色":"砚黑","存储":"1TB","内存":"16GB"}',     5999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 200, 0 FROM sku WHERE product_id = @pid;

-- Product 12: Xiaomi Pad 6S Pro
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Xiaomi Pad 6S Pro', 5, 'Xiaomi',
        '小米 Pad 6S Pro，12.4英寸3K超清屏幕，骁龙8 Gen2，120W快充，小米澎湃OS，影音娱乐性价比之王。',
        'https://picsum.photos/seed/xiaomi-pad-6s-pro/400/400',
        '["https://picsum.photos/seed/xiaomi-pad-6s-pro-1/400/400","https://picsum.photos/seed/xiaomi-pad-6s-pro-2/400/400"]',
        1, 5480);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Pad 6S Pro 8+128GB 黑色',  '{"颜色":"黑色","存储":"128GB","内存":"8GB"}',  2799.00),
(@pid, 'Pad 6S Pro 8+256GB 远山蓝', '{"颜色":"远山蓝","存储":"256GB","内存":"8GB"}',  3099.00),
(@pid, 'Pad 6S Pro 12+512GB 银色', '{"颜色":"银色","存储":"512GB","内存":"12GB"}', 3799.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 280, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 5: 笔记本电脑 (category_id=6) — 6 products
-- ============================================================

-- Product 13: Apple MacBook Pro 14" M3 Pro
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MacBook Pro 14英寸 M3 Pro', 6, 'Apple',
        'MacBook Pro 14英寸，M3 Pro芯片，Liquid Retina XDR显示屏，18小时续航，专业创作性能怪兽。',
        'https://picsum.photos/seed/macbook-pro-14-m3/400/400',
        '["https://picsum.photos/seed/macbook-pro-14-m3-1/400/400","https://picsum.photos/seed/macbook-pro-14-m3-2/400/400"]',
        1, 3250);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'MacBook Pro 14 M3 Pro 18GB/512GB 深空黑', '{"颜色":"深空黑","芯片":"M3 Pro","内存":"18GB","存储":"512GB"}', 14999.00),
(@pid, 'MacBook Pro 14 M3 Pro 36GB/1TB 银色',     '{"颜色":"银色","芯片":"M3 Pro","内存":"36GB","存储":"1TB"}',     17999.00),
(@pid, 'MacBook Pro 14 M3 Max 36GB/1TB 深空黑',   '{"颜色":"深空黑","芯片":"M3 Max","内存":"36GB","存储":"1TB"}',   19999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 120, 0 FROM sku WHERE product_id = @pid;

-- Product 14: Apple MacBook Air 15" M3
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MacBook Air 15英寸 M3', 6, 'Apple',
        'MacBook Air 15英寸，M3芯片，Liquid Retina显示屏，无风扇设计超静音，超轻薄大屏办公首选。',
        'https://picsum.photos/seed/macbook-air-15-m3/400/400',
        '["https://picsum.photos/seed/macbook-air-15-m3-1/400/400","https://picsum.photos/seed/macbook-air-15-m3-2/400/400"]',
        1, 4890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'MacBook Air 15 M3 8GB/256GB 星光色',  '{"颜色":"星光色","芯片":"M3","内存":"8GB","存储":"256GB"}',  9499.00),
(@pid, 'MacBook Air 15 M3 16GB/512GB 午夜色', '{"颜色":"午夜色","芯片":"M3","内存":"16GB","存储":"512GB"}', 10999.00),
(@pid, 'MacBook Air 15 M3 24GB/1TB 深空灰',   '{"颜色":"深空灰","芯片":"M3","内存":"24GB","存储":"1TB"}',   11999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 160, 0 FROM sku WHERE product_id = @pid;

-- Product 15: Samsung Galaxy Book4 Ultra
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Samsung Galaxy Book4 Ultra', 6, 'Samsung',
        '三星 Galaxy Book4 Ultra，16英寸3K AMOLED触摸屏，酷睿Ultra 9处理器，RTX4070显卡，全场景旗舰创作本。',
        'https://picsum.photos/seed/galaxy-book4-ultra/400/400',
        '["https://picsum.photos/seed/galaxy-book4-ultra-1/400/400","https://picsum.photos/seed/galaxy-book4-ultra-2/400/400"]',
        1, 980);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Book4 Ultra i9/16GB/1TB 月光灰',  '{"颜色":"月光灰","处理器":"Core Ultra 9","内存":"16GB","存储":"1TB","显卡":"RTX 4070"}', 12999.00),
(@pid, 'Book4 Ultra i9/32GB/1TB 月光灰',  '{"颜色":"月光灰","处理器":"Core Ultra 9","内存":"32GB","存储":"1TB","显卡":"RTX 4070"}', 14999.00),
(@pid, 'Book4 Ultra i9/64GB/2TB 月光灰',  '{"颜色":"月光灰","处理器":"Core Ultra 9","内存":"64GB","存储":"2TB","显卡":"RTX 4070"}', 16999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 80, 0 FROM sku WHERE product_id = @pid;

-- Product 16: Huawei MateBook X Pro
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Huawei MateBook X Pro', 6, 'Huawei',
        '华为 MateBook X Pro，14.2英寸3.1K OLED原色全面屏，酷睿Ultra 9，超轻980g，超级终端智慧办公。',
        'https://picsum.photos/seed/huawei-matebook-x-pro/400/400',
        '["https://picsum.photos/seed/huawei-matebook-x-pro-1/400/400","https://picsum.photos/seed/huawei-matebook-x-pro-2/400/400"]',
        1, 2760);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'MateBook X Pro i7/16GB/1TB 砚黑',  '{"颜色":"砚黑","处理器":"Core Ultra 7","内存":"16GB","存储":"1TB"}',  8999.00),
(@pid, 'MateBook X Pro i9/32GB/1TB 宣白',  '{"颜色":"宣白","处理器":"Core Ultra 9","内存":"32GB","存储":"1TB"}', 10999.00),
(@pid, 'MateBook X Pro i9/32GB/2TB 拂晓粉','{"颜色":"拂晓粉","处理器":"Core Ultra 9","内存":"32GB","存储":"2TB"}', 11999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 130, 0 FROM sku WHERE product_id = @pid;

-- Product 17: Xiaomi Book Pro 16
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Xiaomi Book Pro 16', 6, 'Xiaomi',
        '小米 Book Pro 16，16英寸3.1K 165Hz高刷屏，酷睿Ultra旗舰处理器，轻薄与性能的巧妙平衡。',
        'https://picsum.photos/seed/xiaomi-book-pro-16/400/400',
        '["https://picsum.photos/seed/xiaomi-book-pro-16-1/400/400","https://picsum.photos/seed/xiaomi-book-pro-16-2/400/400"]',
        1, 1840);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Book Pro 16 i5/16GB/512GB 星辰银', '{"颜色":"星辰银","处理器":"Core Ultra 5","内存":"16GB","存储":"512GB"}', 6499.00),
(@pid, 'Book Pro 16 i7/16GB/1TB 星辰银',   '{"颜色":"星辰银","处理器":"Core Ultra 7","内存":"16GB","存储":"1TB"}',   7499.00),
(@pid, 'Book Pro 16 i9/32GB/1TB 暗影黑',   '{"颜色":"暗影黑","处理器":"Core Ultra 9","内存":"32GB","存储":"1TB"}',   8499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 140, 0 FROM sku WHERE product_id = @pid;

-- Product 18: Sony VAIO SX14
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Sony VAIO SX14', 6, 'Sony',
        '索尼 VAIO SX14，14英寸4K HDR屏幕，碳纤维机身仅重999g，全日系工艺，商务人士的轻薄旗舰。',
        'https://picsum.photos/seed/sony-vaio-sx14/400/400',
        '["https://picsum.photos/seed/sony-vaio-sx14-1/400/400","https://picsum.photos/seed/sony-vaio-sx14-2/400/400"]',
        1, 650);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'VAIO SX14 i7/16GB/512GB 尊荣黑', '{"颜色":"尊荣黑","处理器":"Core i7","内存":"16GB","存储":"512GB"}', 9999.00),
(@pid, 'VAIO SX14 i7/32GB/1TB 古铜色',   '{"颜色":"古铜色","处理器":"Core i7","内存":"32GB","存储":"1TB"}',   11999.00),
(@pid, 'VAIO SX14 i9/32GB/1TB 限量红',   '{"颜色":"限量红","处理器":"Core i9","内存":"32GB","存储":"1TB"}',   13999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 60, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 6: 智能手表 (category_id=7) — 4 products
-- ============================================================

-- Product 19: Apple Watch Ultra 2
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Apple Watch Ultra 2', 7, 'Apple',
        'Apple Watch Ultra 2，49毫米钛金属表壳，超亮3000尼特屏幕，精准双频GPS，潜水电脑，极限运动最佳搭档。',
        'https://picsum.photos/seed/apple-watch-ultra-2/400/400',
        '["https://picsum.photos/seed/apple-watch-ultra-2-1/400/400","https://picsum.photos/seed/apple-watch-ultra-2-2/400/400"]',
        1, 3560);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Watch Ultra 2 高山回环式表带 蓝色', '{"表带":"高山回环式","颜色":"蓝色","尺寸":"49mm"}', 6499.00),
(@pid, 'Watch Ultra 2 海洋表带 午夜黑',      '{"表带":"海洋表带","颜色":"午夜黑","尺寸":"49mm"}', 6999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 190, 0 FROM sku WHERE product_id = @pid;

-- Product 20: Samsung Galaxy Watch6 Classic
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Samsung Galaxy Watch6 Classic', 7, 'Samsung',
        '三星 Galaxy Watch6 Classic，经典旋转表圈回归，蓝宝石玻璃镜面，BioActive传感器，智能穿戴标杆。',
        'https://picsum.photos/seed/galaxy-watch6-classic/400/400',
        '["https://picsum.photos/seed/galaxy-watch6-classic-1/400/400","https://picsum.photos/seed/galaxy-watch6-classic-2/400/400"]',
        1, 2890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Watch6 Classic 43mm 蓝牙版 黑色', '{"颜色":"黑色","尺寸":"43mm","网络":"蓝牙"}', 2699.00),
(@pid, 'Watch6 Classic 47mm 蓝牙版 银色', '{"颜色":"银色","尺寸":"47mm","网络":"蓝牙"}', 3299.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 230, 0 FROM sku WHERE product_id = @pid;

-- Product 21: Huawei Watch GT 4
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Huawei Watch GT 4', 7, 'Huawei',
        '华为 Watch GT 4，几何美学设计，TruSeen 5.5+心率监测，14天超长续航，时尚与科技的完美融合。',
        'https://picsum.photos/seed/huawei-watch-gt4/400/400',
        '["https://picsum.photos/seed/huawei-watch-gt4-1/400/400","https://picsum.photos/seed/huawei-watch-gt4-2/400/400"]',
        1, 6230);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Watch GT 4 41mm 珍珠白',    '{"颜色":"珍珠白","尺寸":"41mm"}', 1499.00),
(@pid, 'Watch GT 4 46mm 幻夜黑',    '{"颜色":"幻夜黑","尺寸":"46mm"}', 1799.00),
(@pid, 'Watch GT 4 46mm 精英款 钢色','{"颜色":"不锈钢色","尺寸":"46mm","版本":"精英款"}', 2399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 300, 0 FROM sku WHERE product_id = @pid;

-- Product 22: Casio G-Shock GMW-B5000
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Casio G-Shock GMW-B5000全金属', 7, 'Casio',
        '卡西欧 G-Shock GMW-B5000，全金属不锈钢表壳，太阳能动力，蓝牙连接，经典小方块的全金属进化。',
        'https://picsum.photos/seed/casio-g-shock-gmw-b5000/400/400',
        '["https://picsum.photos/seed/casio-g-shock-gmw-b5000-1/400/400","https://picsum.photos/seed/casio-g-shock-gmw-b5000-2/400/400"]',
        1, 1780);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'G-Shock GMW-B5000D 银色',  '{"颜色":"银色","材质":"不锈钢"}', 2999.00),
(@pid, 'G-Shock GMW-B5000GD 金色', '{"颜色":"金色","材质":"不锈钢"}', 3499.00),
(@pid, 'G-Shock GMW-B5000TCM 钛合金','{"颜色":"钛原色","材质":"钛合金"}', 4499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 100, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 7: 男装 (category_id=10) — 6 products
-- ============================================================

-- Product 23: Nike 春季运动休闲外套
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Sportswear 春季男子运动休闲外套', 10, 'Nike',
        'Nike Sportswear 春季男子运动休闲外套，轻盈梭织面料，宽松剪裁，拉链插手口袋，运动与日常完美切换。',
        'https://picsum.photos/seed/nike-sportswear-jacket/400/400',
        '["https://picsum.photos/seed/nike-sportswear-jacket-1/400/400","https://picsum.photos/seed/nike-sportswear-jacket-2/400/400"]',
        1, 1340);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '运动休闲外套 M码 黑色', '{"颜色":"黑色","尺寸":"M"}', 499.00),
(@pid, '运动休闲外套 L码 灰色', '{"颜色":"灰色","尺寸":"L"}', 499.00),
(@pid, '运动休闲外套 XL码 深蓝','{"颜色":"深蓝","尺寸":"XL"}', 699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 300, 0 FROM sku WHERE product_id = @pid;

-- Product 24: Adidas 三叶草经典卫衣
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas Originals 三叶草经典卫衣', 10, 'Adidas',
        'Adidas Originals 三叶草经典卫衣，纯棉毛圈面料，胸前大三叶草Logo，经典版型，街头潮流百搭单品。',
        'https://picsum.photos/seed/adidas-originals-hoodie/400/400',
        '["https://picsum.photos/seed/adidas-originals-hoodie-1/400/400","https://picsum.photos/seed/adidas-originals-hoodie-2/400/400"]',
        1, 2670);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '三叶草卫衣 M码 学院藏青', '{"颜色":"学院藏青","尺寸":"M"}', 399.00),
(@pid, '三叶草卫衣 L码 传奇灰',   '{"颜色":"传奇灰","尺寸":"L"}',   399.00),
(@pid, '三叶草卫衣 XL码 瑕白',    '{"颜色":"瑕白","尺寸":"XL"}',    599.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 350, 0 FROM sku WHERE product_id = @pid;

-- Product 25: Nike Dri-FIT 速干短袖T恤
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Dri-FIT 男子速干短袖T恤', 10, 'Nike',
        'Nike Dri-FIT 男子速干短袖T恤，Dri-FIT导湿速干科技，轻盈透气，运动训练与日常穿搭皆宜。',
        'https://picsum.photos/seed/nike-drifit-tee/400/400',
        '["https://picsum.photos/seed/nike-drifit-tee-1/400/400","https://picsum.photos/seed/nike-drifit-tee-2/400/400"]',
        1, 5120);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Dri-FIT T恤 M码 白色', '{"颜色":"白色","尺寸":"M"}', 199.00),
(@pid, 'Dri-FIT T恤 L码 黑色', '{"颜色":"黑色","尺寸":"L"}', 199.00),
(@pid, 'Dri-FIT T恤 XL码 蓝色','{"颜色":"蓝色","尺寸":"XL"}', 299.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 450, 0 FROM sku WHERE product_id = @pid;

-- Product 26: Adidas 运动束脚长裤
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas 男子运动束脚长裤', 10, 'Adidas',
        'Adidas 男子运动束脚长裤，AEROREADY吸汗面料，弹力修身剪裁，拉链口袋，从健身房到街头自由切换。',
        'https://picsum.photos/seed/adidas-tapered-pants/400/400',
        '["https://picsum.photos/seed/adidas-tapered-pants-1/400/400","https://picsum.photos/seed/adidas-tapered-pants-2/400/400"]',
        1, 2190);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '运动束脚长裤 M码 黑色', '{"颜色":"黑色","尺寸":"M"}', 349.00),
(@pid, '运动束脚长裤 L码 灰色', '{"颜色":"灰色","尺寸":"L"}', 349.00),
(@pid, '运动束脚长裤 XL码 墨绿','{"颜色":"墨绿","尺寸":"XL"}', 499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 320, 0 FROM sku WHERE product_id = @pid;

-- Product 27: MUJI 男式法国亚麻衬衫
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 男式法国亚麻衬衫', 10, 'MUJI',
        'MUJI 男式法国亚麻衬衫，100%法国亚麻，天然透气凉爽质感，简约直筒剪裁，春夏百搭经典款。',
        'https://picsum.photos/seed/muji-linen-shirt-m/400/400',
        '["https://picsum.photos/seed/muji-linen-shirt-m-1/400/400","https://picsum.photos/seed/muji-linen-shirt-m-2/400/400"]',
        1, 1890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '法国亚麻衬衫 M码 原色', '{"颜色":"原色","尺寸":"M"}', 299.00),
(@pid, '法国亚麻衬衫 L码 藏蓝', '{"颜色":"藏蓝","尺寸":"L"}', 299.00),
(@pid, '法国亚麻衬衫 XL码 白色','{"颜色":"白色","尺寸":"XL"}', 399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 280, 0 FROM sku WHERE product_id = @pid;

-- Product 28: Nike 男子针织运动夹克
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Tech Fleece 男子针织运动夹克', 10, 'Nike',
        'Nike Tech Fleece 男子针织运动夹克，轻盈保暖Tech Fleece面料，符合人体工学的剪裁，现代运动美学典范。',
        'https://picsum.photos/seed/nike-tech-fleece-jacket/400/400',
        '["https://picsum.photos/seed/nike-tech-fleece-jacket-1/400/400","https://picsum.photos/seed/nike-tech-fleece-jacket-2/400/400"]',
        1, 1560);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Tech Fleece 夹克 M码 黑色',  '{"颜色":"黑色","尺寸":"M"}',  599.00),
(@pid, 'Tech Fleece 夹克 L码 灰色',  '{"颜色":"灰色","尺寸":"L"}',  599.00),
(@pid, 'Tech Fleece 夹克 XL码 卡其', '{"颜色":"卡其","尺寸":"XL"}', 899.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 200, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 8: 女装 (category_id=11) — 6 products
-- ============================================================

-- Product 29: MUJI 女式棉麻混纺连衣裙
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 女式棉麻混纺连衣裙', 11, 'MUJI',
        'MUJI 女式棉麻混纺连衣裙，有机棉与亚麻混纺，自然亲肤透气，A字宽松版型，简约日系风格。',
        'https://picsum.photos/seed/muji-linen-dress/400/400',
        '["https://picsum.photos/seed/muji-linen-dress-1/400/400","https://picsum.photos/seed/muji-linen-dress-2/400/400"]',
        1, 3420);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '棉麻连衣裙 S码 原色', '{"颜色":"原色","尺寸":"S"}', 399.00),
(@pid, '棉麻连衣裙 M码 藏蓝', '{"颜色":"藏蓝","尺寸":"M"}', 399.00),
(@pid, '棉麻连衣裙 L码 墨绿', '{"颜色":"墨绿","尺寸":"L"}', 599.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 260, 0 FROM sku WHERE product_id = @pid;

-- Product 30: Nike 女子运动内衣套装
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Swoosh 女子运动内衣套装', 11, 'Nike',
        'Nike Swoosh 女子运动内衣套装，Dri-FIT速干面料，中度支撑，可拆卸胸垫，瑜伽跑步多场景适用。',
        'https://picsum.photos/seed/nike-sports-bra-set/400/400',
        '["https://picsum.photos/seed/nike-sports-bra-set-1/400/400","https://picsum.photos/seed/nike-sports-bra-set-2/400/400"]',
        1, 4890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '运动内衣套装 S码 黑色', '{"颜色":"黑色","尺寸":"S"}', 299.00),
(@pid, '运动内衣套装 M码 白色', '{"颜色":"白色","尺寸":"M"}', 299.00),
(@pid, '运动内衣套装 L码 粉色', '{"颜色":"粉色","尺寸":"L"}', 499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 380, 0 FROM sku WHERE product_id = @pid;

-- Product 31: Adidas 女子瑜伽运动套装
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas 女子瑜伽运动套装', 11, 'Adidas',
        'Adidas 女子瑜伽运动套装，高弹力AEROREADY面料，高腰紧身设计，四向拉伸，瑜伽健身的理想搭档。',
        'https://picsum.photos/seed/adidas-yoga-set/400/400',
        '["https://picsum.photos/seed/adidas-yoga-set-1/400/400","https://picsum.photos/seed/adidas-yoga-set-2/400/400"]',
        1, 2750);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '瑜伽运动套装 S码 黑灰', '{"颜色":"黑灰","尺寸":"S"}', 399.00),
(@pid, '瑜伽运动套装 M码 香槟粉','{"颜色":"香槟粉","尺寸":"M"}', 399.00),
(@pid, '瑜伽运动套装 L码 雾霾蓝','{"颜色":"雾霾蓝","尺寸":"L"}', 699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 290, 0 FROM sku WHERE product_id = @pid;

-- Product 32: MUJI 女式美利奴羊毛开衫
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 女式美利奴羊毛开衫', 11, 'MUJI',
        'MUJI 女式美利奴羊毛开衫，100%超细美利奴羊毛，轻盈柔软保暖，简约设计，秋冬内搭外穿皆宜。',
        'https://picsum.photos/seed/muji-wool-cardigan/400/400',
        '["https://picsum.photos/seed/muji-wool-cardigan-1/400/400","https://picsum.photos/seed/muji-wool-cardigan-2/400/400"]',
        1, 1340);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '羊毛开衫 S码 米白', '{"颜色":"米白","尺寸":"S"}', 499.00),
(@pid, '羊毛开衫 M码 驼色', '{"颜色":"驼色","尺寸":"M"}', 499.00),
(@pid, '羊毛开衫 L码 深灰', '{"颜色":"深灰","尺寸":"L"}', 699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 220, 0 FROM sku WHERE product_id = @pid;

-- Product 33: Adidas 女子纯棉宽松T恤
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas Originals 女子纯棉宽松T恤', 11, 'Adidas',
        'Adidas Originals 女子纯棉宽松T恤，100%精梳棉面料，Oversize宽松版型，胸前经典三叶草印花。',
        'https://picsum.photos/seed/adidas-oversize-tee-w/400/400',
        '["https://picsum.photos/seed/adidas-oversize-tee-w-1/400/400","https://picsum.photos/seed/adidas-oversize-tee-w-2/400/400"]',
        1, 6120);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '宽松T恤 S码 白色', '{"颜色":"白色","尺寸":"S"}', 199.00),
(@pid, '宽松T恤 M码 黑色', '{"颜色":"黑色","尺寸":"M"}', 199.00),
(@pid, '宽松T恤 L码 沙色', '{"颜色":"沙色","尺寸":"L"}', 299.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 420, 0 FROM sku WHERE product_id = @pid;

-- Product 34: Nike 女子速干运动短裤
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Dri-FIT 女子速干运动短裤', 11, 'Nike',
        'Nike Dri-FIT 女子速干运动短裤，轻盈速干面料，内置打底，宽松舒适，跑步训练夏日必备。',
        'https://picsum.photos/seed/nike-drifit-shorts-w/400/400',
        '["https://picsum.photos/seed/nike-drifit-shorts-w-1/400/400","https://picsum.photos/seed/nike-drifit-shorts-w-2/400/400"]',
        1, 3980);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '运动短裤 S码 黑色', '{"颜色":"黑色","尺寸":"S"}', 249.00),
(@pid, '运动短裤 M码 粉色', '{"颜色":"粉色","尺寸":"M"}', 249.00),
(@pid, '运动短裤 L码 紫色', '{"颜色":"紫色","尺寸":"L"}', 399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 350, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 9: 鞋靴 (category_id=12) — 6 products
-- ============================================================

-- Product 35: Nike Air Jordan 1 Retro High OG
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Air Jordan 1 Retro High OG', 12, 'Nike',
        'Air Jordan 1 Retro High OG 经典复刻，优质皮革鞋面，Air缓震科技，篮球文化的不朽符号。',
        'https://picsum.photos/seed/air-jordan-1-retro-high/400/400',
        '["https://picsum.photos/seed/air-jordan-1-retro-high-1/400/400","https://picsum.photos/seed/air-jordan-1-retro-high-2/400/400"]',
        1, 4210);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'AJ1 Retro High 40码 禁穿黑红', '{"颜色":"黑红","尺寸":"40"}', 1299.00),
(@pid, 'AJ1 Retro High 42码 芝加哥白红', '{"颜色":"白红","尺寸":"42"}', 1299.00),
(@pid, 'AJ1 Retro High 44码 皇家蓝',     '{"颜色":"皇家蓝","尺寸":"44"}', 1699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 180, 0 FROM sku WHERE product_id = @pid;

-- Product 36: Adidas Ultraboost Light 跑鞋
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas Ultraboost Light 跑鞋', 12, 'Adidas',
        'Adidas Ultraboost Light 跑鞋，Light BOOST中底科技比上一代轻30%，Primeknit+编织鞋面，如袜般贴合。',
        'https://picsum.photos/seed/adidas-ultraboost-light/400/400',
        '["https://picsum.photos/seed/adidas-ultraboost-light-1/400/400","https://picsum.photos/seed/adidas-ultraboost-light-2/400/400"]',
        1, 5340);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Ultraboost Light 39码 核心黑', '{"颜色":"核心黑","尺寸":"39"}', 999.00),
(@pid, 'Ultraboost Light 42码 云雾白', '{"颜色":"云雾白","尺寸":"42"}', 999.00),
(@pid, 'Ultraboost Light 44码 碳灰',   '{"颜色":"碳灰","尺寸":"44"}',   1399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 250, 0 FROM sku WHERE product_id = @pid;

-- Product 37: Nike Air Force 1 '07
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Air Force 1 \'07', 12, 'Nike',
        'Nike Air Force 1 \'07 经典全白，优质皮革鞋面，Nike Air缓震中底，潮流圈永恒的基础款。',
        'https://picsum.photos/seed/nike-air-force-1/400/400',
        '["https://picsum.photos/seed/nike-air-force-1-1/400/400","https://picsum.photos/seed/nike-air-force-1-2/400/400"]',
        1, 7890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Air Force 1 40码 全白', '{"颜色":"全白","尺寸":"40"}', 799.00),
(@pid, 'Air Force 1 42码 全白', '{"颜色":"全白","尺寸":"42"}', 799.00),
(@pid, 'Air Force 1 44码 黑白', '{"颜色":"黑白","尺寸":"44"}', 999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 380, 0 FROM sku WHERE product_id = @pid;

-- Product 38: Adidas Samba OG 经典板鞋
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas Samba OG 经典板鞋', 12, 'Adidas',
        'Adidas Samba OG 经典板鞋，复古足球训练鞋演化而来，皮革鞋面与生胶大底，复古潮流标杆。',
        'https://picsum.photos/seed/adidas-samba-og/400/400',
        '["https://picsum.photos/seed/adidas-samba-og-1/400/400","https://picsum.photos/seed/adidas-samba-og-2/400/400"]',
        1, 6520);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Samba OG 40码 黑白', '{"颜色":"黑白","尺寸":"40"}', 699.00),
(@pid, 'Samba OG 42码 白绿', '{"颜色":"白绿","尺寸":"42"}', 699.00),
(@pid, 'Samba OG 44码 白黑', '{"颜色":"白黑","尺寸":"44"}', 899.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 340, 0 FROM sku WHERE product_id = @pid;

-- Product 39: Nike Dunk Low Retro
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Nike Dunk Low Retro', 12, 'Nike',
        'Nike Dunk Low Retro，复古篮球鞋型，优质皮革与耐磨橡胶大底，学院风配色，街头潮流热宠。',
        'https://picsum.photos/seed/nike-dunk-low-retro/400/400',
        '["https://picsum.photos/seed/nike-dunk-low-retro-1/400/400","https://picsum.photos/seed/nike-dunk-low-retro-2/400/400"]',
        1, 4980);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Dunk Low Retro 40码 熊猫黑白', '{"颜色":"黑白","尺寸":"40"}', 799.00),
(@pid, 'Dunk Low Retro 42码 灰白',     '{"颜色":"灰白","尺寸":"42"}', 799.00),
(@pid, 'Dunk Low Retro 44码 学院蓝',    '{"颜色":"学院蓝","尺寸":"44"}', 1099.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 280, 0 FROM sku WHERE product_id = @pid;

-- Product 40: Adidas Gazelle Indoor 复古运动鞋
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Adidas Gazelle Indoor 复古运动鞋', 12, 'Adidas',
        'Adidas Gazelle Indoor 复古运动鞋，翻毛皮鞋面，半透明生胶大底，1960年代手球训练鞋经典重生。',
        'https://picsum.photos/seed/adidas-gazelle-indoor/400/400',
        '["https://picsum.photos/seed/adidas-gazelle-indoor-1/400/400","https://picsum.photos/seed/adidas-gazelle-indoor-2/400/400"]',
        1, 3670);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Gazelle Indoor 40码 暗夜绿', '{"颜色":"暗夜绿","尺寸":"40"}', 699.00),
(@pid, 'Gazelle Indoor 42码 酒红',   '{"颜色":"酒红","尺寸":"42"}',  699.00),
(@pid, 'Gazelle Indoor 44码 学院蓝', '{"颜色":"学院蓝","尺寸":"44"}', 899.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 260, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 10: 厨房用品 (category_id=13) — 5 products
-- ============================================================

-- Product 41: Philips 空气炸锅 Premium XXL
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Philips 空气炸锅 Premium XXL', 13, 'Philips',
        '飞利浦空气炸锅 Premium XXL，Rapid Air高速热风循环技术，减脂90%，7.3L超大容量，家庭健康烹饪利器。',
        'https://picsum.photos/seed/philips-airfryer-xxl/400/400',
        '["https://picsum.photos/seed/philips-airfryer-xxl-1/400/400","https://picsum.photos/seed/philips-airfryer-xxl-2/400/400"]',
        1, 2670);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '空气炸锅 Premium XXL 标准版', '{"版本":"标准版","容量":"7.3L","颜色":"黑色"}', 999.00),
(@pid, '空气炸锅 Premium XXL 豪华版', '{"版本":"豪华版","容量":"7.3L","颜色":"不锈钢"}', 1499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 200, 0 FROM sku WHERE product_id = @pid;

-- Product 42: Panasonic 智能IH电饭煲
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Panasonic 智能IH电磁加热电饭煲', 13, 'Panasonic',
        '松下智能IH电饭煲，5段IH电磁加热，备长炭铜釜内胆，24小时预约，智能米量判定，每一粒都Q弹香甜。',
        'https://picsum.photos/seed/panasonic-ih-cooker/400/400',
        '["https://picsum.photos/seed/panasonic-ih-cooker-1/400/400","https://picsum.photos/seed/panasonic-ih-cooker-2/400/400"]',
        1, 4230);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'IH电饭煲 3L 白色', '{"容量":"3L","颜色":"白色"}', 1299.00),
(@pid, 'IH电饭煲 4L 黑色', '{"容量":"4L","颜色":"黑色"}', 1499.00),
(@pid, 'IH电饭煲 5L 香槟金','{"容量":"5L","颜色":"香槟金"}', 1999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 220, 0 FROM sku WHERE product_id = @pid;

-- Product 43: Bosch 全自动意式咖啡机
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Bosch 全自动意式咖啡机', 13, 'Bosch',
        '博世全自动意式咖啡机，一键卡布奇诺功能，陶瓷研磨器保留咖啡原香，15Bar高压萃取，家庭咖啡大师。',
        'https://picsum.photos/seed/bosch-coffee-machine/400/400',
        '["https://picsum.photos/seed/bosch-coffee-machine-1/400/400","https://picsum.photos/seed/bosch-coffee-machine-2/400/400"]',
        1, 1450);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '全自动咖啡机 标准版 黑色', '{"版本":"标准版","颜色":"黑色"}', 3999.00),
(@pid, '全自动咖啡机 智能版 不锈钢', '{"版本":"智能版","颜色":"不锈钢"}', 5999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 120, 0 FROM sku WHERE product_id = @pid;

-- Product 44: Philips 高速破壁料理机
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Philips 高速破壁料理机', 13, 'Philips',
        '飞利浦高速破壁料理机，45000转/分钟高速电机，ProBlend 6刀片系统，冷热双打，豆浆果汁汤羹一机搞定。',
        'https://picsum.photos/seed/philips-blender/400/400',
        '["https://picsum.photos/seed/philips-blender-1/400/400","https://picsum.photos/seed/philips-blender-2/400/400"]',
        1, 3120);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '破壁料理机 标准版 黑色', '{"版本":"标准版","颜色":"黑色"}', 899.00),
(@pid, '破壁料理机 静音版 白色', '{"版本":"静音版","颜色":"白色"}', 1399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 180, 0 FROM sku WHERE product_id = @pid;

-- Product 45: Panasonic 变频微波蒸烤箱
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Panasonic 变频微波蒸烤箱', 13, 'Panasonic',
        '松下变频微波蒸烤箱，变频微波+蒸汽烘烤三位一体，1000W变频精准加热，无油烟健康烹饪新体验。',
        'https://picsum.photos/seed/panasonic-steam-oven/400/400',
        '["https://picsum.photos/seed/panasonic-steam-oven-1/400/400","https://picsum.photos/seed/panasonic-steam-oven-2/400/400"]',
        1, 1980);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '变频微波蒸烤箱 23L 黑色', '{"容量":"23L","颜色":"黑色"}', 1999.00),
(@pid, '变频微波蒸烤箱 27L 白色', '{"容量":"27L","颜色":"白色"}', 2499.00),
(@pid, '变频微波蒸烤箱 30L 银色', '{"容量":"30L","颜色":"银色"}', 2999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 150, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 11: 家居家纺 (category_id=14) — 5 products
-- ============================================================

-- Product 46: MUJI 水洗棉四件套
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 水洗棉四件套', 14, 'MUJI',
        'MUJI 水洗棉四件套，100%有机棉水洗工艺处理，自然皱褶触感，柔软亲肤，极简风格卧室美学。',
        'https://picsum.photos/seed/muji-bedding-set/400/400',
        '["https://picsum.photos/seed/muji-bedding-set-1/400/400","https://picsum.photos/seed/muji-bedding-set-2/400/400"]',
        1, 5340);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '水洗棉四件套 1.5m床 原色', '{"尺寸":"1.5m床","颜色":"原色"}', 399.00),
(@pid, '水洗棉四件套 1.8m床 灰色', '{"尺寸":"1.8m床","颜色":"灰色"}', 499.00),
(@pid, '水洗棉四件套 2.0m床 藏蓝', '{"尺寸":"2.0m床","颜色":"藏蓝"}', 699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 300, 0 FROM sku WHERE product_id = @pid;

-- Product 47: MUJI 超声波香薰机
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 超声波香薰机', 14, 'MUJI',
        'MUJI 超声波香薰机，超声波雾化技术静音运行，柔和LED灯光，搭配精油营造舒适居家氛围。',
        'https://picsum.photos/seed/muji-aroma-diffuser/400/400',
        '["https://picsum.photos/seed/muji-aroma-diffuser-1/400/400","https://picsum.photos/seed/muji-aroma-diffuser-2/400/400"]',
        1, 7890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '超声波香薰机 标准版 白色', '{"版本":"标准版","颜色":"白色"}', 299.00),
(@pid, '超声波香薰机 大容量版 磨砂','{"版本":"大容量版","颜色":"磨砂白"}', 499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 400, 0 FROM sku WHERE product_id = @pid;

-- Product 48: Panasonic 无线除螨吸尘器
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Panasonic 无线除螨吸尘器', 14, 'Panasonic',
        '松下无线除螨吸尘器，60000次/分钟强力拍打，UV紫外线杀菌，双重过滤系统，深度清洁床上看不见的过敏原。',
        'https://picsum.photos/seed/panasonic-mite-vacuum/400/400',
        '["https://picsum.photos/seed/panasonic-mite-vacuum-1/400/400","https://picsum.photos/seed/panasonic-mite-vacuum-2/400/400"]',
        1, 4560);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '除螨吸尘器 标准版 珍珠白', '{"版本":"标准版","颜色":"珍珠白"}', 599.00),
(@pid, '除螨吸尘器 强力版 磨砂灰', '{"版本":"强力版","颜色":"磨砂灰"}', 999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 260, 0 FROM sku WHERE product_id = @pid;

-- Product 49: MUJI 白鹅绒羽绒被
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('MUJI 白鹅绒羽绒被', 14, 'MUJI',
        'MUJI 白鹅绒羽绒被，95%白鹅绒填充，800+蓬松度，高支高密防绒面料，轻盈保暖蓬松不压身。',
        'https://picsum.photos/seed/muji-down-duvet/400/400',
        '["https://picsum.photos/seed/muji-down-duvet-1/400/400","https://picsum.photos/seed/muji-down-duvet-2/400/400"]',
        1, 2890);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '白鹅绒羽绒被 春秋被 1.8m',  '{"厚度":"春秋被","尺寸":"1.8m床"}', 899.00),
(@pid, '白鹅绒羽绒被 冬被 1.8m',    '{"厚度":"冬被","尺寸":"1.8m床"}',   1299.00),
(@pid, '白鹅绒羽绒被 加厚冬被 2.0m', '{"厚度":"加厚冬被","尺寸":"2.0m床"}', 1599.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 180, 0 FROM sku WHERE product_id = @pid;

-- Product 50: Dyson Purifier 空气净化风扇
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Dyson Purifier 空气净化风扇', 14, 'Dyson',
        '戴森 Purifier 空气净化风扇，HEPA H13级过滤，Air Multiplier气流倍增技术，精准监测并净化全屋空气。',
        'https://picsum.photos/seed/dyson-purifier/400/400',
        '["https://picsum.photos/seed/dyson-purifier-1/400/400","https://picsum.photos/seed/dyson-purifier-2/400/400"]',
        1, 1670);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '空气净化风扇 标准版 白银色', '{"版本":"标准版","颜色":"白银色"}', 3999.00),
(@pid, '空气净化风扇 加湿版 白金色', '{"版本":"加湿版","颜色":"白金色"}', 4999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 90, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Part 12: 灯具照明 (category_id=15) — 4 products
-- ============================================================

-- Product 51: Philips Hue 智能彩光灯带
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Philips Hue 智能彩光灯带', 15, 'Philips',
        '飞利浦 Hue 智能彩光灯带，1600万色可调，App/语音/感应控制，可与音乐游戏联动，打造沉浸式居家光影体验。',
        'https://picsum.photos/seed/philips-hue-strip/400/400',
        '["https://picsum.photos/seed/philips-hue-strip-1/400/400","https://picsum.photos/seed/philips-hue-strip-2/400/400"]',
        1, 3670);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Hue彩光灯带 2m基础套装',  '{"长度":"2m","版本":"基础套装"}', 399.00),
(@pid, 'Hue彩光灯带 2m+1m扩展套装','{"长度":"2m+1m","版本":"扩展套装"}', 699.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 240, 0 FROM sku WHERE product_id = @pid;

-- Product 52: Dyson Solarcycle Morph 照明台灯
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Dyson Solarcycle Morph 照明台灯', 15, 'Dyson',
        '戴森 Solarcycle Morph 照明台灯，智能追踪当地日光色温，4种照明模式，60年持久亮度，工业设计美学杰作。',
        'https://picsum.photos/seed/dyson-solarcycle/400/400',
        '["https://picsum.photos/seed/dyson-solarcycle-1/400/400","https://picsum.photos/seed/dyson-solarcycle-2/400/400"]',
        1, 780);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'Solarcycle Morph 标准版 黑色', '{"版本":"标准版","颜色":"黑色"}', 3999.00),
(@pid, 'Solarcycle Morph 智能版 银色', '{"版本":"智能版","颜色":"银色"}', 4999.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 70, 0 FROM sku WHERE product_id = @pid;

-- Product 53: Xiaomi 米家智能LED吸顶灯
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Xiaomi 米家智能LED吸顶灯', 15, 'Xiaomi',
        '小米米家智能LED吸顶灯，Ra95高显色，亮度和色温无极调节，支持小爱同学/Siri/HomeKit语音控制。',
        'https://picsum.photos/seed/xiaomi-ceiling-light/400/400',
        '["https://picsum.photos/seed/xiaomi-ceiling-light-1/400/400","https://picsum.photos/seed/xiaomi-ceiling-light-2/400/400"]',
        1, 5120);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, '米家吸顶灯 32W 卧室款', '{"功率":"32W","适用":"卧室"}', 299.00),
(@pid, '米家吸顶灯 45W 客厅款', '{"功率":"45W","适用":"客厅"}', 399.00),
(@pid, '米家吸顶灯 60W 大空间款','{"功率":"60W","适用":"大空间"}', 499.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 320, 0 FROM sku WHERE product_id = @pid;

-- Product 54: Panasonic LED护眼台灯
SET @pid = 0;
INSERT INTO product (name, category_id, brand, description, main_image, images, status, sale_count)
VALUES ('Panasonic LED护眼台灯', 15, 'Panasonic',
        '松下 LED护眼台灯，国AA级照度，Ra>95高显色，无可视频闪，智能感光调节，守护全家视力健康。',
        'https://picsum.photos/seed/panasonic-led-desk-lamp/400/400',
        '["https://picsum.photos/seed/panasonic-led-desk-lamp-1/400/400","https://picsum.photos/seed/panasonic-led-desk-lamp-2/400/400"]',
        1, 6450);
SET @pid = LAST_INSERT_ID();
INSERT INTO sku (product_id, name, spec, price) VALUES
(@pid, 'LED护眼台灯 标准版 白色',     '{"版本":"标准版","颜色":"白色"}', 199.00),
(@pid, 'LED护眼台灯 智能感光版 深灰', '{"版本":"智能感光版","颜色":"深灰"}', 399.00);
INSERT INTO sku_stock (sku_id, stock, lock_stock)
SELECT id, 400, 0 FROM sku WHERE product_id = @pid;

-- ============================================================
-- Seed data complete: 54 new products, 8 new categories
-- Total products: 5 (updated originals) + 54 (new) = 59
-- Brands used: Apple, Samsung, Huawei, Xiaomi, Sony, Dyson,
--   Nike, Adidas, MUJI, Philips, Panasonic, Bosch, Casio
-- ============================================================
