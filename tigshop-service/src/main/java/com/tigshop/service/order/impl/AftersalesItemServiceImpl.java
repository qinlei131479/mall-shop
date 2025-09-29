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

package com.tigshop.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AftersalesItemCreateDTO;
import com.tigshop.bean.dto.order.AftersalesItemListDTO;
import com.tigshop.bean.dto.order.AftersalesItemUpdateDTO;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.AftersalesItemVO;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 售后商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class AftersalesItemServiceImpl extends BaseServiceImpl<AftersalesItemMapper, AftersalesItem> implements AftersalesItemService {

    private final OrderItemService orderItemService;

    private final ProductService productService;

    private final ProductSkuService productSkuService;

    @Override
    public ListResVO<AftersalesItemVO, AftersalesItemListDTO> list(AftersalesItemListDTO listDTO) {
        // 分页
        Page<AftersalesItem> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AftersalesItem> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("aftersales_item", keyword);
        }*/
        // 执行查询
        Page<AftersalesItem> aftersalesItemPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AftersalesItem> aftersalesItemPageRecords = aftersalesItemPage.getRecords();
        // 转换为VO
        List<AftersalesItemVO> aftersalesItemVOList = aftersalesItemPageRecords.stream()
                .map(aftersalesItem -> {
                    AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
                    BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);
                    return aftersalesItemVO;
                }).toList();
        return new ListResVO<>(aftersalesItemVOList, listDTO, aftersalesItemPage.getTotal());
    }

    @Override
    public AftersalesItemVO detail(Integer id) {
        if (id != null) {
            AftersalesItem aftersalesItem = this.getById(id);
            AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
            BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);
            return aftersalesItemVO;
        }
        return new AftersalesItemVO();
    }

    @Override
    public boolean create(AftersalesItemCreateDTO createDTO) {
        if (createDTO != null) {
            AftersalesItem aftersalesItem = new AftersalesItem();
            BeanUtils.copyProperties(createDTO, aftersalesItem);
            return this.save(aftersalesItem);
        }
        return false;
    }

    @Override
    public boolean update(AftersalesItemUpdateDTO updateDTO) {
        if (updateDTO != null) {
            AftersalesItem AftersalesItem = new AftersalesItem();
            BeanUtils.copyProperties(updateDTO, AftersalesItem);
            return this.updateById(AftersalesItem);
        }
        return false;
    }

    @Override
    public List<AftersalesItemVO> getItemByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AftersalesItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AftersalesItem::getAftersaleId, ids);
        List<AftersalesItem> aftersalesItems = this.list(queryWrapper);
        List<AftersalesItemVO> aftersalesItemVOS = aftersalesItems.stream().map(aftersalesItem -> {
            AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
            OrderItem orderItem = orderItemService.getById(aftersalesItem.getOrderItemId());

            BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);

            if (orderItem != null) {
                aftersalesItemVO.setPrice(orderItem.getPrice());
                aftersalesItemVO.setQuantity(orderItem.getQuantity());
                aftersalesItemVO.setProductName(orderItem.getProductName());
                aftersalesItemVO.setProductSn(orderItem.getProductSn());
                aftersalesItemVO.setProductId(orderItem.getProductId());
                aftersalesItemVO.setPicThumb(orderItem.getPicThumb());
                aftersalesItemVO.setOrderSn(orderItem.getOrderSn());
                aftersalesItemVO.setOrderId(orderItem.getOrderId());
            }
            return aftersalesItemVO;
        }).toList();
        return aftersalesItemVOS;
    }

    @Override
    public void returnStock(Integer aftersaleId) {
        List<AftersalesItem> aftersalesItems = this.lambdaQuery().eq(AftersalesItem::getAftersaleId, aftersaleId).list();
        Map<Integer, Integer> aftersalesItemMap = aftersalesItems.stream().collect(Collectors.toMap(AftersalesItem::getOrderItemId, AftersalesItem::getNumber));
        List<Integer> orderItemIds = aftersalesItems.stream().map(AftersalesItem::getOrderItemId).toList();

        List<OrderItem> orderItems = orderItemService.lambdaQuery().in(OrderItem::getItemId, orderItemIds).list();

        orderItems.forEach(orderItem -> {
            Integer num = aftersalesItemMap.get(orderItem.getItemId());
            if (orderItem.getSkuId() == 0) {
                // skuId 为 0 的返还 商品库存
                productService.incStock(orderItem.getProductId(), num);
            } else {
                // skuId 不为 0 的返还 商品库存和SKU库存
                productService.incStock(orderItem.getProductId(), num);
                productSkuService.incStock(orderItem.getSkuId(), num);
            }
        });

    }

    @Override
    public List<AftersalesItem> validAftersalesItems(List<Integer> orderItemIds) {
        return this.baseMapper.validAftersalesItems(orderItemIds);
    }
}
