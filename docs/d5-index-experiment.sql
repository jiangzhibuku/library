-- ============================================================
-- D5 索引优化实验
-- 环境：MySQL 8.0 / InnoDB / library 库 / 本地 SSD
-- 数据量：borrow 表 200,003 行（EXPLAIN 估算值 194,491，以 COUNT(*) 为准）
-- 目的：验证联合索引对等值查询的加速效果，并对比深分页写法
-- ============================================================

-- ---------- 0. 实验前：构造"无索引"基线 ----------
SHOW INDEX FROM borrow;
-- 若存在 idx_user_status，先删除，否则测不出全表扫描
-- ALTER TABLE borrow DROP INDEX idx_user_status;

-- 精确行数
SELECT COUNT(*) FROM borrow;
-- 结果：200003


-- ---------- 1. 优化前：全表扫描 ----------
EXPLAIN SELECT * FROM borrow WHERE user_id = 500 AND status = 0;
-- 结果：
--   type    = ALL          （全表扫描，最差）
--   key     = NULL         （未使用任何索引）
--   rows    = 194491       （预估扫描约 19.4 万行）
--   Extra   = Using where
-- 实测耗时：25.96 ms（SHOW PROFILES，Query 1）


-- ---------- 2. 建立联合索引 ----------
ALTER TABLE borrow ADD INDEX idx_user_status (user_id, status);
-- 建索引耗时：0.394 s（约 20 万行数据）


-- ---------- 3. 优化后：索引查找 ----------
EXPLAIN SELECT * FROM borrow WHERE user_id = 500 AND status = 0;
-- 结果：
--   type     = ref
--   key      = idx_user_status
--   key_len  = 9           （user_id BIGINT 8 字节 + status TINYINT 1 字节，两列全部命中）
--   rows     = 81
--   filtered = 100.00
-- 实测耗时：0.40 ms（SHOW PROFILES，Query 5）


-- ---------- 4. 结论 ----------
-- 执行计划：type 从 ALL 变为 ref
-- 扫描行数：194,491 -> 81            （减少约 2400 倍）
-- 查询耗时：25.96 ms -> 0.40 ms      （快约 65 倍）
-- key_len=9 证明联合索引的两列都被使用，而不是只用了一列


-- ---------- 5. 深分页对比 ----------
-- 慢写法：先读出 100,000 行，再丢弃前 99,990 行
SELECT * FROM borrow ORDER BY id LIMIT 99990, 10;
-- 耗时：15.52 ms（SHOW PROFILES，Query 15）

-- 快写法：直接从 id=99990 之后读取 10 行
SELECT * FROM borrow WHERE id > 99990 ORDER BY id LIMIT 10;
-- 耗时：0.73 ms（SHOW PROFILES，Query 16）

-- 对比：约 21 倍


-- ---------- 6. 深分页执行计划对比 ----------
EXPLAIN SELECT * FROM borrow ORDER BY id LIMIT 99990, 10;
-- type = index      （全索引扫描：从第一行开始顺序走 10 万个位置）
-- key  = PRIMARY
-- rows = 100000     （需要走过 10 万个索引项才能到达偏移位置）

EXPLAIN SELECT * FROM borrow WHERE id > 99990 ORDER BY id LIMIT 10;
-- type = range      （范围扫描：直接定位到 id>99990 的起点）
-- key  = PRIMARY
-- rows = 97245      （注意：这是"范围内共有多少行"的估算，不是实际读取行数）
--                     因为有 LIMIT 10，实际读到第 10 行就停止

-- 重要说明：
-- EXPLAIN 的 rows 表示"满足条件预计匹配的行数"，不体现 LIMIT 的提前停止。
-- 因此这里的 rows（100000 vs 97245）看不出差距，真正的差异在 type：
--   index = 从头顺序扫描；range = 定位后读取。
-- 实际耗时 15.52ms vs 0.73ms，差约 21 倍。


-- ---------- 7. 原理备注 ----------
-- LIMIT offset, size 的执行过程：
--   扫描 -> 取出 offset+size 行 -> 丢弃前 offset 行 -> 返回剩余 size 行
-- 被丢弃的行同样消耗了 IO 和 CPU，offset 越大浪费越多。
--
-- WHERE id > lastId 属于游标分页（seek method）：
--   在 B+树中直接定位到 lastId 之后的位置，只读取需要的行。
--
-- 代价：只能"下一页"，不能"跳到第 N 页"；
--       排序字段必须唯一且有序（通常用主键 id）。
