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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.lang.LocalesRelationDTO;
import com.tigshop.bean.model.lang.LocalesLang;
import com.tigshop.bean.vo.setting.LocalesRelationVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.lang.LocalesLangService;
import com.tigshop.service.lang.LocalesRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 语言关联
 *
 * @author Tigshop团队
 * @create 2024年12月31日 15:51
 */
@RestController
@RequestMapping("/adminapi/lang/localesRelation")
@Tag(name = "语言关联")
public class LocalesRelationController {
    @Resource
    private LocalesRelationService localesRelationService;

    @Resource
    private LocalesLangService localesLangService;

    @GetMapping("/list")
    @Operation(summary = "语言关联列表")
    public Page<LocalesRelationVO> list(BasePage basePage) {
        return localesRelationService.list(basePage);
    }

    @GetMapping("/detail")
    @Operation(summary = "语言关联详情")
    public LocalesRelationVO detail(@RequestParam Integer id) {
        return localesRelationService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建语言关联")
    @PreAuthorize("@pms.hasPermission('localesRelationModifyManage')")
    public void create(@RequestBody @Valid LocalesRelationDTO dto) {
        localesRelationService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改语言关联")
    @PreAuthorize("@pms.hasPermission('localesRelationModifyManage')")
    public void update(@RequestBody @Valid LocalesRelationDTO dto) {
        localesRelationService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除语言关联")
    @PreAuthorize("@pms.hasPermission('localesRelationModifyManage')")
    public void del(@RequestBody @Valid OperateDTO dto) {
        localesRelationService.del(dto.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作语言关联")
    @PreAuthorize("@pms.hasPermission('localesRelationModifyManage')")
    public void batch(@RequestBody @Valid BatchDTO dto) {
        localesRelationService.batch(dto);
    }

    @GetMapping("/config")
    @Operation(summary = "语言关联配置")
    public List<LocalesLang> config() {
        return localesLangService.list();
    }
}
