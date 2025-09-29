package com.tigshop.api.controller.o2o;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.o2o.UserPickupDelParam;
import com.tigshop.bean.param.o2o.UserPickupParam;
import com.tigshop.bean.query.o2o.UserPickupQuery;
import com.tigshop.bean.vo.o2o.UserPickupInfoVO;
import com.tigshop.service.o2o.UserPickupInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 20:30
 */
@Tag(name = "用户自提信息")
@RestController
@RequestMapping("/api/o2o/userPickup")
@RequiredArgsConstructor
public class UserPickupController {

    private final UserPickupInfoService userPickupInfoService;

    @GetMapping("/getUserPickups")
    @Operation(summary = "用户自提信息列表")
    public Page<UserPickupInfoVO> getUserPickups(UserPickupQuery query) {
        return userPickupInfoService.getUserPickups(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建用户自提信息")
    public void create(@RequestBody @Valid UserPickupParam param) {
        userPickupInfoService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "修改用户自提信息")
    public void update(@RequestBody @Valid UserPickupParam param) {
        userPickupInfoService.update(param);
    }

    @GetMapping("/detail")
    @Operation(summary = "用户自提信息详情")
    public UserPickupInfoVO detail(@RequestParam Integer userPickupId) {
        return userPickupInfoService.detail(userPickupId);
    }

    @PostMapping("/del")
    @Operation(summary = "删除用户自提信息")
    public void del(@RequestBody @Valid UserPickupDelParam param) {
        userPickupInfoService.del(param.getUserPickupId());
    }
}