<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="收藏的商品"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row collect-product">
            <div class="title-or-tabs">
                <div class="tag-and-input">
                    <div class="tig-tabs">
                        <MemberTopMenu :menuList="menuList"></MemberTopMenu>
                    </div>
                    <div class="input">
                        <el-input v-model="filterParams.keyword" :placeholder="$t('请输入商品名称')" type="text" />
                        <el-button class="ml10" @click="loadFilter">{{ $t("搜索") }}</el-button>
                    </div>
                </div>
            </div>
            <div class="page-info-body">
                <div v-loading="loading" class="collect-list-content">
                    <template v-if="total > 0">
                        <div v-for="item in filterState" class="card">
                            <div class="image-info-card">
                                <NuxtLink
                                    :title="item.productName"
                                    :to="urlFormat({ path: 'product', id: item.productId, sn: item.productSn })"
                                    class="nuxt"
                                    target="_blank"
                                >
                                    <el-image :src="imageFormat(item.picThumb)" class="pro_pic" loading="lazy" />
                                    <div>
                                        <div class="text-ellipsis">
                                            {{ item.productName }}
                                        </div>
                                    </div>
                                </NuxtLink>
                                <div style="text-align: center">
                                    <FormatPrice v-model="item.price" :fontStyle="{ fontSize: '14px', fontWeight: '700', lineHeight: '220x' }"></FormatPrice>
                                </div>
                            </div>
                            <div class="operation-card">
                                <template v-if="item.productSku?.length > 0">
                                    <NuxtLink :to="urlFormat({ path: 'product', id: item.productId, sn: item.productSn })" class="butt butt1" target="_blank">
                                        <i class="iconfont-pc icon-shiwu-gouwuche"></i>{{ $t(getBuyText()) }}
                                    </NuxtLink>
                                </template>
                                <template v-else>
                                    <Buy :id="item.productId" :disabled="false" :number="1" class="butt butt1">
                                        <i class="iconfont-pc icon-shiwu-gouwuche"></i>{{ $t(getBuyText()) }}
                                    </Buy>
                                </template>
                                <div class="butt" @click="deleteASiteCollection(item.productId)">
                                    <i class="iconfont-pc icon-changyonggoupiaorenshanchu"></i>{{ $t("取消收藏") }}
                                </div>
                            </div>
                        </div>
                        <div class="el-page">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </template>
                    <template v-else>
                        <div class="no-result">
                            <span v-if="!loading">{{ $t("您还没有收藏的商品") }}</span>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref } from "vue";
import type { CollectProductFilterParams, CollectProductFilterState } from "~/types/user/collectProduct";
import { delCollectProduct, getCollectProductList } from "~/api/user/collectProduct";
import { urlFormat } from "~/utils/format";
import { isMerchant } from "~/utils/util";
import { Pagination } from "~/components/list";
import Buy from "~/components/product/Buy.vue";
import { useRouter } from "vue-router";
definePageMeta({
    middleware: "auth"
});
const filterState = ref(<CollectProductFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<CollectProductFilterParams>({
    //初使化用于查询的参数
    page: 1,
    keyword: ""
});
const { t } = useI18n();

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getCollectProductList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
loadFilter();

const deleteASiteCollection = async (value: number) => {
    try {
        loading.value = true;
        const result = await delCollectProduct({ id: value });
        await loadFilter();
        message.success(t("取消收藏成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const menuList = computed(() => {
    const list = [{ value: "收藏的商品", url: "/member/collectProduct/list", size: 0 }];

    if (isMerchant()) {
        list.push({ value: "收藏的店铺", url: "/member/collectShop/list", size: 0 });
    }

    return list;
});
</script>

<style lang="less" scoped>
@import "/assets/css/member/member";

.collect-product {
    .title-or-tabs {
        .tag-and-input {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            width: 100%;

            .tig-tabs {
                & > div {
                    color: #333333;
                    font-size: 16px;
                    font-weight: 400;
                    letter-spacing: 0;
                    display: inline-block;
                }
            }

            .tig-font-style {
                font-size: 14px;
            }

            .image {
                margin-bottom: 20px;
            }

            .input {
                display: flex;
                align-items: center;
            }
        }
    }

    .collect-list-content {
        background-color: #ffffff;
        box-sizing: border-box;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 20px;

        .card {
            border: 1px solid #eee;
            width: 23%;

            .image-info-card {
                display: flex;
                flex-direction: column;
                padding: 24px;
                .pro_pic {
                    border: 0.8px solid #eee;
                    transition: opacity 0.2s linear;
                }

                .nuxt {
                    & > img {
                        width: 160px;
                        height: 160px;
                    }

                    & > div {
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        flex-wrap: nowrap;
                        margin-top: 16px;

                        .text-ellipsis {
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            width: 160px;
                            margin-bottom: 10px;
                        }
                    }
                }
            }

            .operation-card {
                height: 34px;
                cursor: pointer;
                background-color: #f9f9f9;
                border-top: 1px solid #eee;
                font-size: 12px;
                display: flex;
                justify-content: center;
                align-items: center;
                color: #666666;
                line-height: 34px;

                .butt1 {
                    border-right: 1px solid #eee;
                }

                .butt {
                    flex: 1;
                    text-align: center;
                    align-items: center;
                    display: flex;
                    justify-content: center;
                    gap: 8px;
                }
            }
        }
    }
}
</style>
