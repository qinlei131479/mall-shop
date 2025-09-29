package com.tigshop.service.msgTemplate.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.dto.msg.MessageTemplateListDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.mapper.finance.OrderInvoiceMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.setting.MessageTemplateMapper;
import com.tigshop.mapper.setting.MessageTypeMapper;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private List<MessageSender> senders;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private UserAuthorizeMapper userAuthorizeService;
    @Resource
    private ConfigService configService;
    @Resource
    private MessageTypeMapper messageTypeService;
    @Resource
    private MessageTemplateMapper messageTemplateService;
    @Resource
    private OrderInvoiceMapper orderInvoiceMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public void sendMessage(Message message) {
        for (MessageSender sender : senders) {
            if (sender.supports(message.getType())) {
                sender.send(message);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum) {
        sendUserMessage(userId, orderId, messageTypeEnum, BigDecimal.ZERO);
    }

    @Override
    public void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum, BigDecimal totalRefund) {
        if (!Arrays.asList(MessageTypeEnum.values()).contains(messageTypeEnum)) {
            return;
        }

        // 获取订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null || ObjectUtil.notEqual(order.getUserId(), userId)) {
            log.error("未查询到对应订单 {}", orderId);
            return;
        }

        try {
            // 获取模板信息相关数据
            MessageTemplateListDTO messageTemplateList = getMessageTemplateList(messageTypeEnum);

            // 发送站内信
            sendWebSiteMessage(messageTemplateList, messageTypeEnum, order);

            // 需要发送短信
            sendSmsMessage(messageTypeEnum, messageTemplateList, order);

            // 需要发送公众号消息
            sendWechatMessage(messageTemplateList, messageTypeEnum, order, totalRefund);

            // 需要小程序消息
            sendMiniProgramMessage(userId, orderId, messageTypeEnum, messageTemplateList, order);
        } catch (Exception e) {
            log.error("模板信息发送报错", e);
        }

        // 发送后台消息
        if (messageTypeEnum == MessageTypeEnum.NEW_ORDER) {
            AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
            adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.NEW_ORDER.getCatId());
            adminMsgCreateDTO.setTitle("您有新的订单：" + order.getOrderSn() + "，金额：" + order.getTotalAmount());
            adminMsgCreateDTO.setContent("您有新的订单：" + order.getOrderSn() + "，请注意查看");
            adminMsgCreateDTO.setShopId(order.getShopId());
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("orderId", order.getOrderId());
            adminMsgCreateDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(adminMsgCreateDTO);
            if (order.getShopId() != null && order.getShopId() > 0) {
                //如果是店铺订单则给平台订单也发一条
                adminMsgCreateDTO.setShopId(0);
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
            if (order.getVendorId() != null && order.getVendorId() > 0) {
                adminMsgCreateDTO.setVendorId(order.getVendorId());
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
        }

        if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
            AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
            adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.PAID_ORDER.getCatId());
            adminMsgCreateDTO.setTitle("您的订单已支付完成：" + order.getOrderSn() + "，金额：" + order.getTotalAmount());
            adminMsgCreateDTO.setContent("您的订单：【" + order.getOrderSn() + "】已支付完成，请注意查看");
            adminMsgCreateDTO.setShopId(order.getShopId());
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("orderId", order.getOrderId());
            adminMsgCreateDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(adminMsgCreateDTO);
            if (order.getShopId() != null && order.getShopId() > 0) {
                //如果是店铺订单则给平台订单也发一条
                adminMsgCreateDTO.setShopId(0);
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
            if (order.getVendorId() != null && order.getVendorId() > 0) {
                adminMsgCreateDTO.setVendorId(order.getVendorId());
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
        }

    }

    private void sendMiniProgramMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getMiniProgram() != null && messageTemplateList.getTypeInfo().getIsMiniProgram() == 1 && messageTemplateList.getMiniProgram().getDisabled() == 0) {
            String openId = Optional.ofNullable(userAuthorizeService.selectOne(new LambdaQueryWrapper<UserAuthorize>()
                            .eq(UserAuthorize::getUserId, userId)
                            .eq(UserAuthorize::getAuthorizeType, 2)))
                    .map(UserAuthorize::getOpenId)
                    .orElse("");
            String templateId = messageTemplateList.getMiniProgram().getInfo().getTemplateId();
            if (ObjectUtil.isNotEmpty(openId) && ObjectUtil.isNotEmpty(templateId)) {
                String page = "/pages/user/order/info?id=" + orderId;
                HashMap<String, Object> message = new HashMap<>();
                message.put("touser", openId);
                message.put("template_id", templateId);
                message.put("page", page);
                HashMap<String, Object> data = new HashMap<>();
                if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
                    data.put("character_string2", Map.of("value", order.getOrderSn()));
                    data.put("time1", Map.of("value", DateUtil.format(DateUtil.date(order.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("amount4", Map.of("value", order.getTotalAmount()));
                }
                if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                    data.put("thing4", Map.of("value", order.getLogisticsName()));
                    data.put("character_string5", Map.of("value", order.getTrackingNo()));
                    data.put("date3", Map.of("value", DateUtil.format(DateUtil.date(order.getShippingTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("thing8", Map.of("value", order.getConsignee() + " " + order.getMobile()));
                    data.put("character_string2", Map.of("value", order.getOrderSn()));
                }
                message.put("data", data);
                log.info("发送小程序消息：{}，内容：{}", templateId, message);
                sendMessage(Message.builder()
                        .type(MessageTemplateType.MiniProgram)
                        .message(message)
                        .build());
            }
        }
    }

    private void sendWechatMessage(MessageTemplateListDTO messageTemplateList, MessageTypeEnum messageTypeEnum, Order order, BigDecimal totalRefund) {
        if (messageTemplateList != null && messageTemplateList.getWechat() != null && messageTemplateList.getTypeInfo().getIsWechat() == 1 && messageTemplateList.getWechat().getDisabled() == 0) {
            String openId = Optional.ofNullable(userAuthorizeService.selectOne(new LambdaQueryWrapper<UserAuthorize>()
                            .eq(UserAuthorize::getUserId, order.getUserId())
                            .eq(UserAuthorize::getAuthorizeType, 1)))
                    .map(UserAuthorize::getOpenId)
                    .orElse("");
            String templateId = messageTemplateList.getWechat().getInfo().getTemplateId();
            if (ObjectUtil.isNotEmpty(openId) && ObjectUtil.isNotEmpty(templateId)) {
                String h5Domain = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
                if (ObjectUtil.isEmpty(h5Domain)) {
                    h5Domain = configService.getConfigVal(SettingsEnum.PC_DOMAIN);
                }
                String url = h5Domain + "/pages/user/order/info?id=" + order.getOrderId();
                HashMap<String, Object> message = new HashMap<>();
                message.put("touser", openId);
                message.put("template_id", templateId);
                message.put("url", url);
                HashMap<String, Object> data = new HashMap<>();
                if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
                    data.put("character_string3", Map.of("value", order.getOrderSn()));
                    data.put("time7", Map.of("value", DateUtil.format(DateUtil.date(order.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("amount4", Map.of("value", order.getTotalAmount()));
                }
                if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                    data.put("thing21", Map.of("value", order.getLogisticsName()));
                    data.put("character_string18", Map.of("value", order.getTrackingNo()));
                    data.put("time3", Map.of("value", DateUtil.format(DateUtil.date(order.getShippingTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("thing17", Map.of("value", order.getConsignee() + " " + order.getMobile()));
                    data.put("character_string2", Map.of("value", order.getOrderSn()));
                }
                if (messageTypeEnum == MessageTypeEnum.ORDER_REFUND) {
                    data.put("character_string5", Map.of("value", order.getOrderSn()));
                    data.put("amount2", Map.of("value", totalRefund));
                }
                message.put("data", data);
                log.info("发送公众号消息：{}，内容：{}", templateId, message);
                sendMessage(Message.builder()
                        .type(MessageTemplateType.Wechat)
                        .message(message)
                        .build());
            }
        }
    }

    private void sendSmsMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getMsg() != null && messageTemplateList.getTypeInfo().getIsMsg() == 1 && messageTemplateList.getMsg().getDisabled() == 0) {
            String templateCode = messageTemplateList.getMsg().getInfo().getTemplateId();
            HashMap<String, Object> content = new HashMap<>();

            User user = userMapper.selectById(order.getUserId());
            String mobile = user.getMobile();

            if (messageTypeEnum == MessageTypeEnum.ORDER_INVOICE) {
                OrderInvoice orderInvoice = orderInvoiceMapper.selectOne(
                        Wrappers.lambdaQuery(OrderInvoice.class)
                                .eq(OrderInvoice::getOrderId, order.getOrderId())
                );
                mobile = orderInvoice.getMobile();
            }

            if (messageTypeEnum == MessageTypeEnum.NEW_ORDER) {
                content.put("order_sn", order.getOrderSn());
            }

            if (Arrays.asList(MessageTypeEnum.ORDER_PAY, MessageTypeEnum.ORDER_REFUND, MessageTypeEnum.ORDER_PAY_SHOP, MessageTypeEnum.NEW_ORDER_SHOP).contains(messageTypeEnum)) {
                content.put("order", order.getOrderSn());
            }

            if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                content.put("shipping_name", order.getLogisticsName());
                content.put("code", order.getTrackingNo());
            }

            if (Arrays.asList(MessageTypeEnum.ORDER_PAY_SHOP, MessageTypeEnum.NEW_ORDER_SHOP).contains(messageTypeEnum)) {
                content.put("mobile", configService.getConfigVal(SettingsEnum.SMS_SHOP_MOBILE));
                content.put("fee", order.getTotalAmount());
            }

            log.info("发送短信：{}，内容：{}", templateCode, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.SMS)
                    .message(Map.of("mobile", mobile, "template_code", templateCode, "content", content))
                    .build());
        }
    }

    private void sendWebSiteMessage(MessageTemplateListDTO messageTemplates, MessageTypeEnum messageTypeEnum, Order order) {
        if (messageTemplates != null && messageTemplates.getMessage() != null && messageTemplates.getTypeInfo().getIsMessage() == 1 && messageTemplates.getMessage().getDisabled() == 0) {
            String title = messageTemplates.getMessage().getInfo().getTemplateName();
            String content = messageTemplates.getMessage().getInfo().getContent();
            HashMap<String, Object> link = new HashMap<>();
            link.put("path", "order");
            link.put("label", "订单提醒");
            link.put("id", order.getOrderId());
            link.put("name", title);
            content = content.replace("{order_sn}", order.getOrderSn());
            if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                content = content.replace("{logistics_name}", order.getLogisticsName());
                content = content.replace("{tracking_no}", order.getTrackingNo());
            }
            log.info("发送站内信：{}，内容：{}", title, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.SITE)
                    .message(Map.of("user_id", order.getUserId(), "title", title, "content", content, "link", link))
                    .build());
        }
    }

    private MessageTemplateListDTO getMessageTemplateList(MessageTypeEnum messageTypeEnum) {
        MessageType messageType = messageTypeService.selectById(messageTypeEnum.getId());
        if (messageType == null) {
            return null;
        }
        MessageTemplateListDTO result = new MessageTemplateListDTO();
        result.setTypeInfo(messageType);

        MessageTemplateListDTO.MessageTemplateListVO message = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(4, messageTypeEnum, messageType, message, messageType.getIsMessage());
        result.setMessage(message);

        MessageTemplateListDTO.MessageTemplateListVO wechat = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(1, messageTypeEnum, messageType, wechat, messageType.getIsWechat());
        result.setWechat(wechat);

        MessageTemplateListDTO.MessageTemplateListVO miniProgram = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(2, messageTypeEnum, messageType, miniProgram, messageType.getIsMiniProgram());
        result.setMiniProgram(miniProgram);

        MessageTemplateListDTO.MessageTemplateListVO msg = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(3, messageTypeEnum, messageType, msg, messageType.getIsMsg());
        result.setMsg(msg);

        MessageTemplateListDTO.MessageTemplateListVO app = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(5, messageTypeEnum, messageType, app, messageType.getIsApp());
        result.setApp(msg);
        return result;
    }

    private void fillData(Integer type, MessageTypeEnum messageTypeEnum, MessageType messageType, MessageTemplateListDTO.MessageTemplateListVO app, Integer disabled) {
        MessageTemplate appTemplate = messageTemplateService.selectOne(
                Wrappers.lambdaQuery(MessageTemplate.class)
                        .eq(MessageTemplate::getType, type)
                        .eq(MessageTemplate::getMessageId, messageTypeEnum.getId())
        );
        disabled = disabled == -1 ? 1 : 0;
        app.setDisabled(disabled);
        app.setInfo(appTemplate);
    }
}
