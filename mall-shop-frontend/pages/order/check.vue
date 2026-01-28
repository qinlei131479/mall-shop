<template>
    <div>
        <CommonHeader title="订单结算页"></CommonHeader>
        <PayHeader :step="2"></PayHeader>

        <div class="container">
            <div class="orderConfirmCon">
                <Address
                    ref="addressRef"
                    v-model="showMoreAddress"
                    :active-address-id="formState.addressId"
                    :addressList="addressList"
                    @addrssRefresh="getAddressListData"
                    @okCallback="onChangeAddress"
                ></Address>

                <template v-if="addressList && addressList.length > 0">
                    <PayType :pay-type-id="formState.payTypeId" :payment-type-list="paymentTypeList" @changePayType="onChangePayType"></PayType>
                </template>

                <div class="form_item">
                    <div class="top_row flex justify-between">
                        <div class="tit">{{ $t("购物清单") }}</div>
                        <NuxtLink v-if="flowType !== 3" class="font-color" to="/cart/"> {{ $t("返回修改购物车") }}</NuxtLink>
                    </div>

                    <template v-if="cartList && cartList.length > 0">
                        <div class="product_detail flex" v-for="(store, index) in cartList" :key="store.shopId">
                            <div class="delivery">
                                <div class="tit">{{ $t("配送方式") }}</div>
                                <template v-if="store.noShipping === 1">
                                    <div class="delivery_tags flex flex-wrap">
                                        <div class="pay_tab flex align-center active">
                                            <span>无需配送</span>
                                            <i class="icon-gou"></i>
                                        </div>
                                    </div>
                                </template>
                                <template v-else-if="shippingTypeData[store.shopId] && shippingTypeData[store.shopId].length > 0">
                                    <div class="delivery_tags flex flex-wrap">
                                        <template v-for="(item, key) in shippingTypeData[store.shopId]" :key="item">
                                            <div
                                                class="pay_tab flex align-center"
                                                :class="{
                                                    active: item.shippingTypeId === formState.shippingType[store.shopId].typeId
                                                }"
                                                @click="onChangeShippingType(store.shopId, item)"
                                            >
                                                <span>{{ item.shippingTypeName }} </span>
                                                <i class="icon-gou"></i>
                                            </div>
                                        </template>
                                    </div>
                                </template>
                                <template v-else>
                                    <div class="no_delivery">
                                        <span class="errInfo red" style="display: inline; margin: 0">{{ $t("该收货地址不支持配送") }}</span>
                                    </div>
                                </template>
                            </div>

                            <div class="detail">
                                <div class="shop_name">{{ store.shopId == 0 ? $t("自营") : `${$t("商家")}：${store.shopTitle}` }}</div>
                                <div class="list flex align-start" v-for="product in store.carts">
                                    <div class="flex align-start">
                                        <div class="pic">
                                            <NuxtLink target="_blank" :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })">
                                                <img :src="imageFormat(product.picThumb)" alt="" />
                                            </NuxtLink>
                                        </div>
                                        <div class="describe flex flex-column">
                                            <div class="name ellipsis" :title="product.productName">
                                                <NuxtLink target="_blank" :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })">
                                                    {{ product.productName }}
                                                </NuxtLink>
                                            </div>
                                            <div class="mt10 sku-name-box">
                                                <div v-for="(sku, skuIndex) in product.skuData" class="sku-name-item">
                                                    <span class="sku-name">{{ sku.name }}：</span>{{ sku.value }}
                                                </div>
                                            </div>
                                            <template v-if="product.extraSkuData && product.extraSkuData.length">
                                                <div class="extraskudata">
                                                    <template v-for="attr in product.extraSkuData" :key="attr">
                                                        <div class="extraskudata-item">
                                                            <div class="title">{{ attr.attrName }}</div>
                                                            <div class="line-box">
                                                                <div class="line"></div>
                                                            </div>
                                                            <div class="content">
                                                                <span class="content-text">{{ attr.attrValue }}</span>
                                                                <FormatPrice v-model="attr.attrPrice" :showText="false"></FormatPrice>
                                                            </div>
                                                        </div>
                                                    </template>
                                                </div>
                                            </template>
                                            <template v-if="product.activityInfo && activityList(product.activityInfo).length > 0">
                                                <div class="activity-box">
                                                    <template v-for="activityItem in activityList(product.activityInfo)" :key="activityItem.promotionId">
                                                        <div class="promotion">
                                                            <div class="ticket-content">
                                                                {{ activityItem.type === 1 ? $t("秒杀价") : $t("限时折扣") }}
                                                            </div>
                                                        </div>
                                                    </template>
                                                </div>
                                            </template>
                                        </div>
                                    </div>
                                    <div class="money">
                                        <FormatPrice :fontStyle="{ fontWeight: '600' }" v-model="product.price" :showText="false"></FormatPrice>
                                    </div>
                                    <div class="quantity">x{{ product.quantity }}</div>
                                </div>
                                <template v-if="store.gift && store.gift.length > 0">
                                    <div class="gift-box">
                                        <div class="gift-title">{{ $t("赠品") }}</div>
                                        <div class="gift-content">
                                            <template v-for="subItem in store.gift" :key="subItem.giftId">
                                                <NuxtLink
                                                    target="_blank"
                                                    :to="
                                                        urlFormat({
                                                            path: 'product',
                                                            id: subItem.productId,
                                                            sn: subItem.productSn
                                                        })
                                                    "
                                                >
                                                    <div class="gift-content-item">
                                                        <div class="gift-item-left">
                                                            <el-image class="gift-img" lazy :src="imageFormat(subItem.picThumb)"> </el-image>
                                                        </div>
                                                        <div class="gift-item-right">
                                                            <div class="gift-item-name">{{ subItem.productName }}</div>
                                                            <template v-if="subItem.skuData && subItem.skuData.length > 0">
                                                                <div class="sku-card">
                                                                    <template v-for="(skuItem, skuIndex) in subItem.skuData" :key="skuIndex">
                                                                        <div class="sku-item">{{ skuItem.value }}</div>
                                                                    </template>
                                                                </div>
                                                            </template>
                                                            <div class="gift-item-num">x{{ subItem.num }}</div>
                                                        </div>
                                                    </div>
                                                </NuxtLink>
                                            </template>
                                        </div>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </template>

                    <div class="add_remark">
                        <div class="a_rmk_tit">{{ $t("订单备注") }}：</div>
                        <div class="a_rmk_con">
                            <el-input
                                v-model="formState.buyerNote"
                                type="textarea"
                                :placeholder="$t('提示：请勿填写有关支付、收货、发票方面的信息')"
                                style="width: 400px; font-size: 12px"
                                show-word-limit
                                :maxlength="50"
                            />
                        </div>
                    </div>
                </div>

                <template v-if="titleText">
                    <div class="form_item">
                        <div class="top_row">
                            <div class="tit">{{ $t(titleText) }}</div>
                        </div>
                        <client-only>
                            <template v-if="commonStore.useCoupon != 0">
                                <CouponInfo
                                    v-model:useCoupon="useCoupon"
                                    v-model:couponList="couponList"
                                    v-model:selectedIds="formState.useCouponIds"
                                    v-model:selectedUserCouponIds="formState.selectUserCouponIds"
                                    @change="onCouponChange"
                                    v-model:couponLoading="couponLoading"
                                ></CouponInfo>
                            </template>

                            <template v-if="commonStore.usePoints != 0 && flowType === 1">
                                <div class="from">
                                    <div class="check-box flex align-center">
                                        <el-checkbox :checked="formState.usePoint > 0" @click="onUsePoint">
                                            <span class="tit">{{ $t(`使用${commonStore.integralName}`) }}</span></el-checkbox
                                        >
                                        <div style="margin-left: 10px">
                                            {{ $t("当前账户共") }} <span class="font-color"> {{ userStore.userInfo.points }} </span> {{ $t(commonStore.integralName) }}
                                        </div>
                                    </div>
                                </div>
                            </template>

                            <div class="from_list" v-if="showUsePoint">
                                <div class="arrow"></div>
                                <div class="integral flex">
                                    <div class="tit">{{ $t(`使用${commonStore.integralName}`) }}：</div>
                                    <div class="inp_box">
                                        <div class="inp">
                                            <el-input :max="availablePoints" v-model="inputPoint" @change="usePointChange" />
                                            <span class="points_amount" v-if="total.pointsAmount > 0">
                                                &emsp;{{ $t("共抵扣") }}
                                                <FormatPrice class="red" v-model="total.pointsAmount" :showText="false" />
                                            </span>
                                        </div>
                                        <div class="tips">
                                            {{ $t("该订单最多可用") }}{{ availablePoints }} {{ $t(commonStore.integralName) }}
                                            <a href="/article/issue/info?articleId=7" target="_blank"
                                                ><span class="font-color">【{{ $t(`如何获得${commonStore.integralName}`) }}？】</span></a
                                            >
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <template v-if="commonStore.useSurplus != 0 && userStore.userInfo.balance > 0">
                                <div class="from">
                                    <div class="check-box flex align-center">
                                        <el-checkbox :checked="formState.useBalance > 0" @click="onUseBalance">
                                            <span class="tit">{{ $t("使用余额") }}</span></el-checkbox
                                        >
                                        <div style="margin-left: 10px">
                                            {{ $t("当前账户余额") }} <span class="font-color">{{ userStore.userInfo.balance }} </span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </client-only>
                    </div>
                </template>

                <template v-if="commonStore.canInvoice == 1 && !isOverseas()">
                    <div class="form_item">
                        <div class="top_row flex align-end">
                            <div class="tit">{{ $t("发票信息") }}</div>
                            <div class="tips">{{ $t("开企业抬头发票须填写纳税人识别号，以免影响报销") }}</div>
                        </div>
                        <div class="from">
                            <div class="flex align-center invoice-box">
                                <div class="title flex align-center">
                                    <span>{{
                                        !formState.invoiceData.invoiceType
                                            ? $t("不开发票")
                                            : formState.invoiceData.invoiceType == 1
                                              ? $t("电子普通发票")
                                              : $t("增值税专用发票")
                                    }}</span>

                                    <template v-if="formState.invoiceData.invoiceType == 1">
                                        <el-tooltip
                                            :content="$t('电子普通发票与纸质发票具有同等法律效力，可支持报销入账、商品售后凭证。')"
                                            placement="bottom-start"
                                            effect="light"
                                        >
                                            <i class="iconfont-pc icon-tishi"></i>
                                        </el-tooltip>
                                    </template>
                                </div>
                                <div v-if="formState.invoiceData.invoiceType" class="type">
                                    <span v-if="formState.invoiceData.invoiceType == 1">{{
                                        formState.invoiceData.titleType == 1 ? $t("个人") : $t("企业")
                                    }}</span>
                                    <span v-if="formState.invoiceData.invoiceType == 2">{{ $t("增值税专用发票") }}</span>
                                </div>
                                <div class="type">
                                    <span>{{ formState.invoiceData.companyName ?? "" }}</span>
                                </div>
                                <Info
                                    :params="{
                                        invoiceData: formState.invoiceData,
                                        amount: total?.unpaidAmount,
                                        type: 'check',
                                        address: addressList.find((item: any) => formState.addressId == item.addressId)
                                    }"
                                    @callback="getInvoiceData"
                                    @changeType=""
                                >
                                    <div class="btn hand-pointer">{{ $t("修改") }}</div>
                                </Info>
                            </div>
                        </div>
                    </div>
                </template>

                <div class="money_info">
                    <ClientOnly>
                        <div class="total_list">
                            <div class="item">
                                {{ $t("商品总价") }}：
                                <FormatPrice v-model="total.productAmount" :showText="false" />
                            </div>
                            <div class="item" v-if="total.serviceFee && total.serviceFee > 0">
                                {{ $t("附加费用") }}：
                                <FormatPrice v-model="total.serviceFee" :showText="false" />
                            </div>
                            <div class="item">
                                <div class="tip-item">
                                    <i class="freight-icon"></i>
                                    <div class="ui-tips">
                                        <span class="ui-tips-arrow"></span>
                                        <div class="ui-tips-main">
                                            <div class="tit">{{ $t("运费明细") }}</div>
                                            <div class="goods_list">
                                                <div class="goods_col" v-for="(store, index) in cartList">
                                                    <div class="shop_name">{{ store.shopId == 0 ? $t("自营") : store.shopTitle }}</div>
                                                    <div class="list">
                                                        <div class="txt">
                                                            {{ $t("运费") }}：<FormatPrice :showText="false" v-model="total.storeShippingFee[store.shopId]" />
                                                        </div>
                                                        <div class="item" style="padding: 10px 0">
                                                            <swiper
                                                                v-model:swiperOption="swiperOption"
                                                                :itemList="store.carts"
                                                                style="width: 100%; padding: 0 30px"
                                                            >
                                                                <template v-slot:default="{ item }">
                                                                    <div class="img_box" style="width: 65px">
                                                                        <img :src="imageFormat(item.picThumb)" alt="" />
                                                                    </div>
                                                                </template>
                                                            </swiper>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                + {{ $t("配送费用") }}：
                                <FormatPrice :showText="false" v-model="total.shippingFee" />
                            </div>
                            <div class="item" v-if="total.balance > 0" style="display: flex; align-items: center">
                                <span style="display: inline-flex; align-items: center">-</span>
                                <span style="margin-left: 4px">{{ $t("使用余额") }}：</span>
                                <FormatPrice v-model="total.balance" :showText="false" />
                            </div>
                            <div class="item" v-if="total.pointsAmount > 0 && flowType != 3" style="display: flex; align-items: center">
                                <span style="display: inline-flex; align-items: center">-</span>
                                <span style="margin-left: 4px">{{ $t(`${commonStore.integralName}减免`) }}：</span>
                                <FormatPrice v-model="total.pointsAmount" :showText="false" />
                            </div>
                            <div class="item" v-if="total.discounts > 0">
                                <CommonPopover trigger="hover" v-model="showPopover">
                                    <template #reference>
                                        <div class="tip-item">
                                            <i class="freight-icon"></i>
                                        </div>
                                    </template>
                                    <template #default v-if="showPopover">
                                        <div class="discount">
                                            <div class="discount-title">
                                                <div class="title-text">{{ $t("优惠明细") }}</div>
                                            </div>
                                            <div class="discount-content">
                                                <div class="discount-item-box" v-if="total?.productAmount > 0">
                                                    <div class="item-label">{{ $t("商品总额") }}</div>
                                                    <div class="item-value">
                                                        <format-price v-model="total.productAmount" :fontStyle="{ fontWeight: 'bold' }"></format-price>
                                                    </div>
                                                </div>
                                                <div class="discount-item-box hassub">
                                                    <div class="flex justify-between" v-if="total?.discounts > 0" style="align-items: center">
                                                        <div class="item-label">{{ $t("共优惠") }}</div>
                                                        <div
                                                            class="item-value discounts-value"
                                                            style="color: var(--general); display: flex; align-items: center"
                                                        >
                                                            <span style="display: inline-flex; align-items: center">-</span>
                                                            <format-price v-model="total.discounts"></format-price>
                                                        </div>
                                                    </div>
                                                    <div class="sub-item-box" v-if="total?.discountCouponAmount > 0" style="align-items: center">
                                                        <div class="sub-item-label">{{ $t("优惠券") }}</div>
                                                        <div class="sub-item-value discounts-value" style="display: flex; align-items: center">
                                                            <span style="display: inline-flex; align-items: center">-</span>
                                                            <format-price v-model="total.discountCouponAmount"></format-price>
                                                        </div>
                                                    </div>
                                                    <div class="sub-item-box" v-if="total.discountDiscountAmount > 0" style="align-items: center">
                                                        <div class="sub-item-label">{{ $t("其他优惠") }}</div>
                                                        <div class="sub-item-value discounts-value" style="display: flex; align-items: center">
                                                            <span style="display: inline-flex; align-items: center">-</span>
                                                            <format-price v-model="total.discountDiscountAmount"></format-price>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="discount-item-box" v-if="total.discountAfter && total.discountAfter > 0">
                                                    <div class="item-label">{{ $t("合计") }}</div>
                                                    <div class="item-value">
                                                        <format-price v-model="total.discountAfter" :fontStyle="{ fontWeight: 'bold' }"></format-price>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </CommonPopover>

                                - {{ $t("共优惠") }}：
                                <FormatPrice :showText="false" v-model="total.discounts" />
                            </div>
                        </div>
                    </ClientOnly>
                    <div v-if="total.orderSendPoint && total.orderSendPoint > 0" class="complete">
                        *{{
                            isOverseas()
                                ? $t(`该订单完成后，您将预计获得{0}${commonStore.integralName}`, [total.orderSendPoint])
                                : `该订单完成后，您将预计获得${total.orderSendPoint}${commonStore.integralName}`
                        }}
                    </div>
                    <div class="total_fee flex justify-end">
                        <div class="unpaidamount-label">{{ $t("应付款金额") }}：</div>
                        <template v-if="flowType == 3">
                            <div class="points-box">
                                <div class="points-value">
                                    {{ total?.exchangePoints }}
                                    <div class="points-text">{{ $t(commonStore.integralName) }}</div>
                                </div>
                                <div class="symbol">+</div>
                            </div> </template
                        ><FormatPrice class="price" v-model="total.unpaidAmount" :showText="false"></FormatPrice>
                    </div>
                    <div class="submit flex justify-end">
                        <el-button type="primary" class="btn" :disabled="submitDisabled" :loading="submitLoading" @click="!submitLoading && onSubmit()">{{
                            $t("提交订单")
                        }}</el-button>
                    </div>
                </div>
            </div>
        </div>

        <ModalRegion v-if="editAddressShow" v-model="editAddressShow" :addressId="editAddressId"></ModalRegion>
        <CommonPageFooter></CommonPageFooter>
    </div>
