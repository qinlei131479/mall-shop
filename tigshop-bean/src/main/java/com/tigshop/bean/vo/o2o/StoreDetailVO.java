package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.dto.shop.StoreExtendedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "店铺详情VO")
public class StoreDetailVO extends StoreExtendedDto {

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "店铺表名称")
    private String shopTitle;

    @Schema(description = "店铺logo/门店logo")
    private String shopLogo;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "评价数量")
    private Integer shopStarNum;

    @Schema(description = "销量")
    private Integer shopSales;

    @Schema(description = "标签")
    private List<String> tipNames;

    @Schema(description = "是否收藏店铺")
    private Boolean collectShop;

    @Schema(description = "距离 单位m")
    private String distance;
}
