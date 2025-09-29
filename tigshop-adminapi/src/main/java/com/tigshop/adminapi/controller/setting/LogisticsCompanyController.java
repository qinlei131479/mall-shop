// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyUpdateDTO;
import com.tigshop.bean.vo.setting.LogisticsCompanyVO;
import com.tigshop.service.setting.LogisticsCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 配送方式配置信息控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/setting/logisticsCompany")
@Tag(name = "配送方式配置信息", description = "配送方式配置信息功能")
@PreAuthorize("@pms.hasPermission('logisticsCompanyManage','orderManage','vendorOrderManage')")
public class LogisticsCompanyController {
    @Resource
    private LogisticsCompanyService logisticsCompanyService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<LogisticsCompanyVO> list(LogisticsCompanyListDTO listDTO) {
        return logisticsCompanyService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public LogisticsCompanyVO detail(@RequestParam Integer id) {
        return logisticsCompanyService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyUpdateManage')")
    public void create(@RequestBody LogisticsCompanyCreateDTO createDTO) {
        logisticsCompanyService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyUpdateManage')")
    public void update(@RequestBody LogisticsCompanyUpdateDTO updateDTO) {
        logisticsCompanyService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        logisticsCompanyService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        logisticsCompanyService.updateField(updateField,  GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        logisticsCompanyService.batch(batchDTO);
    }
}
