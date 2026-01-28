<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="评价晒单"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row collection-store">
            <div class="title-or-tabs">
                <div class="tag-and-input">
                    <div class="tig-tabs">
                        <div v-for="item in menuList" :class="orderType == item.type ? 'active' : ''" class="tab" @click="changeType(item.type)">
                            <div v-if="item.size > 0" class="sub">{{ item.size }}</div>
                            <div class="tig-font-style">{{ $t(item.value) }}</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="page-info-body" v-loading="loading">
                <div v-if="orderType != 2">
                    <table v-for="item in filterState" class="order-table back-greed">
                        <thead>
                            <tr>
                                <th colspan="4">
                                    {{ $t("订单号") }}：<span class="font-color">{{ item.orderSn }}</span>
                                    <span style="margin-left: 30px">{{ $t("下单时间") }}：{{ item.addTime }}</span>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="order-tr">
                                <td class="column-1 wid-1">
                                    <div v-for="product in item.items" style="display: flex; align-items: center; justify-content: space-between">
                                        <NuxtLink
                                            :title="product.productName"
                                            :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })"
                                            class="nuxt"
                                            target="_blank"
                                            style="display: flex; align-items: center; justify-content: space-between"
                                        >
                                            <el-image
                                                :src="imageFormat(product.picThumb)"
                                                loading="lazy"
                                                style="height: 40px; width: 40px; transition: opacity 0.2s linear; margin-right: 10px"
                                            />
                                            <span>{{ product.productName }}</span>
                                        </NuxtLink>
                                        <div>
                                            <ul style="line-height: 1.5">
                                                <li style="color: #999999">X{{ product.quantity }}</li>
                                            </ul>
                                        </div>
                                    </div>
                                </td>
                                <td class="column-2-4">
                                    <div>
                                        <div>{{ $t("收货人") }}:{{ item.consignee }}</div>
                                    </div>
                                </td>
                                <td class="column-2-4">
                                    <div>
                                        <div>
                                            {{ $t("订单总额") }}:
                                            <FormatPrice v-model="item.totalAmount" :showText="false"></FormatPrice>
                                        </div>
                                    </div>
                                </td>
                                <td class="column-2-4">
                                    <div>
                                        <div class="black">
                                            <NuxtLink :to="'/member/order/Info?id=' + item.orderId">{{ $t("订单详情") }}</NuxtLink>
                                        </div>
                                        <div>
                                            <NuxtLink :to="'/member/comment/Info?id=' + item.orderId">
                                                <el-button size="small">{{ orderType == 0 ? $t("去评价") : $t("待晒单") }}</el-button>
                                            </NuxtLink>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div v-else>
                    <MemberCommentList v-model="filterState"></MemberCommentList>
                </div>
                <div class="el-page" v-if="filterState.length > 0 && !loading">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
                <template v-if="filterState.length == 0">
                    <div class="no-result">
                        <div v-if="!loading">
                            <span class="sign sign1"></span>
                        </div>
                        <div v-if="!loading">
                            <ul>
                                <li>{{ $t("当前筛选下还没有任何订单哦") }}!</li>
                                <li style="font-size: 12px">
                                    {{ $t("现在就去") }}：
                                    <NuxtLink class="font-color" to="/">{{ $t("选购商品") }}</NuxtLink>
                                </li>
                            </ul>
                        </div>
                    </div>
                </template>
            </div>
        </div>
    </div>

    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref, onMounted } from "vue";
import { getOrderList } from "~/api/user/order";
import { getShowedList, getCommentList, getCommentSubNum } from "~/api/user/comment";
import type { CommentFilterState, CommentFilterParams } from "~/types/user/comment.d";
import { Pagination } from "~/components/list";
import { urlFormat } from "~/utils/format";
import { imageFormat } from "@/utils/format";
const menuList = reactive<any>([
    { type: 0, value: "待评价", size: 0 },
    { type: 1, value: "待晒单", size: 0 },
    { type: 2, value: "已评价", size: 0 }
]);
definePageMeta({
    middleware: "auth"
});
const { t } = useI18n();
const route = useRoute();
const orderType = ref<any>(route.query.orderType || 0);
const filterState = ref(<CommentFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<CommentFilterParams>({
    //初使化用于查询的参数
    page: 1
});
const changeType = (type: number) => {
    orderType.value = type;
    filterParams.page = 1;
    loadFilter();
};
const loadFilter = async () => {
    filterState.value = [];
    loading.value = true;
    try {
        let result: any = [];
        if (orderType.value == 0) {
            result = await getOrderList({ ...filterParams, commentStatus: 0 });
        }
        if (orderType.value == 1) {
            result = await getShowedList({ ...filterParams });
        }
        if (orderType.value == 2) {
            result = await getCommentList({ ...filterParams });
        }
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const getSubNum = async () => {
    try {
        const result = await getCommentSubNum();
        menuList[0].size = result.awaitComment;
        menuList[1].size = result.showPics;
        menuList[2].size = result.commented;
    } catch (error: any) {
        message.error(error.message);
    }
};
onMounted(() => {
    getSubNum();
    loadFilter();
});
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.collection-store {
    .title-or-tabs {
        .tag-and-input {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            width: 100%;

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

    .order-table {
        color: #666;
        border: 1px solid #eee;
        width: 100%;
        margin-top: 20px;
        background-color: #f7f7f7;
        height: 40px;
        line-height: 40px;
        padding-left: 10px;

        & > thead > tr > th {
            padding-left: 10px;
        }

        .order-tr {
            background-color: white;

            .column-1 {
                width: 62%;
                border: 1px solid #eee;

                & > div {
                    padding: 15px;
                    box-sizing: border-box;
                    display: flex;
                    align-items: center;
                    flex-direction: row;
                    border-bottom: 1px solid #eee;
                    width: 100%;

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
                width: 14%;
                border: 1px solid #eee;

                & > div {
                    display: flex;
                    height: 100%;
                    padding: 20px 0;
                    box-sizing: border-box;
                    flex-direction: column;
                    justify-content: flex-start;
                    align-items: center;
                    color: #aaaaaa;
                    gap: 8px;

                    & > .black {
                        color: black;
                    }

                    & > .money {
                        text-align: center;
                        width: 80%;
                        border-bottom: 1px solid #e5e5e5;
                    }

                    & > div {
                        line-height: 2;
                    }
                }

                a {
                    cursor: pointer;
                }
            }
        }
    }
}
</style>
