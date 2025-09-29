// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.salesman;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/6/20 10:53
 */
@Data
@Schema(description = "成交客户")
public class CustomerListVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "注册时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String regTime;

    @Schema(description = "等级名称")
    private String rankName;

}
