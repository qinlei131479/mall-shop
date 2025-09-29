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

package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.promotion.CouponDTO;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 优惠券服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class CouponServiceImpl extends BaseServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final AdminLogService adminLogService;

    private final PromotionService promotionService;

    private final UserRankService userRankService;

    private final UserCouponMapper userCouponMapper;
    private final TranslatePackage translatePackage;

    @Override
    public Page<CouponVO> list(CouponPageQuery listDTO) {
        // 分页
        Page<Coupon> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Coupon::getCouponName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > -1) {
            queryWrapper.eq(Coupon::getShopId, shopId);
        }
        queryWrapper.eq(Coupon::getIsDelete, 0);
        // 执行查询
        Page<Coupon> couponPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Coupon> couponPageRecords = couponPage.getRecords();
        // 转换为VO
        List<CouponVO> couponVOList = couponPageRecords.stream()
                .map(coupon -> {
                    CouponVO couponVO = new CouponVO();
                    convert(coupon, couponVO);
                    BeanUtils.copyProperties(coupon, couponVO);
                    return couponVO;
                }).toList();
        return PageUtil.convertPage(couponPage, couponVOList);
    }

    /**
     * 数据转换
     *
     * @param coupon   coupon
     * @param couponVO couponVO
     */
    public void convert(Coupon coupon, CouponVO couponVO) {
        // 时间转换
        Long useStartTime = coupon.getUseStartDate();
        if (useStartTime > 0) {
            DateTime useStartDate = DateUtil.date(useStartTime * 1000);
            couponVO.setUseStartDate(DateUtil.format(useStartDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            couponVO.setUseStartDate("0");
        }

        Long useEndTime = coupon.getUseEndDate();
        if (useEndTime > 0) {
            DateTime useEndDate = DateUtil.date(useEndTime * 1000);
            couponVO.setUseEndDate(DateUtil.format(useEndDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            couponVO.setUseEndDate("0");
        }

        DateTime addDate = DateUtil.date(coupon.getAddTime() * 1000);
        couponVO.setAddTime(DateUtil.format(addDate, "yyyy-MM-dd HH:mm:ss"));

        // json 转换
        couponVO.setSendRangeData(StringUtils.str2IntList(coupon.getSendRangeData()));
        couponVO.setLimitUserRank(StringUtils.str2IntList(coupon.getLimitUserRank()));
    }

    @Override
    public CouponVO detail(Integer id) {
        if (id != null && id > 0) {
            Coupon coupon = this.getById(id);
            CouponVO couponVO = new CouponVO();
            convert(coupon, couponVO);
            BeanUtils.copyProperties(coupon, couponVO);
            return couponVO;
        }
        return null;
    }

    /**
     * 数据判断
     *
     * @param dto    dto
     * @param coupon coupon
     */
    public void dataJudge(CouponDTO dto, Coupon coupon) {
        if ((StrUtil.isNotBlank(dto.getUseStartDate()) && !"0".equals(dto.getUseStartDate())) && (StrUtil.isNotBlank(dto.getUseEndDate()) && !"0".equals(dto.getUseEndDate()))) {
            coupon.setUseStartDate(StringUtils.dateToTimestampExample(dto.getUseStartDate()));
            coupon.setUseEndDate(StringUtils.dateToTimestampExample(dto.getUseEndDate()));
        }
        if (dto.getSendType() == 0) {
            dto.setUseStartDate("0");
            dto.setUseEndDate("0");
        } else {
            dto.setDelayDay(0);
            dto.setUseDay(0);
        }
        Integer reduceType = dto.getReduceType();
        if (reduceType != null && reduceType == 2) {
            dto.setMinOrderAmount(BigDecimal.valueOf(0));
        }

        Integer shopId = getShopId();
        shopId = shopId == null ? 0 : shopId;
        coupon.setShopId(shopId);

        if (dto.getCouponType() == 1) {
            dto.setCouponDiscount(BigDecimal.valueOf(0));
        }
        // 数组转json
        if (dto.getSendRangeData() == null) {
            coupon.setSendRangeData("[]");
        } else {
            coupon.setSendRangeData(dto.getSendRangeData().toString());
        }
        if (dto.getLimitUserRank() == null) {
            coupon.setLimitUserRank("[]");
        } else {
            coupon.setLimitUserRank(dto.getLimitUserRank().toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(CouponDTO createDTO) {
        if (createDTO != null) {
            Coupon coupon = new Coupon();
            dataJudge(createDTO, coupon);
            // 当前时间
            coupon.setAddTime(StringUtils.getCurrentTime());
            BeanUtils.copyProperties(createDTO, coupon);
            boolean isSave = this.save(coupon);
            if (isSave) {
                // 营销活动写入
                savePromotion(coupon);
                adminLogService.createByLogInfo(StrUtil.format("新增优惠券:{}", coupon.getCouponName()));
            }
            return isSave;
        }
        return false;
    }

    /**
     * 营销活动写入
     *
     * @param coupon coupon
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePromotion(Coupon coupon) {
        Integer relationId = coupon.getCouponId();
        relationId = relationId == null ? 0 : relationId;
        Long startTime = 0L;
        Long endTime = 0L;
        if (coupon.getSendType() > 0) {
            startTime = coupon.getUseStartDate();
            endTime = coupon.getUseEndDate();
        }
        // 根据 relationId 查询是否有营销活动
        if (promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())) > 0) {
            // 更新部分字段
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                    .set(Promotion::getStartTime, startTime)
                    .set(Promotion::getEndTime, endTime)
                    .set(Promotion::getPromotionName, coupon.getCouponName())
                    .set(Promotion::getIsAvailable, coupon.getIsShow())
                    .set(Promotion::getRange, coupon.getSendRange())
                    .set(Promotion::getRangeData, coupon.getSendRangeData()));
        } else {
            Promotion promotion = new Promotion();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            promotion.setPromotionName(coupon.getCouponName());
            promotion.setStartTime(startTime);
            promotion.setEndTime(endTime);
            promotion.setType(PromotionType.TYPE_COUPON.getCode());
            promotion.setShopId(shopId);
            promotion.setRelationId(coupon.getCouponId());
            promotion.setRange(coupon.getSendRange());
            promotion.setRangeData(coupon.getSendRangeData());
            promotion.setIsAvailable(coupon.getIsShow());

            boolean isSuccess = promotionService.save(promotion);
            if (!isSuccess) {
                throw new GlobalException(SERVICE_ERROR);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CouponDTO updateDTO) {
        Coupon coupon = new Coupon();
        dataJudge(updateDTO, coupon);
        BeanUtils.copyProperties(updateDTO, coupon);
        boolean isSuccess = this.updateById(coupon);

        if (isSuccess) {
            savePromotion(coupon);
            adminLogService.createByLogInfo(StrUtil.format("修改优惠券:{}", coupon.getCouponName()));
        }
    }

    /**
     * 删除
     *
     * @param id id
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        return softDel(id);
    }

    /**
     * 软删除
     *
     * @param id id
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean softDel(Integer id) {
        boolean update = this.lambdaUpdate()
                .set(Coupon::getIsDelete, 1)
                .eq(Coupon::getCouponId, id)
                .update();
        if (update) {
            // 软删除
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                    .set(Promotion::getIsDelete, 1));
        }
        return update;
    }

    /**
     * 批量操作
     *
     * @param batchDTO 批量操作参数
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            List<Integer> ids = batchDTO.getIds();
            // 批量软删除
            boolean isMove = this.update(new LambdaUpdateWrapper<Coupon>()
                    .in(Coupon::getCouponId, ids)
                    .set(Coupon::getIsDelete, 1));
            if (isMove) {
                // 批量软删除
                promotionService.update(new LambdaUpdateWrapper<Promotion>()
                        .in(Promotion::getRelationId, ids)
                        .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                        .set(Promotion::getIsDelete, 1));
            }
            return isMove;
        }
        return false;
    }

    /**
     * 配置项
     *
     * @return PromotionConfigVO
     */
    @Override
    public List<UserRank> config() {
        return userRankService.list();
    }

    @Override
    public Page<CouponClientVO> getList(CouponPageQuery pageQuery) {
        Page<Coupon> page = buildSortOrder(pageQuery);

        Integer userId = getCurrentUserId();
        boolean shopFlag = pageQuery.getShopId() != null && pageQuery.getShopId() != -1;
        boolean validDateFlag = pageQuery.getValidDate() != null && pageQuery.getValidDate() != -1;
        boolean receiveFlag = pageQuery.getReceiveFlag() != null && pageQuery.getReceiveFlag() == 1;

        Page<Coupon> couponPage = this.lambdaQuery()
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsShow, pageQuery.getIsShow())
                .eq(receiveFlag && shopFlag, Coupon::getShopId, pageQuery.getShopId())
                .and(receiveFlag && validDateFlag, query ->
                        query.and(query1 ->
                                        query1.ge(Coupon::getUseEndDate, StringUtils.getCurrentTime()))
                                .or()
                                .eq(Coupon::getSendType, 0)
                )
                .orderByDesc(receiveFlag, Coupon::getAddTime)
                .page(page);

        List<Coupon> coupons = couponPage.getRecords();
        if (CollUtil.isEmpty(coupons)) {
            return PageUtil.convertPage(couponPage, Collections.emptyList());
        }

        List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).toList();
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class).in(UserCoupon::getCouponId, couponIds).eq(UserCoupon::getUserId, userId)
        );


        List<CouponClientVO> resultCoupons = coupons.stream()
                .map(coupon -> new CouponClientVO(coupon, userCoupons))
                .toList();
        return PageUtil.convertPage(couponPage, resultCoupons);
    }

    @Override
    public List<CouponDetailVO> getProductCouponList(int productId, int shopId) {
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        Long currentTime = StringUtils.getCurrentTime();
        queryWrapper.eq(Coupon::getShopId, shopId)
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsShow, 1)
                .and(i -> i.and(j -> j.le(Coupon::getUseStartDate, currentTime)
                        .ge(Coupon::getUseEndDate, currentTime)).or().eq(Coupon::getSendType, 0));

        List<Coupon> coupons = this.list(queryWrapper);

        // 使用 filter 筛选符合条件的优惠券
        return coupons.stream()
                .filter(coupon -> {
                    String sendRangeData = coupon.getSendRangeData();
                    List<Integer> sendRangeDataList = StringUtils.str2IntList(sendRangeData);
                    Integer sendRange = coupon.getSendRange();

                    // 过滤不符合条件的优惠券
                    return (sendRange != 3 || sendRangeDataList.contains(productId)) && (sendRange != 4 || !sendRangeDataList.contains(productId));
                })
                .map(coupon -> {
                    // 复制属性到 VO 对象
                    CouponDetailVO couponDetailVO = new CouponDetailVO();
                    BeanUtils.copyProperties(coupon, couponDetailVO);
                    couponDetailVO.setReceiveNum(getReceiveNumAttr(coupon.getCouponId()));
                    couponDetailVO.setUseStartDate(TigUtils.handelTime(coupon.getUseStartDate()));
                    couponDetailVO.setUseEndDate(TigUtils.handelTime(coupon.getUseEndDate()));
                    return couponDetailVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        if (promotionVO.getIsReceive() == null || promotionVO.getIsReceive() != 1) {
            return null;
        }
        Coupon coupon = this.getById(promotionVO.getRelationId());
        //看是否满足优惠券要求
        if (coupon.getCouponUnit() == 1 && (coupon.getMinOrderAmount().compareTo(cartList.stream().map(CartVO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)) <= 0 || coupon.getReduceType() == 2)
                || coupon.getCouponUnit() == 2 && coupon.getMinOrderAmount().compareTo(BigDecimal.valueOf(cartList.stream().mapToInt(CartVO::getQuantity).sum())) <= 0
        ) {
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal priceAmount = BigDecimal.ZERO;
            promotionVO.setAmount(BigDecimal.ZERO);
            for (CartVO cartVO : cartList) {
                priceAmount = priceAmount.add(cartVO.getSubtotal());
            }
            if (coupon.getCouponType() == 1) {
                if (priceAmount.subtract(coupon.getCouponMoney()).compareTo(BigDecimal.ZERO) <= 0) {
                    promotionVO.setAmount(coupon.getCouponMoney());
                } else {
                    promotionVO.setAmount(coupon.getCouponMoney());
                    discount = priceAmount.subtract(coupon.getCouponMoney()).divide(priceAmount, 6, RoundingMode.HALF_UP);
                }
            } else {
                BigDecimal checkDiscountAmount = priceAmount.multiply(BigDecimal.valueOf(1 - coupon.getCouponDiscount().divide(BigDecimal.valueOf(10), 6, RoundingMode.HALF_UP).doubleValue()));
                if (coupon.getMaxOrderAmount().compareTo(BigDecimal.ZERO) > 0 && checkDiscountAmount.compareTo(coupon.getMaxOrderAmount()) > 0) {
                    checkDiscountAmount = coupon.getMaxOrderAmount();
                }
                if (priceAmount.subtract(checkDiscountAmount).compareTo(BigDecimal.ZERO) > 0) {
                    // discount = BigDecimal.ZERO;
                    discount = priceAmount.subtract(checkDiscountAmount).divide(priceAmount, 6, RoundingMode.HALF_UP);
                }
                //降checkDiscountAmount保留2位小数
                checkDiscountAmount = checkDiscountAmount.setScale(6, RoundingMode.HALF_UP);
                promotionVO.setAmount(checkDiscountAmount);
            }
            for (CartVO cartVO : cartList) {
                if (coupon.getCouponType() == 1) {
                    cartVO.setPrice(discount.multiply(cartVO.getProductPrice()).setScale(2, RoundingMode.HALF_UP));
                } else {
                    cartVO.setPrice(discount.multiply(cartVO.getProductPrice()).setScale(2, RoundingMode.HALF_UP));
                    //promotionVO.setAmount(promotionVO.getAmount().add(cartVO.getProductPrice().subtract(cartVO.getPrice())));
                }
            }
            CartPromotionParsePriceDTO cartPromotionParam = new CartPromotionParsePriceDTO();
            cartPromotionParam.setCartList(cartList);
            cartPromotionParam.setPromotion(promotionVO);
            return cartPromotionParam;
        }
        return null;
    }

    @Override
    public String getCouponPromotionDesc(Coupon coupon) {
        if (coupon.getReduceType() == 1) {
            if (coupon.getCouponType() == 1) {
                String desc = translatePackage.translate("满")+" %s"+translatePackage.translate("减")+" %s";
                return String.format(desc, coupon.getMinOrderAmount(), coupon.getCouponMoney());
            } else {
                String desc = translatePackage.translate("满")+" %s"+
                        translatePackage.translate("打")+" %s"+translatePackage.translate("折");
                return String.format(desc, coupon.getMinOrderAmount(), coupon.getCouponDiscount());
            }
        } else {
            if (coupon.getCouponType() == 1) {
                String desc = translatePackage.translate("立减")+" %s";
                return String.format(desc, coupon.getCouponMoney());
            } else {
                String desc = translatePackage.translate("打")+" %s"+translatePackage.translate("折");
                return String.format(desc, coupon.getCouponDiscount());
            }
        }
    }

    /**
     * 获取优惠券领取数量
     *
     * @param couponId 优惠券ID
     * @return long
     */
    public long getReceiveNumAttr(Integer couponId) {
        if (couponId != null) {
            LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserCoupon::getCouponId, couponId)
                    .eq(UserCoupon::getUserId, SecurityUtils.getCurrentUserId());
            return userCouponMapper.selectCount(queryWrapper);
        }
        return 0;
    }
}
