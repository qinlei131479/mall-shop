// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.common.TranslateDTO;
import com.tigshop.bean.dto.im.AutoSendDTO;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.dto.vendor.VendorDiscontinueDTO;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.feieyun.FeieyunApiResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.Translations;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.shop.Account;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.VendorAccountLog;
import com.tigshop.bean.model.vendor.VendorSettlementOrder;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.param.print.PrintTemplate;
import com.tigshop.bean.param.print.PrintTemplateItem;
import com.tigshop.bean.param.print.PrintTemplateOption;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.common.annotation.RetryLimit;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SmsUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.FeieyunApiClient;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.mapper.salesman.SalesmanCustomerMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.mapper.user.UserRankMapper;
import com.tigshop.mapper.vendor.VendorAccountLogMapper;
import com.tigshop.mapper.vendor.VendorSettlementOrderMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.lang.TranslationsService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.print.PrintConfigService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.vendor.VendorProductService;
import com.tigshop.service.vendor.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tigshop.bean.enums.order.AftersalesStatusEnum.IN_REVIEW;

/**
 * @author Tigshop
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class RabbitMqConsumer {

    private final OrderService orderService;

    private final OrderItemService orderItemService;

    private final ImMessageService imMessageService;

    private final RedisCache redisCache;

    private final AftersalesService aftersalesService;

    private final AccountService accountService;

    private final ShopService shopService;

    private final LocalesService localesService;

    private final TranslationsService translationsService;

    private final ConfigService configService;

    private final VendorProductService vendorProductService;

    private final VendorService vendorService;

    private final ProductService productService;

    private final StatementService statementService;

    private static final Integer MAX_RETRY_COUNT = 5;
    private static final Integer MAX_RETRY_COUNT_TTL_MINUTES = 5;
    private final SalesmanConfigMapper salesmanConfigMapper;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanCustomerMapper salemanCustomerMapper;
    private final SalesmanOrderMapper salesmanOrderMapper;
    private final UserRankMapper userRankMapper;
    private final UserService userService;
    private final UserRankConfigMapper userRankConfigMapper;
    private final VendorProductSkuMapper vendorProductSkuMapper;
    private final VendorAccountLogMapper vendorAccountLogMapper;
    private final PrintConfigService printConfigService;
    private final FeieyunApiClient feieyunApiClient;
    private final PaylogMapper paylogMapper;
    private final VendorSettlementOrderMapper vendorSettlementOrderMapper;
    private final AftersalesItemMapper aftersalesItemMapper;
    private final TigshopProperties tigshopProperties;
    private final StatementOutputMapper statementOutputMapper;

    @RabbitListener(queues = RabbitMQConfig.SEND_SMS_QUEUE)
    @RetryLimit
    public void receiveSms(SmsSendDTO smsSend) {
        log.info("收到短信任务：{}", JSONUtil.toJsonStr(smsSend));
        sendSms(smsSend);
    }

    // 调用第三方短信 API
    private void sendSms(SmsSendDTO smsSend) {
        log.info("正在发送短信到：{}", smsSend.getPhoneNumbers());
        log.info("短信内容：{}", smsSend.getTemplateParam());

        // 模拟调用阿里云短信服务
        SendSmsResponse sendSmsResponse = SmsUtils.sendSms(
                smsSend.getAccessKeyId(),
                smsSend.getAccessKeySecret(),
                smsSend.getPhoneNumbers(),
                smsSend.getSignName(),
                smsSend.getTemplateCode(),
                smsSend.getTemplateParam());
        if (!"OK".equals(sendSmsResponse.getBody().getCode())) {
            log.info("短信发送失败！");
        }
        log.info("短信发送成功！");
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    @RetryLimit
    public void receiveOrderCancelMessage(Integer orderId) {
        log.info("收到订单取消消息：{}", orderId);
        orderService.cancelOrder(orderId);
    }

    @RabbitListener(queues = RabbitMQConfig.AUTO_MESSAGE_QUEUE)
    @RetryLimit
    public void receiveAutoReplyMessage(AutoSendDTO autoSend) {
        // 在此执行自动回复逻辑
        if ("autoWelcome".equals(autoSend.getType())) {
            imMessageService.autoWelcome(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoWaiting".equals(autoSend.getType())) {
            imMessageService.autoWaiting(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoOffWork".equals(autoSend.getType())) {
            imMessageService.autoOffWork(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoTransfer".equals(autoSend.getType())) {
            imMessageService.autoTransfer(Integer.parseInt(autoSend.getConversationId()),
                    Integer.parseInt(autoSend.getOldServantId()),
                    Integer.parseInt(autoSend.getOldServantId()));
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRM_RECEIPT_QUEUE)
    @RetryLimit
    public void receiveOrderConfirmReceiptMessage(Integer orderId) {
        log.info("收到订单自动确认收货消息：{}", orderId);

        // 判断是否已经收货
        Long receivedCount = orderService.lambdaQuery()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getShippingStatus, ShippingStatusEnum.SHIPPED.getCode())
                .count();
        if (receivedCount == 1) {
            return;
        }

        // 判断订单是否售后中
        Long aftersalesCount = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                .count();
        if (aftersalesCount == 1) {
            return;
        }
        orderService.confirmReceipt(orderId);
    }

    @RabbitListener(queues = RabbitMQConfig.AUTO_RETURN_GOODS_QUEUE)
    @RetryLimit
    public void receiveAutoReturnGoodsMessage(Integer aftersalesId) {
        log.info("收到订单退货申请自动确认消息：{}", aftersalesId);

        // 判断是否已经收货
        Aftersales aftersales = aftersalesService.lambdaQuery()
                .eq(Aftersales::getAftersaleId, aftersalesId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                .eq(Aftersales::getAftersaleType, AftersalesTypeEnum.RETURN.getCode())
                .one();
        if (aftersales == null) {
            return;
        }
        AftersalesEditParam dto = AftersalesEditParam.builder()
                .id(aftersalesId)
                .status(AftersalesStatusEnum.APPROVED_FOR_PROCESSING.getCode())
                .refundAmount(aftersales.getRefundAmount())
                .reply(aftersales.getReply())
                .returnAddress(aftersales.getReturnAddress())
                .returnGoodsTip("您的售后申请已通过，请联系商家获取退货地址")
                .build();
        aftersalesAudit(dto);
    }

    private void aftersalesAudit(AftersalesEditParam dto) {
        // 1. 查询售后
        Aftersales aftersales = aftersalesService.getById(dto.getId());
        Assert.notNull(aftersales, () -> new GlobalException("售后信息不存在"));
        Assert.isTrue(IN_REVIEW.getCode() == aftersales.getStatus(), () -> new GlobalException("该售后状态不能操作"));

        // 3. 更新售后
        Aftersales updateAftersales = dto.createUpdateAftersales(aftersales);
        aftersalesService.updateById(updateAftersales);
    }


    @RabbitListener(queues = RabbitMQConfig.STORE_SETTLEMENT_QUEUE)
    @RetryLimit
    public void storeSettlementMessage(Integer orderId) {
        log.info("【进入队列】收到订单确认收货后自动结算店铺资金消息：{}", orderId);

        Order order = orderService.getById(orderId);

        if (order == null) {
            log.error("【错误】未找到订单信息：{}", orderId);
            return;
        }
        // 判断是否已经结算
        if (!Objects.equals(order.getIsSettlement(), Constants.NO)) {
            return;
        }

        Shop shop = shopService.getById(order.getShopId());
        if (shop == null) {
            log.error("【错误】未找到店铺信息：{}", order.getShopId());
            return;
        }
        BigDecimal paidAmount = order.getPaidAmount();
        BigDecimal refundAmount = BigDecimal.ZERO;
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        if (CollUtil.isNotEmpty(aftersalesList)) {
            for (Aftersales aftersales : aftersalesList) {
                refundAmount = refundAmount.add(aftersales.getRefundAmount());
                if (order.getVendorId() != null && order.getVendorId() > 0) {
                    refundAmount = refundAmount.add(getVendorRefundAmount(orderId, null));
                }
            }
        }
        paidAmount = paidAmount.subtract(refundAmount);
        BigDecimal vendorAmount = BigDecimal.ZERO;
        if (order.getVendorId() != null && order.getVendorId() > 0) {
            // 供应商订单
            vendorAmount = getVendorOrderAmount(orderId);
        }

        paidAmount = paidAmount.subtract(vendorAmount);
        BigDecimal fee = BigDecimal.ZERO;
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            RecordRate recordRate = statementService.getRecordRate(orderId, StatementType.ORDER.getCode());
            BigDecimal shopServiceFee = recordRate.getShopServiceFee();
            fee = paidAmount.multiply(shopServiceFee)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        shopService.lambdaUpdate()
                .eq(Shop::getShopId, order.getShopId())
                .setIncrBy(Shop::getShopMoney, paidAmount.subtract(fee))
                .update();

        //保存资金账户日志信息
        this.saveAccount(shop, paidAmount.subtract(fee), order);

        //供应商结算
        if (tigshopProperties.getIsVendor() == 1 && order.getVendorId() != null && order.getVendorId() > 0) {
            this.vendorSettlementMessage(order.getOrderId());
        }
        Order updateOrder = new Order();
        updateOrder.setOrderId(orderId);
        updateOrder.setIsSettlement(Constants.YES);
        orderService.updateById(updateOrder);

        // 订单结算
        updateStatement2Settlement(orderId, StatementType.ORDER.getCode());
        // 服务费结算
        updateStatement2Settlement(orderId, StatementType.SERVICE_FEE.getCode());
        // 售后结算
        List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.ORDER_REFUND.getCode()));
        // 服务费结算
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.SERVICE_FEE.getCode()));
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_SETTLEMENT_QUEUE)
    @RetryLimit
    public void refundSettlementMessage(Integer aftersalesId) {
        log.info("【进入队列】收到退款结算：{}", aftersalesId);

        Aftersales byId = aftersalesService.getById(aftersalesId);
        if (byId == null) {
            log.error("【错误】未找到售后信息：{}", aftersalesId);
            return;
        }
        Integer orderId = byId.getOrderId();
        Order order = orderService.getById(orderId);

        if (order == null) {
            log.error("【错误】未找到订单信息：{}", orderId);
            return;
        }
        // 判断是否已经结算
        if (Objects.equals(order.getIsSettlement(), Constants.NO)) {
            return;
        }

        try {
            Shop shop = shopService.getById(order.getShopId());
            if (shop == null) {
                log.error("【错误】未找到店铺信息：{}", order.getShopId());
                return;
            }

            BigDecimal vendorRefundAmount = BigDecimal.ZERO;

            if (order.getVendorId() != null && order.getVendorId() > 0) {
                vendorRefundAmount = getVendorRefundAmount(orderId, aftersalesId);
            }

            BigDecimal shopRefundAmount = BigDecimal.ZERO;
            List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, orderId)
                    .eq(Aftersales::getAftersaleId, aftersalesId)
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .list();
            if (CollUtil.isNotEmpty(aftersalesList)) {
                for (Aftersales aftersales : aftersalesList) {
                    shopRefundAmount = shopRefundAmount.add(aftersales.getRefundAmount());
                    if (order.getVendorId() != null && order.getVendorId() > 0) {
                        shopRefundAmount = shopRefundAmount.subtract(vendorRefundAmount);
                    }
                }
            }
            RecordRate recordRate = statementService.getRecordRate(orderId, StatementType.ORDER.getCode());
            BigDecimal shopServiceFee = recordRate.getShopServiceFee();
            if (shopRefundAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal fee = BigDecimal.ZERO;
                if (shopRefundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    fee = shopRefundAmount.multiply(shopServiceFee)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                //保存资金账户日志信息
                this.saveDescAccount(shop, shopRefundAmount.subtract(fee), order);
                shopService.lambdaUpdate()
                        .eq(Shop::getShopId, order.getShopId())
                        .setDecrBy(Shop::getShopMoney, shopRefundAmount.subtract(fee))
                        .update();
            }
            BigDecimal supplierServiceFee = recordRate.getSupplierServiceFee();
            if (vendorRefundAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal fee = vendorRefundAmount.multiply(supplierServiceFee)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                //保存资金账户日志信息
                saveDescVendorAccountLog(vendorService.getById(order.getVendorId()), vendorRefundAmount.subtract(fee), order);
                vendorService.lambdaUpdate()
                        .eq(Vendor::getVendorId, order.getVendorId())
                        .setDecrBy(Vendor::getVendorMoney, vendorRefundAmount.subtract(fee))
                        .update();
            }


        } catch (GlobalException e) {
            log.error("【异常】收到退款结算 RabbitMQ：{}", e.getMessage());
        }

    }

    public void updateStatement2Settlement(Integer record, Integer recordType) {
        int update = statementOutputMapper.update(new LambdaUpdateWrapper<StatementOutput>()
                .eq(StatementOutput::getRecordId, record)
                .eq(StatementOutput::getRecordType, recordType)
                .set(StatementOutput::getRealSettlement, Constants.YES)
                .set(StatementOutput::getRealSettlementTime, StringUtils.getCurrentTime()));
        log.info("【出账单】更新 {} 条为已结算", update);
    }

    /**
     * 供应商分的钱款
     *
     * @param orderId
     * @return
     */
    private BigDecimal getVendorOrderAmount(Integer orderId) {
        BigDecimal vendorAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .isNotNull(OrderItem::getVendorId)
                .list();

        if (CollUtil.isEmpty(orderItems)) {
            return BigDecimal.ZERO;
        }

        // 计算供应商应得金额（扣除退款）
        for (OrderItem item : orderItems) {
            BigDecimal itemAmount = item.getVendorProductSupplyPrice().multiply(new BigDecimal(item.getQuantity()));
            vendorAmount = vendorAmount.add(itemAmount);
        }
        return vendorAmount;
    }

    /**
     * 供应商退的钱款
     *
     * @param orderId
     * @return
     */
    private BigDecimal getVendorRefundAmount(Integer orderId, Integer aftersalesId) {
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .isNotNull(OrderItem::getVendorId)
                .list();

        if (CollUtil.isEmpty(orderItems)) {
            return BigDecimal.ZERO;
        }
        List<Integer> aftersalesIds = null;
        if (aftersalesId != null) {
            aftersalesIds = List.of(aftersalesId);
        } else {
            List<Aftersales> vendorAftersalesList = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, orderId)
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .list();
            aftersalesIds = vendorAftersalesList.stream().map(Aftersales::getAftersaleId).toList();
        }
        // 计算供应商应得金额（扣除退款）
        BigDecimal vendorRefundAmountAll = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            // 计算如果按件退多少
            if (ObjectUtil.isNotEmpty(aftersalesIds)) {
                List<AftersalesItem> aftersalesItems = aftersalesItemMapper.selectList(new LambdaQueryWrapper<AftersalesItem>()
                        .in(AftersalesItem::getAftersaleId, aftersalesIds)
                        .eq(AftersalesItem::getOrderItemId, item.getItemId())
                );
                if (ObjectUtil.isNotEmpty(aftersalesItems)) {
                    int number = aftersalesItems.stream().mapToInt(AftersalesItem::getNumber).sum();
                    vendorRefundAmountAll = vendorRefundAmountAll.add(item.getVendorProductSupplyPrice().multiply(new BigDecimal(number)));
                }
            }
        }
        return vendorRefundAmountAll;
    }

    private void saveAccount(Shop shop, BigDecimal paidAmount, Order order) {
        BigDecimal newShopMoney = shop.getShopMoney().add(paidAmount).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.setShopMoney(shop.getShopMoney());
        account.setShopId(order.getShopId());
        account.setFrozenMoney(shop.getFrozenMoney());
        account.setNewShopMoney(newShopMoney);
        account.setNewFrozenMoney(shop.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        account.setIsNew(1);
        accountService.save(account);
    }

    private void saveDescAccount(Shop shop, BigDecimal refundAmount, Order order) {
        BigDecimal newShopMoney = shop.getShopMoney().subtract(refundAmount).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.setShopMoney(shop.getShopMoney());
        account.setShopId(order.getShopId());
        account.setFrozenMoney(shop.getFrozenMoney());
        account.setNewShopMoney(newShopMoney);
        account.setNewFrozenMoney(shop.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        account.setIsNew(1);
        accountService.save(account);
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_NAME_QUEUE)
    @RetryLimit
    public void translationName(TranslateDTO dto) {
        log.info("【进入队列】收到翻译信息：{}", dto.getTranslationName());
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("translationName::" + dto.getTranslationName(), uuid, 60, TimeUnit.SECONDS);
        if (lock) {
            try {
                Boolean cacheObject = redisCache.getCacheObject("noConfigSecret");
                if (cacheObject != null && cacheObject) {
                    log.info("未配置翻译 cache");
                    return;
                }
                // 获取语言配置
                String langVolcengineSecret = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_SECRET.getBizCode()).getBizVal();
                if (ObjectUtil.isEmpty(langVolcengineSecret) || ObjectUtil.equals(langVolcengineSecret, "youLangVolcengineSecret")) {
                    log.info("未配置翻译 mysql");
                    redisCache.setCacheObject("noConfigSecret", true, 60, TimeUnit.SECONDS);
                    return;
                }
                String translationKey = SecureUtil.md5(dto.getTranslationName());
                long count = translationsService.count(new LambdaQueryWrapper<Translations>()
                        .eq(Translations::getTranslationKey, translationKey)
                        .eq(Translations::getDataType, 0)
                );
                if (count > 0) {
                    return;
                }
                // 获取所有语种
                List<Locales> list = localesService.list(new LambdaQueryWrapper<Locales>().eq(Locales::getIsEnabled, 1));
                List<Translations> translationsList = new ArrayList<>();
                Translations translations = Translations.builder()
                        .translationName(dto.getTranslationName())
                        .translationKey(translationKey)
                        .dataType(0)
                        .build();
                translationsList.add(translations);
                list.forEach(item -> translationsService.batchTranslations(translationsList, item.getId()));
                translationsService.save(translations);
            } catch (Exception e) {
                log.error("【异常】收到翻译信息 RabbitMQ：{}", e.getMessage());
            } finally {
                redisCache.unlock("translationName::" + dto.getTranslationName(), uuid);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SALESMAN_SETTLEMENT_QUEUE)
    @RetryLimit
    public void salesmanSettlementMessage(Integer orderId) {
        log.info(" 订单确认收货后第15天结算 ：{}", orderId);
        orderService.settlement(orderId, 1);
    }

    @RabbitListener(queues = RabbitMQConfig.SALESMAN_UPDATE_QUEUE)
    @RetryLimit
    public void salesmanUpdateMessage(Integer salesmanId) {
        log.info("判断分销员是否可以升级 ：{}", salesmanId);
        // 获取升级配置
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(new LambdaQueryWrapper<SalesmanConfig>().eq(SalesmanConfig::getCode, "salesmanConfig"));
        if (salesmanConfig == null) {
            return;
        }
        String data = salesmanConfig.getData();
        JSONObject config = JSONUtil.parseObj(data);
        JSONArray level = config.getJSONArray("level");
        // 防止死循环，加个安全保护：最多升级20次
        int maxUpgradeTimes = 20;
        int upgradeCount = 0;
        while (true) {
            // 获取当前分销等级
            Salesman salesman = salesmanMapper.selectById(salesmanId);
            Integer levelId = salesman.getLevel();
            Integer nextLevelId = levelId + 1;
            JSONObject levelJson = null;
            for (int i = 0; i < level.size(); i++) {
                if (nextLevelId.equals(level.getJSONObject(i).getInt("id"))) {
                    levelJson = level.getJSONObject(i);
                }
            }
            // 下一级为空，直接跳过
            if (levelJson == null) {
                return;
            }
            BigDecimal salesAmount = null;
            Integer salesInviteUsers = null;
            BigDecimal selfBuyAmount = null;
            // 获取分销下一级等级
            JSONObject jsonObject = levelJson.getJSONObject("condition");
            if (jsonObject.getJSONObject("salesAmount").getBool("checked")) {
                //推广金额
                salesAmount = jsonObject.getJSONObject("salesAmount").getBigDecimal("value");
            }
            if (jsonObject.getJSONObject("salesInviteUsers").getBool("checked")) {
                //发展客户数
                salesInviteUsers = jsonObject.getJSONObject("salesInviteUsers").getInt("value");
            }
            if (jsonObject.getJSONObject("selfBuyAmount").getBool("checked")) {
                //自购金额
                selfBuyAmount = jsonObject.getJSONObject("selfBuyAmount").getBigDecimal("value");
            }
            // 获取拉新的客户数，判断是否满足下一级
            Long inviteUsers = salemanCustomerMapper.selectCount(new LambdaQueryWrapper<SalesmanCustomer>().eq(SalesmanCustomer::getSalesmanId, salesmanId));
            log.info("发展客户数：{}；升级发展客户数：{}", inviteUsers, salesInviteUsers);
            if (salesInviteUsers != null && inviteUsers < salesInviteUsers) {
                return;
            }
            // 获取自购金额，判断是否满足下一级
            BigDecimal selfBuy = orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getUserId, salesman.getUserId()).eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode()))
                    .stream()
                    .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("自购金额：{}；升级自购金额：{}", selfBuy, selfBuyAmount);
            if (selfBuyAmount != null && selfBuy.compareTo(selfBuyAmount) < 0) {
                return;
            }
            // 获取分销金额数，判断是否满足下一级
            BigDecimal sales = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>().eq(SalesmanOrder::getSalesmanId, salesmanId))
                    .stream()
                    .map(SalesmanOrder::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("分销金额：{}；分销升级金额：{}", sales, salesAmount);
            if (salesAmount != null && sales.compareTo(salesAmount) < 0) {
                return;
            }
            salesman.setLevel(nextLevelId);
            salesmanMapper.updateById(salesman);
            upgradeCount++;
            if (upgradeCount > maxUpgradeTimes) {
                log.error("分销员升级超过 {} 次，可能存在死循环，salesmanId={}", maxUpgradeTimes, salesmanId);
                break;
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.VIP_UPDATE_QUEUE)
    @RetryLimit
    public void vipUpdateMessage(Integer userId) {
        log.info("判断会员是否可以升级 ：{}", userId);
        UserRankConfig userRankConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "rank_config"));
        if (userRankConfig == null) {
            return;
        }
        List<UserRank> userRankList = userRankMapper.selectList(new LambdaQueryWrapper<>());
        // 获取当前等级
        User currentUser = userService.getById(userId);
        Integer rankId = currentUser.getRankId();
        Integer nextRankId = rankId + 1;
        UserRank nextRank = null;
        for (UserRank userRank : userRankList) {
            if (nextRankId.equals(userRank.getRankId())) {
                nextRank = userRank;
            }
        }
        // 下一级为空，直接跳过
        if (nextRank == null) {
            return;
        }
        if (userRankConfig.getRankType() == 1) {
            // 成长值升级逻辑
            if (BigDecimal.valueOf(currentUser.getGrowthPoints()).compareTo(nextRank.getMinGrowthPoints()) < 0) {
                return;
            }
        } else {
            // 消费金额升级逻辑
            if (currentUser.getOrderAmount().compareTo(nextRank.getMinGrowthPoints()) < 0) {
                return;
            }
        }

        currentUser.setRankId(nextRank.getRankId());
        userService.updateById(currentUser);
    }

    @RabbitListener(queues = RabbitMQConfig.VENDOR_DISCONTINUE_QUEUE)
    @RetryLimit
    public void vendorDiscontinueMessage(VendorDiscontinueDTO discontinueDTO) {
        log.info("收到供应商断供消息：{}", JSONUtil.toJsonStr(discontinueDTO));
        Integer vendorId = discontinueDTO.getVendorId();
        Integer vendorProductId = discontinueDTO.getVendorProductId();

        if (vendorId != null) {
            // 下架与vendorId关联的产品
            vendorProductService.discontinueByVendorId(vendorId);
            productService.discontinueByVendorId(vendorId);
        } else if (vendorProductId != null) {
            // 下架与vendorProductId关联的产品
            vendorProductService.discontinueByVendorProductId(vendorProductId);
            productService.discontinueByVendorProductId(vendorProductId);
        } else if (ObjectUtil.isNotEmpty(discontinueDTO.getVendorProductIds())) {
            // 下架与vendorProductId关联的产品
            // vendorProductService.discontinueByVendorProductIds(discontinueDTO.getVendorProductIds());
            productService.discontinueByVendorProductIds(discontinueDTO.getVendorProductIds());
        }
    }

    /**
     * 供应商商结算，由店铺结算触发
     *
     * @param orderId
     */
    @RabbitListener(queues = RabbitMQConfig.VENDOR_SETTLEMENT_QUEUE)
    @RetryLimit
    public void vendorSettlementMessage(Integer orderId) {
        log.info("【进入队列】收到供应商结算消息：{}", orderId);

        Order order = orderService.getById(orderId);
        if (order == null) {
            log.error("【错误】未找到订单信息：{}", orderId);
            return;
        }

        // 判断是否已经结算
        if (!Objects.equals(order.getIsSettlement(), Constants.NO)) {
            log.info("订单已结算，跳过处理：{}", orderId);
            return;
        }

        // 获取订单中的供应商商品信息
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .isNotNull(OrderItem::getVendorId)
                .list();

        if (CollUtil.isEmpty(orderItems)) {
            log.info("订单中没有供应商商品，跳过结算：{}", orderId);
            return;
        }

        // 计算供应商应得金额（扣除退款）
        BigDecimal vendorAmount = BigDecimal.ZERO;
        vendorAmount = getVendorOrderAmount(orderId).subtract(getVendorRefundAmount(orderId, null));
        if (vendorAmount == null) {
            log.info("订单中没有供应商商品，跳过结算：{}", orderId);
            return;
        }

        Vendor vendor = vendorService.getById(order.getVendorId());
        if (vendor == null) {
            log.error("【错误】未找到供应商信息：{}", order.getVendorId());
            return;
        }
        RecordRate recordRate = statementService.getRecordRate(orderId, StatementType.ORDER.getCode());
        BigDecimal supplierServiceFee = recordRate.getSupplierServiceFee();
        BigDecimal fee = vendorAmount.multiply(supplierServiceFee)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        vendorService.lambdaUpdate()
                .eq(Vendor::getVendorId, order.getVendorId())
                .setIncrBy(Vendor::getVendorMoney, vendorAmount.subtract(fee))
                .update();

        // 保存资金账户日志信息
        saveVendorAccountLog(vendor, vendorAmount.subtract(fee), order);

        vendorSettlementOrderMapper.insert(VendorSettlementOrder.builder()
                .orderId(orderId)
                .shopId(order.getShopId())
                .vendorId(vendor.getVendorId())
                .amount(vendorAmount)
                .addTime(StringUtils.getCurrentTime())
                .build());
        if (order.getShopId() == 0) {
            Order updateOrder = new Order();
            updateOrder.setOrderId(orderId);
            updateOrder.setIsSettlement(Constants.YES);
            orderService.updateById(updateOrder);
        }

        // 订单结算
        updateStatement2Settlement(orderId, StatementType.ORDER.getCode());
        // 服务费结算
        updateStatement2Settlement(orderId, StatementType.SERVICE_FEE.getCode());
        // 售后结算
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.ORDER_REFUND.getCode()));
        // 服务费结算
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.SERVICE_FEE.getCode()));
    }

    private void saveVendorAccountLog(Vendor vendor, BigDecimal paidAmount, Order order) {
        BigDecimal newVendorMoney = vendor.getVendorMoney().add(paidAmount).setScale(2, RoundingMode.HALF_UP);
        VendorAccountLog account = new VendorAccountLog();
        account.setVendorMoney(vendor.getVendorMoney());
        account.setVendorId(order.getVendorId());
        account.setFrozenMoney(vendor.getFrozenMoney());
        account.setNewVendorMoney(newVendorMoney);
        account.setNewFrozenMoney(vendor.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        vendorAccountLogMapper.insert(account);
    }

    private void saveDescVendorAccountLog(Vendor vendor, BigDecimal refundAmount, Order order) {
        BigDecimal newVendorMoney = vendor.getVendorMoney().subtract(refundAmount).setScale(2, RoundingMode.HALF_UP);
        VendorAccountLog account = new VendorAccountLog();
        account.setVendorMoney(vendor.getVendorMoney());
        account.setVendorId(order.getVendorId());
        account.setFrozenMoney(vendor.getFrozenMoney());
        account.setNewVendorMoney(newVendorMoney);
        account.setNewFrozenMoney(vendor.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        vendorAccountLogMapper.insert(account);
    }

    @RabbitListener(queues = RabbitMQConfig.PRINT_ORDER_QUEUE)
    @RetryLimit
    public void receivePrintOrderMessage(PrintOrderDTO printOrderDTO) {
        log.info("收到打印订单消息：{}", JSONUtil.toJsonStr(printOrderDTO));
        // 1. 获取打印机信息
        Print print = printOrderDTO.getPrint();

        // 3. 获取打印机配置
        PrintConfigVO configVO = printConfigService.getConfigsByPrintId(print.getPrintId());
        if (configVO == null) {
            log.error("【错误】未找到打印机配置：{}", print.getPrintId());
            return;
        }

        // 4. 根据打印模板生成打印内容
        String content = generatePrintContent(printOrderDTO.getOrderId(), configVO.getTemplate());

        log.error(content);

        // 5. 调用飞鹅云API打印订单
        String stime = FeieyunApiUtils.getCurrentTimestamp();
        String sig = FeieyunApiUtils.generateSign(print.getThirdAccount(), print.getThirdKey(), stime);

        // 设置打印联数，如果未指定则使用打印机默认值
        Integer printNumber = print.getPrintNumber();
        if (printNumber == null || printNumber <= 0) {
            printNumber = 1;
        }

        String resultStr = feieyunApiClient.printOrder(
                print.getThirdAccount(),
                stime,
                sig,
                "Open_printMsg",
                print.getPrintSn(),
                content,
                printNumber
        );

        FeieyunApiResult result = JSONUtil.parseObj(resultStr).toBean(FeieyunApiResult.class);

        if (result == null || !result.isSuccess()) {
            log.error("【错误】调用飞鹅云API打印订单失败：{}", resultStr);
            return;
        }

        log.info("打印订单成功，订单ID：{}，打印机：{}", printOrderDTO.getOrderId(), print.getPrintName());
    }

    /**
     * 根据订单信息和打印模板生成打印内容
     *
     * @param orderId  订单id
     * @param template 打印模板
     * @return 打印内容
     */
    private String generatePrintContent(Integer orderId, PrintTemplate template) {
        StringBuilder content = new StringBuilder();

        Order order = orderService.getById(orderId);
        OrderVO orderVO = orderService.detail(orderId, order.getUserId());

        // 遍历打印模板，根据配置生成内容
        for (PrintTemplateItem item : template) {
            String title = item.getTitle();
            List<PrintTemplateOption> options = item.getOptions();

            // 根据模板项标题生成对应内容
            switch (title) {
                case "小票头部" -> generateReceiptHeader(content, orderVO, options);
                case "配送信息" -> generateDeliveryInfo(content, orderVO, options);
                case "买家备注" -> generateBuyerNote(content, orderVO, options);
                case "商品信息" -> generateProductInfo(content, orderVO, options);
                case "运费信息" -> generateShippingInfo(content, orderVO, options);
                case "优惠信息" -> generateDiscountInfo(content, orderVO, options);
                case "支付信息" -> generatePaymentInfo(content, orderVO, options);
                case "客户信息" -> generateCustomerInfo(content, orderVO, options);
                case "其他订单信息" -> generateOtherOrderInfo(content, orderVO, options);
                case "二维码" -> generateCodeInfo(content, orderVO, options);
                case "底部公告" -> generateFooter(content, orderVO, options);
            }
        }

        return content.toString();
    }

    /**
     * 生成小票头部
     */
    private void generateReceiptHeader(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        if (PrintTemplateOption.isChoose(options, "商家名称")) {
            Shop shop = shopService.getById(orderVO.getShopId());
            if (shop != null) {
                orderVO.setShopName(shop.getShopTitle());
            }
            content.append("<C>").append(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).append("</C><BR>");
        }
        if (PrintTemplateOption.isChoose(options, "门店名称")) {
            content.append("<CB>").append(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).append("</CB><BR>");
        }
    }

    /**
     * 生成配送信息
     */
    private void generateDeliveryInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "配送方式")) {
            content.append("配送方式：").append(orderVO.getShippingTypeName() != null ? orderVO.getShippingTypeName() : "").append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "商配&同城-收货信息")) {
            if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                content.append(FeieyunApiUtils.formatMultpartLine("收货信息：", orderVO.getConsignee() + "，" + StringUtils.maskMiddleHalf(orderVO.getMobile()) + "，" + orderVO.getUserAddress())).append("<BR>");
            } else {
                content.append(FeieyunApiUtils.formatMultpartLine("收货信息：", orderVO.getConsignee() + "，" + orderVO.getMobile() + "，" + orderVO.getUserAddress())).append("<BR>");
            }
        }
        if (PrintTemplateOption.isChooseAny(options, "配送方式", "同城配送-收货信息")) {
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成买家备注
     */
    private void generateBuyerNote(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "买家备注")) {
            content.append("<BOLD>").append(FeieyunApiUtils.formatMultpartLine("买家备注：", ObjectUtil.isEmpty(orderVO.getBuyerNote()) ? "无" : orderVO.getBuyerNote())).append("</BOLD>").append("<BR>");
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成商品信息
     */
    private void generateProductInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            content.append("商品<BR>");
            content.append("单价          数量          小计<BR>");
            content.append("--------------------------------<BR>");
        }
        if (orderVO.getItems() != null && !orderVO.getItems().isEmpty()) {
            for (OrderItemVO item : orderVO.getItems()) {
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    content.append(item.getProductName());
                    if (ObjectUtil.isNotEmpty(item.getSkuValue())) {
                        content.append("（").append(item.getSkuValue()).append("）");
                    }
                    content.append("<BR>");
                }
                if (PrintTemplateOption.isChoose(options, "规格编码")) {
                    content.append("商品编码：" + item.getProductSn()).append("<BR>");
                }
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    content.append(FeieyunApiUtils.formatLine(configVal + item.getPrice().toPlainString(), item.getQuantity(), configVal + subtotal.toPlainString())).append("<BR>");
                }
            }
        }
        int sum = orderVO.getItems().stream().mapToInt(OrderItemVO::getQuantity).sum();
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            content.append("--------------------------------<BR>");
            content.append(FeieyunApiUtils.formatLine("共" + sum + "件", "<BOLD>合计：" + configVal + orderVO.getProductAmount().toPlainString())).append("</BOLD><BR>");
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成运费信息
     */
    private void generateShippingInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChoose(options, "运费")) {
            content.append(FeieyunApiUtils.formatLine("运费", configVal + orderVO.getShippingFee().toPlainString())).append("<BR>");
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成优惠信息
     */
    private void generateDiscountInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChoose(options, "优惠明细")) {
            content.append(FeieyunApiUtils.formatLine("优惠", "优惠券：-" + configVal + orderVO.getCouponAmount().toPlainString())).append("<BR>");
            content.append(FeieyunApiUtils.formatLine("", "积分抵扣：-" + configVal + orderVO.getPointsAmount().toPlainString())).append("<BR>");
            content.append(FeieyunApiUtils.formatLine("", "活动优惠：-" + configVal + orderVO.getDiscountAmount().toPlainString())).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "优惠总计") && !PrintTemplateOption.isChoose(options, "优惠明细")) {
            content.append(FeieyunApiUtils.formatLine("优惠", "<BOLD>总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
        }
        if (PrintTemplateOption.isChoose(options, "优惠总计") && PrintTemplateOption.isChoose(options, "优惠明细")) {
            content.append(FeieyunApiUtils.formatLine("", "<BOLD>总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
        }
        if (PrintTemplateOption.isChooseAny(options, "优惠明细", "优惠总计")) {
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成支付信息
     */
    private void generatePaymentInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        Paylog paylog = paylogMapper.selectOne(Wrappers.lambdaQuery(Paylog.class)
                .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                .eq(Paylog::getOrderId, orderVO.getOrderId())
                .select(Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId)
        );
        if (PrintTemplateOption.isChoose(options, "支付方式")) {
            if (paylog != null) {
                content.append("支付方式：").append(PayMethodType.getTypeName(paylog.getPayCode())).append("<BR>");
            } else {
                if (orderVO.getPayTypeId() == PaymentType.OFFLINE.getCode()) {
                    content.append("支付方式：").append("线下支付").append("<BR>");
                }
                if (orderVO.getPayTypeId() == PaymentType.COD.getCode()) {
                    content.append("支付方式：").append("货到付款").append("<BR>");
                }
                if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    content.append("支付方式：").append("余额支付").append("<BR>");
                }
            }
        }
        if (PrintTemplateOption.isChoose(options, "实收金额")) {
            content.append("实收金额：").append(configVal + orderVO.getTotalAmount()).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "第三方支付单号")) {
            if (paylog != null) {
                content.append("第三方支付单号：").append(paylog.getTransactionId()).append("<BR>");
            } else {
                content.append("第三方支付单号：").append("无").append("<BR>");
            }
        }
        if (PrintTemplateOption.isChooseAny(options, "支付方式", "实收金额", "第三方支付单号")) {
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成客户信息
     */
    private void generateCustomerInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "客户信息")) {
            content.append("客户姓名：").append(StringUtils.maskMiddleHalf(orderVO.getUser().getUsername())).append("<BR>");
            content.append("手机号：").append(StringUtils.maskMiddleHalf(orderVO.getUser().getMobile())).append("<BR>");
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 生成其他订单信息
     */
    private void generateOtherOrderInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "下单时间")) {
            content.append("下单时间：").append(orderVO.getAddTime()).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "支付时间")) {
            content.append("支付时间：").append(ObjectUtil.isEmpty(orderVO.getPayTime()) ? "-" : orderVO.getPayTime()).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "渠道类型")) {
            content.append("渠道类型：").append(orderVO.getOrderSource()).append("<BR>");
        }
        if (PrintTemplateOption.isChooseAny(options, "下单时间", "支付时间", "渠道类型")) {
            content.append("--------------------------------<BR>");
        }
    }

    /**
     * 订单二维码信息
     */
    private void generateCodeInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "订单二维码")) {
            content.append("订单编号：").append(orderVO.getOrderSn()).append("<BR>");
            content.append("<C>").append("<BC128_C>").append(orderVO.getOrderSn()).append("</BC128_C>").append("</C>").append("<BR>");
        } else {
            content.append("<BR>");
            content.append("<BR>");
        }
    }

    /**
     * 生成底部信息
     */
    private void generateFooter(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options) {
        if (PrintTemplateOption.isChoose(options, "底部公告")) {
            content.append("<C>").append(options.getFirst().getValue()).append("</C><BR>");
        }
        content.append("<BR>");
        content.append("<BR>");
        content.append("<CUT>");
    }

    @RabbitListener(queues = RabbitMQConfig.GENERATE_STATEMENT_QUEUE)
    @RetryLimit
    public void generateStatement(StatementSaveParam param) {
        statementService.saveStatement(param);
    }
}
