package com.tigshop.core;

//**---------------------------------------------------------------------+
//** 配置项 -- 提示
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.TranslationsData;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.lang.TranslationsDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.tigshop.common.constant.Constants.TRANSLATE;

/**
 * @author Jayce
 * @create 2024/9/26 15:03
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TranslatePackageImpl implements TranslatePackage {

    private final TranslationsDataService translationsDataService;

    private final LocalesService localesService;

    private final RedisCache redisCache;

    private final TigshopProperties tigshopProperties;
    ;

    /**
     * 获取提示返回内容
     *
     * @param text 提示
     * @return prompt
     */
    public String translate(String text) {

        return translate(text, DataType.API.getCode());
    }

    /**
     * 获取提示返回内容
     *
     * @param text     提示
     * @param dataType 翻译类型
     * @return prompt
     */
    public String translate(String text, Integer dataType) {
        // 从ThreadLocal中获取请求头中的语言类型
        String headerValue = ThreadLocalUtil.getValue();
        if (headerValue == null || headerValue.trim().isEmpty()) {
            return text;
        }
        if (tigshopProperties.getIsOverseas() == 0) {
            return text;
        }
        String key = TRANSLATE + headerValue + "::" + text;
        String translationsText = redisCache.getCacheObject(key);
        if (ObjectUtil.isEmpty(translationsText)) {
            // 从数据库中查询
            Locales locales = localesService.getOne(new LambdaQueryWrapper<Locales>()
                    .eq(Locales::getLocaleCode, headerValue)
                    .last("limit 1")
            );
            if (locales == null) {
                return text;
            }
            TranslationsData translationsData = translationsDataService.getOne(new LambdaQueryWrapper<TranslationsData>()
                    .eq(TranslationsData::getTranslationName, text)
                    .eq(TranslationsData::getLocaleId, locales.getId())
                    .eq(TranslationsData::getDataType, dataType)
                    .last("limit 1")
            );
            // 降级处理，先忽略 dateType
            if (translationsData == null) {
                translationsData = translationsDataService.getOne(new LambdaQueryWrapper<TranslationsData>()
                        .eq(TranslationsData::getTranslationName, text)
                        .eq(TranslationsData::getLocaleId, locales.getId())
                        .last("limit 1")
                );
                if (translationsData != null) {
                    log.error("翻译datatype不一致 {} :{},{}", translationsData.getTranslationKey(), dataType, translationsData.getDataType());
                }
            }

            if (translationsData == null) {
                log.error("翻译失败，找不到翻译数据，key:{}", key);
                return text;
            } else {
                redisCache.setCacheObject(key, translationsData.getTranslationValue());
                return translationsData.getTranslationValue();
            }
        }
        // 缓存中存在，直接返回
        return StrUtil.isNotBlank(translationsText) ? translationsText : text;
    }
}
