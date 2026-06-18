-- seckill_deduct.lua
-- Atomic stock deduction with duplicate prevention
-- KEYS[1]: seckill:stock:{seckillProductId}    (stock counter)
-- KEYS[2]: seckill:over:{seckillProductId}      (sold-out flag)
-- KEYS[3]: seckill:user:{seckillProductId}:{userId}  (user repeat check)
-- ARGV[1]: max_retry (if concurrent DECR causes negative, restore)
-- Returns: >=0 success (remaining stock), -1 sold-out, -2 repeat, -3 stock insufficient
local stockKey = KEYS[1]
local overKey = KEYS[2]
local userKey = KEYS[3]

-- Fast fail: already sold out
local over = redis.call('GET', overKey)
if over == '1' then
    return -1
end

-- Fast fail: repeat purchase
local bought = redis.call('GET', userKey)
if bought == '1' then
    return -2
end

-- Check stock count
local stock = tonumber(redis.call('GET', stockKey) or '-1')
if stock == -1 then
    -- Stock not loaded yet — treat as closed
    return -1
end
if stock <= 0 then
    redis.call('SET', overKey, '1')
    return -3
end

-- Atomic deduct
local remaining = redis.call('DECR', stockKey)

-- DECR might go negative under extreme concurrency — rollback
if remaining < 0 then
    redis.call('INCR', stockKey)
    redis.call('SET', overKey, '1')
    return -3
end

-- Mark user as purchased
redis.call('SET', userKey, '1', 'EX', 86400)

-- Check if just sold out
if remaining == 0 then
    redis.call('SET', overKey, '1')
end

return remaining
