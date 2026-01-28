<template>
    <view>
        <template v-if="pageModles && pageModles.title">
            <navBar :module="pageModles" />
        </template>

        <template v-for="(module, index) in modules" :key="module.moduleIndex">
            <template v-if="module.type == 'searchBar' && module?.isShow">
                <searchBar :module="module.module" />
            </template>
            <template v-else-if="module.type == 'catNav' && module?.isShow">
                <catNav :module="module.module" :scroll-top="scrollTop" @change-cat-nav="onChangeCatNav" @is-cut="getShow" />
                <sticky :module="module.module" :scroll-top="scrollTop" @change-cat-nav="onChangeCatNav" @is-cut="getShow" />
            </template>
            <view :class="{ show }">
                <template v-if="showCatNav == 0">
                    <template v-if="module.type == 'notice' && module?.isShow">
                        <notice :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'titleAd' && module?.isShow">
                        <titleAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageAd' && module?.isShow">
                        <imageAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageNav' && module?.isShow">
                        <imageNav :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'coupon' && module?.isShow">
                        <coupon :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'seckill' && module?.isShow">
                        <seckill :module="module.module" />
                    </template>
                    <template v-else-if="module.type == 'whiteBlank' && module?.isShow">
                        <whiteBlank :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'whiteLine' && module?.isShow">
                        <whiteLine :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageSquareAd' && module?.isShow">
                        <imageSquareAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageHotarea' && module?.isShow">
                        <imageHotarea :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'product' && module?.isShow">
                        <product :module="module.module" :module-index="module.moduleIndex" />
                    </template>

                    <template v-else-if="module.type == 'categoryNav' && module?.isShow">
                        <categoryNav :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'categoryNavA2' && module?.isShow">
                        <categoryNavA2 :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'banner' && module?.isShow">
                        <banner :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'slidingImage' && module?.isShow">
                        <SlidingImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'slideLargeImage' && module?.isShow">
                        <slideLargeImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'comboSuggestions' && module?.isShow">
                        <comboSuggestions :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'noticeA2' && module?.isShow">
                        <noticeA2 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'specialOffer' && module?.isShow">
                        <specialOffer :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'mixedProductDisplay' && module?.isShow">
                        <mixedProductDisplay :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'categoryRec' && module?.isShow">
                        <categoryRec :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'countdown' && module?.isShow">
                        <countdown :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'groupSliding' && module?.isShow">
                        <groupSliding :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'productGrouping' && module?.isShow">
                        <productGrouping :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'imageTextComponent' && module?.isShow">
                        <imageTextComponent :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'categoryNavA1' && module?.isShow">
                        <categoryNavA1 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                </template>
            </view>
        </template>
        <template v-if="showCatNav == 1">
            <view>
                <template v-if="imgUrlData.length > 0">
                    <view class="bannerBox" :style="'background: ' + currentColor + ';'">
                        <view :class="{ show }" class="bannerWrap">
                            <swiper
                                :current="current"
                                :indicator-dots="false"
                                :autoplay="true"
                                :circular="true"
                                interval="3000"
                                duration="500"
                                indicator-color="rgba(255,255,255,0.6)"
                                indicator-active-color="#fff"
                                :style="{ height: swiperHeight + 'px' }"
                            >
                                <template v-for="(item, index) in imgUrlData" :key="index">
                                    <swiper-item>
                                        <view class="swiper-slide">
                                            <image class="swiper-image" mode="widthFix" :src="imageFormat(item.picUrl)" @load="computeImgHeight" />
                                        </view>
                                    </swiper-item>
                                </template>
                            </swiper>
                        </view>
                    </view>
                </template>

                <template v-if="childCatInfoData.length > 0">
                    <view class="categoryBox" :style="'background: ' + currentColor + ';'">
                        <view :class="{ show }" class="categoryWrap">
                            <template v-for="(item, index) in childCatInfoData" :key="index">
                                <template v-if="index < 10">
                                    <view class="item" @click="redirect({ url: '/pages/search/result', param: { categoryId: item.categoryId } })">
                                        <view class="item-img">
                                            <tig-image :src="item.categoryPic" />
                                        </view>
                                        <view class="txt">{{ item.categoryName }}</view>
                                    </view>
                                </template>
                            </template>
                        </view>
                    </view>
                </template>

                <template v-if="brandInfoData.length > 0">
                    <view class="brandBox">
                        <view :class="{ show }" class="brandWrap">
                            <template v-for="(brand, index) in brandInfoData" :key="index">
                                <view v-if="index < 8" class="item">
                                    <view class="itemWrap">
                                        <view @click="redirect({ url: '/pages/search/result', param: { brandId: brand.brandId } })">
                                            <tig-image :src="brand.brandLogo" />
                                            <view class="txt">{{ brand.brandName }}</view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </template>
            </view>
        </template>
    </view>
