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

package com.tigshop.adminapi.controller.lang;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.lang.LocalesDTO;
import com.tigshop.bean.dto.lang.LocalesListDTO;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.service.lang.LocalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.CURRENCY_FIELDS;

/**
 * 语言列表
 *
 * @author Tigshop团队
 * @create 2024年12月31日 17:35
 */
@RestController
@Tag(name = "语言列表")
@RequestMapping("/adminapi/lang/locales")
@Validated
public class LocalesController {
    @Resource
    private LocalesService localesService;

    @GetMapping("/list")
    @Operation(summary = "语言列表")
    public Page<LocalesVO> list(LocalesListDTO basePage) {
        return localesService.list(basePage);
    }

    @GetMapping("/detail")
    @Operation(summary = "语言详情")
    public LocalesVO detail(@RequestParam Integer id) {
        return localesService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建语言")
    @PreAuthorize("@pms.hasPermission('localesModifyManage')")
    public void create(@RequestBody @Validated LocalesDTO dto) {
        localesService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改语言")
    @PreAuthorize("@pms.hasPermission('localesModifyManage')")
    public void update(@RequestBody @Validated LocalesDTO dto) {
        localesService.update(dto);
    }

    @PostMapping("/updateField")
    @Operation(summary = "修改字段")
    @PreAuthorize("@pms.hasPermission('localesModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO dto) {
        boolean b = localesService.updateField(dto, CURRENCY_FIELDS);
        if (b && "isDefault".equals(dto.getField())) {
            localesService.update(new LambdaUpdateWrapper<Locales>().eq(Locales::getIsDefault, 1).set(Locales::getIsDefault, 0));
            localesService.updateField(dto, CURRENCY_FIELDS);
        }
    }

    @PostMapping("/del")
    @Operation(summary = "删除语言")
    @PreAuthorize("@pms.hasPermission('localesModifyManage')")
    public void del(@RequestBody @Validated OperateDTO dto) {
        localesService.del(dto.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作语言")
    @PreAuthorize("@pms.hasPermission('localesModifyManage')")
    public void batch(@RequestBody @Validated BatchDTO dto) {
        localesService.batch(dto);
    }

}
