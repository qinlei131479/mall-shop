<template>
    <CommonPageHeader :shopId="shopId" :showDefNav="false"></CommonPageHeader>
    <div style="background: white" v-if="loading">
        <template v-for="(item, index) in moduleList" :key="index">
            <ShopInfo :pageModule="pageModule" :shopId="Number(shopId)" :module="item.module" v-if="item.type == 'shopInfo' && item.isShow"></ShopInfo>
            <div class="container">
                <ShopNotice :module="item.module" v-if="item.type == 'notice' && item.isShow"></ShopNotice>
                <ShopCoupon :shopId="Number(shopId)" :module="item.module" v-if="item.type==  'coupon' && item.isShow"></ShopCoupon>
                <ShopImageSquareAd :module="item.module" v-if="item.type == 'imageSquareAd' && item.isShow"></ShopImageSquareAd>
                <ShopWhiteBlank :module="item.module" v-if="item.type == 'whiteBlank' && item.isShow"></ShopWhiteBlank>
                <ShopWhiteLine :module="item.module" v-if="item.type == 'whiteLine' && item.isShow"></ShopWhiteLine>
                <ShopTitleAd :module="item.module" v-if="item.type == 'titleAd' && item.isShow"></ShopTitleAd>
                <ShopImageNav :module="item.module" v-if="item.type == 'imageNav' && item.isShow"></ShopImageNav>
                <ShopImageAd :module="item.module" v-if="item.type == 'imageAd' && item.isShow && item.module.isFluxWidth == 0"></ShopImageAd>
                <ShopProduct
                    :shopId="Number(shopId)"
                    :decorateId="decorateId"
                    :moduleIndex="item.moduleIndex"
                    :module="item.module"
                    v-if="item.type == 'product' && item.isShow"
                ></ShopProduct>
                <ShopImageHotarea :module="item.module" v-if="item.type == 'imageHotarea' && item.isShow"></ShopImageHotarea>
            </div>
            <ShopImageAd v-if="item.type == 'imageAd' && item.isShow && item.module.isFluxWidth == 1" :module="item.module"></ShopImageAd>
        </template>
    </div>
    <CommonPageFooter :mt30="false" :showFriendly="false"></CommonPageFooter>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { getShopDecorate } from "~/api/shop/shop";
import ShopNotice from "~/components/shop/ShopNotice.vue";
import ShopWhiteBlank from "~/components/shop/ShopWhiteBlank.vue";
import ShopWhiteLine from "~/components/shop/ShopWhiteLine.vue";
import ShopImageHotarea from "~/components/shop/ShopImageHotarea.vue";
import ShopProduct from "~/components/shop/ShopProduct.vue";
import ShopCoupon from "~/components/shop/ShopCoupon.vue";
import { useCommonStore } from "@/store/common";
const commonStore = useCommonStore();
const { t } = useI18n();
const titleStr = computed(() => {
    let title = t("店铺详情");
    if (commonStore.poweredByStatus != 1) {
        title += "-powered by tigshop";
    }
    return title;
});
const loading = ref(false);
const description = ref("");
const keywords = ref("");
useHead({
    title: titleStr,
    meta: [
        { name: "description", content: description },
        { name: "keywords", content: keywords }
    ]
});
const route = useRoute();
const shopId = ref(route.path.split("/").pop());
const moduleList: any = ref([]);
const pageModule: any = ref({});
const decorateId = ref();
const getModuleList = async () => {
    loading.value = false;
    try {
        const result = await getShopDecorate({ shopId: shopId.value });
        decorateId.value = result.decorateId;
        moduleList.value = result.moduleList;
        Object.assign(pageModule.value, result.pageModule);
    } catch (error) {
    } finally {
        loading.value = true;
    }
};
getModuleList();
</script>
<style scoped lang="less"></style>
