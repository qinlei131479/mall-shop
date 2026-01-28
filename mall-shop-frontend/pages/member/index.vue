<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="会员中心"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="user-info">
                <div class="image-div">
                    <el-upload
                        :action="requestPrefix + '/user/user/modifyAvatar'"
                        :headers="uploadHeaders"
                        :on-success="handleAvatarSuccess"
                        :show-file-list="false"
                        class="avatar-uploader"
                    >
                        <el-image :src="imageFormat(formState.avatar)" alt="" />
                    </el-upload>
                </div>
                <div class="user-col">
                    <div class="username">
                        <span>{{ userStore.userInfo.nickname ? userStore.userInfo.nickname : userStore.userInfo.dimUsername }}</span>
                        <div v-if="isB2B() && commonStore.closeAuth === 1">
                            <template v-if="Number(formState.isCompanyAuth) > 0">
                                <img class="auth-icon" :src="auth" @click="toAuth" />
                            </template>
                            <template v-else>
                                <img class="auth-icon" :src="noAuth" @click="toAuth" />
                            </template>
                        </div>
                        <span class="user_rank" style="margin: 0 0 0 10px">
                            <el-image :src="formState.rankIco" style="width: 20px" />
                        </span>
                    </div>
                    <div class="user-span-info">
                        <span>{{ $t(commonStore.integralName) }}：</span>
                        <span class="font-color">{{ formState.points ? formState.points : "-" }}</span
                        >{{ $t("分") }}
                    </div>
                    <div class="user-span-info">
                        <span>{{ $t("余额") }}：</span>
                        <FormatPrice v-model="formState.balance" :fontStyle="{ color: 'var(--price)' }" :showText="false"></FormatPrice>
                        <NuxtLink class="ml10" to="/member/account/deposit">[{{ $t("去充值") }}]</NuxtLink>
                    </div>
                    <div class="verify">
                        <template v-if="Number(formState.mobileValidated) > 0">
                            <i class="iconfont-pc icon-shoujiyiyanzheng has"></i>
                            <span>{{ $t("手机已验证") }}</span>
                        </template>
                        <template v-else>
                            <i class="iconfont-pc icon-shoujiweiyanzheng no"></i>
                            <NuxtLink to="/member/security/info">{{ $t("手机未验证(立即验证)") }}</NuxtLink>
                        </template>
                        <template v-if="isB2B() && !isOverseas() && commonStore.closeAuth === 1">
                            <div style="margin-left: 15px">
                                <template v-if="Number(formState.isCompanyAuth) > 0">
                                    <i class="iconfont-pc icon-shimingrenzheng has"></i>
                                    <NuxtLink to="/member/userCertification/info">{{ $t("实名认证（已认证）") }}</NuxtLink>
                                </template>
                                <template v-else>
                                    <i class="iconfont-pc icon-shimingrenzheng no"></i>
                                    <NuxtLink to="/member/userCertification/info">{{ $t("实名认证（未认证）") }}</NuxtLink>
                                </template>
                            </div>
                        </template>
                    </div>
                </div>
                <div class="info-line">
                    <span class="top-icon"></span>
                    <span class="bottom-icon"></span>
                </div>
                <div class="right-info">
                    <NuxtLink to="/member/coupon/list" class="right-link">
                        <div class="ico-page">
                            <div class="coupon_pic pic"></div>
                            <div class="mes_name">{{ $t("优惠券") }}</div>
                            <div class="mes_name">{{ formState.awaitCoupon ? formState.awaitCoupon : "0" }}{{ $t("张") }}</div>
                        </div>
                    </NuxtLink>
                    <NuxtLink to="/member/order/list?type=awaitPay" class="right-link">
                        <div class="ico-page">
                            <div class="pay_pic pic"></div>
                            <div class="mes_name">{{ $t("待付款") }}</div>
                            <div class="mes_name">{{ formState.awaitPay ? formState.awaitPay : "0" }}{{ $t("单") }}</div>
                        </div>
                    </NuxtLink>
                    <NuxtLink to="/member/order/list?type=awaitShipping" class="right-link">
                        <div class="ico-page">
                            <div class="take_pic pic"></div>
                            <div class="mes_name">{{ $t("待发货") }}</div>
                            <div class="mes_name">{{ formState.awaitShipping ? formState.awaitShipping : "0" }}{{ $t("单") }}</div>
                        </div>
                    </NuxtLink>
                    <NuxtLink to="/member/order/list?type=awaitComment" class="right-link">
                        <div class="ico-page">
                            <div class="ass_pic pic"></div>
                            <div class="mes_name">{{ $t("待评价") }}</div>
                            <div class="mes_name">{{ ordernum.awaitComment ? ordernum.awaitComment : "0" }}{{ $t("条") }}</div>
                        </div>
                    </NuxtLink>
                </div>
            </div>
            <div class="title-or-tabs bw">
                <span>{{ $t("近期订单") }}</span>
                <NuxtLink class="h4" to="/member/order/list"> {{ $t("更多订单") }}<i class="iconfont-pc icon-jiantou_you"></i> </NuxtLink>
            </div>
            <div class="page-info-body">
                <MemberOrderList :modelValue="0"></MemberOrderList>
            </div>
            <div class="swiper_box">
                <div class="tab_list">
                    <div :class="tabType == 1 ? 'active' : ''" class="tab" @mouseenter="tabType = 1">{{ $t("最近收藏") }}</div>
                    <div :class="tabType == 2 ? 'active' : ''" class="tab" @mouseenter="tabType = 2">{{ $t("最近浏览") }}</div>
                </div>
                <div v-if="tabType == 1" class="cheap_prod">
                    <swiper v-model:swiperOption="swiperOption" :itemList="collectList">
                        <template v-slot:default="{ item }">
                            <div class="tab-list flex-wrap">
                                <div v-for="items in item" class="item">
                                    <NuxtLink
                                        :title="items.productName"
                                        :to="urlFormat({ path: 'product', id: items.productId, sn: items.productSn })"
                                        class="nuxt"
                                        target="_blank"
                                    >
                                        <el-image :src="imageFormat(items.picThumb)" class="pro_pic" loading="lazy" style="transition: opacity 0.2s linear" />
                                        <div>
                                            <div class="text-ellipsis">
                                                {{ items.productName }}
                                            </div>
                                        </div>
                                    </NuxtLink>
                                    <p>
                                        <FormatPrice v-model="items.price" :fontStyle="{ fontSize: '14px', color: 'var(--price)' }"></FormatPrice>
                                    </p>
                                    <NuxtLink :to="urlFormat({ path: 'product', id: items.productId, sn: items.productSn })" target="_blank">
                                        <el-button type="primary">{{ $t("立即购买") }}</el-button>
                                    </NuxtLink>
                                </div>
                            </div>
                        </template>
                    </swiper>
                    <div v-if="collectList.length === 0" class="no-result" style="height: 319px">{{ $t("暂无数据") }}</div>
                </div>
                <div v-else class="cheap_prod">
                    <swiper v-model:swiperOption="swiperOption" :itemList="browseList">
                        <template v-slot:default="{ item }">
                            <div class="tab-list flex-wrap">
                                <div v-for="items in item" class="item">
                                    <NuxtLink
                                        :title="items.productName"
                                        :to="urlFormat({ path: 'product', id: items.productId, sn: items.productSn })"
                                        class="nuxt"
                                        target="_blank"
                                    >
                                        <el-image :src="imageFormat(items.picThumb)" class="pro_pic" loading="lazy" style="transition: opacity 0.2s linear" />
                                        <div>
                                            <div class="text-ellipsis">
                                                {{ items.productName }}
                                            </div>
                                        </div>
                                    </NuxtLink>
                                    <p>
                                        <FormatPrice v-model="items.price" :fontStyle="{ fontSize: '14px', color: 'var(--price)' }"></FormatPrice>
                                    </p>
                                    <NuxtLink :to="urlFormat({ path: 'product', id: items.productId, sn: items.productSn })" target="_blank">
                                        <el-button type="primary">{{ $t("立即购买") }}</el-button>
                                    </NuxtLink>
                                </div>
                            </div>
                        </template>
                    </swiper>
                    <div v-if="browseList.length === 0" class="no-result" style="height: 319px">{{ $t("暂无数据") }}</div>
                </div>
            </div>
        </div>
    </div>

    <CommonPageFooter :type="2" bg="#fff"></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref } from "vue";
