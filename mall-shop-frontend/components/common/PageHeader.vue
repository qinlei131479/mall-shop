<template>
    <CommonTopBar :colorType="colorType" :pageType="pageType"></CommonTopBar>
    <div class="header" :style="{ borderBottom: pageType != 1 ? '1px none #ddd' : '1px solid #ddd' }">
        <div class="top-area container">
            <div class="wrapper">
                <div class="logo">
                    <NuxtLink exact-active-class="''" to="/" class="alv1">
                        <img :style="{ width: pageType != 1 ? '140px' : '190px' }" :src="imageFormat(commonStore.shopLogo)" alt="" />
                    </NuxtLink>
                </div>
                <template v-if="pageType != 1">
                    <SimNav :page="page"></SimNav>
                </template>
                <template v-if="pageType == 1">
                    <DefSearch></DefSearch>
                </template>
                <template v-else>
                    <SimSearch></SimSearch>
                </template>
                <ShoppingCart v-if="pageType == 1"></ShoppingCart>
            </div>
            <template v-if="showDefNav && pageType == 1">
                <DefNav :page="page"></DefNav>
            </template>
        </div>
    </div>
    <CommonService :phone="phone" :shopId="shopId" :orderId="orderId" :productId="productId" v-if="showService"></CommonService>
    <ClientOnly>
        <CommonStatisticLog
            ref="statisticLogRef"
            :immediateLog="immediateLog"
            :shopCategoryId="shopCategoryId"
            :shopId="shopId"
            :productId="productId"
        ></CommonStatisticLog>
    </ClientOnly>
</template>
<script lang="ts" setup>
import { useCommonStore } from "@/store/common";
import SimSearch from "~/components/common/src/SimSearch.vue";
import DefSearch from "~/components/common/src/DefSearch.vue";
import DefNav from "~/components/common/src/DefNav.vue";
import SimNav from "~/components/common/src/SimNav.vue";

const commonStore: any = useCommonStore();
const statisticLogRef = ref();
commonStore.loadNav();
const pageType = computed(() => {
    return commonStore.decoratePageConfig?.headerStyle;
});
const colorType = computed(() => {
    return commonStore.decoratePageConfig?.colorStyle;
});

defineProps({
    page: { type: String, default: "" },
    showService: {
        type: Boolean,
        default: false
    },
    shopId: {
        type: [Number, String],
        default: 0
    },
    productId: {
        type: [Number, String],
        default: 0
    },
    orderId: {
        type: [Number, String],
        default: 0
    },
    shopCategoryId: {
        type: [Number, String],
        default: ""
    },
    immediateLog: {
        type: Boolean,
        default: true
    },
    phone: {
        type: String,
        default: ""
    },
    showDefNav: {
        type: Boolean,
        default: true
    }
});

const handleLog = () => {
    statisticLogRef.value?.__commonLog();
};
defineExpose({
    handleLog
});
</script>
<style lang="less" scoped>
.acea-row {
    display: flex;
    flex-wrap: wrap;
}

.acea-row.row-middle {
    align-items: center;
}

.acea-row.row-right {
    justify-content: flex-end;
}

.acea-row.row-center-wrapper {
    align-items: center;
    justify-content: center;
}

.acea-row.row-between-wrapper {
    align-items: center;
    justify-content: space-between;
}

.header {
    background: #fff;

    position: relative;
}

.top-area {
    position: relative;

    .wrapper {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .logo {
            min-height: 100px;
            display: flex;
            align-items: center;

            img {
                padding: 10px 0;
            }
        }
    }
}
</style>
