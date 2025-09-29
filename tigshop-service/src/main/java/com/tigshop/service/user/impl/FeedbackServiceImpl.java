// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.dto.user.FeedbackCreateDTO;
import com.tigshop.bean.dto.user.FeedbackUpdateDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.user.UserFeedbackStatus;
import com.tigshop.bean.enums.user.UserFeedbackType;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.user.UserFeedbackDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.user.FeedbackMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.FeedbackService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.user.FeedbackConstants.USER_IS_NULL;
import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 会员留言
 *
 * @author baishang
 */
@Service
public class FeedbackServiceImpl extends BaseServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    @Resource
    UserService userService;
    @Resource
    OrderService orderService;
    @Resource
    AdminUserService adminUserService;
    @Resource
    ProductService productService;
    @Resource
    ShopService shopService;
    @Resource
    AdminMsgService adminMsgService;
    @Resource
    private TranslatePackageImpl translatePackage;

    @Override
    public Page<UserFeedbackVO> list(FeedBackListPageQuery pageQuery) {

        // 分页参数
        Page<Feedback> page = buildSortOrder(pageQuery);

        List<String> types = StrUtil.split(pageQuery.getType(), StrUtil.COMMA);

        Page<Feedback> feedbackPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Feedback::getContent, pageQuery.getKeyword())
                .in(CollUtil.isNotEmpty(types), Feedback::getType, types)
                .eq(pageQuery.getIsOrder() != null && pageQuery.getIsOrder() == 1, Feedback::getOrderId, 0)
                .gt(pageQuery.getIsOrder() != null && pageQuery.getIsOrder() > 1, Feedback::getOrderId, 0)
                .eq(pageQuery.getProductId() != null, Feedback::getProductId, pageQuery.getProductId())
                .eq(pageQuery.getStatus() != null, Feedback::getStatus, pageQuery.getStatus())
                .eq(pageQuery.getUserId() != null && pageQuery.getUserId() > 0, Feedback::getUserId, pageQuery.getUserId())
                .page(page);

        List<Feedback> feedbackList = feedbackPage.getRecords();
        List<UserFeedbackVO> list = feedbackList.stream()
                .map(feedback -> {
                    UserFeedbackVO userFeedbackVO = new UserFeedbackVO();
                    BeanUtils.copyProperties(feedback, userFeedbackVO);
                    userFeedbackVO.setAddTime(TigUtils.handelTime(feedback.getAddTime()));
                    userFeedbackVO.setTypeName(UserFeedbackType.getTypeName(feedback.getType()));
                    LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
                    queryWrapperUser.eq(User::getUserId, feedback.getUserId());
                    User user = userService.getOne(queryWrapperUser);
                    if (user != null) {
                        userFeedbackVO.setNickname(user.getNickname());
                    }
                    LambdaQueryWrapper<Order> queryWrapperOrder = new LambdaQueryWrapper<>();
                    queryWrapperOrder.eq(Order::getOrderId, feedback.getOrderId());
                    Order order = orderService.getOne(queryWrapperOrder);
                    if (order != null) {
                        userFeedbackVO.setOrderSn(order.getOrderSn());
                    }
                    LambdaQueryWrapper<Feedback> queryWrapperFeedback = new LambdaQueryWrapper<>();
                    queryWrapperFeedback.eq(Feedback::getParentId, feedback.getId());
                    queryWrapperFeedback.orderByDesc(Feedback::getId);
                    queryWrapperFeedback.last("limit 1");
                    Feedback feedbackReply = this.getOne(queryWrapperFeedback);
                    if (feedbackReply != null) {
                        UserFeedbackVO.Reply reply = new UserFeedbackVO.Reply();
                        BeanUtils.copyProperties(feedbackReply, reply);
                        reply.setAddTime(TigUtils.handelTime(feedbackReply.getAddTime()));
                        userFeedbackVO.setReply(reply);
                    }
                    return userFeedbackVO;
                })
                .toList();
        return PageUtil.convertPage(feedbackPage, list);
    }

    @Override
    public UserFeedbackDetailVO detail(Integer id) {
        Feedback feedback = this.getById(id);
        UserFeedbackDetailVO feedbackVO = new UserFeedbackDetailVO();
        BeanUtils.copyProperties(feedback, feedbackVO);
        feedbackVO.setAddTime(TigUtils.handelTime(feedback.getAddTime()));
        feedbackVO.setStatusName(UserFeedbackStatus.getStatusName(feedback.getStatus()));
        feedbackVO.setTypeName(UserFeedbackType.getTypeName(feedback.getType()));

        LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
        queryWrapperUser.eq(User::getUserId, feedback.getUserId());
        User user = userService.getOne(queryWrapperUser);
        if (user != null) {
            feedbackVO.setNickname(user.getNickname());
        }

        if (feedback.getOrderId() > 0) {
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId, feedback.getOrderId());
            Order order = orderService.getOne(queryWrapper);
            if (order != null) {
                UserFeedbackDetailVO.OrderInfo orderInfo = new UserFeedbackDetailVO.OrderInfo();
                BeanUtils.copyProperties(order, orderInfo);
                feedbackVO.setOrderInfo(orderInfo);
                feedbackVO.setOrderSn(order.getOrderSn());
            }
        }

        if (feedback.getProductId() > 0) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getProductId, feedback.getProductId());
            Product product = productService.getOne(queryWrapper);
            if (product != null) {
                UserFeedbackDetailVO.Product productVO = new UserFeedbackDetailVO.Product();
                BeanUtils.copyProperties(product, productVO);
                feedbackVO.setProduct(productVO);
            }
        }

        if (feedback.getShopId() > 0) {
            LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Shop::getShopId, feedback.getShopId());
            Shop shop = shopService.getOne(queryWrapper);
            if (shop != null) {
                UserFeedbackDetailVO.Shop shopVO = new UserFeedbackDetailVO.Shop();
                BeanUtils.copyProperties(shop, shopVO);
                shopVO.setShopTitle(shop.getShopTitle());
            }
        }

        if (feedback.getParentId() > 0) {
            LambdaQueryWrapper<Feedback> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Feedback::getId, feedback.getParentId());
            Feedback feedbackReply = this.getOne(queryWrapper);
            if (feedbackReply != null) {
                UserFeedbackDetailVO.Reply reply = new UserFeedbackDetailVO.Reply();
                BeanUtils.copyProperties(feedbackReply, reply);
                feedbackVO.setReply(reply);
            }
        }

        if (StrUtil.isNotBlank(feedback.getFeedbackPics()) && feedback.getFeedbackPics().length() > 3) {
            feedbackVO.setFeedbackPics(JsonUtil.jsonToList(feedback.getFeedbackPics(), String.class));
        }
        return feedbackVO;
    }

    @Override
    public boolean submit(FeedbackCreateDTO dto) {
        Integer userId = getCurrentUserId();
        if (dto != null) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUserId, userId);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                throw new GlobalException(translatePackage.translate(USER_IS_NULL));
            }
            Feedback feedback = new Feedback();
            BeanUtils.copyProperties(dto, feedback);
            if (dto.getFeedbackPics() != null && dto.getFeedbackPics().length > 0) {
                feedback.setFeedbackPics(JsonUtil.toJson(dto.getFeedbackPics()));
            } else {
                feedback.setFeedbackPics("[]");
            }
            feedback.setUserId(userId);
            feedback.setUsername(user.getUsername());
            feedback.setAddTime(StringUtils.getCurrentTime());
            if (feedback.getOrderId() != null) {
                LambdaQueryWrapper<Order> queryWrapperOrder = new LambdaQueryWrapper<>();
                queryWrapperOrder.eq(Order::getOrderId, feedback.getOrderId());
                Order order = orderService.getOne(queryWrapperOrder);
                if (order != null) {
                    feedback.setShopId(order.getShopId());
                }
            }
            boolean save = save(feedback);

            //记录管理员消息
            AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
            adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.FEEDBACK.getCatId());
            adminMsgCreateDTO.setTitle("您有一个新的意见反馈");
            adminMsgCreateDTO.setContent("用户" + user.getUsername() + "提交了一个新的意见反馈");
            adminMsgCreateDTO.setShopId(0);
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("id", feedback.getId());
            adminMsgCreateDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(adminMsgCreateDTO);

            return save;
        }
        return false;
    }

    @Override
    public boolean update(FeedbackUpdateDTO dto) {
        if (dto != null) {
            Integer currentAdminId = getCurrentAdminId();
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getAdminId, currentAdminId);
            AdminUser adminUser = adminUserService.getOne(queryWrapper);
            if (dto.getParentId() != null && dto.getParentId() == 0) {
                Feedback feedback = new Feedback();
                BeanUtils.copyProperties(dto, feedback);
                feedback.setId(null);
                feedback.setAddTime(StringUtils.getCurrentTime());
                feedback.setUserId(currentAdminId);
                feedback.setUsername(adminUser.getUsername());
                if (dto.getFeedbackPics() != null && dto.getFeedbackPics().length > 0) {
                    feedback.setFeedbackPics(JsonUtil.toJson(dto.getFeedbackPics()));
                } else {
                    feedback.setFeedbackPics("[]");
                }
                feedback.setParentId(dto.getId());
                feedback.setTitle("reply");
                this.save(feedback);
                Feedback replyFeedback = this.getById(dto.getId());
                replyFeedback.setStatus(1);
                return this.updateById(replyFeedback);
            } else {
                LambdaQueryWrapper<Feedback> queryWrapperFeedback = new LambdaQueryWrapper<>();
                queryWrapperFeedback.eq(Feedback::getParentId, dto.getParentId());
                Feedback feedback = this.getOne(queryWrapperFeedback);
                feedback.setUserId(currentAdminId);
                feedback.setUsername(adminUser.getUsername());
                feedback.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
                feedback.setMobile(dto.getMobile() != null ? dto.getMobile() : "");
                feedback.setContent(dto.getContent() != null ? dto.getContent() : "");
                if (dto.getFeedbackPics() != null && dto.getFeedbackPics().length > 0) {
                    feedback.setFeedbackPics(JsonUtil.toJson(dto.getFeedbackPics()));
                } else {
                    feedback.setFeedbackPics("[]");
                }
                return this.updateById(feedback);
            }
        }
        return false;
    }

}
