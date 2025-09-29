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
import com.tigshop.bean.dto.lang.CurrencyDTO;
import com.tigshop.bean.dto.lang.CurrencyListDTO;
import com.tigshop.bean.model.lang.Currency;
import com.tigshop.bean.vo.setting.CurrencyVO;
import com.tigshop.service.lang.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.CURRENCY_FIELDS;

/**
 * 货币管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:44
 */
@RestController
@RequestMapping("/adminapi/lang/currency")
@Validated
@Tag(name = "货币管理")
public class CurrencyController {
    @Resource
    private CurrencyService currencyService;

    @GetMapping("/list")
    @Operation(summary = "货币管理列表")
    public Page<CurrencyVO> list(CurrencyListDTO dto) {
        return currencyService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "货币管理详情")
    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    public CurrencyVO detail(@RequestParam Integer id) {
        return currencyService.detail(id);
    }

    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    @PostMapping("/create")
    @Operation(summary = "货币管理创建")
    public void create(@Valid @RequestBody CurrencyDTO dto) {
        currencyService.create(dto);
    }

    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    @PostMapping("/update")
    @Operation(summary = "货币管理修改")
    public void update(@Valid @RequestBody CurrencyDTO dto) {
        currencyService.update(dto);
    }

    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    @PostMapping("/del")
    @Operation(summary = "货币管理删除")
    public void del(@Valid @RequestBody OperateDTO dto) {
        currencyService.del(dto.getId());
    }

    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    @PostMapping("/batch")
    @Operation(summary = "货币管理批量操作")
    public void batch(@Valid @RequestBody BatchDTO dto) {
        currencyService.batch(dto);
    }

    @PreAuthorize("@pms.hasPermission('currencyModifyManage')")
    @PostMapping("/updateField")
    @Operation(summary = "货币管理修改字段")
    public void updateField(@Valid @RequestBody UpdateFieldDTO dto){
        boolean b = currencyService.updateField(dto, CURRENCY_FIELDS);
        if (b && "isDefault".equals(dto.getField())) {
            currencyService.update(new LambdaUpdateWrapper<Currency>().set(Currency::getIsDefault, 0));
            currencyService.updateField(dto, CURRENCY_FIELDS);
        }
    }
}
