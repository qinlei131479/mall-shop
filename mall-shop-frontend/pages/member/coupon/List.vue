<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="我的优惠券"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs bw">
                <span>{{ $t("我的优惠券") }}</span>
                <NuxtLink target="_blank" to="/coupon/list">
                    <i class="iconfont-pc icon-wodeyouhuiquan"></i>
                    {{ $t("领取更多优惠券") }}
                </NuxtLink>
            </div>
            <div class="coupon-list">
                <div v-loading="loading" class="coupon-item-list">
                    <template v-if="total > 0">
                        <template v-for="(item, index) in filterState">
                            <div
                                :class="[item.status == 2 || item.status == 1 || item.status == 3 ? 'card-true' : 'card-false']"
                                class="card"
                                @click.stop="openLink('/search/?couponId=' + item.couponId, item.status)"
                            >
                                <div v-if="item.status == 1" class="triangle">
                                    <div class="be-about-to-expire"></div>
                                </div>
                                <div v-if="item.status == 3" class="triangle-effective">
                                    <div class="effective">
                                        即将生效
                                    </div>
                                </div>
                                <div v-if="item.status == 4" class="triangle">
                                    <div class="have-been-used"></div>
                                </div>
                                <div v-if="item.status == 5" class="triangle">
                                    <div class="have-expired"></div>
                                </div>
                                <div class="triangle-right">
                                    <DeleteRecord :params="{ id: item.id }" :requestApi="delCoupon" @afterDelete="loadFilter" @click.stop>
                                        <div class="c-del"></div>
                                    </DeleteRecord>
                                </div>
                                <div class="c-price">
                                    <template v-if="item.couponType == 1">
                                        <FormatPrice
                                            :showText="false"
                                            v-model="item.couponMoney"
                                            :currencyStyle="{ fontSize: '24px', fontFamily: 'Verdana, serif', lineHeight: '40px' }"
                                            :fontStyle="{ fontSize: '60px', fontWeight: 'bold' }"
                                        ></FormatPrice>
                                    </template>
                                    <template v-else>
                                        <!--折扣券-->
                                        <div class="coupon_discount">
                                            {{ item.couponDiscount }}<span>{{ $t("折") }}</span>
                                        </div>
                                    </template>
                                </div>
                                <div class="c-limit">{{ item.couponName ? "【" + item.couponName + "】" : "" }}</div>
                                <div :class="[item.status === 2 || item.status == 1 || item.status == 3 ? 'c-time-true' : 'c-time-false']">
                                    {{ item.startDate }} {{ $t("至") }} {{ item.endDate }}
                                </div>
                                <div class="type-bottom"></div>
                            </div>
                        </template>
                        <div class="el-page">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </template>
                    <template v-else>
                        <div class="no-result">
                            <span v-if="!loading">{{ $t("您还没有优惠券") }}</span>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import type { CouponFilterParams, MyCouponFilterState } from "~/types/user/coupon.d";
import { reactive, ref } from "vue";
import { addCoupon, delCoupon, getMyCouponList } from "~/api/user/coupon";
import DeleteRecord from "~/components/member/DeleteRecord.vue";
import { Pagination } from "~/components/list";

definePageMeta({
    middleware: "auth"
});
const filterState = ref(<MyCouponFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<CouponFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: 9
} as CouponFilterParams);

const openLink = (url: string, status: number) => {
    if (status === 2 || status === 1) {
        window.open(url);
    }
};
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getMyCouponList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total
        // total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