</template>
<script lang="ts" setup>
import { reactive, ref, computed } from "vue";
import PayHeader from "@/pages/order/src/PayHeader.vue";
import CouponInfo from "./src/CouponInfo.vue";
import { ModalRegion } from "@/components/region";
import { getOrderCheck, updateOrderCheck, updateCouponData, orderSubmit, getCheckInvoice, getPaymentType, getShippingType } from "@/api/order/check";
import type { PaymentTypeItem, ShippingTypeItem, CheckCartList, CheckTotal, CheckCouponList } from "~/types/order/check.d";
import { urlFormat, imageFormat } from "@/utils/format";
import { message } from "ant-design-vue";
import { ElLoading } from "element-plus";
import Info from "~/pages/member/orderInvoice/Info.vue";
import Address from "@/pages/order/src/Address.vue";
import PayType from "@/pages/order/src/PayType.vue";
import { useCommonStore } from "~/store/common";
import { useUserStore } from "~/store/user";
import { isOverseas } from "~/utils/util";
import { getAddressList, setSelectedAddress } from "@/api/user/address";
import type { AddressFilterState } from "~/types/user/address.d";

definePageMeta({
    middleware: "auth"
});

const { t } = useI18n();

const commonStore = useCommonStore();
const userStore = useUserStore();

