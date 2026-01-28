<template>
    <CommonPageHeader
        :showService="true"
        :phone="formState.shop ? formState.shop.kefuPhone : ''"
        :orderId="formState.orderId"
        :shopId="formState.shopId"
    ></CommonPageHeader>
    <CommonHeader title="订单详情"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs right-info">
                <span>{{ $t("订单详情") }}</span>
                <div v-if="!formState.availableActions.splitOrder">
                    <NuxtLink :to="'/member/feedback/askList?orderId=' + formState.orderId + '&orderSn=' + formState.orderSn"
                        ><el-button>{{ $t("订单留言/咨询") }}</el-button></NuxtLink
                    >
                </div>
            </div>
            <div class="order-info">
                <div class="page-info-body lr">
                    <div class="order-left">
                        <div>
                            <span
                                >{{ $t("订单号") }}：<strong>{{ formState.orderSn }}</strong></span
                            >
                            <span style="margin-left: 40px" v-if="formState.shop && formState.shop.shopTitle"
                                >{{ $t("下单店铺") }}：<NuxtLink :to="urlFormat({ path: 'shop', id: formState.shopId })" target="_blank"
                                    ><span class="font-color">{{ formState.shop.shopTitle }}</span></NuxtLink
                                ></span
                            >
                            <span style="margin-left: 40px"
                                >{{ $t("订单状态") }}：
                                <strong>
                                    {{ $t(formState.orderStatusName || "") }}
                                </strong>
                            </span>
                        </div>
                        <template v-if="formState.autoDeliveryDays">
                            <div>{{ $t("订单将在") }}{{ formState.autoDeliveryDays }}{{ $t("天后自动收货") }}</div>
                        </template>
                        <template v-else>
                            <div>{{ $t(oStatusInfo[formState.orderStatus] || "") }}</div>
                        </template>
                    </div>
                    <div class="order-right">
                        <el-button v-if="formState.availableActions.cancelOrder" @click="onCancelOrder">{{ $t("取消订单") }}</el-button>
                        <div v-if="formState.availableActions.toPay">
                            <NuxtLink :to="'/order/pay/?id=' + formState.orderId" target="_balnk">
                                <el-button type="primary">{{ $t("付款") }}</el-button>
                            </NuxtLink>
                        </div>
                        <el-button type="primary" v-if="formState.availableActions.confirmReceipt" @click="onConfirmReceipt">{{ $t("确认收货") }}</el-button>
                        <NuxtLink v-if="formState.availableActions.toAftersales" target="_blank" :to="'/member/return/Info?orderId=' + formState.orderId">
                            <el-button>{{ $t("整单售后") }}</el-button>
                        </NuxtLink>
                    </div>
                </div>

                <div class="page-info-body">
                    <div class="sub-title">{{ $t("订单状态") }}</div>
                    <div class="tig-steps">
                        <template v-for="(item, index) in formState.stepStatus?.steps">
                            <div
                                :style="{
                                    color: index <= formState.stepStatus?.current ? 'var(--general)' : '#c4c4c4',
                                    border: index <= formState.stepStatus?.current ? '4px solid var(--general)' : '4px solid #eee'
                                }"
                                class="steps-card"
                            >
                                <div style="font-weight: 600">{{ index + 1 }}</div>
                                <div :style="{ color: index <= formState.stepStatus?.current ? 'var(--general)' : '#333' }" class="card-text">
                                    <div>{{ $t(item.title || "") }}</div>
                                    <div>{{ $t(item.description || "") }}</div>
                                </div>
                            </div>
                            <div
                                v-if="index != formState.stepStatus?.steps?.length - 1"
                                :style="{ backgroundColor: index < formState.stepStatus?.current ? 'var(--general)' : '#eee' }"
                                class="steps-line"
                            ></div>
                        </template>
                    </div>
                    <LogisticsTracking
                        :trackingNo="formState.trackingNo"
                        :logisticsName="formState.logisticsName"
                        :shippingMethod="formState.shippingMethod"
                        v-if="formState.orderId && (formState.orderStatus === 2 || formState.orderStatus === 5)"
                        v-model:id="formState.orderId"
                    ></LogisticsTracking>
                </div>
                <div class="page-info-body">
                    <div class="sub-title">{{ $t("订单信息") }}</div>
                    <ul class="order-ul" style="margin-top: 20px">
                        <li>
                            {{ $t("收货信息") }}：<span>{{ formState.userAddress }} {{ formState.consignee }} {{ formState.mobile }}</span>
                        </li>
                        <template v-if="formState.shippingTypeName">
                            <li>
                                {{ $t("配送方式") }}：<span>{{ formState.shippingTypeName }}</span>
                            </li>
                        </template>
                        <li>
                            {{ $t("支付方式") }}：<span>{{
                                formState.payTypeId === 1 ? $t("在线支付") : formState.payTypeId === 2 ? $t("货到付款") : $t("线下支付")
                            }}</span>
                        </li>
                        <template v-if="formState.orderType === 6 && formState.items[0].virtualSample">
                            <li class="virtualsample">
                                {{ $t("虚拟内容") }}：<span class="virtualsample-value" v-html="formState.items[0].virtualSample"></span>
                            </li>
                        </template>
                    </ul>
                </div>

                <div class="page-info-body" v-if="showEcard">
                    <div class="sub-title">{{ $t("卡券信息") }}</div>
                    <template v-for="item in formState.items">
                        <div class="sub-sub-title">{{ item.productName }}</div>
                        <ul class="order-ul virtualsample-box" v-if="item.eCard.length > 0">
                            <li class="virtualsample-item" v-for="subItem in item.eCard" :key="subItem">
                                <div class="item">
                                    {{ $t("卡号") }}：<span>{{ subItem.cardNumber }}</span>
                                </div>
                                <div class="item">
                                    {{ $t("卡密") }}：<span>{{ subItem.cardPwd }}</span>
                                </div>
                            </li>
                        </ul>
                    </template>
                </div>

                <div class="page-info-body">
                    <div class="sub-title">{{ $t("订单商品") }}</div>
                    <table class="order-table-info">
                        <thead>
                            <tr class="receipt-header">
                                <th>{{ $t("商品名称") }}</th>
                                <th>{{ $t("商品价格") }}</th>
                                <th>{{ $t("购买数量") }}</th>
                                <th>{{ $t("小计") }}</th>
                                <th v-if="formState.availableActions.toAftersales">{{ $t("操作") }}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in formState.items" class="receipt-body">
                                <td class="receipt-item">
                                    <div>
                                        <img :alt="item.productName" :src="imageFormat(item.picThumb)" class="img-border" />
                                        {{ item.productName }}
                                        <div class="sku-style" v-if="item.skuData">
                                            <div v-for="sku in item.skuData">{{ sku.name }}：{{ sku.value }}</div>
                                        </div>
                                        <template v-if="item.extraSkuData && item.extraSkuData.length">
                                            <div class="extraskudata">
                                                <template v-for="attr in item.extraSkuData" :key="attr">
                                                    <div class="extraskudata-item">
                                                        <div class="title">{{ attr.attrName }}</div>
                                                        <div class="line-box">
                                                            <div class="line"></div>
                                                        </div>
                                                        <div class="content">
                                                            <span class="content-text">{{ attr.attrValue }}</span>
                                                            <FormatPrice class="price" v-model="attr.attrPrice"></FormatPrice>
                                                            <span class="num">x{{ item.quantity }}</span>
                                                        </div>
                                                    </div>
                                                </template>
                                            </div>
                                        </template>
                                    </div>
                                </td>
                                <td class="receipt-item price"><FormatPrice v-model="item.price"></FormatPrice></td>
                                <td class="receipt-item price">{{ item.quantity }}</td>
                                <td class="receipt-item price"><FormatPrice v-model="item.subtotal"></FormatPrice></td>
                                <template v-if="formState.payStatus === 2">
                                    <td class="receipt-item operation">
                                        <template v-if="item.isGift === 0">
                                            <template v-if="item.aftersalesItem">
                                                <NuxtLink target="_blank" :to="'/member/return/logInfo?id=' + item?.aftersalesItem?.aftersaleId">
                                                    <el-button size="small">{{ $t("售后详情") }}</el-button>
                                                </NuxtLink>
                                            </template>

                                            <template v-else-if="formState.availableActions.toAftersales">
                                                <NuxtLink target="_blank" :to="'/member/return/Info?orderId=' + formState.orderId + '&itemId=' + item.itemId">
                                                    <el-button size="small">{{ $t("申请售后") }}</el-button>
                                                </NuxtLink>
                                            </template>
                                        </template>
                                    </td>
                                </template>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr class="receipt-footer">
                                <th :colspan="formState.availableActions.toAftersales ? 3 : 2"></th>
                                <th class="receipt-footer-it one" colspan="2">
                                    <div class="item">
                                        <div class="label">{{ $t("商品总价") }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.productAmount"></FormatPrice></div>
                                    </div>
                                    <div class="item" v-if="formState.serviceFee && formState.serviceFee > 0">
                                        <div class="label">{{ $t("附加费用") }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.serviceFee"></FormatPrice></div>
                                    </div>
                                    <div class="item" v-if="formState.shippingFee! > 0">
                                        <div class="label">{{ $t("运费") }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.shippingFee"></FormatPrice></div>
                                    </div>
                                    <div class="item" v-if="formState.balance! > 0">
                                        <div class="label">{{ $t("使用余额") }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.balance"></FormatPrice></div>
                                    </div>
                                    <div class="item" v-if="formState.pointsAmount! > 0">
                                        <div class="label">{{ $t(`使用${commonStore.integralName}`) }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.pointsAmount"></FormatPrice></div>
                                    </div>
                                    <div class="item" v-if="formState.discountAmount! > 0">
                                        <div class="label">{{ $t("其他优惠") }}：</div>
                                        <div class="text">
                                            -
                                            <FormatPrice v-model="formState.discountAmount"></FormatPrice>
                                        </div>
                                    </div>
                                    <div class="item" v-if="formState.couponAmount! > 0">
                                        <div class="label">{{ $t("优惠券优惠") }}：</div>
                                        <div class="text">
                                            -
                                            <FormatPrice v-model="formState.couponAmount"></FormatPrice>
                                        </div>
                                    </div>
                                    <div class="item" v-if="formState.unpaidAmount && formState.unpaidAmount > 0">
                                        <div class="label">{{ $t("待支付金额") }}：</div>
                                        <div class="text"><FormatPrice v-model="formState.unpaidAmount"></FormatPrice></div>
                                    </div>
                                    <div class="item">
                                        <span v-if="formState.payStatus == 2">{{ $t("实付款") }}：</span>
                                        <span v-else>{{ $t("应付款") }}：</span>
                                        <FormatPrice v-model="formState.totalAmount" :fontStyle="{ fontSize: '20px', color: 'var(--price)' }"></FormatPrice>
                                    </div>
                                </th>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import type { OrderFormState } from "~/types/user/order.d";
import { cancelOrder, getOrder, confirmReceipt } from "~/api/user/order";
import LogisticsTracking from "~/components/member/order/LogisticsTracking.vue";
import { message, Modal } from "ant-design-vue";
import { useCommonStore } from "~/store/common";

definePageMeta({
    middleware: "auth"
});

const commonStore = useCommonStore();

const { t } = useI18n();
const router = useRouter();
const formState = ref<OrderFormState>({
    orderStatus: 0,
    availableActions: {}
} as OrderFormState);
const loading = ref<boolean>(true);
const showEcard = computed(() => {
    let value = false;
    formState.value.items?.forEach((item) => {
        if (item.eCard && item.eCard.length > 0) {
            value = true;
        }
    });

    return value;
});
const fetchOrder = async () => {
    try {
        const result = await getOrder({ id: router.currentRoute.value.query.id });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
fetchOrder();

const oStatusInfo = ref([
    "您的订单已提交成功，请尽快完成付款哦！",
    "感谢您在本店的购物，您的订单已提交成功，我们会尽快为您发货！",
    "您的订单已经发货，请注意查收，祝您购物愉快！",
    '您的订单已被取消，您可以通过"再次购买"，重新购买订单中的商品。',
    '您的订单被认定为无效订单，订单已被关闭。您可以通过"再次购买"，重新购买订单中的商品。给您造成的不便请多多谅解！',
    "您的订单已经确认收货，感谢您在本店购物，祝您生活愉快！"
]);

const onCancelOrder = async () => {
    Modal.confirm({
        title: t("确认要取消该订单吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                const result = await cancelOrder({ id: formState.value.orderId });
                message.success(t("取消订单成功"));
                await fetchOrder();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};
/**
 * 确认收货
 */
const onConfirmReceipt = async () => {
    Modal.confirm({
        title: t("确认要收货吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                const result = await confirmReceipt({ id: formState.value.orderId });
                await fetchOrder();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

// 订单中是否有已申请售后的商品
const hasAftersalesGoods = (goodsItem: any) => {
    return goodsItem?.some((item: any) => Reflect.has(item, "aftersalesItem") && !item?.aftersalesItem?.aftersaleId);
};
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.extraskudata {
    color: #999;
    .extraskudata-item {
        display: flex;

        .title {
            padding-right: 10px;
        }

        .content-text {
            padding-right: 5px;
        }
        .content {
            .price {
                width: auto;
            }
        }

        .num {
            padding-left: 5px;
        }
    }
}

.sub-sub-title {
    font-size: 14px;
    padding-top: 20px;
    color: #333;
}

.right-info {
    justify-content: space-between;
    align-items: center;
}

.order-info {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .lr {
        display: flex;

        .order-left {
            flex: 1;
            line-height: 3;

            & > div {
                display: flex;
            }
        }

        .order-right {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            width: 200px;
            justify-content: flex-end;
        }
    }

    .sub-title {
        color: #333333;
        font-size: 16px;
        font-weight: 400;
        letter-spacing: 0;
        display: inline-block;
    }

    .order-ul {
        li {
            line-height: 3;
            color: #666;

            span {
                color: #333333;
            }
        }

        &.virtualsample-box {
            .virtualsample-item {
                display: flex;

                .item {
                    padding-right: 10px;

                    &:last-child {
                        padding-right: 0;
                    }
                }
            }
        }
    }

    .order-table-info {
        width: 100%;
        margin: 10px auto;
        background-color: #fcfcfc;
        border-collapse: collapse;
        border: 1px solid #eee;

        .receipt-header {
            line-height: 2;
            border-bottom: 1px solid #eeeeee;

            & > th {
                padding: 10px;
            }
        }

        .receipt-body {
            background-color: white;
            border: 1px solid #eeeeee;

            .receipt-item {
                padding: 10px;
                border: 1px solid #eeeeee;
            }
            .sku-style {
                width: 100%;
                display: flex;
                flex-direction: column;
                gap: 10px;
                & > div {
                    width: 100%;
                }
            }

            & > td:first-child > div {
                & > img {
                    height: 60px;
                    width: 60px;
                }

                display: flex;
                align-items: flex-start;
                gap: 20px;
                flex-wrap: wrap;
            }

            .operation {
                width: 80px;
            }
        }

        .receipt-footer {
            border-bottom: 1px dashed #e8e8e8;

            .receipt-footer-it {
                text-align: right;
                padding: 10px;
                line-height: 2;
                .item {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    line-height: 28px;
                }
                .two {
                    color: #888;
                    font-size: 14px;
                }
            }
        }
    }
}

.virtualsample {
    display: flex;
    width: 100%;
    .virtualsample-value {
        flex: 1;
        overflow: hidden;
        img {
            display: block;
            width: 100%;
            height: auto;
        }
    }
}
</style>
