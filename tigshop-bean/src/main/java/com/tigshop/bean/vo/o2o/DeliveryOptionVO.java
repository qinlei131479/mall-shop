package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 门店配送方式返回参数
 *
 * @author Tigshop项目组
 * @create 2025年08月22日 14:51
 */
@Data
public class DeliveryOptionVO {
    @Schema(description = "配送方式")
    private Integer deliveryType;
    @Schema(description = "配送门店商品")
    private List<Product> deliveryProducts;
    @Schema(description = "自提门店商品")
    private List<PickupShopInfo> pickupProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PickupShopInfo {
        @Schema(description = "门店ID")
        private Integer shopId;
        @Schema(description = "门店自提点ID")
        private Set<Shop> pickups;
        @Schema(description = "属于这个店铺的商品ID")
        private List<Product> products;
    }
}