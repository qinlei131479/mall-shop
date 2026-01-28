<template>
    <div class="main-box">
        <div class="right-main-box">
            <template v-if="homeStore.previewId > 0">
                <div class="preview-box">
                    <div class="container">
                        {{ $t("预览") }}：&emsp;<span style="">{{ $t("PC端") }}</span>
                    </div>
                </div>
            </template>
            <div style="background-color: #f2f3f5">
                <CommonPageHeader page="index" :showService="false"></CommonPageHeader>
                <div v-if="homeStore.moduleList.length > 0">
                    <template v-for="(item, index) in homeStore.moduleList" :key="index">
                        <HomeBanner v-if="item.type == 'pcBanner' && item.isShow" v-model="item.module" :class="{ container: item.module.bannerStyle == 1 }" />
                    </template>
                </div>
                <div class="container">
                    <div v-if="homeStore.moduleList.length > 0" class="home-warp">
                        <template v-for="(item, index) in homeStore.moduleList" :key="index">
                            <HomeCatWide :moduleIndex="item.moduleIndex"  v-if="item.type === 'pcCatWide' && item.isShow"/>
                            <HomeSeckill v-if="item.type == 'pcSeckill' && item.isShow" v-model="item.module" />
                            <HomeCoupon v-if="item.type == 'pcCoupon' && item.isShow" v-model="item.module" />
                            <HomeCatProductNormal v-if="item.type == 'pcCatProductNormal' && item.isShow" v-model="item.module" />
                            <HomeCatProductWide v-if="item.type == 'pcCatProductWide' && item.isShow" v-model="item.module" />
                            <HomeCatProductSimple v-if="item.type === 'pcCatProductSimple' && item.isShow" v-model="item.module" :moduleIndex="item.moduleIndex" />
                            <HomeCustomAd1 v-if="item.type == 'pcCustomAd1' && item.isShow" v-model="item.module" />
                            <HomeCustomAd2 v-if="item.type == 'pcCustomAd2' && item.isShow" v-model="item.module" />
                            <HomeImageAd v-if="item.type == 'pcImageAd' && item.isShow" v-model="item.module" />
                            <HomeRecommendProduct
                                v-if="item.type == 'pcRecommendProduct' && item.isShow"
                                v-model="item.module"
                                :decorateId="homeStore.decorateId"
                                :moduleIndex="item.moduleIndex"
                            />
                        </template>
                        <div class="home-elevator">
                            <HomeElevatorList class="home-elevator-list"></HomeElevatorList>
                        </div>
                    </div>
                </div>
            </div>
            <CommonPageFooter :showFriendly="true"></CommonPageFooter>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { getHomeData } from "@/api/home/home";
import { useHomeStore } from "@/store/home";
import { useCommonStore } from "@/store/common";
import HomeCatWide from "@/components/home/CatWide.vue"

const route = useRoute();

const homeStore = useHomeStore();

const commonStore = useCommonStore();

const headTitle = computed(() => {
    let title = commonStore.shopTitle;
    if (commonStore.poweredByStatus != 1) {
        title += "-powered by tigshop";
    }
    return title;
});
const headDescription = ref(commonStore.shopDesc);
const headKeywords = ref(commonStore.shopKeywords);
useHead({
    title: headTitle,
    meta: [
        { name: "description", content: headDescription },
        { name: "keywords", content: headKeywords }
    ]
});

const getModuleList = async (previewId: number) => {
    try {
        const result = await getHomeData({ previewId });
        homeStore.moduleList = result?.moduleList ?? [];
        homeStore.decorateId = result?.decorateId ?? 0;
    } catch (error) {
    } finally {
        homeStore.moduleLoaded = true;
    }
};

watch(
    () => route.query,
    (newQuery) => {
        let previewId = 0;
        if (route.query && route.query.previewId) {
            previewId = Number(route.query.previewId);
            homeStore.previewId = previewId;
            homeStore.moduleList = [];
            homeStore.decorateId = 0;
        } else {
            homeStore.previewId = 0;
        }
        if (homeStore.moduleLoaded === false) {
            getModuleList(previewId);
        }
    },
    { immediate: true }
);

</script>
<style lang="less" scoped>
.main-box {
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    position: relative;
    .ChooseStyle-class {
        position: sticky;
        z-index: 100;
        left: 0;
        top: 0;
    }

    .right-main-box {
        width: 100%;
        position: relative;

        .thematic-style {
            position: absolute;
            top: 60px;
            z-index: 20;
            left: 0;
            width: 70px;
            background-color: white;
            cursor: pointer;
            gap: 4px;

            font-size: 16px;
            display: flex;
            align-items: center;
            justify-content: center;

            box-sizing: border-box;
            padding: 0 10px;
            transition: height 0.5s ease; /* 平滑过渡效果 */
            line-height: 1.5;
            height: 120px;
            box-shadow: 0px 4px 8px 0px #dcdcdc;
            border-top-right-radius: 6px;
            border-bottom-right-radius: 6px;
            .div-to-rotate {
                /* 使用transform属性来旋转元素 */
                transform: rotate(-90deg);
                /* 或者使用弧度值 */
                /* transform: rotate(1.5708rad); */
            }
        }

        .home-warp {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            position: relative;

            .home-elevator {
                position: absolute;
                right: -100px;
                top: 30px;
                height: calc(100% - 30px);
            }
        }

        .skeleton {
            display: flex;
            flex-direction: column;
            gap: 20px;

            .sck1 {
                display: flex;
                margin-top: 10px;
                gap: 10px;

                & > div {
                    display: flex;
                    gap: 10px;
                    flex-direction: column;
                }
            }
        }
        .preview-box {
            padding: 20px;
            background-color: #fff;
            border-bottom: 1px solid #ebeef5;
        }
        .preview-box span {
            position: relative;
            display: inline-block;
            font-size: 14px;
        }
        .preview-box span::after {
            content: "";
            position: absolute;
            top: 25px;
            left: 0;
            width: 100%;
            height: 2px;
            background-color: #155bd4;
        }
    }
}

.home-elevator-list {
    position: sticky; /* 初始使用绝对定位 */
    top: 50px;
    transition: top 0.3s; /* 可选：添加平滑过渡效果 */
    left: 1340px;
    width: 60px;
    background-color: white;
    z-index: 9999;
}

.not-zh {
    .home-elevator-list {
        width: 86px;
    }
}
</style>
