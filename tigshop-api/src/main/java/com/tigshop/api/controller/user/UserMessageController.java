package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.dto.user.UserMessageUpdateDTO;
import com.tigshop.bean.vo.user.UserMessageVO;
import com.tigshop.service.user.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 站内信
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/message/"))
@Tag(name = "前台用户站内信")
@Validated
public class UserMessageController {

    @Resource
    UserMessageService userMessageService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserMessageVO> list(UserMessageListPageQuery listDTO) {
        return userMessageService.list(getCurrentUserId(), listDTO);
    }

    @PostMapping("/updateAllRead")
    @Operation(summary = "设置为全部已读")
    public void setAllMessageRead() {
        userMessageService.setAllMessageRead(getCurrentUserId());
    }

    @PostMapping("/updateMessageRead")
    @Operation(summary = "设置为已读")
    public void setMessageRead(@Valid @RequestBody UserMessageUpdateDTO updateDTO) {
        userMessageService.setMessageRead(getCurrentUserId(), updateDTO.getId());
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@Valid @RequestBody OperateDTO dto) {
        userMessageService.deleteUserMessage(dto.getId());
    }

}
