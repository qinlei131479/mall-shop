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
package com.tigshop.adminapi.controller.decorate;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.DecorateCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateListDTO;
import com.tigshop.bean.dto.decorate.DecorateSaveDTO;
import com.tigshop.bean.dto.decorate.DecorateUpdateDTO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.service.decorate.DecorateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.DECORATE_FIELDS;

/**
 * 页面管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/decorate")
@Tag(name = "装修管理")
public class DecorateController {
    @Resource
    private DecorateService decorateService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<DecorateVO> list(DecorateListDTO listDTO) {
        if (listDTO.getParentId() == null) {
            listDTO.setParentId(0);
        }
        return decorateService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public DecorateVO detail(@RequestParam Integer id, @RequestParam(required = false) Integer localeId, @RequestParam(required = false)  Integer parentId, @RequestParam Integer decorateType) {
        return decorateService.detail(id, localeId, parentId, decorateType);
    }

    @PreAuthorize("@pms.hasPermission('decorateCreateManage')")
    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody DecorateCreateDTO createDTO) {
        decorateService.create(createDTO);
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@RequestBody DecorateUpdateDTO updateDTO) {
        if (updateDTO.getParentId() == null) {
            updateDTO.setParentId(0);
        }
        if (updateDTO.getLocaleId() == null) {
            updateDTO.setLocaleId(0);
        }
        decorateService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        decorateService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        decorateService.updateField(updateField, DECORATE_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        decorateService.batch(batchDTO);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制")
    public void copy(@RequestBody OperateDTO operateDTO) {
        decorateService.copy(operateDTO.getId());
    }
    @PostMapping("/setHome")
    @Operation(summary = "设置为首页")
    @PreAuthorize("@pms.hasPermission('decorateSetHomeManage')")
    public void setHome(@RequestBody OperateDTO operateDTO) {
        decorateService.setHome(operateDTO.getId());
    }

    @PostMapping("/saveDraft")
    @Operation(summary = "保存装修草稿")
    public void saveDraft(@RequestBody DecorateSaveDTO decorateSaveDTO) {
        decorateService.saveDecorateToDraft(decorateSaveDTO.getId(), decorateSaveDTO.getData(), decorateSaveDTO.getParentId(), decorateSaveDTO.getLocaleId());
    }
    @PostMapping("/publish")
    @Operation(summary = "发布装修")
    @PreAuthorize("@pms.hasPermission('decoratePublishManage')")
    public void publish(@RequestBody DecorateSaveDTO decorateSaveDTO) {
        decorateService.publishDecorate(decorateSaveDTO.getId(), decorateSaveDTO.getData(), decorateSaveDTO.getParentId(), decorateSaveDTO.getLocaleId());
    }

    @GetMapping("/loadDraftData")
    @Operation(summary = "加载草稿")
    public JSONObject loadDraftData(@RequestParam(value = "id") Integer id) {
        return decorateService.loadDraftData(id);
    }
}
