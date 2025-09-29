// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.bean.dto.user.UserFundManagementDTO;
import com.tigshop.bean.dto.user.UserListDTO;
import com.tigshop.bean.param.user.UserBatchParam;
import com.tigshop.bean.param.user.UserParam;
import com.tigshop.bean.query.user.UserFundDetailPageQuery;
import com.tigshop.bean.vo.user.UserItemVO;
import com.tigshop.service.salesman.SalesmanService;
import com.tigshop.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.USER_FIELDS;

/**
 * 会员控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/user/user")
@Tag(name = "会员管理")
@Validated
@PreAuthorize("@pms.hasPermission('userManage')")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private SalesmanService salesmanService;

    @GetMapping("/list")
    @Operation(summary = "会员列表")
    public Page<UserDTO> list(UserListDTO userList) {
        return userService.list(userList);
    }

    @GetMapping("/detail")
    @Operation(summary = "会员详情")
    public UserItemVO detail(@RequestParam Integer id) {
        return userService.getUserAndUserRank(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建会员")
    @PreAuthorize("@pms.hasPermission('userCreateManage')")
    public void create(@RequestBody UserDTO user) {
        userService.create(user);
    }

    @PostMapping("/update")
    @Operation(summary = "更新会员")
    @PreAuthorize("@pms.hasPermission('userModifyManage')")
    public void update(@RequestBody @Valid UserParam user) {
        userService.update(user);
    }

    @PostMapping("/del")
    @Operation(summary = "删除会员")
    @PreAuthorize("@pms.hasPermission('userDelManage')")
    public void del(@RequestBody OperateDTO operate) {
        userService.del(operate.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新会员字段")
    @PreAuthorize("@pms.hasPermission('userModifyFieldManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        userService.updateField(updateField, USER_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userBatchManage')")
    public void batch(@RequestBody UserBatchParam batch) {
        userService.batch(batch);
    }

    @PostMapping("/search")
    @Operation(summary = "会员搜索")
    public void searchByMobile(@RequestParam String search) {
        salesmanService.getUserByMobileOrUsername(search);
    }

    @PostMapping("/fundManagement")
    @Operation(summary = "会员资金管理")
    @PreAuthorize("@pms.hasPermission('fundManagementManage')")
    public void fundManagement(@RequestBody UserFundManagementDTO dto) {
        userService.fundManagement(dto);
    }

    @GetMapping("/userFundDetail")
    @Operation(summary = "资金明细")
    public Page<?> userFundDetail(@Validated UserFundDetailPageQuery pageQuery) {
        return userService.userFundDetail(pageQuery);
    }

    @Operation(summary = "管理员退出登录")
    @PostMapping("logout")
    public void logout() {
        userService.adminLogout();
    }
}