const route = useRoute();

const flowType = computed(() => {
    return route.query.flowType ? Number(route.query.flowType) : 1;
});

// 当前结算表单数据
interface IformState {
    addressId: number;
    shippingType: {
        [key: string]: {
            typeId: number;
            shopId: number;
            typeName: string;
        };
    };
    payTypeId: number;
    usePoint: number;
    useBalance: number;
    useCouponIds: number[];
    selectUserCouponIds: number[];
    buyerNote: string;
    invoiceData: any;
    useDefaultCouponIds?: number;
}

const formState = reactive<IformState>({
    addressId: 0,
    shippingType: {},
    payTypeId: 0,
    usePoint: 0,
    useBalance: 0,
    useCouponIds: [],
    selectUserCouponIds: [],
    buyerNote: "",
    invoiceData: {}
});

const addressList = ref<AddressFilterState[]>([]);
const addressRef = useTemplateRef("addressRef");
const getAddressListData = async () => {
    return new Promise((resolve, reject) => {
        getAddressList({ page: 1, size: 99 })
            .then((res) => {
                if (res.records.length == 0) {
                    addressRef.value?.show();
                    reject("没有地址");
                }
                formState.addressId = res.records[0].addressId;
                addressList.value = res.records;
                resolve(void 0);
            })
            .catch((e) => {
                console.log(e);
                reject(e);
            });
    });
};
const setSelectedAddressData = async (addressId: number) => {
    try {
        await setSelectedAddress(addressId);
    } catch (e) {
        console.log(e);
    }
};

