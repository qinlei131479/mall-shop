package com.tigshop.bean.dto.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用户列表查询条件
 */
@Getter
@Setter
@Schema(description = "用户列表查询条件")
public class UserListDTO extends BasePage {

    @Schema(description = "来源")
    private Integer fromTag;

    @Schema(description = "会员等级")
    private Integer rankId;

    @Schema(description = "可用金额")
    private BigDecimal balance;

    @Schema(description = "积分大于")
    private BigDecimal pointsGt;

    @Schema(description = "积分小于")
    private BigDecimal pointsLt;

    @Schema(description = "是否分页")
    private Integer isPage;

    public UserListDTO() {
        this.isPage = 0;
        this.fromTag = 0;
    }
}
