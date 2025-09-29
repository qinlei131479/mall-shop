package com.tigshop.service.o2o.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.o2o.pickup.PickupEndJson;
import com.tigshop.bean.dto.o2o.pickup.PickupTimeJson;
import com.tigshop.bean.model.o2o.ShopPickupConfig;
import com.tigshop.bean.model.o2o.ShopPickupTpl;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.param.o2o.ShopPickupConfigParam;
import com.tigshop.bean.vo.o2o.ShopPickupConfigVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.ShopPickupTplMapper;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.ShopPickupConfigService;
import com.tigshop.service.o2o.ShopPickupTplService;
import com.tigshop.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;

/**
 * @author Admin
 * @description 针对表【shop_pickup_tpl(门店自提模板表)】的数据库操作Service实现
 * @createDate 2025-08-21 10:07:40
 */
@Service
@RequiredArgsConstructor
public class ShopPickupTplServiceImpl extends BaseServiceImpl<ShopPickupTplMapper, ShopPickupTpl>
        implements ShopPickupTplService {

    private final ShopPickupConfigService shopPickupConfigService;
    private final ProductMapper productMapper;

    static final String DEFAULT_TPL_NAME = "默认自提模板";
    private final TranslatePackage translatePackage;
    private final StoreProductMapper storeProductMapper;

    @Override
    @Transactional
    public void initPickUpTpl(Integer shopId) {
        ShopPickupTpl shopPickupTpl = new ShopPickupTpl();
        shopPickupTpl.setShopId(Long.valueOf(shopId));
        shopPickupTpl.setTplName(DEFAULT_TPL_NAME);
        shopPickupTpl.setStockingStatus(NO);
        shopPickupTpl.setPickupTimeStatus(NO);
        shopPickupTpl.setPickupEndStatus(NO);
        shopPickupTpl.setIsDefault(YES);
        shopPickupTpl.setCreateTime(StringUtils.getCurrentTime());
        shopPickupTpl.setUpdateTime(StringUtils.getCurrentTime());
        this.save(shopPickupTpl);
    }

    @Override
    @Transactional
    public ShopPickupConfigVO getShopPickupTpl(Integer shopId) {
        ShopPickupConfigVO shopPickupConfigVO = new ShopPickupConfigVO();
        ShopPickupConfig shopPickupConfig = shopPickupConfigService.getOne(new LambdaQueryWrapper<ShopPickupConfig>().eq(ShopPickupConfig::getShopId, shopId));
        if (shopPickupConfig == null) {
            shopPickupConfigService.initPickUpConfig(shopId);
            shopPickupConfig = shopPickupConfigService.getOne(new LambdaQueryWrapper<ShopPickupConfig>().eq(ShopPickupConfig::getShopId, shopId));
        }
        shopPickupConfigVO.setStatus(shopPickupConfig.getStatus());
        ShopPickupTpl one = getOne(new LambdaQueryWrapper<ShopPickupTpl>().eq(ShopPickupTpl::getShopId, shopId));
        if (one == null) {
            initPickUpTpl(shopId);
            one = getOne(new LambdaQueryWrapper<ShopPickupTpl>().eq(ShopPickupTpl::getShopId, shopId));
        }
        shopPickupConfigVO.setStockingStatus(one.getStockingStatus());
        shopPickupConfigVO.setPickupTimeStatus(one.getPickupTimeStatus());
        shopPickupConfigVO.setPickupEndStatus(one.getPickupEndStatus());
        shopPickupConfigVO.setPickupTimeJson(JSONUtil.toBean(one.getPickupTimeJson(), PickupTimeJson.class));
        shopPickupConfigVO.setPickupEndJson(JSONUtil.toBean(one.getPickupEndJson(), PickupEndJson.class));
        return shopPickupConfigVO;
    }

    @Override
    @Transactional
    public void saveShopPickupTpl(ShopPickupConfigParam param) {
        if (param.getShopId() == null) {
            param.setShopId(getShopId());
        }
        if (param.getStatus() == 0) {
            // 判断是否有配置自提的商品，如果有则不允许关闭
            long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getShopId, param.getShopId()).eq(Product::getIsShopPickup, YES));
            if (count > 0) {
                throw new GlobalException("已有商品使用该配送方式，无法关闭");
            }
            List<StoreProduct> storeProducts = storeProductMapper.selectList(new LambdaQueryWrapper<StoreProduct>().eq(StoreProduct::getShopId, param.getShopId()));
            for (StoreProduct storeProduct : storeProducts) {
                Product product = productMapper.selectById(storeProduct.getProductId());
                if (Objects.equals(product.getIsShopPickup(), YES)) {
                    throw new GlobalException("已有商品使用该配送方式，无法关闭");
                }
            }
        }

        ShopPickupConfig shopPickupConfig = shopPickupConfigService.getOne(new LambdaQueryWrapper<ShopPickupConfig>().eq(ShopPickupConfig::getShopId, param.getShopId()));
        ShopPickupTpl one = getOne(new LambdaQueryWrapper<ShopPickupTpl>().eq(ShopPickupTpl::getShopId, param.getShopId()));
        shopPickupConfig.setStatus(param.getStatus());
        shopPickupConfigService.updateById(shopPickupConfig);

        one.setStockingStatus(param.getStockingStatus());
        one.setPickupTimeStatus(param.getPickupTimeStatus());
        one.setPickupEndStatus(param.getPickupEndStatus());
        PickupTimeJson pickupTimeJson = param.getPickupTimeJson();
        if (pickupTimeJson != null && param.getPickupTimeStatus().equals(YES)) {
            String startTime = pickupTimeJson.getTimeList().getFirst().getStartTime();
            Assert.notBlank(startTime, () -> new GlobalException(translatePackage.translate("自提时间段开始时间不能为空")));
        }
        one.setPickupTimeJson(JSONUtil.toJsonStr(param.getPickupTimeJson()));
        one.setPickupEndJson(JSONUtil.toJsonStr(param.getPickupEndJson()));
        this.updateById(one);
    }
}




