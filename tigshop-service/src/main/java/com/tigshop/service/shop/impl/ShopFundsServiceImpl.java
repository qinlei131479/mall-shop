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

package com.tigshop.service.shop.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.ShopFundsCreateDTO;
import com.tigshop.bean.dto.shop.ShopFundsListDTO;
import com.tigshop.bean.dto.shop.ShopFundsUpdateDTO;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.shop.ShopFunds;
import com.tigshop.bean.vo.shop.ShopFundsVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.shop.ShopFundsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.shop.ShopFundsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ShopFundsServiceImpl extends BaseServiceImpl<ShopFundsMapper, ShopFunds> implements ShopFundsService {
    @Resource
    OrderService orderService;
    @Override
    public Page<ShopFundsVO> list(ShopFundsListDTO listDTO) {
        // 分页
        Page<ShopFunds> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShopFunds> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        queryWrapper.ne(ShopFunds::getShopType, ShopTypeEnum.PICKUP.getCode());
        // 状态筛选
        Integer status = listDTO.getStatus();
        if (status != null && status > 0) {
            queryWrapper.eq(ShopFunds::getStatus, status);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ShopFunds::getShopTitle, keyword);
        }
        // 执行查询
        Page<ShopFunds> shopFundsPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShopFunds> shopFundsPageRecords = shopFundsPage.getRecords();
        // 转换为VO
        List<ShopFundsVO> shopFundsVOList = shopFundsPageRecords.stream()
                .map(shopFunds -> {
                    ShopFundsVO shopFundsVO = new ShopFundsVO();
                    BeanUtils.copyProperties(shopFunds, shopFundsVO);
                    long timestampInSeconds = shopFunds.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    shopFundsVO.setAddTime(sdf.format(date));
                    shopFundsVO.setStatusText(ShopType.getTypeName(shopFunds.getStatus()));
                    BigDecimal sumOrderUnSettlementAmount = orderService.getOrderUnSettlementAmount(shopFunds.getShopId());
                    shopFundsVO.setUnSettlementOrder(sumOrderUnSettlementAmount);
                    return shopFundsVO;
                }).toList();
        return PageUtil.convertPage(shopFundsPage, shopFundsVOList);
    }

    @Override
    public ShopFundsVO detail(Integer id) {
        if (id != null) {
            ShopFunds shopFunds = this.getById(id);
            ShopFundsVO shopFundsVO = new ShopFundsVO();
            BeanUtils.copyProperties(shopFunds, shopFundsVO);
            return shopFundsVO;
        }
        return null;
    }

    @Override
    public ShopFunds getShopByShopId(Integer shopId) {
        LambdaQueryWrapper<ShopFunds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopFunds::getShopId, shopId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean create(ShopFundsCreateDTO createDTO) {
        if (createDTO != null) {
            ShopFunds shopFunds = new ShopFunds();
            BeanUtils.copyProperties(createDTO, shopFunds);
            return this.save(shopFunds);
        }
        return false;
    }

    @Override
    public boolean update(ShopFundsUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShopFunds shopFunds = new ShopFunds();
            BeanUtils.copyProperties(updateDTO, shopFunds);
            return this.updateById(shopFunds);
        }
        return false;
    }

    @Override
    public Long getCountByMerchantId(Integer merchantId) {
        LambdaQueryWrapper<ShopFunds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopFunds::getMerchantId, merchantId);
        return this.count(queryWrapper);
    }
}
