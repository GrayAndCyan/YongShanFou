package com.mizore.mob.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.mizore.mob.dto.Result;
import com.mizore.mob.entity.VoucherOrder;
import com.mizore.mob.mapper.VoucherOrderMapper;
import com.mizore.mob.service.ISeckillVoucherService;
import com.mizore.mob.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mizore.mob.util.UserHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mizore
 * @since 2023-12-04
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    StringRedisTemplate stringRedisTemplate;


    // 提前在类加载阶段就加载好锁
    private final static DefaultRedisScript<Long> JUDGE_SCRIPT;
    static {
        JUDGE_SCRIPT = new DefaultRedisScript<>();
        JUDGE_SCRIPT.setLocation(new ClassPathResource("judge.lua"));
        JUDGE_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new OrderHandle());
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private class OrderHandle implements Runnable{

        @Override
        public void run() {
            while (true) {
                String streamKey = "stream.orders";
                // 从stream.orders读一条消息 XADDGROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                // >：读下一个未消费的消息
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                );
                if (list == null || list.isEmpty()) {
                    // 暂时没有在stream读取到消息
                    continue;
                }
                // 读取到了一条消息
                try {
                    MapRecord<String, Object, Object> mapRecord = list.get(0);
                    Map<Object, Object> value = mapRecord.getValue();
                    VoucherOrder order = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    createVoucherOrder(order);
                    // ack
                    stringRedisTemplate.opsForStream().acknowledge(streamKey, "g1", mapRecord.getId());
                } catch (Exception e) {
                    // 出现异常，去pending list读
                    log.error("处理在stream.orders读到的消息时异常",e);
                    readPendingList();
                }
            }
        }
    }

    private void readPendingList() {
        String streamKey = "stream.orders";
        while (true) {
            // 从pending list读一条消息 XADDGROUP g1 c1 COUNT 1 STREAMS stream.orders 0
            // 0：读pending-list中第一条消息
            List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                    Consumer.from("g1", "c1"),
                    StreamReadOptions.empty().count(1),
                    StreamOffset.create(streamKey, ReadOffset.from("0"))
            );
            if (list == null || list.isEmpty()) {
                // 暂时没有在stream读取到消息
                break;
            }
            // 读取到了一条消息
            try {
                MapRecord<String, Object, Object> mapRecord = list.get(0);
                Map<Object, Object> value = mapRecord.getValue();
                VoucherOrder order = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                createVoucherOrder(order);
                // ack确认
                stringRedisTemplate.opsForStream().acknowledge(streamKey, "g1", mapRecord.getId());
            } catch (Exception e) {
                log.error("处理在pending list读到的消息时异常",e);
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }



    @Resource
    private SnowflakeGenerator snowflakeGenerator;
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.执行lua脚本进行购买资格判断：库存是否充足与一人一单限制，对满足的订单放入 stream.orders
        Integer userId = UserHolder.get().getId();
        Long orderId = snowflakeGenerator.next();
        Long result = stringRedisTemplate.execute(JUDGE_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString(), orderId.toString());
        // 2.判断结果是否为0
        // 2.1 不为0,无下单资格
        int res = result.intValue();
        if (res != 0) {
            return Result.error(res == 1 ? "库存不足！" : "已达到限购上限！");
        }
        // 2.2 为0,订单已经由lua放入redis stream队列
        return Result.ok(orderId);
    }

    @Transactional
    public void createVoucherOrder(VoucherOrder order) {

        seckillVoucherService.update()
                .setSql("stock = (stock - 1)") // update stock = stock - 1
                .eq("voucher_id", order.getId()).gt("stock", 0)  // where vouncher_id = ? and stock > 0
                .update();
        save(order);
    }


}
