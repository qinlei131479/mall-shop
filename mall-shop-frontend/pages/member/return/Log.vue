<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="申请记录"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs">
                <div class="tag-and-input">
                    <div class="tig-tabs">
                        <MemberTopMenu :menuList="menuList"></MemberTopMenu>
                    </div>
                </div>
            </div>
            <div class="page-info-body">
                <div class="order-title">
                    <div class="ul">
                        <div style="width: 49%">{{ $t("商品名称") }}</div>
                        <div style="width: 15%">{{ $t("申请数量") }}</div>
                        <div style="width: 0%"></div>
                        <div style="width: 12%">{{ $t("售后类型") }}</div>
                        <div style="width: 12%">{{ $t("审核状态") }}</div>
                        <div style="width: 12%">{{ $t("操作") }}</div>
                    </div>
                </div>
                <div v-loading="loading">
                    <template v-if="total > 0">
                        <table v-for="(item, index) in filterState" class="order-table">
                            <thead>
                                <tr>
                                    <th colspan="4">
                                        {{ $t("售后单号") }}：<span class="font-color">{{ item.aftersalesSn }}</span>
                                        <span style="margin-left: 30px">{{ $t("订单号") }}：{{ item.orderSn }}</span>
                                        <span style="margin-left: 30px">{{ $t("申请时间") }}：{{ item.addTime }}</span>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr class="order-tr">
                                    <td class="column-1">
                                        <div v-for="product in item.aftersalesItems">
                                            <NuxtLink
                                                :title="product.productName"
                                                :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })"
                                                class="nuxt"
                                                target="_blank"
                                            >
                                                <el-image
                                                    :src="imageFormat(product.picThumb)"
                                                    loading="lazy"
                                                    style="height: 40px; width: 40px; transition: opacity 0.2s linear"
                                                />
                                                <div>
                                                    <div class="text-ellipsis">
                                                        {{ product.productName }}
                                                    </div>
                                                </div>
                                            </NuxtLink>
                                            <div style="color: #aaa">X{{ product.number }}</div>
                                            <div></div>
                                        </div>
                                    </td>
                                    <td class="column-2-4">
                                        <div>
                                            {{ item.aftersalesTypeName }}
                                        </div>
                                    </td>
                                    <td class="column-2-4">
                                        <div>
                                            {{ item.statusName }}
                                        </div>
                                    </td>
                                    <td class="column-2-4">
                                        <div class="black">
                                            <NuxtLink :to="'/member/return/logInfo?id=' + item.aftersaleId" class="font-color">{{ $t("详情") }}</NuxtLink>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <div class="el-page">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </template>
                    <template v-else>
                        <div class="no-result">
                            <span v-if="!loading">{{ $t("暂无已申请退换货订单") }}</span>
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
import { useRouter } from "vue-router";

import type { AfterSalesRecordFilterParams, AfterSalesRecordFilterState } from "~/types/user/afterSalesRecord";
import { getAfterSalesRecordList } from "~/api/user/afterSalesRecord";
import { urlFormat } from "~/utils/format";
import { Pagination } from "~/components/list";
import { message } from "ant-design-vue";

const filterState = ref(<AfterSalesRecordFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<AfterSalesRecordFilterParams>({
    //初使化用于查询的参数
    page: 1
});
definePageMeta({
    middleware: "auth"
});
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAfterSalesRecordList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

const router = useRouter();
const splitStr = (str: string) => {
    return str.split("?")[0];
};
const menuList = reactive<any>([
    { value: "退/换货申请", url: "/member/return/list", size: 0 },
    { value: "申请记录", url: "/member/return/log", size: 0 }
]);
</script>

<style lang="less" scoped>
@import "/assets/css/member/member";

.order-table {
    color: #666;
    border: 1px solid #eee;
    width: 100%;
    background-color: #f7f7f7;
    height: 40px;
    line-height: 40px;
    margin-bottom: 10px;

    & > thead > tr > th {
        padding-left: 10px;
    }

    .order-tr {
        background-color: white;
        box-sizing: border-box;

        .column-1 {
            width: 64%;
            border: 1px solid #eee;

            .nuxt {
                display: flex;
                gap: 10px;
                flex: 1;
            }

            & > div {
                padding: 15px;
                box-sizing: border-box;
                display: flex;
                align-items: center;
                flex-direction: row;
                border-bottom: 1px solid #eee;
                width: 100%;

                & > span {
                    flex: 1;
                    margin-left: 10px;
                    line-height: 2;
                }

                & > div {
                    margin-left: 10px;
                    width: 60px;
                    text-align: center;
                }
            }

            & > div:last-child {
                border-bottom: none;
            }
        }

        .column-2-4 {
            width: 12%;

            border: 1px solid #eee;

            & > div {
                display: flex;
                height: 100%;
                box-sizing: border-box;
                justify-content: center;
                align-items: center;
            }

            a {
                cursor: pointer;
            }
        }
    }
}
</style>
