<template>
    <div class="shop">
        <div class="col2">
            <div>{{ $t("评分详细") }}</div>
            <div>{{ $t("与行业相比") }}</div>
        </div>
        <div class="col3">
            <div>{{ $t("商品评分") }}：</div>
            <div>0.00 {{ $t("分") }}</div>
            <div class="name"><span class="shop-ratingHigh"></span>0.00%</div>
        </div>
        <div class="col3">
            <div>{{ $t("服务评分") }}：</div>
            <div>0.00 {{ $t("分") }}</div>
            <div class="name"><span class="shop-ratingHigh"></span>0.00%</div>
        </div>
        <div class="col3">
            <div>{{ $t("物流评分") }}：</div>
            <div>0.00 {{ $t("分") }}</div>
            <div class="name"><span class="shop-ratingHigh"></span>0.00%</div>
        </div>
        <div class="line"></div>
        <div class="title">
            <span class="sub-title"> {{ $t("店铺名称") }}：</span>
            <NuxtLink class="name" :to="urlFormat({ path: 'shop', id: shopInfo.shopId })">{{ shopInfo.shopTitle }}</NuxtLink>
        </div>
    </div>
</template>
<script setup lang="ts">
const props = defineProps({
    shopInfo: { type: Object, default: {} }
});
</script>
<style scoped lang="less">
.shop {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .col2 {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        align-items: center;
    }
    .col3 {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        align-items: center;
    }
    .line {
        border-bottom: 1px dashed var(--general);
    }
    .title {
        font-size: 12px;
        display: flex;
        .sub-title {
            min-width: 60px;
        }
    }
    .name {
        display: flex;
        gap: 4px;
        align-items: center;
        color: var(--general);
        .shop-ratingHigh {
            background: url("@/assets/images/shop/store_header.png") 0 -60px no-repeat;
            height: 9px;
            display: inline-block;
            zoom: 1;
            vertical-align: middle;
            width: 9px;
        }
    }
}
</style>
