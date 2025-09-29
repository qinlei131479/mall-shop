// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserCouponCreateDTO;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.dto.user.UserCouponUpdateDTO;
import com.tigshop.bean.enums.user.UserCouponStatus;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tigshop.common.constant.promotion.CouponConstants.COUPON_NOT_EXIST;
import static com.tigshop.common.constant.promotion.CouponConstants.COUPON_SEND_LIMIT;
import static com.tigshop.common.constant.user.UserCouponConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户优惠券服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class UserCouponServiceImpl extends BaseServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    private final CouponMapper couponMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final TranslatePackageImpl translatePackage;

    @Override
    public Page<UserCouponVO> list(UserCouponListDTO listDTO) {
        Page<UserCoupon> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 获取当前登录用户ID
        Integer userId = getCurrentUserId();
        // 根据条件查询用户优惠券
        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        Long usedTime = listDTO.getUsedTime();
        if (usedTime != null) {
            queryWrapper.eq("used_time", usedTime);
        }
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("CASE " +
                "WHEN used_time = 0 AND start_date <= '" + StringUtils.getCurrentTime() + "' AND end_date >= '" + StringUtils.getCurrentTime() + "' THEN 0 " +
                "ELSE 1 END");
        queryWrapper.orderByAsc("start_date");

        Page<UserCoupon> userCouponPage = this.page(page, queryWrapper);
        List<UserCoupon> userCouponList = userCouponPage.getRecords();
        if (userCouponList != null && !userCouponList.isEmpty()) {
            List<UserCouponVO> userCouponVOList = new java.util.ArrayList<>(userCouponList.stream().map(userCoupon -> {
                UserCouponVO userCouponVO = new UserCouponVO();
                BeanUtils.copyProperties(userCoupon, userCouponVO);
                userCouponVO.setStatus(getStatus(userCoupon.getStartDate(), userCoupon.getEndDate(),
                        userCoupon.getOrderId()));
                userCouponVO.setStatusName(getStatusName(userCoupon.getStartDate(), userCoupon.getEndDate(),
                        userCoupon.getOrderId()));
                userCouponVO.setStartDate(TigUtils.handelTime(userCoupon.getStartDate()));
                userCouponVO.setEndDate(TigUtils.handelTime(userCoupon.getEndDate()));
                //查询优惠券信息
                LambdaQueryWrapper<Coupon> couponQueryWrapper = new LambdaQueryWrapper<>();
                couponQueryWrapper.eq(Coupon::getCouponId, userCoupon.getCouponId());
                Coupon coupon = couponMapper.selectOne(couponQueryWrapper);
                if (coupon != null) {
                    BeanUtils.copyProperties(coupon, userCouponVO);
                    if (coupon.getSendRangeData() != null && !"[]".equals(coupon.getSendRangeData())) {
                        userCouponVO.setSendRangeData(JsonUtil.fromJson(coupon.getSendRangeData(), Integer[].class));
                    } else {
                        userCouponVO.setSendRangeData(new Integer[]{});
                    }
                }
                return userCouponVO;
            }).toList());
//            userCouponVOList.sort(Comparator.comparingInt(UserCouponVO::getStatus));
//            int page =  listDTO.getPage() == null ? 1 : listDTO.getPage();
//            int size = 9;
//            int fromIndex = (page - 1) * size;
//            int toIndex = Math.min(fromIndex + size, userCouponVOList.size());
            //return userCouponVOList.subList(fromIndex, toIndex);
            return PageUtil.convertPage(userCouponPage, userCouponVOList);
        } else {
            return new Page<>();
        }

    }

    /**
     * 获取优惠券状态
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param orderId   订单id
     * @return Integer
     */
    private Integer getStatus(Long startDate, Long endDate, Integer orderId) {
        if (orderId != null && orderId > 0) {
            return UserCouponStatus.STATUS_USED.getCode();
        }

        if (endDate < StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_EXPIRED.getCode();
        }

        if (startDate > StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_NOT_STARTED.getCode();
        }

        if (startDate < StringUtils.getCurrentTime() && StringUtils.getCurrentTime() - startDate < 3 * 24 * 60 * 60) {
            return UserCouponStatus.STATUS_EXPIRING_SOON.getCode();
        }
        return UserCouponStatus.STATUS_NORMAL.getCode();
    }

    private String getStatusName(Long startDate, Long endDate, Integer orderId) {
        if (orderId != null && orderId > 0) {
            return UserCouponStatus.STATUS_USED.getDescription();
        }

        if (endDate < StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_EXPIRED.getDescription();
        }

        if (startDate > StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_NOT_STARTED.getDescription();
        }

        if (startDate < StringUtils.getCurrentTime() &&
                StringUtils.getCurrentTime() - startDate < 3 * 24 * 60 * 60) {
            return UserCouponStatus.STATUS_EXPIRING_SOON.getDescription();
        }
        return UserCouponStatus.STATUS_NORMAL.getDescription();
    }

    @Override
    public CouponDetailVO detail(Integer id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return null;
        }

        Integer userId = getCurrentUserId();
        long count = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, coupon.getCouponId())
                .eq(UserCoupon::getUserId, userId)
                .count();

        CouponDetailVO couponDetailVO = new CouponDetailVO();
        BeanUtils.copyProperties(coupon, couponDetailVO);

        couponDetailVO.setReceiveNum(count);
        couponDetailVO.setUseStartDate(TigUtils.handelTime(coupon.getUseStartDate()));
        couponDetailVO.setUseEndDate(TigUtils.handelTime(coupon.getUseEndDate()));
        couponDetailVO.setAddTime(TigUtils.handelTime(coupon.getAddTime()));

        if (coupon.getSendRangeData() != null && !"[]".equals(coupon.getSendRangeData())) {
            couponDetailVO.setSendRangeData(JsonUtil.fromJson(coupon.getSendRangeData(), Integer[].class));
        } else {
            couponDetailVO.setSendRangeData(new Integer[]{});
        }

        if (coupon.getLimitUserRank() != null && !"[]".equals(coupon.getLimitUserRank())) {
            couponDetailVO.setLimitUserRank(JsonUtil.fromJson(coupon.getLimitUserRank(), Integer[].class));
        } else {
            couponDetailVO.setLimitUserRank(new Integer[]{});
        }

        if (userId > 0 && count > 0 && coupon.getLimitNum() > 0) {
            couponDetailVO.setIsReceive(1);
        } else {
            couponDetailVO.setIsReceive(0);
        }

        return couponDetailVO;
    }

    @Override
    public boolean create(UserCouponCreateDTO createDTO) {
        if (createDTO != null) {
            UserCoupon userCoupon = new UserCoupon();
            BeanUtils.copyProperties(createDTO, userCoupon);
            return this.save(userCoupon);
        }
        return false;
    }

    @Override
    public boolean update(UserCouponUpdateDTO updateDTO) {
        if (updateDTO != null) {
            UserCoupon userCoupon = new UserCoupon();
            BeanUtils.copyProperties(updateDTO, userCoupon);
            return this.updateById(userCoupon);
        }
        return false;
    }

    @Override
    public List<UserCoupon> getUserShopCouponList(Integer shopId) {
        return this.list(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getOrderId, 0)
                .eq(UserCoupon::getUsedTime, 0)
                .eq(UserCoupon::getUserId, SecurityUtils.getCurrentUserId())
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime())
        );
    }

    @Override
    public void claim(UserCouponClaimParam param) {
        Integer userId = getCurrentUserId();
        Integer couponId = param.getCouponId();

        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        Assert.notNull(coupon, () -> new GlobalException(translatePackage.translate(COUPON_NOT_EXIST)));

        // 校验优惠券领取限制
        Long userCouponCount = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getUserId, userId)
                .count();
        Assert.isTrue(userCouponCount < coupon.getLimitNum() || coupon.getLimitNum() == 0, () -> new GlobalException(translatePackage.translate(COUPON_EXCEED_LIMIT)));

        // 校验优惠券发放限制
        Long sendCouponCount = this.lambdaQuery().eq(UserCoupon::getCouponId, couponId).count();
        Assert.isTrue(sendCouponCount < coupon.getSendNum() || coupon.getSendNum() == 0, () -> new GlobalException(translatePackage.translate(COUPON_SEND_LIMIT)));

        // 会员是否能够领取
        if (coupon.getIsNewUser() == 1) {
            // 新人专享
            Assert.isTrue(userCouponCount == 0, () -> new GlobalException(translatePackage.translate(COUPON_ONLY_CLAIM_ONCE)));

            // 判断是否是新人
            long orderCount = orderMapper.selectCount(
                    Wrappers.lambdaQuery(Order.class).eq(Order::getUserId, userId).gt(Order::getPayStatus, 0)
            );
            Assert.isTrue(orderCount == 0, () -> new GlobalException(translatePackage.translate(COUPON_ONLY_CLAIM_FOR_NEW_USER)));
        }

        if (coupon.getIsNewUser() == 2) {
            // 会员专享
            User user = userMapper.selectById(userId);

            if (StringUtils.isNotBlank(coupon.getLimitUserRank()) && !"[]".equals(coupon.getLimitUserRank())) {
                List<Integer> rankIds = JSON.parseArray(coupon.getLimitUserRank(), Integer.class);

                // 如果 rankIds 非空，判断用户的 rankId 是否在限制范围内
                boolean rankFlag = CollUtil.isNotEmpty(rankIds) && !rankIds.contains(user.getRankId());
                Assert.isTrue(!rankFlag, () -> new GlobalException(translatePackage.translate(COUPON_ONLY_CLAIM_FOR_USER_RANK)));
            }
        }

        // 计算使用期限
        long startDate, endDate;
        if (coupon.getSendType() > 0) {
            //固定时间
            startDate = coupon.getUseStartDate();
            endDate = coupon.getUseEndDate();
        } else {
            //领取天数
            startDate = StringUtils.getCurrentTime() + coupon.getDelayDay() * 24 * 60 * 60;
            endDate = startDate + coupon.getUseDay() * 24 * 60 * 60;
        }
        UserCoupon userCoupon = UserCoupon.builder()
                .couponId(coupon.getCouponId())
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        this.save(userCoupon);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void useCoupon(List<Integer> useCouponIds, Integer currentUserId, Integer orderId) {
        // 去重
        useCouponIds = useCouponIds.stream().distinct().toList();
        for (Integer useCouponId : useCouponIds) {
            UserCoupon userCoupon = this.getOne(
                    new LambdaUpdateWrapper<UserCoupon>()
                            .eq(UserCoupon::getCouponId, useCouponId)
                            .eq(UserCoupon::getUserId, currentUserId)
                            .eq(UserCoupon::getOrderId, 0)
                            .last("limit 1")
            );
            if (userCoupon != null) {
                userCoupon.setOrderId(orderId);
                userCoupon.setUsedTime(StringUtils.getCurrentTime());
                this.updateById(userCoupon);
            } else {
                throw new RuntimeException(translatePackage.translate("优惠券不存在"));
            }
        }
    }

    @Override
    public Long getUserCouponCount(int userId) {
        Long time = StringUtils.getCurrentTime();
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);
        queryWrapper.eq(UserCoupon::getOrderId, 0);
        queryWrapper.le(UserCoupon::getStartDate, time);
        queryWrapper.ge(UserCoupon::getEndDate, time);
        return this.count(queryWrapper);
    }

    @Override
    public UserCoupon getUserCouponByCouponId(Integer currentUserId, Integer relationId) {

        return this.getOne(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, currentUserId)
                .eq(UserCoupon::getCouponId, relationId).eq(UserCoupon::getOrderId, 0)
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime()).last("limit 1"));
    }

    @Override
    public List<OrderCheckVO.Coupon> listUserCouponList(Integer currentUserId, Integer shopId) {
        List<OrderCheckVO.Coupon> coupons = this.baseMapper.listUserCouponList(currentUserId, shopId);
        for (OrderCheckVO.Coupon coupon : coupons) {
            String endDate = coupon.getEndDate();
            if (StringUtils.isNotBlank(endDate)) {
                coupon.setEndDate(TigUtils.handelTime(Long.valueOf(endDate)));
            } else {
                coupon.setEndDate("");
            }
        }

        return coupons;
    }
}
