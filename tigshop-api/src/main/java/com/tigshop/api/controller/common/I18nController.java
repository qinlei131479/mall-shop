// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.common;

import com.tigshop.bean.vo.lang.LocalesCurrencyVO;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.service.lang.LocalesRelationService;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.lang.TranslationsDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 多语言
 *
 * @author Tigshop团队
 * @create 2025年01月17日 10:08
 */
@RestController
@RequestMapping("/api/common/i18n")
@Tag(name = "多语言")
public class I18nController {
    @Resource
    private LocalesRelationService localesRelationService;

    @Resource
    private LocalesService localesService;

    @Resource
    private TranslationsDataService translationsDataService;

    @GetMapping("/getDefaultLocale")
    @Operation(summary = "获取默认语言")
    public LocalesVO getDefaultLocale(@RequestParam String code){
        return localesRelationService.getDefaultLocale(code);
    }

    @GetMapping("/getLocales")
    @Operation(summary = "获取语言列表")
    public List<LocalesCurrencyVO> getLocales(){
        return localesService.getLocales();
    }

    @GetMapping("/getLocaleTranslations")
    @Operation(summary = "获得对应语言包")
    public Map<String, String> getLocaleTranslations(@RequestParam("localeCode") String localeCode){
        return translationsDataService.getLocaleTranslations(localeCode);
    }
}