<template>
    <div class="ele-menu">
        <div v-if="commonStore.sidebarNav.length > 0">
            <template v-if="!props.showTop">
                <template v-for="(nav, index) in commonStore.sidebarNav" key="index">
                    <CommonLink :item="nav">
                        <div v-if="nav.title != t('顶部')" :class="{ 'no-border': index === commonStore.sidebarNav.length && scrollTop < 630 }" class="card">
                            <template v-if="nav.icon"> <em :class="nav.icon" class="main_pel_m"></em> <br /> </template>
                            {{ $t(nav.title) }}
                        </div>
                    </CommonLink>
                </template>
            </template>
            <div :class="{ 'no-border': scrollTop < 630 }" class="card" v-if="showService">
                <CommonService :isIndex="true">
                    <i class="iconfont-pc icon-kefu"></i> <br />
                    {{ $t("客服") }}
                </CommonService>
            </div>
            <div v-if="scrollTop >= 630" :class="{ 'no-border': true }" class="card" @click="scrollToTop">
                <i class="iconfont-pc icon-shangjiantou"></i> <br />
                {{ $t("顶部") }}
            </div>
        </div>
        <div v-else></div>
    </div>
</template>
<script lang="ts" setup>
import { useCommonStore } from "@/store/common";
import { getHomeCustomerServiceConfig } from "~/api/home/home";
const { t } = useI18n();
const commonStore = useCommonStore();
commonStore.loadNav();
const props = defineProps({
    showTop: {
        type: Boolean,
        default: false
    }
});
const showService = ref(false);
const __getServiceConfig = async () => {
    try {
        const result = await getHomeCustomerServiceConfig();
        showService.value = result.show;
    } catch (error) {
        console.error(error);
    }
};
__getServiceConfig();

const scrollTop = ref(0); // 定义一个响应式变量用于存储滚动距离
function updateScroll() {
    scrollTop.value = window.scrollY; // 更新滚动距离
}

// 返回顶部的方法
const scrollToTop = () => {
    window.scrollTo({
        top: 0, // 顶部的位置，0表示最上面
        behavior: "smooth" // 平滑滚动
    });
};

onMounted(() => {
    window.addEventListener("scroll", updateScroll); // 组件挂载时添加滚动事件监听器
});

onUnmounted(() => {
    window.removeEventListener("scroll", updateScroll); // 组件卸载时移除滚动事件监听器
});
</script>
<style lang="less" scoped>
.ele-menu {
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    width: 100%;

    .card {
        cursor: pointer;
        position: relative;
        padding: 10px 10px;
        text-align: center;
        line-height: 20px;
        font-size: 14px;
        color: #333;

        &::after {
            content: "";
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            height: 0.5px;
            background: linear-gradient(to right, transparent, #c4c4c4, transparent);
        }

        &.no-border::after {
            display: none;
        }

        &:hover {
            background-color: var(--general);
            color: var(--main-text);
            transition: 0.3s;
        }
    }
}
</style>
