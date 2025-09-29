package com.tigshop.bean.param.o2o;

import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.o2o.ProductOrgTypeEnum;
import com.tigshop.bean.model.o2o.ProductOrg;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分配组织
 *
 * @author kidd
 * @since 2025/8/21 14:18
 */
@Data
public class ProductOrgParam {

    @NotNull(message = "商品 ID 不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @Valid
    @NotEmpty(message = "分配组织不能为空")
    @Schema(description = "分配组织")
    private List<orgItem> orgItems;

    @Data
    public static class orgItem {

        @NotNull(message = "分配组织类型不能为空")
        @Schema(description = "分配组织类型；1-门店，2-区域")
        private Integer orgType;

        @NotNull(message = "是否分配全部不能为空")
        @Schema(description = "是否分配全部；0-否，1-是")
        private Integer isAll;

        @Schema(description = "分配组织 ID 集合")
        private List<Long> orgIds;

    }

    public void valid() {
        for (orgItem orgItem : this.orgItems) {
            if (Constants.NO.equals(orgItem.getIsAll())) {
                Assert.notEmpty(orgItem.getOrgIds(), () -> new GlobalException("请选择分配组织"));
            }
        }
    }

    public List<ProductOrg> buildProductDistributions() {
        List<ProductOrg> productOrgs = new ArrayList<>();

        this.orgItems.forEach(orgItem -> {

            if (Constants.YES.equals(orgItem.getIsAll())) {
                ProductOrg productOrg = new ProductOrg();
                productOrg.setProductId(this.productId);
                productOrg.setOrgType(orgItem.getOrgType());
                productOrg.setOrgId(0L);
                productOrgs.add(productOrg);
            } else {
                List<ProductOrg> itemProductOrgs = orgItem.getOrgIds().stream()
                        .map(orgId -> {
                            ProductOrg productOrg = new ProductOrg();
                            productOrg.setProductId(this.productId);
                            productOrg.setOrgType(orgItem.getOrgType());
                            productOrg.setOrgId(orgId);
                            return productOrg;
                        })
                        .toList();
                productOrgs.addAll(itemProductOrgs);
            }

        });

        return productOrgs;
    }

    public List<Long> getRegionOrgIds() {
        return this.orgItems.stream()
                .filter(orgItem -> ProductOrgTypeEnum.REGION.getCode().equals(orgItem.getOrgType()))
                .flatMap(orgItem -> orgItem.getOrgIds().stream())
                .collect(Collectors.toList());
    }

    public List<Integer> getShopOrgIds() {
        return this.orgItems.stream()
                .filter(orgItem -> ProductOrgTypeEnum.STORE.getCode().equals(orgItem.getOrgType()))
                .flatMap(orgItem -> orgItem.getOrgIds().stream())
                .map(Long::intValue)
                .collect(Collectors.toList());
    }

}
