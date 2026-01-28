<template>
    <div class="order-list">
        <div v-if="modelValue" class="tag-and-input">
            <div class="tig-tabs">
                <div v-for="item in menuTypeList" :class="menuType == item.type ? 'active' : ''" class="tab" @click="changeType(item.type)">
                    <span v-if="item.num">{{ item.num }}</span>
                    <p>{{ $t(item.value) }}</p>
                </div>
            </div>
            <div class="input">
                <el-input v-model="filterParams.keyword" :placeholder="$t('收货人名称或订单号')" type="text" />
                <el-button class="ml10" @click="loadFilter()">{{ $t("搜索") }}</el-button>
            </div>
        </div>
        <div class="order-title">
            <div class="ul">
                <template v-if="modelValue">
                    <div style="width: 23%">
                        <MemberOrderSelect v-model="filterParams.dateType" :selectList="orderTimeList" @loadFilter="loadFilter"></MemberOrderSelect>
                    </div>
                    <div style="width: 28%">{{ $t("商品名称") }}</div>
                </template>
                <template v-else>
                    <div style="width: 49%">{{ $t("商品名称") }}</div>
                </template>
                <div style="width: 6%">{{ $t("数量") }}</div>
                <div style="width: 7%"></div>
                <div style="width: 12%">{{ $t("金额") }}</div>
                <div style="width: 14%">{{ $t("交易状态") }}</div>
                <div style="width: 12%">{{ $t("操作") }}</div>
            </div>
        </div>
        <div v-loading="loading">
            <template v-if="total > 0">
                <table v-for="(item, index) in filterState" class="order-table">
                    <thead>
                        <tr>
                            <th colspan="4" class="order-table-title">
                                {{ $t("订单号") }}：<NuxtLink :to="'/member/order/info?id=' + item.orderId" target="_blank"
                                    ><span class="font-color">{{ item.orderSn }}</span></NuxtLink
                                >
                                <span style="margin-left: 30px" v-if="item.shop && item.shop.shopTitle"
                                    >{{ $t("下单店铺") }}：<NuxtLink :to="urlFormat({ path: 'shop', id: item.shopId })" target="_blank"
                                        ><span class="font-color">{{ item.shop.shopTitle }}</span></NuxtLink
                                    ></span
                                >
                                <span style="margin-left: 30px">{{ $t("下单时间") }}：{{ item.addTime }}</span>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr class="order-tr">
                            <td class="column-1">
                                <div v-for="product in item.items">
                                    <NuxtLink
                                        :title="product.productName"
                                        :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })"
                                        class="nuxt"
                                        target="_blank"
                                    >
                                        <el-image
                                            :src="imageFormat(product.picThumb)"
                                            loading="lazy"
                                            style="height: 40px; width: 40px; transition: opacity 0.2s linear; flex-shrink: 0"
                                        />
                                        <div>
                                            <div class="text-ellipsis">
                                                {{ product.productName }}
                                            </div>
                                        </div>
                                    </NuxtLink>
                                    <div style="color: #aaa">X{{ product.quantity }}</div>
                                    <div></div>
                                </div>
                            </td>
                            <td class="column-2-4">
                                <div>
                                    <div>
                                        {{ $t("总额") }}:
                                        <FormatPrice v-model="item.totalAmount" :showText="false"></FormatPrice>
                                    </div>
                                    <template v-if="item.orderStatus == 0 && item.unpaidAmount > 0">
                                        <div class="black">{{ $t("应付") }}</div>
                                        <div class="money">
                                            <FormatPrice v-model="item.unpaidAmount" :showText="false"></FormatPrice>
                                        </div>
                                    </template>
                                    <div>{{ item.payTypeId === 1 ? $t("在线支付") : item.payTypeId === 2 ? $t("货到付款") : $t("线下支付") }}</div>
                                </div>
                            </td>
                            <td class="column-2-4">
                                <div>
                                    <div
                                        :class="{
                                            orange: item.orderStatus === 0,
                                            black: item.orderStatus === 1 || item.orderStatus === 2 || item.orderStatus === 4,
                                            gray: item.orderStatus === 3,
                                            green: item.orderStatus === 5
                                        }"
                                    >
                                        {{ $t(item.orderStatusName) }}
                                    </div>
                                    <div class="black">
                                        <NuxtLink :to="'/member/order/info?id=' + item.orderId" target="_blank">{{ $t("订单详情") }}</NuxtLink>
                                    </div>
                                    <div v-if="(item.orderStatus == 2 || item.orderStatus == 5) && item.shippingMethod == 1" class="black">
                                        <el-popover :width="600" placement="bottom" @show="item.showPopover = true" @hide="item.showPopover = false">
                                            <template v-if="item.showPopover">
                                                <LogisticsTracking
                                                    :trackingNo="item.trackingNo"
                                                    :logisticsName="item.logisticsName"
                                                    :id="item.orderId"
                                                ></LogisticsTracking>
                                            </template>
                                            <template #reference>
                                                <span style="cursor: pointer">
                                                    <i> {{ $t("物流跟踪") }}</i>
                                                    <i style="font-size: 12px" class="iconfont-pc icon-xiajiantou1"></i>
                                                </span>
                                            </template>
                                        </el-popover>
                                    </div>
                                </div>
                            </td>
                            <td class="column-2-4">
                                <div>
                                    <div v-if="item.availableActions.toPay">
                                        <NuxtLink :to="'/order/pay/?id=' + item.orderId" target="_balnk">
                                            <el-button size="small" type="primary">{{ $t("付款") }}</el-button>
                                        </NuxtLink>
                                    </div>
                                    <a v-if="item.availableActions.cancelOrder" @click="onCancelOrder(item.orderId)"> {{ $t("取消订单") }} </a>
                                    <a v-if="item.availableActions.delOrder" @click="onDelOrder(item.orderId)"> {{ $t("删除") }} </a>
                                    <div v-if="item.availableActions.confirmReceipt" class="mb10">
                                        <el-button type="primary" size="small" @click="onConfirmReceipt(item.orderId)">{{ $t("确认收货") }}</el-button>
                                    </div>
                                    <div v-if="item.availableActions.toAftersales" class="mb10">
                                        <NuxtLink :to="'/member/return/info/?orderId=' + item.orderId">
                                            <el-button size="small">{{ $t("申请售后") }}</el-button>
                                        </NuxtLink>
                                    </div>
                                    <div v-if="item.items && item.items[0]?.aftersalesItem" class="mb10">
                                        <NuxtLink :to="'/member/return/logInfo?id=' + item.items[0]?.aftersalesItem?.aftersaleId">
                                            <el-button size="small">{{ $t("售后详情") }}</el-button>
                                        </NuxtLink>
                                    </div>
                                    <div v-if="item.availableActions.rebuy" class="mb10">
                                        <el-button size="small" @click="onReBuy(item.orderId)">{{ $t("再次购买") }}</el-button>
                                    </div>
                                    <div v-if="item.availableActions.toComment" class="mb10">
                                        <NuxtLink :to="'/member/comment/info/?id=' + item.orderId">
                                            <el-button size="small">{{ $t("去评价") }}</el-button>
                                        </NuxtLink>
                                    </div>
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
                    <div v-if="!loading">
                        <span class="sign sign1"></span>
                    </div>
                    <div v-if="!loading">
                        <ul>
                            <li>{{ $t("当前筛选下还没有任何订单哦") }}</li>
                            <li style="font-size: 12px">
                                {{ $t("现在就去") }}：
                                <NuxtLink class="font-color" to="/"> {{ $t("选购商品") }}</NuxtLink>
                            </li>
                        </ul>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import type { OrderFilterParams, OrderFilterState } from "~/types/user/order.d";
