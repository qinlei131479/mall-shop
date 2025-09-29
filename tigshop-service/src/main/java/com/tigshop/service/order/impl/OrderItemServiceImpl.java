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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderItemCreateDTO;
import com.tigshop.bean.dto.order.OrderItemListDTO;
import com.tigshop.bean.dto.order.OrderItemUpdateDTO;
import com.tigshop.bean.dto.order.OrderModifyProductDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderExtraSkuDataDTO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.ShowPic;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.CommentMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    @Resource
    ECardService eCardService;
    @Resource
    CommentMapper commentMapper;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductService productService;

    @Override
    public ListResVO<OrderItemVO, OrderItemListDTO> list(OrderItemListDTO listDTO) {
        // 分页
        Page<OrderItem> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();

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
            queryWrapper.like("order_item", keyword);
        }*/
        // 执行查询
        Page<OrderItem> orderItemPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderItem> orderItemPageRecords = orderItemPage.getRecords();
        // 转换为VO
        List<OrderItemVO> orderItemVOList = orderItemPageRecords.stream()
                .map(orderItem -> {
                    OrderItemVO orderItemVO = new OrderItemVO();
                    BeanUtils.copyProperties(orderItem, orderItemVO);
                    return orderItemVO;
                }).toList();
        return new ListResVO<>(orderItemVOList, listDTO, orderItemPage.getTotal());
    }

    @Override
    public OrderItemVO detail(Integer id) {
        if (id != null) {
            OrderItem orderItem = this.getById(id);
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, orderItemVO);
            return orderItemVO;
        }
        return new OrderItemVO();
    }

    @Override
    public boolean create(OrderItemCreateDTO createDTO) {
        if (createDTO != null) {
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(createDTO, orderItem);
            return this.save(orderItem);
        }
        return false;
    }

    @Override
    public boolean update(OrderItemUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(updateDTO, orderItem);
            return this.updateById(orderItem);
        }
        return false;
    }

    @Override
    public List<OrderItemVO> getItemByOrderIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderItem::getOrderId, ids);
        List<OrderItem> orderItems = this.list(queryWrapper);
        if (CollUtil.isEmpty(orderItems)) {
            return Collections.emptyList();
        }

        Map<Integer, Product> productMap = new HashMap<>();
        List<Integer> productId = orderItems.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isNotEmpty(productId)) {
            List<Product> productList = productService.lambdaQuery()
                    .in(Product::getProductId, productId)
                    .list();
            productMap = Optional.ofNullable(productList)
                    .orElse(new ArrayList<>())
                    .stream()
                    .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }


        Map<Integer, Product> finalProductMap = productMap;
        return orderItems.stream().map(orderItem -> {
            OrderItemVO orderItemVo = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, orderItemVo);
            Product product = finalProductMap.get(orderItem.getProductId());
            if (product != null) {
                orderItemVo.setVirtualSample(product.getVirtualSample());
            }
            if (!Objects.equals(orderItem.getSkuData(), "[]")) {
                orderItemVo.setSkuData(JSON.parseArray(orderItem.getSkuData(), ProductSkuDTO.SkuData.class));
            } else {
                orderItemVo.setSkuData(new ArrayList<>());
            }
            if (ObjectUtil.isNotEmpty(orderItem.getSkuId())) {
                orderItemVo.setProductStock(productSkuService.getProductStock(orderItem.getProductId(), orderItem.getSkuId()));
                ProductSku productSku = productSkuService.getById(orderItem.getSkuId());
                if (productSku != null){
                    orderItemVo.setSkuValue(productSku.getSkuValue());
                }
            } else {
                orderItemVo.setProductStock(productSkuService.getProductStock(orderItem.getProductId(), 0));
            }
            if (JSONUtil.isTypeJSONArray(orderItem.getExtraSkuData())) {
                List<OrderExtraSkuDataDTO> extraSkuData = JSONUtil.parseArray(orderItem.getExtraSkuData()).toList(OrderExtraSkuDataDTO.class);
                if (CollUtil.isNotEmpty(extraSkuData)) {
                    OrderExtraSkuDataDTO orderExtraSkuDataDTO = extraSkuData.getFirst();
                    orderExtraSkuDataDTO.setTotalAttrPrice(orderExtraSkuDataDTO.getAttrPrice().multiply(BigDecimal.valueOf(orderItemVo.getQuantity())));
                }
                orderItemVo.setExtraSkuData(extraSkuData);
            } else {
                orderItemVo.setExtraSkuData(new ArrayList<>());
            }
            orderItemVo.setECard(eCardService.getCardByOrderItemId(orderItem.getItemId()));
            orderItemVo.setSubtotal(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
            //获取评论信息
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.eq(Comment::getOrderItemId, orderItem.getItemId())
                    .eq(Comment::getOrderId, orderItem.getOrderId())
                    .eq(Comment::getUserId, orderItem.getUserId())
                    .eq(Comment::getParentId, 0);
            Comment comment = commentMapper.selectOne(commentQueryWrapper);
            if (comment != null) {
                OrderItemVO.CommentInfoVO commentInfoVO = new OrderItemVO.CommentInfoVO();
                BeanUtils.copyProperties(comment, commentInfoVO);
                commentInfoVO.setCommentTag(JsonUtil.jsonToList(comment.getCommentTag(), String.class));
                commentInfoVO.setShowPics(JsonUtil.jsonToList(comment.getShowPics(), ShowPic.class));
                orderItemVo.setCommentInfo(commentInfoVO);
            } else {
                orderItemVo.setCommentInfo(null);
            }
            return orderItemVo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyOrderItem(OrderVO orderVO, OrderModifyProductDTO modifyProductDTO) {
        List<Integer> itemIds = new ArrayList<>();
        for (OrderItem item : modifyProductDTO.getItems()) {
            if (item.getItemId() != null) {
                itemIds.add(item.getItemId());
            } else {
                item.setItemId(null);
                item.setOrderId(orderVO.getOrderId());
            }

            //判断库存

        }
        if (!itemIds.isEmpty()) {
            this.removeByIds(itemIds);
        }
        this.saveBatch(modifyProductDTO.getItems());
    }
}
