<template>
    <div>
        <div class="from">
            <div class="check-box">
                <el-checkbox v-model="useCoupon" @change="onUseCoupon">
                    <div class="tit">{{ $t("使用优惠券") }}</div>
                </el-checkbox>
            </div>
        </div>
        <div class="from_list" v-if="useCoupon">
            <div class="arrow"></div>
            <div class="coupon-box">
                <div class="tab_list flex" v-if="false">
                    <div class="tab" v-for="(item, index) in tabList" @click="current = index" :class="current == index ? 'current' : ''">{{ $t("item") }}</div>
                </div>
                <div v-loading="couponLoading">
                    <div class="coupon_list" v-if="current == 0">
                        <template v-if="couponList && (couponList?.enableCoupons.length > 0 || couponList?.disableCoupons.length > 0)">
                            <template v-if="couponList?.enableCoupons.length > 0">
                                <div
                                    class="coupon_item hand-pointer"
                                    v-for="(coupon, index) in couponList.enableCoupons"
                                    type="checkbox"
                                    :class="{ active: coupon.selected }"
                                    @click="toggleCouponSelection(coupon)"
                                >
                                    <div class="c-top-dong"></div>
                                    <div class="item-selected-cancel">{{ $t("取消勾选") }}</div>
                                    <div class="priceNum" v-if="coupon.couponType == 1">
                                        <FormatPrice v-model="coupon.couponMoney"></FormatPrice>
                                        <span v-if="coupon.isGlobal" class="coupon-globle">[{{ $t("全场券") }}]</span>
                                    </div>
                                    <div class="zk" v-if="coupon.couponType == 2">
                                        {{ coupon.couponDiscount }} <span>{{ $t("折") }}</span>
                                        <span v-if="coupon.isGlobal" class="coupon-globle">[{{ $t("全场券") }}]</span>
                                    </div>
                                    <div class="time">{{ $t("有效期至") }}{{ coupon.endDate }}</div>
                                    <div class="detail">
                                        {{ coupon.couponName }}
                                    </div>
                                    <i class="icon-gou"></i>
                                    <div v-if="coupon.disableReason" class="coupon-notice">
                                        <i class="iconfont-pc icon-tishi"></i>{{ coupon.disableReason }}
                                    </div>
                                    <div v-if="coupon.selected && coupon.couponType == 1" class="coupon-notice">
                                        <i class="iconfont-pc icon-tishi"></i> {{ $t("已减") }}<FormatPrice v-model="coupon.couponMoney"></FormatPrice>
                                    </div>
                                </div>
                            </template>
                            <template v-if="couponList && couponList?.disableCoupons.length > 0">
                                <div
                                    class="coupon_item hand-pointer disabled"
                                    v-for="coupon in couponList.disableCoupons"
                                    type="checkbox"
                                    :class="{ active: coupon.selected }"
                                >
                                    <div class="c-top-dong"></div>
                                    <div class="item-selected-cancel">{{ $t("取消勾选") }}</div>
                                    <div class="priceNum" v-if="coupon.couponType == 1">
                                        <FormatPrice v-model="coupon.couponMoney"></FormatPrice>
                                        <span v-if="coupon.isGlobal" class="coupon-globle">[{{ $t("全场券") }}]</span>
                                    </div>
                                    <div class="zk" v-if="coupon.couponType == 2">
                                        {{ coupon.couponDiscount }} <span>{{ $t("折") }}</span>
                                        <span v-if="coupon.isGlobal" class="coupon-globle">[{{ $t("全场券") }}]</span>
                                    </div>
                                    <div class="time">{{ $t("有效期至") }}{{ coupon.endDate }}</div>
                                    <div class="detail">
                                        {{ coupon.couponName }}
                                    </div>
                                    <i class="icon-gou"></i>
                                    <div v-if="coupon.disableReason" class="coupon-notice">
                                        <i class="iconfont-pc icon-tishi"></i>{{ coupon.disableReason }}
                                    </div>
                                    <div v-if="coupon.selected && coupon.couponType == 2" class="coupon-notice">
                                        <i class="iconfont-pc icon-tishi"></i> {{ $t("已减") }}<FormatPrice v-model="coupon.couponMoney"></FormatPrice>
                                    </div>
                                </div>
                            </template>
                        </template>
                        <template v-else>
                            <div class="no-cou">
                                {{ $t("暂无可用优惠券") }}～<NuxtLink target="_blank" class="font-color" to="/coupon/list">{{ $t("去领券") }}</NuxtLink>
                            </div>
                        </template>
                    </div>
                </div>

                <div class="discountCode flex" v-if="false">
                    <div class="tit">{{ $t("输入券号") }}：</div>
                    <div class="inp_box">
                        <div class="inp">
                            <input type="text" />
                        </div>
                        <div class="btn">{{ $t("添加优惠券") }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, watch } from "vue";
