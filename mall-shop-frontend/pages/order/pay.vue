<template>
    <CommonHeader title="支付订单"></CommonHeader>
    <PayHeader :step="2"></PayHeader>
    <div class="container" v-if="!loading">
        <div class="success_txt">{{ $t("您的订单已成功提交，请付款") }}！</div>
        <div class="price_box">
            <div class="num_box">
                <div class="tit">{{ $t("需支付") }}：</div>
                <FormatPrice class="price" v-model="order.unpaidAmount" :showText="false"></FormatPrice>
            </div>
            <div class="order_num">
                <div class="tit">{{ $t("订单号") }}：</div>
                <div class="num hand-pointer">
                    <NuxtLink :to="'/member/order/info?id=' + order.orderId" target="_blank">{{ order.orderSn }}</NuxtLink>
                </div>
            </div>
        </div>
        <div class="pay_type_box" v-if="order.payTypeId < 3">
            <div class="tit_row">
                <h3>{{ $t("平台支付") }}</h3>
                <p>{{ $t("支持所有银行卡或信用卡，更迅速、安全") }}</p>
            </div>
            <div class="type_tab">
                <el-radio-group v-model="payType">
                    <el-radio class="tab flex align-center" value="wechat" v-if="payment.includes('wechat')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_wechat.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="alipay" v-if="payment.includes('alipay')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_alipay.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="paypal" v-if="payment.includes('paypal')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_paypal.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="yabanpayWechat" v-if="payment.includes('yabanpayWechat')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_wechat.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="yabanpayAlipay" v-if="payment.includes('yabanpayAlipay')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_alipay.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="yunpayWechat" v-if="payment.includes('yunpayWechat')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_wechat.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="yunpayAlipay" v-if="payment.includes('yunpayAlipay')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_alipay.jpg" /></div>
                    </el-radio>
                    <el-radio class="tab flex align-center" value="yunpayYunshanfu" v-if="payment.includes('yunpayYunshanfu')">
                        <div class="cont"><img src="/assets/images/extra/payment_method_yunpay_yunshanfu.jpg" /></div>
                    </el-radio>
                </el-radio-group>
            </div>
        </div>
        <div class="pay_type_box" v-else>
            <el-tabs type="border-card" v-model="activeName">
                <el-tab-pane :label="$t('银行汇款')" :name="1">
                    <div v-html="offlinePaymentList.offlinePayBank"></div>
                </el-tab-pane>
                <el-tab-pane :label="$t('企业汇款')" :name="2">
                    <div v-html="offlinePaymentList.offlinePayCompany"></div>
                </el-tab-pane>
            </el-tabs>
        </div>
        <div class="btn_box" v-if="order.payTypeId < 3">
            <NuxtLink :to="payLink" target="_blank">
                <div class="btn">
                    <span>{{ $t("去付款") }}</span>
                    <i class="iconfont-pc icon-yuanjiaojiantouyou"></i>
                </div>
            </NuxtLink>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref, shallowRef, onMounted, onUnmounted } from "vue";
import { orderPayInfo } from "~/api/order/pay";
import PayHeader from "./src/PayHeader.vue";
const activeName = ref<number>(1);
const route = useRoute();
const id = ref(route.query.id);
const payType = ref("wechat");
const loading = ref(false);
const order = ref({});
const payment = ref<string[]>([]);
const offlinePaymentList = ref<any>({});
const checkType = (item: any) => {
    payType.value = item.type;
};
const loadOrderPayInfo = async () => {
    loading.value = true;
    try {
        const result = await orderPayInfo({
            id: id.value
        });
        order.value = result.order;
        payment.value = result.paymentList;
        offlinePaymentList.value = result.offlinePaymentList;
        loading.value = false;
    } catch (error: any) {
        message.error(error.message);
        navigateTo("/member/order/list/");
    } finally {
    }
};
loadOrderPayInfo();
const payLink = computed(() => {
    switch (payType.value) {
        case "wechat":
            return "/order/wechat/?id=" + id.value;
        case "alipay":
            return "/order/alipay/?id=" + id.value;
        case "paypal":
            return "/order/paypal/?id=" + id.value;
        case "yabanpayWechat":
            return "/order/yabanpay/?type=wechat&id=" + id.value;
        case "yabanpayAlipay":
            return "/order/yabanpay/?type=alipay&id=" + id.value;
        case "yunpayWechat":
            return "/order/yunpay/?type=wechat&id=" + id.value;
        case "yunpayAlipay":
            return "/order/yunpay/?type=alipay&id=" + id.value;
        case "yunpayYunshanfu":
            return "/order/yunpay/?type=yunshanfu&id=" + id.value;
        default:
            return "";
    }
});
</script>
<style lang="less" scoped>
.container {
    :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
        color: #333;
    }
    :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item:not(.is-disabled):hover) {
        color: #333;
    }
    .success_txt {
        color: #239700;
        font-size: 14px;
        margin-bottom: 15px;
    }

    .price_box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
        .num_box {
            display: flex;
            align-items: flex-end;
            .tit {
                color: #333;
                font-size: 18px;
                font-weight: bold;
                line-height: 26px;
            }
            :deep(.price) {
                vertical-align: bottom;
                .util {
                    color: var(--price);
                    font-size: 16px;
                    line-height: 28px;
                }
                .num,
                .decimals {
                    color: var(--price);
                    font-size: 28px;
                }
            }
        }
        .order_num {
            display: flex;
            align-items: center;
            a {
                color: var(--general);
            }
        }
    }
    .pay_type_box {
        border: 1px solid #ccc;
        background: #fff;
        border-radius: 3px;
        padding: 30px 30px;
        .tit_row {
            display: flex;
            align-items: flex-end;
            h3 {
                color: #333;
                font-size: 18px;
                font-weight: 400;
                margin-right: 10px;
            }
            p {
                color: #999;
            }
        }
        .type_tab {
            display: flex;
            .tab {
                border: 1px solid #d9d9d9;
                cursor: pointer;
                height: 39px;
                padding: 4px 5px 4px 15px;
                background: #fff;
                overflow: hidden;
                width: 146px;
                margin-right: 20px;
                margin-top: 25px;
                .cont {
                    height: 35px;
                    img {
                        height: 35px;
                    }
                }
                .check {
                    width: 10px;
                    height: 10px;
                    border: 1px solid #333;
                    border-radius: 100px;
                    margin-right: 5px;
                    text-align: center;
                    line-height: 10px;
                    span {
                        display: inline-block;
                        width: 7px;
                        height: 7px;
                        border-radius: 100px;
                    }
                }
            }
            .active {
                border: 1px solid #ed3714;
                .check {
                    border: 1px solid #0075ff;
                    span {
                        background-color: #0075ff;
                    }
                }
            }
        }
    }
    .btn_box {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        margin-top: 20px;
        .btn {
            display: flex;
            align-items: center;
            background-color: var(--general);
            border: 1px solid var(--general);
            color: var(--main-text);
            cursor: pointer;
            border-radius: 2px;
            height: 16px;
            line-height: 16px;
            text-align: center;
            margin-right: 20px;
            width: 80px;
            padding: 10px 12px;
            font-size: 16px;
            span {
                display: inline-block;
                margin-left: 10px;
            }
            i {
                font-size: 12px;
                margin-left: 5px;
            }
        }
        .tips {
            display: flex;
            margin-right: 16px;
            margin-top: 10px;
            cursor: pointer;
            p {
                margin-left: 5px;
                span {
                    color: #b2002e;
                    margin-left: 5px;
                }
            }
        }
    }
}
.cont {
    width: 126px;
    & > img {
        width: 100%;
    }
}
</style>
