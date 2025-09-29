// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.o2o;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:35
 */
@Data
public class StoreListVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺Logo URL")
    private String shopLogo;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "店铺状态码")
    private Integer status;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "是否有优惠券")
    private Integer hasCoupon;

    @Schema(description = "标签")
    private List<String> tipNames;

    @Schema(description = "距离 单位m")
    private BigDecimal distance;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系方式(原始json字符串)")
    @JsonIgnore
    private String contactJson;

    @Schema(description = "联系方式")
    private List<StoreExtendedDto.ShopContactConfig> contactConfigs;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "评价数量")
    private Integer shopStarNum;

    @Schema(description = "销量")
    private Integer shopSales;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "店铺收藏数量")
    private Long shopCollect;

    @Schema(description = "是否收藏店铺")
    private Boolean collectShop;

}
