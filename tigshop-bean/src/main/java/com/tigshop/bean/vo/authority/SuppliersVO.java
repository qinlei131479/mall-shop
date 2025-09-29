// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 供应商管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "供应商管理")
public class SuppliersVO {
    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "供应商名称")
    private String suppliersName;

    @Schema(description = "简介")
    private String suppliersDesc;

    @Schema(description = "状态")
    private Integer isCheck;

    @Schema(description = "国家")
    private Integer country;

    @Schema(description = "省份")
    private Integer province;

    @Schema(description = "城市")
    private Integer city;

    @Schema(description = "地区")
    private Integer district;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系地址")
    private String contactAddress;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "地区列表")
    private List<Integer> regions;
}