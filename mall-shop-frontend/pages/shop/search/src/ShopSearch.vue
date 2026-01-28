<template>
    <CommonPageHeader></CommonPageHeader>
    <div style="background: white" v-if="loading">
        <template v-for="(item, index) in moduleList" :key="index">
            <ShopInfo
                @treeList="treeListData"
                :pageModule="pageModule"
                :shopId="Number(shopId)"
                :module="item.module"
                v-if="item.type == 'shopInfo'"
            ></ShopInfo>
        </template>
        <div class="container">
            <div class="page-info">
                <CatGoodsList v-model:queryParams="queryParams" v-model:filter="filter"></CatGoodsList>
            </div>
        </div>
    </div>
    <CommonPageFooter :mt30="false" :showFriendly="false"></CommonPageFooter>
</template>
<script setup lang="ts">
import { ref, computed } from "vue";
import { getShopDecorate } from "~/api/shop/shop";
import CatGoodsList from "~/components/common/CatGoodsList.vue";
import type { QueryParams } from "~/types/search/search";
import { useRouter } from "vue-router";
import { useCommonStore } from "@/store/common";
const commonStore = useCommonStore();
const { t } = useI18n();
const titleStr = computed(() => {
    let title = t("店铺商品搜索");
    if (commonStore.poweredByStatus != 1) {
        title += "-powered by tigshop";
    }
    return title;
});
const leftMenuList = ref([]);
const treeListData = (items: any) => {
    leftMenuList.value = items;
};
const loading = ref(false);
const description = ref("");
const keyword = ref("");
useHead({
    title: titleStr,
    meta: [
        { name: "description", content: description },
        { name: "keyword", content: keyword }
    ]
});
const route = useRoute();
const shopId = ref(route.path.split("/").pop());
const queryParams = computed(() => {
    let query = <QueryParams>{};
    if (filter.page > 1) {
        query.page = filter.page;
    }
    if (filter.shopId > 0) {
        query.shopId = filter.shopId;
    }
    if (filter.keyword != "") {
        query.keyword = filter.keyword;
    }
    return query;
});

const filter: any = reactive({
    page: 1,
    keyword: "",
    shopId: -1
});
const router = useRouter();
watch(
    [() => router.currentRoute.value.query],
    () => {
        filter.keyword = router.currentRoute.value.query.keyword;
        filter.shopId = shopId.value;
    },
    { deep: true, immediate: true }
);

const moduleList: any = ref([]);
const pageModule: any = ref({});
const decorateId = ref();
const getModuleList = async () => {
    loading.value = false;
    try {
        const result = await getShopDecorate({ shopId: shopId.value });
        decorateId.value = result.decorateId;
        moduleList.value = result.moduleList;
        Object.assign(pageModule.value, result.pageModule);
    } catch (error) {
    } finally {
        loading.value = true;
    }
};
getModuleList();
</script>
<style scoped lang="less">
.page-info {
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
}
</style>
