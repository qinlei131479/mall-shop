import { getTranslate } from "@/api/common";
import { isOverseas } from "@/utils/util";

export default defineI18nConfig(() => ({
    legacy: false, // 是否兼容之前
    missing: (locale, key, vm) => {
        if (import.meta.client && isOverseas() && key) {
            const untranslatedCache = localStorage.getItem("untranslatedCache") ? JSON.parse(localStorage.getItem("untranslatedCache")!) : [];
            if (!Array.isArray(untranslatedCache)) {
                localStorage.setItem("untranslatedCache", JSON.stringify([]));
                return key;
            }
            if (untranslatedCache?.includes(key)) return key;
            try {
                untranslatedCache.push(key);
                getTranslate({ translationName: key });
                localStorage.setItem("untranslatedCache", JSON.stringify(untranslatedCache));
            } catch (e) {}
        }
        return key;
    }, // 输出键值
    // 使用新版配置项完全禁用警告
    missingWarn: false,
    fallbackWarn: false,
    // 保留旧版配置，以确保兼容性
    silentTranslationWarn: true,
    silentFallbackWarn: true,
    messages: {}
}));
