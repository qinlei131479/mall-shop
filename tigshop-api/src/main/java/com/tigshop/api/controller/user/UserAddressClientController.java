package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.dto.user.UserAddressListDTO;
import com.tigshop.bean.dto.user.UserAddressUpdateDTO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.service.user.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户收货地址
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/address"))
@Tag(name = "用户收货地址")
@Validated
public class UserAddressClientController {
    @Resource
    UserAddressService userAddressService;

    @GetMapping("/list")
    @Operation(summary = "收货地址列表")
    public Page<UserAddressVO> list(UserAddressListDTO listDTO) {
        listDTO.setSortField("is_selected");
        return userAddressService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "收货地址详情")
    public UserAddressVO detail(@RequestParam("id") Integer id) {
        return userAddressService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除收货地址")
    public void del(@RequestBody OperateDTO operate) {
        userAddressService.del(operate.getId());
    }

    @PostMapping("/create")
    @Operation(summary = "创建收货地址")
    public Integer create(@RequestBody UserAddressCreateDTO userAddress) {
        return userAddressService.create(userAddress);
    }

    @PostMapping("/update")
    @Operation(summary = "修改收货地址")
    public void update(@RequestBody UserAddressUpdateDTO userAddress) {
        userAddressService.update(userAddress);
    }

    @PostMapping("/setSelected")
    @Operation(summary = "设置选中状态")
    public void setSelected(@RequestBody UserAddressUpdateDTO userAddress) {
        userAddressService.setSelected(userAddress.getId());
    }
}
