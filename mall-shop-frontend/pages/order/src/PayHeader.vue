<template>
    <div>
        <CommonTopBar></CommonTopBar>
        <div class="cart-top flex justify-center">
            <div class="cart-cont flex justify-between">
                <div class="logo">
                    <NuxtLink exact-active-class="''" to="/">
                        <img :src="imageFormat(commonStore.shopLogo)" width="200" height="auto" alt="" />
                    </NuxtLink>
                </div>
                <div class="steps">
                    <Steps :current="props.step - 1"></Steps>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { useCommonStore } from "@/store/common";
const commonStore = useCommonStore();
const props = defineProps({
    step: { type: Number, default: 0 }
});
</script>
<style lang="less" scoped>
.logo {
    min-height: 100px;
    display: flex;
    align-items: center;
    img {
        width: 190px;
        padding: 10px 0;
    }
}
.cart-top {
    background-color: #ffffff;
    position: relative;
    z-index: 79;
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;

    .cart-cont {
        width: 1190px;
        padding: 0 0 0 0;
    }
}
.steps {
    padding-top: 20px;
}
</style>
