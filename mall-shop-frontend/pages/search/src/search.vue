<template>
    <div style="background-color: #fff">
        <CommonPageHeader></CommonPageHeader>
        <CommonHeader :title="filter.keyword ? `${filter.keyword} - 商品搜索` : '商品搜索'"></CommonHeader>
        <div class="container">
            <shopInfo :keyword="filter.keyword" v-if="isMerchant() && filter.keyword" style="flex-shrink: 0"></shopInfo>
            <CommonCategoryNavigation v-model:id="filter.categoryId" isSearchPage @change="onFilterChange" style="flex-shrink: 0">
                <template v-if="filterSeleted">
                    <div class="nav-selected" v-if="filter.brandIds.length > 0 && filterSeleted.brand">
                        <div
                            class="item"
                            @click="
                                filter.brandIds = [];
                                onFilterChange();
                            "
                        >
                            <span>{{ $t("品牌") }}：</span><span>{{ filterSeleted.brand }}</span>
                        </div>
                    </div>
                    <div class="nav-selected" v-if="filter.minPrice > 0 || filter.maxPrice > 0">
                        <div
                            class="item"
                            @click="
                                filter.maxPrice = 0;
                                filter.minPrice = 0;
                                onFilterChange();
                            "
                        >
                            <span>{{ $t("价格") }}：</span
                            ><span>{{ filter.minPrice ?? 0 }} {{ filter.maxPrice > 0 ? " - " + filter.maxPrice : $t("以上") }}</span>
                        </div>
                    </div>
                    <template v-if="attrList.length > 0 && filterSeleted.attrs">
                        <div class="nav-selected">
                            <div v-for="(value, key) in filterSeleted.attrs" :key="key" class="item" @click="clearAttr(value.attrName)">
                                <span>{{ value.attrName }}：</span
                                ><span v-for="(child, index) in value.attrValue">{{ child }}<span v-if="index < value.attrValue.length - 1">，</span></span>
                            </div>
                        </div>
                    </template>
                </template>

                <div class="nav-selected" v-if="filter.keyword">
                    <div
                        class="item"
                        v-if="filter.keyword"
                        @click="
                            filter.keyword = '';
                            onFilterChange();
                        "
                    >
                        <span>{{ $t("搜索") }}：</span><span>"{{ filter.keyword }}"</span>
                    </div>
                </div>
            </CommonCategoryNavigation>
            <div ref="FilterRef">
                <CommonFilter
                    ref="filterRef"
                    v-model:filter="filter"
                    v-model:filterSeleted="filterSeleted"
                    v-model:queryParams="queryParams"
                    :pageType="pageType"
                    :attrList="attrList"
                    isSearchPage
                    @change="onFilterChange"
                    @changeAttr="onFilterAttrChange"
                ></CommonFilter>
            </div>
            <CommonCatGoodsList
                v-model:queryParams="queryParams"
                v-model:filter="filter"
                :attrs="attrList"
                @change="onFilterChange"
                @pageChange="onPageChange"
            />
        </div>
        <CommonPageFooter></CommonPageFooter>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import type { filterSeleted, SearchFilter, QueryParams } from "@/types/search/search.d";
import shopInfo from "./shopInfo.vue";
import { isMerchant } from "@/utils/util";
const props = defineProps({
    pageType: {
        type: String,
        default: "search"
    }
});
const filterRef = ref();
const route = useRoute();
const attrList = ref<any[]>([]);
const filter = reactive<SearchFilter>({
    page: 1,
    categoryId: 0,
    brandIds: [],
    sortField: "",
    sortOrder: "",
    maxPrice: 0,
    minPrice: '',
    couponId: '',
    keyword: ""
});
// 当路由参数变化时，同步更新filter的值
watchEffect(() => {
    filter.page = route.query.page ? Number(route.query.page) : 1;
    filter.categoryId = route.query.cat ? Number(route.query.cat) : 0;
    filter.brandIds = route.query.brand ? String(route.query.brand).split(",").map(Number) : [];
    filter.sortField = route.query.sort ? String(route.query.sort) : "";
    filter.sortOrder = route.query.order ? String(route.query.order) : "";
    filter.maxPrice = route.query.max ? Number(route.query.max) : '';
    filter.minPrice = route.query.min ? Number(route.query.min) : '';
    filter.keyword = route.query.keyword ? String(route.query.keyword) : "";
    filter.couponId = route.query.couponId ? Number(route.query.couponId) : 0;
});

const filterSeleted = ref<filterSeleted>();

watch(
    () => filterSeleted.value,
    (newVal) => {
        if (newVal?.brand == null) {
            filterRef.value?.clearMultipleBrandIds();
        }
    }
);

const queryParams = computed(() => {
    let query = <QueryParams>{};
    if (filter.page > 1) {
        query.page = filter.page;
    }
    if (filter.categoryId > 0) {
        query.cat = filter.categoryId;
    }
    if (filter.couponId > 0) {
        query.couponId = filter.couponId;
    }
    if (filter.brandIds.length > 0) {
        query.brand = filter.brandIds.join(",");
    }
    if (filter.sortField != "" && ["sale", "price", "time"].includes(filter.sortField)) {
        query.sort = filter.sortField;
    }
    if (filter.sortOrder != "" && ["desc", "asc"].includes(filter.sortOrder)) {
        query.order = filter.sortOrder;
    }
    if (filter.maxPrice > 0) {
        query.max = filter.maxPrice;
    }
    if (filter.minPrice > 0) {
        query.min = filter.minPrice;
    }
    if (filter.keyword != "") {
        query.keyword = filter.keyword;
    }
    return query;
});
const onFilterChange = (page: number = 1) => {
    filter.page = page;
    navigateTo({
        path: "/" + props.pageType + "/",
        query: queryParams.value
    });
};

const onFilterAttrChange = (attrs: any) => {
    attrList.value = attrs;
};

const FilterRef: Ref<HTMLElement | null> = ref(null);
const onPageChange = () => {
    const targetElement = FilterRef.value as HTMLElement;
    if (targetElement) {
        targetElement.scrollIntoView(); // 平滑滚动
    }
};

const clearAttr = (name: string) => {
    attrList.value = attrList.value.filter((item) => item.attrName !== name);
    onFilterChange();
};
</script>
<style lang="less" scoped>
.container {
    .nav-selected {
        min-width: 10px;
        display: flex;
        .item {
            max-width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            box-sizing: border-box;
        }
    }
}
</style>