const paymentTypeList = ref<PaymentTypeItem[]>([]);
const getPaymentTypeData = async () => {
    try {
        const res = await getPaymentType();
        formState.payTypeId = res[0].typeId;
        paymentTypeList.value = res;
    } catch (e) {
        console.log(e);
    }
};

const shippingTypeData = ref<{ [key: string]: ShippingTypeItem[] }>({});
const getShippingTypeData = async () => {
    try {
        const res = await getShippingType({ flowType: flowType.value });
        shippingTypeData.value = res;
        for (const key in res) {
            if (res[key] && res[key].length > 0) {
                formState.shippingType[key] = {
                    typeId: res[key][0].shippingTypeId,
                    shopId: res[key][0].shopId,
                    typeName: res[key][0].shippingTypeName
                };
            }
        }
        console.log("getShippingTypeData", formState.shippingType);
    } catch (e) {
        console.log(e);
    }
};

// 修改配送方式类型
const onChangeShippingType = async (shopId: number, data: ShippingTypeItem) => {
    const { shippingTypeId, shippingTypeName } = data;

    if (formState.shippingType[shopId] && formState.shippingType[shopId].typeId == shippingTypeId) return;

    formState.shippingType[shopId].typeId = shippingTypeId;
    formState.shippingType[shopId].shopId = shopId;
    formState.shippingType[shopId].typeName = shippingTypeName;

    await updateOrderCheckData();
};

