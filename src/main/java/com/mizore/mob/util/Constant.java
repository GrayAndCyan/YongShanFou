package com.mizore.mob.util;

public class Constant {

    public static final String RUNTIME_EXCEPTION_MSG = "服务器被玩坏了，绝对不是服务器的错！！";

    public static final String LOGIN_CODE_PREFIX = "login:code:";
    public static final int LOGIN_CODE_TTL = 5;
    public static final int MAX_COUNT_DISH_PER_PAGE = 10;

    /**
     * 1：顾客，2：配送员。3：餐厅员工
     */
    public static final Byte ROLE_CUSTOMER = 1;
    public static final Byte ROLE_DELIVERY = 2;
    public static final Byte ROLE_STAFF = 3;

    /**
     *  菜品 1：上架  2：下架   默认未上架状态
     */
    public static final Byte DISH_ON_SALE = 1;
    public static final Byte DISH_OUT_SALE = 2;

    /**
     * 订单状态
     * 1：待接单-用户完成支付但待商家确认，
     * 2：备餐中-商家确认后处于备餐中，
     * 3：待配送-备餐完毕等待骑手配送，
     * 4：配送中-骑手抢单后到送达前，
     * 5：已送达并未评价-骑手送达，
     * 6：已评价-用户提交评价，订单完成。
     */
    public static final Byte ORDER_WAIT_CONFIRM = 1;
    public static final Byte ORDER_PREPARING = 2;
    public static final Byte ORDER_WAIT_DELIVERED = 3;
    public static final Byte ORDER_DELIVERING = 4;
    public static final Byte ORDER_DELIVERED = 5;
    public static final Byte ORDER_COMMENTED = 6;
    public static final String STAFF_PREFIX = "staff:";
    public static final String DELIVERY_PREFIX = "delivery:";
    public static final String CUSTOMER_PREFIX = "customer:";
    public static final String SSE_REFRESH_MESSAGE = "refresh";

    // 订单被确认时发给顾客的sse消息
    public static final String SSE_CONFIRMED_MESSAGE = "confirmed";

    // 订单备餐完成时发给顾客的sse消息
    public static final String SSE_COMPLETED_MESSAGE = "preparation completed";
    public static final String SSE_DELIVERING_MESSAGE = "delivering";
    public static final String SSE_ARRIVED_MESSAGE = "arrived";

    // 幂等性redis setnx 的键前缀
    public static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";
    public static final String IDEMPOTENT_VALUE = "1";

    public static final String LOGIN_TOKEN_PREFIX = "login:token:";

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
    public static final Long EXPIRE_SHOP_TTL = 300L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final Integer FEED_COUNT = 2; // 滚动分页查询中的每页条数
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
    public static final String SHOPTYPE_LIST_KEY = "shoptypelist:";
    public static final String FOLLOW_KEY = "follow:user:";


}
