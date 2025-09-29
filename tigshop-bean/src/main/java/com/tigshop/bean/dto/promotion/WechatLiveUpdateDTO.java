// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.promotion.WechatLiveConstants.WECHAT_LIVE_ID_NOT_NULL;

/**
  * 微信直播更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "微信直播参数")
public class WechatLiveUpdateDTO {
    @Schema(description = "微信直播ID")
    @NotNull(message = WECHAT_LIVE_ID_NOT_NULL)
    private Integer wechatLiveId;

    @Schema(description ="微信直播名称")
    private String wechatLiveTitle;

    @Schema(description = "活动范围")
    private Integer actRange;

    @Schema(description = "活动简介")
    private List<Integer> actRangeExt;
}
