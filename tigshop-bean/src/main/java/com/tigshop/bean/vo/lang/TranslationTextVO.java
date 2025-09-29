// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.lang;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 翻译文本
 *
 * @author Tigshop团队
 * @create 2025年01月02日 17:59
 */
@Getter
@Setter
@Schema(description = "翻译文本")
public class TranslationTextVO {
    @Schema(description = "翻译后数据")
    Map<String, Object> translation;

    public TranslationTextVO(Map<String, Object> translation) {
        this.translation = translation;
    }
}
