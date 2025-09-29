// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配送方式配置信息model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("logistics_company")
@Schema(description = "配送方式配置信息")
public class LogisticsCompany {
    @TableId(value = "logistics_id", type = IdType.AUTO)
    @Schema(description = "自增id号")
    private Integer logisticsId;

    @Schema(description = "配送方式的快递公司代号")
    private String logisticsCode;

    @Schema(description = "配送方式名称")
    private String logisticsName;

    @Schema(description = "配送方式描述")
    private String logisticsDesc;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "客户编码")
    private String customerName;

    @Schema(description = "密码")
    private String customerPwd;

    @Schema(description = "月结号")
    private String monthCode;

    @Schema(description = "网点编码")
    private String sendSite;

    @Schema(description = "网点名称")
    private String sendStaff;

    @Schema(description = "快递业务类型")
    private String expType;

    @Schema(description = "无服务调用物流编号")
    private String apiLogisticsCode;
}
