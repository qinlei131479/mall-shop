// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.SignInSettingListDTO;
import com.tigshop.bean.dto.promotion.SignInSettingUpdateDTO;
import com.tigshop.bean.vo.promotion.SignInSettingVO;
import com.tigshop.service.promotion.SignInSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 积分签到控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/signInSetting")
@Tag(name = "积分签到", description = "积分签到功能")
@PreAuthorize("@pms.hasPermission('signInSettingManage')")
@Validated
public class SignInSettingController {
    @Resource
    private SignInSettingService signInSettingService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SignInSettingVO> list(SignInSettingListDTO listDTO) {
        return signInSettingService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SignInSettingVO detail(@RequestParam Integer id) {
        return signInSettingService.detail(id);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('signInSettingModifyManage')")
    public void update(@Valid @RequestBody SignInSettingUpdateDTO updateDTO) {
        signInSettingService.update(updateDTO);
    }
}