import type { CheckCouponList, EnableCoupon } from "@/types/order/check";
interface Coupon {
    id: number;
    shopId: number;
    enabledOverlay: boolean; // 可叠加使用：true 是，false 否
    isGlobal: boolean; // 全场通用券：true 是，false 否
    userCouponId: number;
}
const emit = defineEmits(["change"]);
const useCoupon = defineModel<boolean>("useCoupon");
const couponList = defineModel<CheckCouponList>("couponList");
const couponLoading = defineModel("couponLoading");
const current = ref(0);
const tabList = reactive(["商品优惠券", "优惠码兑换"]);
const onUseCoupon = () => {
    if (useCoupon.value === false) {
        selectedCouponIds.value = [];
        selectedUserCouponIds.value = [];
        emit("change");
    } else {
        emit("change", true);
    }
};

const selectedCouponIds = defineModel<number[]>("selectedIds");
const selectedUserCouponIds = defineModel<number[]>("selectedUserCouponIds");
const selectedDatas = ref<EnableCoupon[]>([]);

watchEffect(() => {
    if (couponList.value && couponList.value.enableCoupons) {
        selectedDatas.value = couponList.value.enableCoupons.filter((item) => item.selected);
    }
});

const isSelectedUserCoupon = (UserCouponId: number): boolean => {
    return selectedUserCouponIds.value ? selectedUserCouponIds.value.includes(UserCouponId) : false;
};
const toggleCouponSelection = (coupon: any) => {
    // return
    const userCouponId = Number(coupon.userCouponId);
    if (isSelectedUserCoupon(userCouponId)) {
        selectedDatas.value = selectedDatas.value.filter((item) => item.userCouponId !== userCouponId);
    } else {
        // 同店铺优惠券
        const sameShopCouponIndex = selectedDatas.value.findIndex((data) => data.shopId === coupon.shopId);
        if (sameShopCouponIndex > -1) {
            selectedDatas.value = selectedDatas.value.filter((data) => data.shopId !== coupon.shopId);
        } else {
            // 不同店铺 同一种优惠券
            const sameCouponIndex = selectedDatas.value.findIndex((data) => data.id === coupon.id);
            if (sameCouponIndex > -1) {
                // 剔除同种优惠券
                selectedDatas.value = selectedDatas.value.filter((data) => data.id !== coupon.id);
            }
        }
        // 加入选中数组
        selectedDatas.value.push(coupon);
    }

    // return;
    selectedCouponIds.value = selectedDatas.value.map(({ id }) => Number(id));
    selectedUserCouponIds.value = selectedDatas.value.map(({ userCouponId }) => Number(userCouponId));
    emit("change");
};
</script>
<style lang="less" scoped>
.check-box {
    line-height: 40px;
    .tit {
        color: #333;
    }
    span {
        margin-left: 10px;
        color: #333;
    }
}