const form = ref({
    couponSn: ""
});
const { t } = useI18n();
const addCouponFun = async () => {
    try {
        loading.value = true;
        const result = await addCoupon(form.value);
        await loadFilter();

        message.success(t("添加成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        form.value.couponSn = "";
    }
};
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.bw {
    display: flex;
    justify-content: space-between;
    align-items: center;

    & > a {
        display: flex;
        justify-content: center;
        gap: 10px;
        cursor: pointer;
    }
}

.coupon-list {
    background: #fff;
    border: 0;
    padding: 20px 20px;

    .coupon-item-list {
        display: flex;
        flex-direction: row;

        flex-wrap: wrap;
        padding: 20px 0;
        box-sizing: border-box;
        gap: 16px;

        .card-false {
            background-color: #c3c3c3; /* 淡蓝色背景 */

            & > .type-bottom {
                background: url("/assets/images/user/coupon20160715.png") repeat scroll -3px -317px;
                height: 3px;
                overflow: hidden;
                position: absolute;
                bottom: 0;
                width: 244px;
            }
        }

        .card-true {
            background-color: #74d2d4; /* 淡蓝色背景 */

            & > .type-bottom {
                background: url("/assets/images/user/coupon20160715.png") repeat scroll -3px -303px;
                height: 3px;
                overflow: hidden;
                position: absolute;
                bottom: 0;
                width: 244px;
            }
        }

        .card {
            width: 300px;
            height: 150px;
            position: relative; /* 设置相对定位 */
            overflow: hidden; /* 确保所有内容都在边框内部 */
            cursor: pointer;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            color: white;
            gap: 4px;

            .c-price {
                line-height: 1;

                .coupon_discount {
                    font-size: 60px;

                    & > span {
                        font-size: 24px;
                    }
                }
            }

            .c-time-true {
                color: #197f81;
            }

            .c-time-false {
                color: #fff;
            }

            .triangle {
                width: 65px; /* 充满父元素 */
                height: 65px; /* 充满父元素 */
                overflow: hidden; /* 确保只显示一半内容 */
                position: absolute; /* 相对于父元素定位 */
                top: 0;
                left: 0;
                transform-origin: 0 0; /* 旋转的基点是左上角 */
            }
            .triangle-effective {
                width: 65px; 
                height: 65px;
                overflow: hidden;
                position: absolute;
                top: 0;
                left: 0;
                -webkit-clip-path: polygon(100% 0, 0 0, 0 100%);
                clip-path: polygon(100% 0, 0 0, 0 100%);
                background: #ffea00;  
                .effective {
                    margin-top: 8px;
                    margin-left: -4px;
                    color: #6a6964;
                    transform: rotate(-45deg);
                }
            }
        }

        .card::before {
            content: "";
            position: absolute;
            top: -30px; /* 半圆的大小，确保其部分在 div 外部 */
            left: 50%; /* 放置在 div 的中心 */
            width: 40px; /* 半圆的宽度 */
            height: 40px; /* 半圆的高度 */
            margin-left: -20px; /* 将半圆向左移动，以便它居中 */
            border-radius: 20px; /* 顶部圆角形成半圆 */
            background-color: white; /* 与页面背景相同的颜色 */

            border-bottom: none; /* 移除半圆底部的边框 */
        }

        .triangle-right {
            position: absolute;
            top: 0;
            right: 0;
            width: 35px; /* 充满父元素 */
            height: 35px; /* 充满父元素 */
            clip-path: polygon(100% 0, 100% 100%, 0 0); /* 创建一个右上角三角形 */
            opacity: 0; /* 默认不显示 */
            transition:
                transform 0.3s ease-in-out,
                opacity 0.3s ease-in-out; /* 动画效果 */
        }

        .card:hover .triangle-right {
            width: 35px; /* 悬停时三角形展开 */
            opacity: 1; /* 悬停时显示 */
        }
    }

    .have-been-used {
        background: url("/assets/images/user/coupon20160715.png") repeat scroll 0 0;
        display: block;
        height: 66px;
        left: 0;
        overflow: hidden;
        position: absolute;
        top: 0;
        width: 65px;
    }

    .be-about-to-expire {
        background: url("/assets/images/user/coupon20160715.png") repeat scroll -100px 0;
        display: block;
        height: 66px;
        left: 0;
        overflow: hidden;
        position: absolute;
        top: 0;
        width: 65px;
    }

    .have-expired {
        background: url("/assets/images/user/coupon20160715.png") repeat scroll -172px 0;
        display: block;
        height: 66px;
        left: 0;
        overflow: hidden;
        position: absolute;
        top: 0;
        width: 65px;
    }

    .c-del {
        background: url("/assets/images/user/coupon20160715.png") repeat scroll -261px 0;
        display: block;
        height: 35px;
        left: 0;
        overflow: hidden;
        position: absolute;
        top: 0;
        width: 35px;
    }
}
</style>
