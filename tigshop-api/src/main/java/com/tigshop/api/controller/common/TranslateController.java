// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.common;


import com.tigshop.bean.dto.common.TranslateDTO;
import com.tigshop.service.lang.TranslationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tigshop团队
 * @create 2025/6/5 10:53
 */
@RestController
@RequestMapping("/api/common/translate")
@Tag(name = "翻译")
public class TranslateController {
    @Resource
    private TranslationsService translationsService;

    @PostMapping("/translate")
    @Operation(summary = "翻译")
    public void translate(@RequestBody TranslateDTO translateDTO) {
        translationsService.translationName(translateDTO);
    }
}
