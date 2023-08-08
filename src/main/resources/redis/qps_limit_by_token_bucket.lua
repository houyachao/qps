--KEYS[1]: 限流 key
--ARGV[1]: 最大令牌数
--ARGV[2]: 每秒生成的令牌数
--ARGV[3]: 本次请求的毫秒数
local info = redis.pcall('HMGET', KEYS[1], 'last_time', 'stored_token_nums')
--最后一次通过限流的时间
local last_time = info[1]
-- 剩余的令牌数量
local stored_token_nums = tonumber(info[2])
local max_token = tonumber(ARGV[1])
local token_rate = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])
local past_time = 0
-- 每毫秒生产令牌速率
local rateOfperMills = token_rate/1000

if stored_token_nums == nil then
    -- 第一次请求或者键已经过期
    stored_token_nums = max_token --令牌恢复至最大数量
    last_time = current_time --记录请求时间
else
    -- 正常情况请求
    past_time = current_time - last_time --经过了多少时间

    if past_time <= 0 then
        --高并发下每个服务的时间可能不一致
        past_time = 0 -- 强制变成0 此处可能会出现少量误差
    end
    -- 两次请求期间内应该生成多少个token
    local generated_nums = math.floor(past_time * rateOfperMills)  -- 向下取整，多余的认为还没生成完
    stored_token_nums = math.min((stored_token_nums + generated_nums), max_token) -- 合并所有的令牌后不能超过设定的最大令牌数
end

local returnVal = nil -- 返回值

if stored_token_nums > 0 then
    returnVal = 1 -- 通过限流
    stored_token_nums = stored_token_nums - 1 -- 减少令牌
    -- 必须要在获得令牌后才能重新记录时间。举例: 当每隔2ms请求一次时,只要第一次没有获取到token,那么后续会无法生产token,永远只过去了2ms
    last_time = last_time + past_time
end

-- 更新缓存
redis.call('HMSET', KEYS[1], 'last_time', last_time, 'stored_token_nums', stored_token_nums)
-- 设置超时时间
-- 令牌桶满额的时间(超时时间)(ms) = 空缺的令牌数 * 生成一枚令牌所需要的毫秒数(1 / 每毫秒生产令牌速率)
redis.call('PEXPIRE', KEYS[1], math.ceil((1/rateOfperMills) * (max_token - stored_token_nums)))

return returnVal