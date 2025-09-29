package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通过微信code获取微信用户信息
 *
 * @author kidd
 * @since 2025/4/29 17:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxLoginInfoByCodeVO {

    @Schema(description = "type")
    private Integer type;

    @Schema(description = "token")
    private String token;
}