</template>

<script lang="ts" setup>
import navBar from "@/components/modules/navbar/index.vue";
import searchBar from "@/components/modules/searchBar/index.vue";
import catNav from "@/components/modules/catNav/index.vue";
import sticky from "@/components/modules/catNav/sticky.vue";
import notice from "@/components/modules/notice/index.vue";
import titleAd from "@/components/modules/titleAd/index.vue";
import imageAd from "@/components/modules/imageAd/index.vue";
import whiteBlank from "@/components/modules/whiteBlank/index.vue";
import whiteLine from "@/components/modules/whiteLine/index.vue";
import imageSquareAd from "@/components/modules/imageSquareAd/index.vue";
import imageNav from "@/components/modules/imageNav/index.vue";
import coupon from "@/components/modules/coupon/index.vue";
import seckill from "@/components/modules/seckill/index.vue";
import imageHotarea from "@/components/modules/imageHotarea/index.vue";
import product from "@/components/modules/product/index.vue";
import categoryNav from "@/components/modules/categoryNav/index.vue";
import categoryNavA2 from "@/components/modules/categoryNavA2/index.vue";
import SlidingImage from "@/components/modules/slidingImage/index.vue";
import banner from "@/components/modules/banner/index.vue";
import slideLargeImage from "@/components/modules/slideLargeImage/index.vue";
import comboSuggestions from "@/components/modules/comboSuggestions/index.vue";
import noticeA2 from "@/components/modules/noticeA2/index.vue";
import specialOffer from "@/components/modules/specialOffer/index.vue";
import mixedProductDisplay from "@/components/modules/mixedProductDisplay/index.vue";
import categoryRec from "@/components/modules/categoryRec/index.vue";
import countdown from "@/components/modules/countdown/index.vue";
import groupSliding from "@/components/modules/groupSliding/index.vue";
import productGrouping from "@/components/modules/productGrouping/index.vue";
import imageTextComponent from "@/components/modules/imageTextComponent/index.vue";
import categoryNavA1 from "@/components/modules/categoryNavA1/index.vue";
import { ref, getCurrentInstance, nextTick } from "vue";
import { redirect, getElementRect } from "@/utils/index";
import { imageFormat } from "@/utils/format";

interface Props {
    modules: any[];
    pageModles: {};
    scrollTop: number;
}
defineProps<Props>();
const emit = defineEmits(["load-goods-list"]);
const showCatNav = ref(0);
const catId = ref(0);
const current = "";

const childCatInfoData = ref<any[]>([]);
const brandInfoData = ref<any[]>([]);
const imgUrlData = ref<any[]>([]);
const currentColor = ref("");
const show = ref(false);
const getShow = (val: boolean) => {
    show.value = val;
};

interface CatNav {
    id: number;
    categoryId: number;
    showCatNav: number;
    catColor: string;
    childCatInfo: any[];
    brandInfo: any[];
    imgUrl: any[];
}

const onChangeCatNav = async (data: CatNav) => {
    const { categoryId, catColor, imgUrl, childCatInfo, brandInfo } = data;
    childCatInfoData.value = childCatInfo;
    brandInfoData.value = brandInfo;
    imgUrlData.value = imgUrl;
    currentColor.value = catColor;
    if (categoryId === catId.value) return;
    catId.value = categoryId; //  记录当前分类id
    nextTick(() => {
        if (catId.value > 0) {
            showCatNav.value = 1;
        } else {
            showCatNav.value = 0;
        }
        show.value = true;
        emit("load-goods-list", categoryId);
    });
};

const instance = getCurrentInstance();
const swiperHeight = ref(0);
const computeImgHeight = (e: any) => {
    nextTick(async () => {
        const res = await getElementRect(".swiper-image", instance);
        swiperHeight.value = res?.height ?? 0;
    });
};
</script>
<style scoped>
@import url("../../static/css/modules.css");
</style>
