package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserBalanceLogListDTO;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.service.finance.UserBalanceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 账户明细
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/account"))
@Tag(name = "前台用户账户信息")
public class AccountController {
    @Resource
    UserBalanceLogService userBalanceLogService;
    @GetMapping("/list")
    @Operation(summary = "账户信息列表")
    public Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        listDTO.setBalance(1);
        return userBalanceLogService.list(listDTO);
    }
}