import { cancelOrder, delOrder, getOrderList, getOrderNum, reBuy, confirmReceipt } from "~/api/user/order";
import { Pagination } from "~/components/list";
import { urlFormat } from "~/utils/format";
import { message, Modal } from "ant-design-vue";
import { useRoute } from "vue-router";
import LogisticsTracking from "~/components/member/order/LogisticsTracking.vue";
const { t } = useI18n();
const props = defineProps({
    modelValue: {
        type: Number,
        default: 1
    }
});

const emit = defineEmits(["change"]);

// 基本参数定义
const filterState = ref(<OrderFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<OrderFilterParams>({
    //初使化用于查询的参数
    page: 1,
    sortField: "addTime",
    sortOrder: "desc",
    keyword: "",
    payStatus: -1,
    dateType: 1
});
const route = useRoute();
const menuType = ref<string>("all");

interface menuType {
    type: string;
    value: string;
    num: number;
}

const menuTypeList = reactive<menuType[]>([
    { type: "all", value: "全部订单", num: 0 },
    { type: "awaitPay", value: "待支付", num: 0 },
    { type: "awaitShipping", value: "待发货", num: 0 },
    { type: "awaitReceived", value: "待收货", num: 0 },
    { type: "awaitComment", value: "待评价", num: 0 }
]);
const changeType = (type: string) => {
    filterParams.page = 1;

    filterParams.orderStatus = -1;
    filterParams.commentStatus = -1;
    if (type === "awaitPay") {
        filterParams.orderStatus = 0;
    } else if (type === "awaitShipping") {
        filterParams.orderStatus = 1;
    } else if (type === "awaitReceived") {
        filterParams.orderStatus = 2;
    } else if (type === "awaitComment") {
        filterParams.commentStatus = 0;
    }
    menuType.value = type;
    loadFilter();
};
const loadNum = async () => {
    loading.value = true;
    try {
        const result = await getOrderNum();
        menuTypeList.forEach((item: any) => {
            let temp: any = result;
            if (temp[item.type] !== undefined) {
                item.num = temp[item.type];
            }
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const payStatusList = reactive([
    { value: -1, label: "交易状态" },
    { value: 0, label: "未支付" },
    { value: 2, label: "已支付" }
]);
const orderTimeList = reactive([
    { value: 0, label: "全部订单" },
    { value: 1, label: "最近三个月订单" },
    { value: 2, label: "最近六个月订单" },
    { value: 3, label: "最近一年内订单" }
]);
const onCancelOrder = async (orderId: number) => {
    Modal.confirm({
        title: t("确认要取消该订单吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                const result = await cancelOrder({ id: orderId });
                message.success(t("取消成功"));
                await loadFilter();
                await loadNum();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};
const onDelOrder = async (orderId: number) => {
    Modal.confirm({
        title: t("确认要删除该订单吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                await delOrder({ id: orderId });
                message.success(t("删除成功"));
                await loadFilter();
                await loadNum();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};
// 再次购买
const onReBuy = async (orderId: number) => {
    try {
        await reBuy({ id: orderId });
        navigateTo(`/cart`);
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

/**
 * 确认收货
 */
const onConfirmReceipt = async (orderId: number) => {
    Modal.confirm({
        title: t("确认要收货吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                const result = await confirmReceipt({ id: orderId });
                loadFilter();
                loadNum();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

const loadFilter = async () => {
    loading.value = true;
    try {
        total.value = 0;
        const result = await getOrderList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadNum().then(() => {
        changeType((route.query.type as string) || "all");
        loadFilter();
    });
});

function getDates(daysBefore: any) {
    const formatDate = (date: any) => date.toISOString().split("T")[0];

    const today = new Date();
    const beforeDate = new Date(today.setDate(today.getDate() - daysBefore));

    return {
        today: formatDate(new Date()), // 重新获取今天的日期，因为上一步修改了today变量
        before: formatDate(beforeDate)
    };
}
const preparePopover = (item: any, index: any) => {
    item.show_popover = true;
};
</script>

<style lang="less" scoped>
@import "/assets/css/member/member";

.order-list {
    .tag-and-input {
        display: flex;
        flex-direction: row;
        justify-content: space-between;

        .image {
            margin-bottom: 20px;
        }

        .input {
            display: flex;
            align-items: center;
        }
    }

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
                    padding: 20px 0;
                    box-sizing: border-box;
                    flex-direction: column;
                    justify-content: flex-start;
                    align-items: center;
                    color: #aaaaaa;

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
.orange {
    color: #ed6a0c;
}
.green {
    color: #31c19e;
}
.gray {
    color: #999999;
}
</style>
