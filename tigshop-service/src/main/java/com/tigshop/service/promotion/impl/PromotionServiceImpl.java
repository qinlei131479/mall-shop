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

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.PromotionCreateDTO;
import com.tigshop.bean.dto.promotion.PromotionListDTO;
import com.tigshop.bean.dto.promotion.PromotionUpdateDTO;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.vo.promotion.PromotionCountVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 营销管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl extends BaseServiceImpl<PromotionMapper, Promotion> implements PromotionService {
    private final CouponMapper couponMapper;

    @Override
    public Page<PromotionVO> list(PromotionListDTO listDTO) {
        // 分页
        Page<Promotion> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Promotion> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Promotion::getPromotionName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > 0) {
            queryWrapper.eq(Promotion::getShopId, shopId);
        }

        String type = listDTO.getType();
        if (StrUtil.isNotEmpty(type)) {
            // 字符串转数组
            List<Integer> typeList = StringUtils.str2IntList(type);
            queryWrapper.in(Promotion::getType, typeList);
        }

        Integer timeType = listDTO.getTimeType();
        if (timeType != null && timeType > 0) {
            long currentTime = StringUtils.getCurrentTime();
            if (timeType == 1) {
                // 进行中
                queryWrapper.and(i -> i.and(j -> j.le(Promotion::getStartTime, currentTime).ge(Promotion::getEndTime, currentTime))
                        .or(j -> j.eq(Promotion::getStartTime, 0).eq(Promotion::getEndTime, 0)));
            } else if (timeType == 2) {
                // 7天内到期
                queryWrapper.le(Promotion::getStartTime, currentTime).le(Promotion::getEndTime, currentTime + 7 * 24 * 60 * 60).ge(Promotion::getEndTime, currentTime).ne(Promotion::getEndTime, 0);
            } else if (timeType == 3) {
                // 未开始
                queryWrapper.gt(Promotion::getStartTime, currentTime);
            }
        }

        queryWrapper.eq(Promotion::getIsDelete, 0);
        queryWrapper.eq(Promotion::getIsAvailable, 1);

        // 执行查询
        Page<Promotion> timeDiscountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Promotion> timeDiscountPageRecords = timeDiscountPage.getRecords();
        // 转换为VO
        List<PromotionVO> timeDiscountVOList = timeDiscountPageRecords.stream()
                .map(this::convert).toList();
        return PageUtil.convertPage(timeDiscountPage, timeDiscountVOList);
    }

    /**
     * 格式转换
     *
     * @param promotion promotion
     */
    public PromotionVO convert(Promotion promotion) {
        PromotionVO promotionVO = new PromotionVO();
        BeanUtils.copyProperties(promotion, promotionVO);
        // 时间转换
        Long startTime = promotion.getStartTime();
        if (startTime > 0) {
            DateTime startDate = DateUtil.date(startTime * 1000);
            promotionVO.setStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            promotionVO.setStartTime("0");
        }

        Long endTime = promotion.getEndTime();
        if (endTime > 0) {
            DateTime endDate = DateUtil.date(endTime * 1000);
            promotionVO.setEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            promotionVO.setEndTime("0");
        }
        if (promotion.getStartTime() == 0 && promotion.getEndTime() == 0) {
            promotionVO.setTimeText("长期有效");
        } else {
            promotionVO.setTimeText(TigUtils.handelTime(promotion.getStartTime()) + " 至 " +
                    TigUtils.handelTime(promotion.getEndTime()));
        }

        // json 转换
        promotionVO.setRangeData(StringUtils.str2IntList(promotion.getRangeData()));
        if (promotion.getSkuIds() != null) {
            promotionVO.setSkuIds(StringUtils.str2IntList(promotion.getSkuIds()));
        }

        // 显示类型
        promotionVO.setTypeText(PromotionType.getStatusName(promotion.getType()));
        return promotionVO;
    }

    @Override
    public boolean create(PromotionCreateDTO createDTO) {
        if (createDTO != null) {
            Promotion promotion = new Promotion();

            Integer shopId = getShopId();
            promotion.setShopId(shopId);

            // 数组转json
            promotion.setRangeData(createDTO.getRangeData().toString());
            if (createDTO.getSkuIds() != null) {
                promotion.setSkuIds(createDTO.getSkuIds().toString());
            }

            BeanUtils.copyProperties(createDTO, promotion);
            return this.save(promotion);
        }
        return false;
    }

    @Override
    public boolean update(PromotionUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Promotion promotion = new Promotion();

            // 数组转jason
            promotion.setRangeData(updateDTO.getRangeData().toString());
            if (updateDTO.getSkuIds() != null) {
                promotion.setSkuIds(updateDTO.getSkuIds().toString());
            }

            BeanUtils.copyProperties(updateDTO, promotion);
            return this.updateById(promotion);
        }
        return false;
    }

    @Override
    public PromotionCountVO getCount() {
        Integer shopId = getShopId();

        LambdaQueryWrapper<Promotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                                        j -> j.le(Promotion::getStartTime, StringUtils.getCurrentTime())
                                                .ge(Promotion::getEndTime, StringUtils.getCurrentTime())
                                ).or(k -> k.eq(Promotion::getEndTime, 0))
                                .or(l -> l.eq(Promotion::getStartTime, 0))
                );
        List<Promotion> countOne = this.list(queryWrapper);

        LambdaQueryWrapper<Promotion> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                                j -> j.le(Promotion::getStartTime, StringUtils.getCurrentTime())
                                        .ge(Promotion::getEndTime, StringUtils.getCurrentTime())
                        ).and(k -> k.le(Promotion::getEndTime, StringUtils.getCurrentTime() + 7 * 24 * 60 * 60))
                        .and(l -> l.ne(Promotion::getEndTime, 0)));

        List<Promotion> countTwo = this.list(queryWrapper1);

        LambdaQueryWrapper<Promotion> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                        j -> j.ge(Promotion::getStartTime, StringUtils.getCurrentTime())
                ));

        List<Promotion> countThree = this.list(queryWrapper2);
        return PromotionCountVO.builder()
                .timeType1Count(countOne.size())
                .timeType2Count(countTwo.size())
                .timeType3Count(countThree.size())
                .build();
    }

    /**
     * 获取所有可用的促销活动
     *
     * @param shopId shopId
     * @return List<Promotion>
     */
    @Override
    public List<PromotionVO> getAllAvailablePromotion(Integer shopId) {
        Long currentTime = StringUtils.getCurrentTime();

        List<Promotion> promotions = this.lambdaQuery()
                .eq(Promotion::getIsAvailable, Constants.YES)
                .eq(shopId != null, Promotion::getShopId, shopId)
                .and(q -> q.le(Promotion::getStartTime, currentTime).ge(Promotion::getEndTime, currentTime)
                        .or(query -> query.eq(Promotion::getStartTime, 0).eq(Promotion::getEndTime, 0))
                )
                .last("order by FIELD(`type`, 1,6,2,3,4,5)")
                .list();
        // 移除 promotions 中 type 为 2 得
        promotions.removeIf(promotion -> {
            if (promotion.getType() == PromotionType.TYPE_COUPON.getCode()) {
                Coupon byId = couponMapper.selectById(promotion.getRelationId());
                return byId.getIsShow() == 0;
            }
            return false;
        });
        return promotions.stream().map(this::convert).toList();
    }

}