.from_list {
    position: relative;
    border-radius: 3px;
    background-color: #fafafa;
    border: 1px solid #e6e6e6;
    padding: 20px 20px;
    margin-bottom: 10px;
    margin-top: 15px;

    .arrow {
        position: absolute;
        left: 20px;
        top: -10px;
        height: 20px;
        width: 20px;
        background: url("/assets/images/flow/ioc_arrow.gif") no-repeat 0 0;
    }

    .coupon-box {
        .tab_list {
            margin-bottom: 10px;

            .tab {
                height: 20px;
                color: #333;
                margin: 5px 55px 10px 0;
                cursor: pointer;
            }

            .current {
                color: #e43a3d;
                border-bottom: 2px solid #e43a3d;
            }
        }

        .coupon_list {
            display: flex;
            flex-wrap: wrap;
            max-height: 260px;
            overflow: hidden;
            overflow-y: scroll;
            padding-top: 2px;

            /*控制滚动条宽度*/
            &::-webkit-scrollbar {
                width: 5px;
                height: 5px;
            }

            &::-webkit-scrollbar-thumb {
                border-radius: 10px;
                background-color: #999;
            }

            .coupon_item {
                height: 102px;
                width: 201px;
                border: 1px solid var(--vice-bg);
                background-color: var(--vice-bg);
                color: var(--vice-text);
                margin-right: 10px;
                margin-left: 10px;
                margin-bottom: 25px;
                position: relative;
                transition: opacity 0.3s;
                :deep(.icon-gou::after) {
                    border-bottom-color: var(--general);
                }
                &:hover {
                    opacity: 0.9;
                }
                .detail {
                    background-color: #fff;
                    color: #888;
                    position: absolute;
                    width: 191px;
                    height: 25px;
                    line-height: 25px;
                    overflow: hidden;
                    bottom: -0px;
                    padding-left: 10px;
                }

                .time {
                    margin-left: 10px;
                }
                .item-selected-cancel {
                    background-color: var(--main-bg);
                    color: var(--main-text);
                    padding: 2px 3px 2px 5px;
                    cursor: pointer;
                    z-index: 21;
                    position: absolute;
                    right: 0;
                    display: none;
                }

                :deep(.priceNum) {
                    font-size: 24px;
                    margin-left: 10px;
                    margin-top: 10px;
                    margin-bottom: 8px;

                    .price {
                        font-size: 24px;
                    }
                }

                .zk {
                    font-size: 24px;
                    margin-left: 10px;
                    margin-top: 10px;
                    margin-bottom: 8px;

                    span {
                        font-size: 12px;
                    }
                }
            }

            .active {
                position: relative;
                border: 1px solid var(--general);
                background-color: var(--general);
                color: var(--main-text);

                .c-top-dong {
                    display: none;
                }

                .icon-gou {
                    display: block;
                }

                &:hover {
                    .item-selected-cancel {
                        display: block;
                    }
                }
            }

            .active::after {
                content: "";
                position: absolute;
            }

            .disabled {
                border-color: #d1d1d1;
                background-color: #d1d1d1;
                cursor: not-allowed;
                color: #999;
                .time {
                    color: #999;
                }

                .detail {
                    color: #d1d1d1;
                }
            }
        }

        .discountCode {
            .tit {
                height: 28px;
                line-height: 28px;
                color: #666;
                margin-left: 10px;
            }

            .inp_box {
                input {
                    height: 20px;
                    line-height: 20px;
                }
            }

            .btn {
                margin-top: 10px;
                text-decoration: none;
                box-shadow: 0 1px 1px rgba(0, 1, 1, 0.08);
                cursor: pointer;
                color: #fff;
                background: #ff6c6c;
                border: 1px solid #ff5e5e;
                border-radius: 2px;
                display: inline-block;
                line-height: 16px;
                padding: 5px 12px;
                text-align: center;
            }
        }
    }
}

.coupon-notice {
    position: absolute;
    bottom: -20px;
    color: #a3a3a3;
    display: flex;
    align-items: center;

    i {
        padding-right: 2px;
        height: 15px;
    }
}

.coupon-globle {
    padding-left: 10px;
    font-size: 12px;
}
.no-cou {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    width: 100%;
}
</style>
