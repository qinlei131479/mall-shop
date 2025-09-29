package com.tigshop.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.CollectShopListDTO;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.user.CollectShopVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.shop.CollectShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.CollectShopService;
import com.tigshop.service.shop.ShopService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 会员收藏店铺实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class CollectShopServiceImpl extends BaseServiceImpl<CollectShopMapper, CollectShop> implements CollectShopService {
    @Resource
    ProductService productService;

    @Resource
    ShopService shopService;
    @Autowired
    private StoreProductMapper storeProductMapper;
    @Autowired
    private TigshopProperties tigshopProperties;

    @Override
    public boolean collect(Integer shopId) {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<CollectShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectShop::getUserId, userId);
        queryWrapper.eq(CollectShop::getShopId, shopId);
        if (this.getOne(queryWrapper) == null) {
            CollectShop collectShop = new CollectShop();
            collectShop.setUserId(userId);
            collectShop.setShopId(shopId);
            return this.save(collectShop);
        } else {
            //删除这条数据
            return this.del(this.getOne(queryWrapper).getCollectId());
        }
    }

    @Override
    public Page<CollectShopVO> list(CollectShopListDTO listDTO) {
        Integer userId = getCurrentUserId();
        // 分页
        Page<CollectShop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<CollectShop> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        if(userId > 0) {
            queryWrapper.eq(CollectShop::getUserId, userId);
        }

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<CollectShop> collectShopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<CollectShop> collectShopPageRecords = collectShopPage.getRecords();
        if(collectShopPageRecords.isEmpty()) {
            return new Page<>();
        }
        // 转换为VO
        List<CollectShopVO> collectShopVOList = collectShopPageRecords.stream()
                .map(collectShop -> {
                    CollectShopVO collectShopVO = new CollectShopVO();
                    BeanUtils.copyProperties(collectShop, collectShopVO);
                    collectShopVO.setProductCount(getProductCount(collectShop.getShopId()));
                    collectShopVO.setCollectCount(getShopCount(collectShop.getShopId()));
                    collectShopVO.setAddTime(TigUtils.handelTime(collectShop.getAddTime()));
                    CollectShopVO.ShopVO shopVO = new CollectShopVO.ShopVO();
                    Shop shop = shopService.getById(collectShop.getShopId());
                    if(shop != null) {
                        BeanUtils.copyProperties(shop, shopVO);
                        shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                        shopVO.setHotProduct(getProductType(shop.getShopId(),"is_hot"));
                        shopVO.setNewProduct(getProductType(shop.getShopId(),"is_new"));
                        shopVO.setBestProduct(getProductType(shop.getShopId(),"is_best"));
                        collectShopVO.setShop(shopVO);
                    }
                    return collectShopVO;
                }).toList();
        return PageUtil.convertPage(collectShopPage, collectShopVOList);
    }

    @Override
    public Long getCollectShopCount(Integer userId) {
        LambdaQueryWrapper<CollectShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectShop::getUserId, userId);
        return this.count(queryWrapper);
    }

    /**
     * 获取店铺收藏数量
     * @param shopId 店铺id
     * @return Long
     */
    public Long getShopCount(Integer shopId) {
        LambdaQueryWrapper<CollectShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectShop::getShopId, shopId);
        return this.count(queryWrapper);
    }

    /**
     * 获取商品数量
     * @param shopId 店铺id
     * @return Long
     */
    public Long getProductCount(Integer shopId) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getShopId, shopId)
                .eq(Product::getProductStatus, YES)
                .eq(Product::getIsDelete, NO);
        long productCount = productService.count(queryWrapper);
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        if(isO2o) {
            Long storeCount = storeProductMapper.selectCount(Wrappers.lambdaQuery(StoreProduct.class)
                    .eq(StoreProduct::getShopId, shopId)
                    .eq(StoreProduct::getProductStatus, YES)
                    .eq(StoreProduct::getIsDelete, NO));
            productCount += storeCount;
        }
        return productCount;
    }

    /**
     * 获取商品类型
     * @param shopId 店铺id
     * @param type 类型
     * @return List
     */
    public List<Product> getProductType(Integer shopId, String type){
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getShopId, shopId);
        queryWrapper.eq(Product::getProductStatus, 1);
        queryWrapper.eq(Product::getIsDelete, 0);
        if(Objects.equals(type, "is_new")) {
            queryWrapper.eq(Product::getIsNew, 1);
        }
        if(Objects.equals(type, "is_hot")){
            queryWrapper.eq(Product::getIsHot, 1);
        }
        if(Objects.equals(type, "is_best")) {
            queryWrapper.eq(Product::getIsBest, 1);
        }
        queryWrapper.last("limit 10");
        return productService.list(queryWrapper);
    }
}
