package com.tigshop.bean.vo.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/4/1 15:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "管理员消息统计")
public class AdminMsgCountVO {
    @Schema(description = "未读消息数量")
    private Long imMsgCount;
    @Schema(description = "获取新订单数量")
    private Long orderCount;
    @Schema(description = "获取未读消息数量")
    private Long unreadMsgCount;
}
