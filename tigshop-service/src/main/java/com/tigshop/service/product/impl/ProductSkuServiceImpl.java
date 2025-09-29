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

package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImConfigService;
import com.tigshop.service.o2o.StoreSkuService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;

/**
 * 商品属性服务接口实现
 *
 * @author Jayce
 * @create 2024年11月21日 13:34
 */
@RequiredArgsConstructor
@Service
public class ProductSkuServiceImpl extends BaseServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    private final ProductMapper productMapper;
    private final ECardService eCardService;
    private final ProductInventoryLogMapper productInventoryLogMapper;
    private final VendorProductSkuMapper vendorProductSkuMapper;
    private final StoreSkuService storeSkuService;
    private final TranslatePackage translatePackage;
    private final ConfigService configService;
    private final StoreSkuService storeProductSkuService;

    @Override
    public List<ProductSkuDTO> getProductSkusByPid(Integer productId) {
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSku::getProductId, productId);
        List<ProductSku> list = this.list(queryWrapper);

        List<ProductSkuDTO> result = list.stream().map(this::convertToDTO).toList();

        List<ProductSkuDTO> skus = result.stream().filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0).toList();
        if (CollUtil.isNotEmpty(skus)) {
            List<Integer> vendorProductIds = skus.stream().map(ProductSkuDTO::getVendorProductSkuId).toList();
            List<VendorProductSku> vendorProductSkus = vendorProductSkuMapper.selectBatchIds(vendorProductIds);
            Map<Long, VendorProductSku> vendorProductSkuMap = vendorProductSkus.stream().collect(Collectors.toMap(VendorProductSku::getId, Function.identity()));

            result.stream()
                    .filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0)
                    .forEach(sku -> {
                        VendorProductSku vendorProductSku = vendorProductSkuMap.get(sku.getVendorProductSkuId().longValue());
                        if (vendorProductSku != null) {
                            sku.setSupplyPrice(vendorProductSku.getSupplyPrice());
                            sku.setSkuStock(vendorProductSku.getSkuStock());
                        }
                    });
        }

        //循环处理skuValue 并把skuValue用:分割后翻译，然后再用:重新组装
        result.forEach(productSkuDTO -> {
            String[] split = productSkuDTO.getSkuValue().split("\\|");
            for (int i = 0; i < split.length; i++) {
                //再按竖线分割
                String[] split1 = split[i].split(":");
                for (int j = 0; j < split1.length; j++) {
                    split1[j] = translatePackage.translate(split1[j], DataType.SKU.getCode());
                }
                split[i] = String.join(":", split1);
            }
            productSkuDTO.setSkuValue(String.join("|", split));
        });
        return result;
    }

    @Override
    public List<ProductSkuDTO> getProductSkusByProductIds(List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductSku::getProductId, productIds);
        List<ProductSku> list = this.list(queryWrapper);
        List<ProductSkuDTO> result = list.stream().map(this::convertToDTO).toList();

        List<ProductSkuDTO> vendorProductSkus = result.stream().filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0).toList();
        if (CollUtil.isNotEmpty(vendorProductSkus)) {
            List<Integer> vendorProductIds = vendorProductSkus.stream().map(ProductSkuDTO::getVendorProductSkuId).toList();
            List<VendorProductSku> skus = vendorProductSkuMapper.selectBatchIds(vendorProductIds);
            Map<Long, VendorProductSku> vendorProductSkuMap = skus.stream().collect(Collectors.toMap(VendorProductSku::getId, Function.identity()));

            result.stream()
                    .filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0)
                    .forEach(sku -> {
                        VendorProductSku vendorProductSku = vendorProductSkuMap.get(sku.getVendorProductSkuId().longValue());
                        if (vendorProductSku != null) {
                            sku.setSkuStock(vendorProductSku.getSkuStock());
                        }
                    });
        }

        return result;
    }

    @Override
    public boolean checkProductStock(int quantity, int productId, int skuId) {
        return getProductStock(productId, skuId) >= quantity;
    }

    @Override
    public int getProductStock(int productId, int skuId) {
        //todo 秒杀库存
        if (skuId == 0) {
            //直接返回商品的库存
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(Product::getProductId, productId);
            Product product = productMapper.selectOne(productQueryWrapper);
            if (product == null) {
                return 0;
            }
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
            return product.getProductStock();

        }
        // 构建通用查询条件
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId);
        queryWrapper.eq(ProductSku::getSkuId, skuId);

        ProductSku sku = this.getOne(queryWrapper);
        return sku != null ? sku.getSkuStock() : 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer skuId, Integer quantity) {
        // 1. 查询商品属性
        ProductSku productSku = this.getById(skuId);

        // 2. 扣减商品库存
        this.lambdaUpdate().eq(ProductSku::getSkuId, skuId).setDecrBy(ProductSku::getSkuStock, quantity).update();

        // 3. 增加库存扣减日志
        Product product = productMapper.selectById(productSku.getProductId());
        int number = productSku.getSkuStock() - quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(productSku.getProductId())
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(productSku.getSkuStock())
                .type(2)
                .changeNumber(quantity)
                .desc("下单扣减库存")
                .shopId(product.getShopId())
                .build();
        productInventoryLogMapper.insert(productInventoryLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incStock(Integer skuId, Integer quantity) {
        // 1. 查询商品属性
        ProductSku productSku = this.getById(skuId);
        // 如果商品规格已被删除
        if (productSku == null) {
            return;
        }

        // 2. 扣减商品库存
        this.lambdaUpdate().eq(ProductSku::getSkuId, skuId).setIncrBy(ProductSku::getSkuStock, quantity).update();

        // 3. 增加库存扣减日志
        Product product = productMapper.selectById(productSku.getProductId());
        int number = productSku.getSkuStock() + quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(productSku.getProductId())
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(productSku.getSkuStock())
                .type(1)
                .changeNumber(quantity)
                .desc("取消订单返还")
                .shopId(product.getShopId())
                .build();
        productInventoryLogMapper.insert(productInventoryLog);
    }

    @Override
    public ProductSku getProductSkusBySn(String goodsSn) {
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSkuSn, goodsSn)
                .select(ProductSku::getSkuId, ProductSku::getProductId);
        List<ProductSku> productSkus = this.list(queryWrapper);
        if (!productSkus.isEmpty()) {
            return productSkus.getFirst();
        }
        return null;
    }

    /**
     * 转换为DTO
     *
     * @param productSku 商品属性实体
     * @return 商品属性DTO
     */
    @Override
    public ProductSkuDTO convertToDTO(ProductSku productSku) {
        if (productSku == null) {
            return null;
        }
        ProductSkuDTO dto = new ProductSkuDTO();
        BeanUtils.copyProperties(productSku, dto);
        dto.setSkuPrice(productSku.getSkuPrice().toString());
        String skuDataStr = productSku.getSkuData();
        if (StrUtil.isNotEmpty(skuDataStr)) {
            JSONArray array = JsonUtil.fromJson(skuDataStr, JSONArray.class);
            List<ProductSkuDTO.SkuData> list = array.toList(ProductSkuDTO.SkuData.class);
            dto.setSkuData(list);
        }
        return dto;
    }

    @Override
    public ProductSkuDTO getDetail(int skuId, Long storeProductId) {
        ProductSku sku = this.getById(skuId);
        ProductSkuDTO productSkuDTO = convertToDTO(sku);
        if (storeProductId != null) {
            StoreProductSku storeProductSku = storeSkuService.lambdaQuery()
                    .eq(StoreProductSku::getStoreProductId, storeProductId)
                    .last("limit 1")
                    .one();
            if (storeProductSku != null) {
                ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    productSkuDTO.setSkuPrice(storeProductSku.getSkuPrice().toString());
                    List<StoreProductSku> list1 = storeProductSkuService.list(new LambdaQueryWrapper<StoreProductSku>().eq(StoreProductSku::getStoreProductId, storeProductId));
                    if (ObjectUtil.isNotEmpty(list1)) {
                        Map<Long, StoreProductSku> skuMap = list1.stream()
                                .collect(Collectors.toMap(StoreProductSku::getProductSkuId, Function.identity()));
                        StoreProductSku storeProductSku2 = skuMap.get((long) skuId);
                        if (storeProductSku2 != null) {
                            productSkuDTO.setSkuPrice(storeProductSku2.getSkuPrice().toString());
                        }
                    }
                }
                ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    productSkuDTO.setSkuStock(storeProductSku.getSkuStock());
                    List<StoreProductSku> list1 = storeProductSkuService.list(new LambdaQueryWrapper<StoreProductSku>().eq(StoreProductSku::getStoreProductId, storeProductId));
                    if (ObjectUtil.isNotEmpty(list1)) {
                        Map<Long, StoreProductSku> skuMap = list1.stream()
                                .collect(Collectors.toMap(StoreProductSku::getProductSkuId, Function.identity()));
                        StoreProductSku storeProductSku2 = skuMap.get((long) skuId);
                        if (storeProductSku2 != null) {
                            productSkuDTO.setSkuStock(storeProductSku2.getSkuStock());
                        }
                    }
                }
            }
        }
        return productSkuDTO;
    }
}
