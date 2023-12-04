-- 优惠券查询，库存判断，用户一人一单判断
-- 需要的参数有优惠券id,用户id
local voucherId = ARGV[1]
local userId = ARGV[2]
-- 订单id
local orderId = ARGV[3]


local stockKey = "seckill:stock:" .. voucherId
local userKey = "seckill:user:" .. voucherId

if (tonumber(redis.call("get", stockKey)) <= 0) then
    -- 库存不足 返回1
    return 1
end
-- 库存充足,用户一人一单判断
if (redis.call("SISMEMBER", userKey, userId) == 1) then
    -- 用户userId已经下单过voucherId了
    return 2
end
-- 可以购买,将用户加入已买集合，将优惠券库存减1
redis.call("SADD", userKey, userId)
redis.call("INCRBY",stockKey, -1)

-- 将订单信息放入stream消息队列  key ： stream.orders
-- XADD stream.orders * k1 v1 k2 v2
redis.call("XADD", "stream.orders", "*", "userId", userId, "voucherId", voucherId, "id", orderId)
return 0