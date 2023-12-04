package com.mizore.mob.service;

import com.mizore.mob.dto.Result;
import com.mizore.mob.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mizore
 * @since 2023-12-04
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder order);
}
