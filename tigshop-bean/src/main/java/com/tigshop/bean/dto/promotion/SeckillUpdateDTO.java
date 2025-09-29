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

import static com.tigshop.common.constant.promotion.SeckillConstants.SECKILL_ID_NOT_NULL;
import static com.tigshop.common.constant.promotion.SeckillConstants.SECKILL_ITEM_NOT_NULL;

/**
  * 限时秒杀更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "限时秒杀参数")
public class SeckillUpdateDTO {
    @Schema(description = "限时秒杀ID")
    @NotNull(message = SECKILL_ID_NOT_NULL)
    private Integer seckillId;

    @Schema(description = "活动名称")
    private String seckillName;

    @Schema(description = "起始时间")
    private String seckillStartTime;

    @Schema(description = "结束时间")
    private String seckillEndTime;

    @Schema(description = "限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "限时秒杀商品")
    @NotNull.List(@NotNull(message = SECKILL_ITEM_NOT_NULL))
    private List<SeckillItemDTO> seckillItem;
}
