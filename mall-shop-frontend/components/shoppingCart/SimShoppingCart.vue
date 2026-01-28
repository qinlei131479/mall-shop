<template>
    <ShoppingCartList class="simCart">
        <template #default="{ count, showMinCart }">
            <div class="cart_info">
                <NuxtLink
                    :style="{ color: colorType == 3 ? (showMinCart ? 'var(--general)' : '#000000') : showMinCart ? 'var(--general)' : '#b0b0b0' }"
                    to="/cart/"
                    class="acea-row row-center-wrapper"
                >
                    <div class="iconfont-pc icon-shiwu-gouwuche"></div>
                    <div class="my-cart">{{ $t("购物车") }}</div>
                    <em>（{{ count }}）</em>
                </NuxtLink>
            </div>
        </template>
    </ShoppingCartList>
</template>
<script setup lang="ts">
const props = defineProps({
    colorType: {
        type: Number,
        default: 1
    }
});
</script>
<style scoped lang="less">
.simCart.cart {
    background: transparent;
    height: 30px;
    padding: 0;
    text-align: center;
    width: 120px;
    position: relative;
    z-index: 10;
    border: 0;
    margin-left: 0;
    :deep(.list_wrapper) {
        position: absolute;
        right: 0;
        top: 30px;
        .list {
            padding-top: 10px;
            background-color: #ffffff;
            border: 1px solid #ddd;
            position: absolute;
            right: -2px;
            text-align: left;
            top: -2px;
            width: 270px;
            border-top: 0;
        }
    }
}
.cart_info {
    position: relative;
    z-index: 9;
    a {
        line-height: 30px;
        font-size: 12px;
        color: #666;

        div:first-child {
            margin-right: 5px;
        }

        .my-cart {
            font-size: 12px;
        }
    }
}
.acea-row {
    display: flex;
    flex-wrap: wrap;
}

.acea-row.row-middle {
    align-items: center;
}

.acea-row.row-right {
    justify-content: flex-end;
}

.acea-row.row-center-wrapper {
    align-items: center;
    justify-content: center;
}

.acea-row.row-between-wrapper {
    align-items: center;
    justify-content: space-between;
}
</style>
