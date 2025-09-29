package com.tigshop.bean.vo.user;

import com.tigshop.bean.model.user.User;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TigShop
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "用户信息")
public class UserBaseVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "时间")
    private String distributionRegisterTime;

    public UserBaseVO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.avatar = user.getAvatar();
        this.mobile = user.getMobile();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.distributionRegisterTime = user.getDistributionRegisterTime() != null ? TigUtils.handelTime(Long.valueOf(user.getDistributionRegisterTime())) : null;
    }
}
