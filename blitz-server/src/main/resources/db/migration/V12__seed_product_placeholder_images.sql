-- 商品图片占位。
--
-- 问题：演示库里的 product.main_image 全为空（104 条商品一条图都没有），
-- 前端商品卡片按 main_image 渲染 <img>，于是整页商品卡片都是空白图，
-- 观感上像是「前端坏了」，实际是数据缺图。
--
-- 处理：按 id 轮换六张本地占位图（blitz-server/src/main/resources/static/img/p1..p6.svg）。
-- 这是**演示用占位**，不是商品实拍图；真实上线时应换成对象存储或 CDN 的地址。
--
-- 只填空白，不覆盖已经有值的记录。

UPDATE product
SET main_image = CONCAT('/img/p', (id % 6) + 1, '.svg')
WHERE main_image IS NULL OR main_image = '';
