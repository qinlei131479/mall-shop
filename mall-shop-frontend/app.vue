<template>
    <div class="app" :class="{ 'not-zh': !isZh && isOverseas() }" :style="themeStore.themeColor">
        <el-config-provider :locale="locale">
            <NuxtPage page-key="static" />
        </el-config-provider>
        <ChooseStyle v-if="isDemo() && !isStore() && !route.path.includes('install')"></ChooseStyle>
    </div>
</template>
<script lang="ts" setup>
import { useThemeStore } from "@/store/theme";
import { useCommonStore } from "@/store/common";
import { useI18nStore } from "@/store/i18n";
import { useCurrencyStore } from "@/store/currency";
// @ts-ignore
import zhCn from "element-plus/dist/locale/zh-cn.min.mjs";
// @ts-ignore
import en from "element-plus/dist/locale/en.min.mjs";
import ChooseStyle from "~/components/common/ChooseStyle.vue";
import { isDemo, isOverseas, isStore } from "@/utils/util";
import { useRoute } from "vue-router";

useHead({
    meta: [
        {
            name: "viewport",
            content: "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
        }
    ]
});

const themeStore = useThemeStore();
const commonStore = useCommonStore();
const i18nStore = useI18nStore();
const currencyStore = useCurrencyStore();

if (isOverseas()) {
    i18nStore.init();
    currencyStore.fetchCurrencyList();
}

const isZh = computed(() => i18nStore.XLocaleCode?.includes("zh"));

const locale = computed(() => {
    if (isOverseas()) {
        return isZh.value ? zhCn : en;
    } else {
        return zhCn;
    }
});

const route = useRoute();

const previewId = computed(() => {
    return route.query.previewId ? Number(route.query.previewId) : 0;
});

const getIsPhone = () => {
    const info = navigator.userAgent;
    return /(iPhone|iPod|Android|ios|Phone)/i.test(info);
};

const handlerToMobile = () => {
    if (getIsPhone() && commonStore.autoRedirect == 1) {
        let url = commonStore.h5Domain ? commonStore.h5Domain : `${window.location.origin}/mobile`;
        window.location.href = url;
    }
};

try {
    await commonStore.loadBaseConfig({ previewId: previewId.value > 0 ? previewId.value : undefined });

    await commonStore.loadThemeSettings();

    if (import.meta.client) {
        handlerToMobile();
    }

    useHead({
        link: [
            //网站link，可以在这里引入css和icon等
            {
                rel: "icon",
                type: "image/x-icon",
                href: imageFormat(commonStore.icoImg)
            }
        ]
    });
} catch (error) {
    console.log(error);
}

onMounted(() => {
    // 禁止双指缩放
    document.addEventListener(
        "touchstart",
        function (event) {
            if (event.touches.length > 1) {
                event.preventDefault();
            }
        },
        { passive: false }
    );

    // 禁止双击缩放
    let lastTouchEnd = 0;
    document.addEventListener(
        "touchend",
        function (event) {
            const now = new Date().getTime();
            if (now - lastTouchEnd <= 300) {
                event.preventDefault();
            }
            lastTouchEnd = now;
        },
        false
    );
    document.addEventListener("gesturestart", function (event) {
        event.preventDefault();
    });
});
</script>
