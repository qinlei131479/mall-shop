// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.o2o;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域关联店铺列表查询参数
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "区域关联店铺列表查询参数")
public class AreaStoreShopListQueryPage extends BasePage {
    
    @Schema(description = "区域门店管理ID")
    private Integer areaStoreManagerId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "是否选择")
    private Integer check = 1;
}