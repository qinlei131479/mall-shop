package com.tigshop.bean.dto.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.user.UserMessageLog;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Data
@Schema(name = "用户消息日志")
public class UserMessageLogCreateDTO {

    @Schema(description = "消息类型")
    private Integer messageType;

    @Schema(description = "标题")
    private String messageTitle;

    @Schema(description = "内容")
    private String messageContent;

    @Schema(description = "链接地址")
    private Object messageLink;

    @Schema(description = "发送类型")
    private Integer sendUserType;

    @Schema(description = "用户列表")
    private String userList;

    @Schema(description = "用户主键")
    private Integer userId;

    @Schema(description = "用户集合")
    private String userIds;

    @Schema(description = "会员等级")
    private Integer userRank;

    public UserMessageLog buildUserMessageLog() {
        String messageLink = Objects.nonNull(this.messageLink) ? JSON.toJSONString(this.messageLink) : "";

        return UserMessageLog.builder()
                .messageType(messageType)
                .sendUserType(sendUserType)
                .userIds(StrUtil.isNotBlank(userIds) ? userIds : "")
                .userRank(userRank)
                .messageTitle(messageTitle)
                .messageContent(messageContent)
                .messageLink(messageLink)
                .sendTime(StringUtils.getCurrentTime())
                .isRecall(Constants.NO)
                .build();
    }

}
