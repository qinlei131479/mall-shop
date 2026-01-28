import { getLang, getLangList, getDefaultLang } from "@/api/common";
import type { LangListItem } from "@/types/common/common.d.ts";

export const useI18nStore = defineStore("i18n", () => {
    const langList = ref<LangListItem[]>([]);

    const { t, locale, setLocale, setLocaleMessage } = useI18n();

    const XLocaleCode = useCookie("X-Locale-Code");

    const currentLanguage = useCookie("currentLanguage");

    const reacquireLanguage = useCookie("reacquireLanguage", {
        default: () => false // 默认值为 false
    });

    const init = () => {
        fetchLangList();

        if (import.meta.client) {
            const data = localStorage.getItem("cacheLang");
            if (data && !reacquireLanguage.value) {
                const { localeCode, langData, language } = JSON.parse(data);
                setXLocaleCode(localeCode);
                setLangData(localeCode, langData, false);
                currentLanguage.value = language;
                return;
            }
        }

        if (!XLocaleCode.value) {
            setXLocaleCode(useRequestHeaders()["accept-language"]?.split(",")[0] || "");
            if (import.meta.client) {
                setXLocaleCode(navigator.language);
            }
            fetchDefaultLangConfig();
        } else {
            fetchLangData(XLocaleCode.value);
        }
    };

    // 获取语言列表
    const fetchLangList = async () => {
        try {
            const result = await getLangList();
            langList.value = result;
        } catch (error: any) {
            console.error(error.message);
        }
    };

    const fetchLangData = async (localeCode: string) => {
        try {
            const result = await getLang(localeCode);
            setXLocaleCode(localeCode);
            setLangData(localeCode, result);
        } catch (error) {
            console.error(error);
        } finally {
            reacquireLanguage.value = false;
        }
    };

    const fetchDefaultLangConfig = async () => {
        try {
            const result = await getDefaultLang(XLocaleCode.value!);

            currentLanguage.value = result.language;

            fetchLangData(result.localeCode);
        } catch (error) {
            console.error(error);
        }
    };

    const setXLocaleCode = (localeCode: string) => {
        XLocaleCode.value = localeCode;
    };

    const setReacquireLanguage = (value: boolean) => {
        reacquireLanguage.value = value;
    };

    // 设置语言数据
    const setLangData = (localeCode: string, langData: any, needCache = true) => {
        setLocale(localeCode);
        setLocaleMessage(localeCode, langData);
        if (needCache) {
            const cacheLang = JSON.stringify({
                localeCode,
                langData,
                language: currentLanguage.value
            });
            localStorage.setItem("cacheLang", cacheLang);
        }
    };

    // 返回响应式属性和方法
    return {
        langList,
        reacquireLanguage,
        XLocaleCode,
        init,
        fetchLangData,
        fetchLangList,
        fetchDefaultLangConfig,
        setXLocaleCode,
        setReacquireLanguage
    };
});