const loading = ref(true);
const checkLoading = ref(true);
const useCoupon = ref(false);
const editAddressId = ref(0);
const showMoreAddress = ref(false);

const titleText = computed(() => {
    const arr = [commonStore.useCoupon == 1 && "优惠券", commonStore.usePoints == 1 && commonStore.integralName, commonStore.useSurplus == 1 && "余额"].filter(Boolean);
    return arr.length ? `使用${arr.join("/")}` : "";
});

const cartList = ref<CheckCartList[]>([]);
const total = ref<CheckTotal>({} as CheckTotal);
const couponList = ref<CheckCouponList>({ disableCoupons: [], enableCoupons: [] });
const availablePoints = ref(0);
const getOrderCheckData = async () => {
    checkLoading.value = true;
    try {
        const result = await getOrderCheck({ flowType: flowType.value, ...formState });
        Object.assign(formState, result.item);
        cartList.value = result.cartList;
        total.value = result.total;
        couponList.value = result.couponList;
        availablePoints.value = result.availablePoints;

        if (result.useCouponIds && result.useCouponIds?.length > 0) {
            useCoupon.value = true;
            formState.useCouponIds = result.useCouponIds;
            formState.selectUserCouponIds = result.selectUserCouponIds!;
        }
        loading.value = false;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        checkLoading.value = false;
    }
};

