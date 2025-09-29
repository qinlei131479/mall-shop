package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.enums.o2o.ProductOrgTypeEnum;
import com.tigshop.bean.model.o2o.ProductOrg;
import com.tigshop.common.constant.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 商品分配
 *
 * @author kidd
 * @since 2025/8/21 14:29
 */
@Data
public class ProductOrgVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "分配组织")
    private List<orgItem> orgItems;

    @Data
    public static class orgItem {

        @Schema(description = "分配组织类型；1-门店，2-区域")
        private Integer orgType;

        @Schema(description = "是否分配全部；0-否，1-是")
        private Integer isAll;

        @Schema(description = "分配组织 ID 集合")
        private List<Long> orgIds;

    }

    public ProductOrgVO(Long productId, List<ProductOrg> productOrgs) {
        this.productId = productId;

        orgItem region = new orgItem();
        region.setOrgType(ProductOrgTypeEnum.REGION.getCode());

        List<ProductOrg> regions = productOrgs.stream()
                .filter(org -> ProductOrgTypeEnum.REGION.getCode().equals(org.getOrgType()))
                .toList();
        boolean regionIsAll = regions.stream().anyMatch(org -> org.getOrgId().equals(0L));
        region.setIsAll(regionIsAll ? Constants.YES : Constants.NO);
        region.setOrgIds(regionIsAll ? Collections.emptyList() : regions.stream().map(ProductOrg::getOrgId).toList());

        orgItem store = new orgItem();
        store.setOrgType(ProductOrgTypeEnum.STORE.getCode());

        List<ProductOrg> stores = productOrgs.stream()
                .filter(org -> ProductOrgTypeEnum.STORE.getCode().equals(org.getOrgType()))
                .toList();

        boolean storeIsAll = stores.stream().anyMatch(org -> org.getOrgId().equals(0L));
        store.setIsAll(storeIsAll ? Constants.YES : Constants.NO);
        store.setOrgIds(storeIsAll ? Collections.emptyList() : stores.stream().map(ProductOrg::getOrgId).toList());

        this.orgItems = Arrays.asList(region, store);
    }
}