import type { UserFormState } from "~/types/user/user";
import { getHistoryProduct, getMemberCenter } from "~/api/user/user";
import { getOrderNum } from "~/api/user/order";
import type { CollectProductFilterParams, CollectProductFilterState } from "~/types/user/collectProduct";
import { getCollectProductList } from "~/api/user/collectProduct";
import { urlFormat } from "~/utils/format";
import type { UploadProps } from "element-plus";
import { requestHeader, requestPrefix } from "~/utils/request";
import { useUserStore } from "~/store/user";
import { isB2B } from "@/utils/util";
import auth from "@/assets/images/common/auth.png";
import noAuth from "@/assets/images/common/no-auth.png";
import { useCommonStore } from "~/store/common";

definePageMeta({
    middleware: "auth"
});

const commonStore = useCommonStore();

const uploadHeaders = requestHeader();
const handleAvatarSuccess: UploadProps["onSuccess"] = (response, uploadFile) => {
    formState.value.avatar = URL.createObjectURL(uploadFile.raw!);
};
const userStore = useUserStore();
userStore.getUserInfo();
const tabType = ref<number>(1);
const ordernum = ref<object>({});

const swiperOption = ref<any>({
    autoplay: false,
    navigation: true,
    effect: "slide",
    loop: true
});
const formState = ref<UserFormState>({
    ...userStore.userInfo
});
const toAuth = () => {
    navigateTo(`/member/userCertification/info`);
};
const getUserInfo = async () => {
    try {
        const result = await getMemberCenter();
        Object.assign(formState.value, result);
        console.log(formState.value);
    } catch (error: any) {
        message.error(error.message);
    }
};
const getawaitInfo = async () => {
    try {
        const result = await getOrderNum();
        ordernum.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};
onMounted(() => {
    getUserInfo();
    getawaitInfo();
    getBrowseList();
});
const collectList = ref<CollectProductFilterState[]>([]);
const collectParams = reactive<CollectProductFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: 15,
    sortField: "",
    sortOrder: "",
    keyword: ""
});
const browseList = ref<CollectProductFilterState[]>([]);
const getBrowseList = async () => {
    try {
        const result = await getHistoryProduct({ ...collectParams });
        browseList.value.push(...filterStateToList(result));
    } catch (error: any) {
        message.error(error.message);
    }
};
const getCollectList = async () => {
    try {
        const result = await getCollectProductList({ ...collectParams });
        if (result.records.length > 0) collectList.value.push(...filterStateToList(result.records));
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
getCollectList();
const filterStateToList = (list: any) => {
    let result = [];
    for (let i = 0; i < list.length; i += 5) {
        let chunk: any = list.slice(i, i + 5);
        result.push(chunk);
    }
    return result;
};
</script>

<style lang="less" scoped>
@import "/assets/css/member/member";

.bw {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;

    .h4 {
        display: flex;
        justify-content: center;
        cursor: pointer;
        align-items: center;
    }
}

.user-info {
    display: flex;
    height: 180px;
    box-sizing: border-box;
    background: #fff url("/assets/images/index/userinfo-bg.png") repeat-y scroll right top;
    border-radius: 86px 0 0 86px;
    padding: 10px 40px;
    align-items: center;
    gap: 20px;

    .image-div {
        border-radius: 50%;
        margin-left: 20px;
        overflow: hidden;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 90px;
        width: 90px;
        background-color: #d5d5d5;
    }

    .user-col {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
        height: 90px;

        .username {
            font-size: 18px;
            display: flex;
            justify-content: center;
            color: var(--general);
            .auth-icon {
                margin-top: 5px;
                margin-left: 5px;
                height: 17px;
            }
        }

        .user-span-info {
            display: flex;
            gap: 6px;
            align-items: center;
            line-height: 1;
        }

        .verify {
            display: flex;
            gap: 4px;
            width: 320px;
            align-items: center;

            .has {
                color: #5abd47;
            }

            .no {
                color: #e3675b;
            }
        }
    }

    .right-info {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        font-size: 14px;

        gap: 40px;
        flex: 1;
        .right-link {
            height: 100px;
        }
        .ico-page {
            cursor: pointer;
            width: 68px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            gap: 4px;
            color: #969696;

            &:hover {
                .pic {
                    top: -10px;
                }

                .mes_name {
                    color: #333;
                }
            }
        }

        .coupon_pic {
            background: var(--main-bg) url(/assets/images/index/icon-coupon.png) no-repeat center;
        }

        .pay_pic {
            background: var(--main-bg) url(/assets/images/index/icon-pay.png) no-repeat center;
        }

        .take_pic {
            background: var(--main-bg) url(/assets/images/index/icon-take.png) no-repeat center;
        }

        .ass_pic {
            background: var(--main-bg) url(/assets/images/index/icon-ass.png) no-repeat center;
        }

        .pic {
            height: 60px;
            width: 60px;
            border-radius: 60px;
            position: relative;
            top: 0;
            transition: all 0.2s ease 0s;
        }
    }

    .info-line {
        position: relative;
        height: 180px;
        border-left: 1px dashed #ddd;

        .top-icon {
            background: url("/assets/images/index/info-top.png") no-repeat scroll 0 0;
            width: 18px;
            height: 8px;
            position: absolute;
            top: 0;
            left: -9px;
            z-index: 10;
        }

        .bottom-icon {
            background: url("/assets/images/index/info-bottom.png") no-repeat scroll 0 0;
            width: 18px;
            height: 8px;
            position: absolute;
            bottom: 0;
            left: -9px;
            z-index: 10;
        }
    }
}

.swiper_box {
    background-color: white;
    border: 0;
    padding: 16px 0;
    margin-bottom: 10px;
    margin-top: 10px;

    .tab_list {
        display: flex;
        margin-bottom: 10px;
        margin-left: 15px;

        .tab {
            padding: 5px 0;
            margin: 0 15px;
            cursor: pointer;
            border-bottom: 2px solid #fff;
        }

        .active {
            border-bottom: 2px solid var(--general);
            color: var(--general);
        }
    }

    .cheap_prod {
        .tab-list {
            margin: 30px 20px;

            .item {
                width: 150px;
                padding: 8px 19px 0 19px;
                display: flex;
                flex-direction: column;
                gap: 5px;

                .text-ellipsis {
                    white-space: pre-wrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    width: 150px;
                    height: 34px;
                    margin-bottom: 10px;
                    line-height: 17px;
                }
            }
        }
    }
}

.cheap_prod :deep(.swiper) {
    padding-bottom: 30px;
}

.cheap_prod :deep(.swiper-button-prev),
.cheap_prod :deep(.swiper-button-next) {
    z-index: 2;
    border: none;
    outline: none;
    transition: background-color 0.2s ease;
}

.cheap_prod :deep(.swiper-button-prev) {
    left: 10px;
    border-top-right-radius: 2px;
    border-bottom-right-radius: 2px;
}

.cheap_prod :deep(.swiper-button-next) {
    right: 10px;
    border-top-left-radius: 2px;
    border-bottom-left-radius: 2px;
}

.cheap_prod :deep(.swiper-button-prev:after),
.cheap_prod :deep(.swiper-button-next:after) {
    color: #e5e5e5;
    font-size: 24px;
    font-weight: bolder;
}

.cheap_prod :deep(.swiper-pagination) {
    padding-left: 25px;
    padding-bottom: 10px;
    text-align: center;
}

.cheap_prod :deep(.swiper-pagination-bullet) {
    position: relative;
    display: inline-block;
    transition: background 0.2s ease;
    width: 12px;
    height: 12px;
    margin-right: 10px;
    background: #e5e5e5;
    border-radius: 100px;
}

.cheap_prod :deep(.swiper-pagination-bullet-active) {
    width: 20px;
    height: 12px;
    background-color: var(--general);
    left: 0;
}
</style>
