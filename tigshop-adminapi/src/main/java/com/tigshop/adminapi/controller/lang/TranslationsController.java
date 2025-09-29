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
import com.tigshop.bean.dto.lang.*;
import com.tigshop.bean.param.lang.TranslationsBatchParam;
import com.tigshop.bean.param.lang.TranslationsCreateParam;
import com.tigshop.bean.param.lang.TranslationsDelParam;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.bean.vo.lang.TranslationsDetailVO;
import com.tigshop.bean.vo.lang.TranslationsVO;
import com.tigshop.service.lang.TranslationsDataService;
import com.tigshop.service.lang.TranslationsService;
import com.volcengine.model.response.translate.TranslateTextResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 翻译
 *
 * @author Tigshop团队
 * @create 2024年12月19日 14:51
 */
@RestController
@RequestMapping("/adminapi/lang/translations")
@Tag(name = "翻译管理")
public class TranslationsController {
    @Resource
    private TranslationsDataService translationsDataService;

    @Resource
    private TranslationsService translationsService;

    @PostMapping("/getTranslations")
    @Operation(summary = "获取翻译信息")
    public TranslationsDetailVO getTranslations(@RequestBody TranslationsDataDTO dto) {
        return translationsDataService.getTranslations(dto.getDataType(), dto.getDataId(), dto.getTranslationName());
    }

    @GetMapping("/getLocalesLimit3")
    @Operation(summary = "获取非中文翻译语言前3条")
    public List<LocalesVO> getLocalesLimit3() {
        return translationsService.getLocalesLimit3();
    }

    @GetMapping("/list")
    @Operation(summary = "翻译列表")
    public  Page<TranslationsVO> list(TranslationsListDTO dto) {
        return translationsService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "翻译详情")
    public TranslationsDetailVO detail(@RequestParam Integer id) {
        return translationsService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建翻译")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void create(@RequestBody TranslationsCreateParam param) {
        translationsService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新翻译")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void update(@RequestBody TranslationsUpdateDTO dto) {
        translationsService.update(dto);
    }

    @PostMapping("/translation")
    @Operation(summary = "翻译")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public TranslateTextResponse.Translation translation(@RequestBody TranslationTextDTO dto) {
        return translationsService.getTranslations(dto.getCode(), dto.getText());
    }

    @PostMapping("/multiple")
    @Operation(summary = "一键翻译")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public boolean multiple(@RequestBody MultipleTranslationDTO dto) {
        return translationsService.multiple(dto);
    }

    @PostMapping("/createTranslations")
    @Operation(summary = "保存翻译内容")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void createTranslations(@RequestBody TranslationsCreateParam param) {
        translationsService.createTranslations(param);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void batch(@RequestBody @Validated TranslationsBatchParam param) {
        translationsService.batch(param);
    }

    @PostMapping("/batchCreate")
    @Operation(summary = "批量创建翻译")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void batchCreate(@RequestBody TranslationsCreateDTO dto) {
        translationsService.batchCreate(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('translationsModifyManage')")
    public void del(@RequestBody @Validated TranslationsDelParam param) {
        Integer id = param.getId();
        TranslationsBatchParam paramDel = new TranslationsBatchParam();
        paramDel.setIds(Collections.singletonList(id));
        paramDel.setType("del");
        translationsService.batch(paramDel);
    }
}