const LoadingBox = ref();
const initPageData = async () => {
    try {
        LoadingBox.value = ElLoading.service({
            lock: false,
            text: t("请求加载中"),
            customClass: "tig-normal-loading"
        });
        loading.value = true;
        await getAddressListData();
        await Promise.all([getPaymentTypeData(), getShippingTypeData(), userStore.getUserInfo()]);
        await getOrderCheckData();
    } catch (error: any) {
        console.log(error);
    } finally {
        loading.value = false;
        LoadingBox.value?.close();
    }
};

initPageData();

const fetcCheckInvoice = async () => {
    try {
        const result = await getCheckInvoice({ ...formState.invoiceData });
        if (result) {
            delete result.id;
            delete result.userId;
            delete result.orderId;
            delete result.status;
            delete result.applyReply;
            delete result.invoiceNo;
            delete result.addTime;
            if (result.titleType == 1) {
                result.companyUserName = result.companyName;
            }
            if (result.titleType == 2) {
                result.companyEnterpriseName = result.companyName;
            }
            formState.invoiceData = result;
        } else {
            let address = addressList.value.find((item: any) => formState.addressId == item.addressId);
            if (address) {
                let temp: any = {
                    companyUserName: address.consignee,
                    mobile: address.mobile,
                    email: address.email,
                    amount: total.value.unpaidAmount
                };
                Object.assign(formState.invoiceData, temp);
            }
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

const updateOrderCheckData = async () => {
    try {
        const result = await updateOrderCheck({ flowType: flowType.value, ...formState });
        Object.assign(formState, result.item);
        cartList.value = result.cartList;
        total.value = result.total;
        availablePoints.value = result.availablePoints;
        return result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        checkLoading.value = false;
    }
};

const swiperOption = ref({
    autoplay: false,
    navigation: true,
    pagination: false,
    effect: "slide",
    loop: false,
    slidesPerView: 4, // 一屏显示的slide个数  'auto'
    slidesPerGroup: 1 //每组多少个swiper滑块
});

// 修改收货人地址id
const onChangeAddress = async (e: any) => {
    try {
        checkLoading.value = true;
        formState.addressId = e.addressId;
        await setSelectedAddressData(e.addressId);
        showMoreAddress.value = false;
        await getAddressListData();
        await getShippingTypeData();
        await getPaymentTypeData(), await updateOrderCheckData();
        fetcCheckInvoice();
    } catch (error) {
    } finally {
    }
};
// 修改支付方式
const onChangePayType = (typeId: any) => {
    if (formState.payTypeId === typeId) return;
    formState.payTypeId = typeId;
    updateOrderCheckData();
};

const inputPoint = ref(0);
const showUsePoint = ref(false);
const getInvoiceData = (data: any) => {
    console.log("getInvoiceData", data);
    formState.invoiceData = data;
};
const onUsePoint = () => {
    if (showUsePoint.value === true) {
        showUsePoint.value = false;
        formState.usePoint = 0;
        inputPoint.value = 0;
    } else {
        showUsePoint.value = true;
    }
    updateOrderCheckData();
};
const usePointChange = () => {
    inputPoint.value = Math.floor(Number(inputPoint.value));
    if (inputPoint.value > availablePoints.value) {
        inputPoint.value = availablePoints.value;
    } else if (inputPoint.value < 0 || !inputPoint.value) {
        inputPoint.value = 0;
    }
    formState.usePoint = inputPoint.value;
    updateOrderCheckData();
};

const onUseBalance = () => {
    formState.useBalance = formState.useBalance == 0 ? userStore.userInfo.balance : 0;
    updateOrderCheckData();
};

const onCouponChange = (value: boolean = false) => {
    updateCoupon(value);
};
const couponLoading = ref(false);
const updateCoupon = async (value: boolean = false) => {
    try {
        couponLoading.value = true;
        formState.useDefaultCouponIds = value ? 1 : 0;
        const result = await updateCouponData(formState);
        formState.useCouponIds = result.useCouponIds ?? [];
        formState.selectUserCouponIds = result.selectUserCouponIds ?? [];
        couponList.value = result.couponList;
        total.value = result.total;
        cartList.value = result.cartList;
        availablePoints.value = result.availablePoints;
        return result;
    } catch (error) {
    } finally {
        couponLoading.value = false;
    }
};
const submitLoading = ref(false);
const onSubmit = async () => {
    try {
        submitLoading.value = true;
        const result = await orderSubmit(formState);
        if (result.returnType == 2) {
            navigateTo(`/member/order/list`, { replace: true });
            userStore.userInfoLoaded = false;
            await userStore.getUserInfo(); // 刷新余额
            return;
        }
        navigateTo(`/order/pay?id=${result.orderId}`, { replace: true });
        return result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        submitLoading.value = false;
    }
};

const editAddressShow = ref<boolean>(false);

const submitDisabled = computed(() => {
    // 1. 检查收货地址
    if (formState.addressId === 0) {
        return true;
    }

    // 2. 检查支付方式
    if (formState.payTypeId === 0) {
        return true;
    }

    if (!formState.shippingType) {
        return true;
    }

    for (const item of cartList.value) {
        if (item.noShipping === 0 && !formState.shippingType[item.shopId]) {
            return true;
        }
    }

    return false;
});

const activityList = (data: any): any[] => {
    return data.filter((item: any) => item.type === 1 || item.type === 6);
};

const showPopover = ref(false);
</script>
<style lang="less">
@import "./css/check.less";
</style>
<style lang="less" scoped>
body {
    background-color: #fff;
}
.extraskudata {
    margin-top: 5px;
    color: #999;
    .extraskudata-item {
        display: flex;

        .title {
            padding-right: 10px;
        }

        .content-text {
            padding-right: 5px;
        }
        .num {
            padding-left: 5px;
        }
    }
}
.discount-title {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
}

.discount-content {
    font-size: 13px;
    .discount-item-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 0;

        .discounts-value {
            font-weight: bold;
        }

        .item-value {
            font-size: 14px;
        }
        &.hassub {
            display: block;
        }
        .sub-item-box {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0 7px;
            color: #969799;
            font-size: 12px;
        }

        &:last-child {
            padding-top: 0;
        }
    }

    .discount-desc {
        font-size: 12px;
        color: #969799;
        letter-spacing: 0;
    }
}

.sku-name-box {
    display: flex;

    .sku-name-item {
        max-width: 110px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding-left: 6px;

        &:first-child {
            padding-left: 0;
        }
    }
}

.activity-box {
    display: flex;
    padding-top: 10px;
}

.promotion {
    display: flex;
    background-color: var(--tag-bg);
    color: var(--tag-text);
    border-radius: 2px;
    padding-bottom: 2px;
    .ticket-content {
        padding: 0 4px;
        max-width: 90px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.gift-box {
    font-size: 14px;
    position: relative;
    padding-top: 8px;
    margin-top: 5px;

    &::after {
        content: " ";
        position: absolute;
        pointer-events: none;
        box-sizing: border-box;
        -webkit-transform-origin: center;
        transform-origin: center;
        top: 0;
        left: 0;
        width: 100%;
        bottom: auto;
        -webkit-transform: scaleY(0.5);
        transform: scaleY(0.5);
        border-top: 1px solid #ebedf0;
    }

    .gift-content {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        grid-gap: 30px;
        padding: 10px 0;
    }

    .gift-content-item {
        display: flex;
        font-size: 12px;
        padding: 10px;
        padding-bottom: 8px;
        position: relative;
        background-color: var(--tag-bg);
        border-radius: 10px;

        .gift-item-left {
            width: 60px;
            height: 60px;
            border-radius: 4px;
            overflow: hidden;
            margin-right: 10px;
        }

        .gift-item-right {
            width: calc(100% - 60px);
            .gift-item-name {
                width: 100%;
                word-break: break-all;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                color: var(--tag-text);
            }
        }
        .gift-item-num {
            font-size: 12px;
            color: #969799;
            position: absolute;
            bottom: 8px;
            right: 10px;
        }
    }

    .sku-card {
        display: inline-block;
        color: #969799;
        margin: 5px 0;

        .sku-item {
            display: inline-block;
            max-width: 130rpx;
        }

        .sku-item::before {
            content: ",";
            padding-left: 5rpx;
        }

        .sku-item:first-child::before {
            content: "";
        }
    }
}

.unpaidamount-label {
    height: inherit;
    display: flex;
    align-items: flex-end;
}
.points-box {
    display: flex;
    font-size: 26px;
    column-gap: 10px;
    color: var(--general);
    padding-right: 10px;
    height: 100%;
    .points-value {
        display: flex;
        align-items: flex-end;
        font-size: 30px;
        column-gap: 2px;
    }

    .points-text {
        font-size: 18px;
        position: relative;
        bottom: -4px;
    }
}

@import "./css/check.less";
</style>
