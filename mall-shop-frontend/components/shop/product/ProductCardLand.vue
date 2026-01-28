<template>
    <div :style="{ padding: module.goodsPadding + 'px', width: 100 / size + '%' }" class="product-card">
        <div
            class="car-info"
            :class="[
                module.goodsStyle == 2
                    ? 'product-card-border-2'
                    : module.goodsStyle == 3
                      ? 'product-card-border-3'
                      : module.goodsStyle == 1
                        ? ''
                        : 'product-card-border-4',
                module.goodsRadioStyle == 2 ? 'box-radius' : ''
            ]"
        >
            <div class="item-photo">
                <NuxtLink target="_blank" :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })" class="item-content">
                    <el-image
                        :class="[module.goodsRadioStyle == 2 ? 'image-box-radius' : '']"
                        class="image-box"
                        :src="imageFormat(product.picUrl)"
                    ></el-image>
                </NuxtLink>
            </div>
            <div class="right-box">
                <div class="item-info" :style="{ padding: module.goodsNamePadding == 1 ? '0 10px' : '' }">
                    <div class="item-name" :style="{ textAlign: module.textAlign == 1 ? 'left' : 'center' }">
                        <NuxtLink
                            v-if="module.showName"
                            target="_blank"
                            :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })"
                            class="item-product-name"
                            :style="{ fontWeight: module.textWeight == 1 ? '' : 'bold', height: module.goodsNameRow == 1 ? '20px' : '40px' }"
                        >
                            {{ product.productName }}
                        </NuxtLink>
                        <div v-if="module.showBrief" class="item-brief">
                            {{ product.productBrief }}
                        </div>
                    </div>
                    <div class="item-action" :style="{ flexDirection: module.textAlign == 1 ? 'row' : 'column' }">
                        <div v-if="module.showPrice" class=" ">
                            <FormatPrice
                                :fontStyle="{ color: 'var(--price)', fontSize: '24px', fontWeight: module.textWeight == 1 ? '' : 'bold' }"
                                v-model="product.productPrice"
                            ></FormatPrice>
                        </div>
                        <div v-if="module.buyBtnStyle > 0" class="item-buy">
                            <template v-if="product.productSku && product.productSku.length > 0">
                                <NuxtLink :to="urlFormat({ path: 'product', id: product.productId, sn: product.productSn })" target="_blank">
                                    <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                </NuxtLink>
                            </template>
                            <template v-else>
                                <ProductBuy :isQuick="true" :id="product.productId" :disabled="product.productStock == 0" :number="1">
                                    <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                </ProductBuy>
                            </template>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import ProductButton from "~/components/shop/product/ProductButton.vue";

const props = defineProps({
    module: { type: Object, default: {} },
    product: { type: Object, default: {} },
    size: { type: Number, default: 1 }
});
import { imageFormat, urlFormat } from "~/utils/format";
</script>
<style scoped lang="less">
.product-card {
    display: flex;
    box-sizing: border-box;
    width: 100%;

    .car-info {
        padding-bottom: 10px;
        box-sizing: border-box;
        width: 100%;

        display: flex;
        flex-direction: row;
        background-color: white;

        .item-photo {
            flex: 0 0 260px; /* 或者使用百分比，例如 width: 30%; 这里30%是示例，具体根据需求调整 */
            max-width: 400px; /* 保持最大宽度限制，防止过大 */
            display: flex;

            .item-content {
                width: 100%;

                .image-box {
                    flex: 1;
                    width: 260px;
                    height: 260px;
                }
            }
        }

        .right-box {
            padding: 10px 0;
            width: calc(100% - 260px);
            display: flex;

            .item-info {
                display: flex;
                flex-direction: column;
                box-sizing: border-box;
                width: 100%;

                .item-name {
                    margin-bottom: auto;
                    display: flex;
                    flex-direction: column;
                    width: 100%;
                    box-sizing: border-box;
                    padding-bottom: 10px;

                    .item-product-name {
                        font-size: 16px;
                        overflow: hidden; /* 隐藏超出容器的内容 */
                        text-overflow: ellipsis; /* 超出部分用省略号表示 */
                        line-height: 20px;
                    }

                    .item-brief {
                        line-height: 20px;
                        height: 20px;
                        color: #aaaaaa;
                        white-space: nowrap; /* 不换行 */
                        overflow: hidden; /* 隐藏超出容器的内容 */
                        text-overflow: ellipsis; /* 超出部分用省略号表示 */
                    }
                }

                .item-action {
                    flex: 1;
                    display: flex;
                    align-items: flex-end;
                    justify-content: space-between;
                    gap: 10px;

                    .item-price {
                        font-size: 20px;
                        color: var(--general);
                        line-height: 30px;
                        height: 30px;
                    }

                    .item-buy {
                        display: flex;
                        justify-content: flex-end;
                    }
                }
            }
        }
    }

    .product-card-border-2 {
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
    }

    .product-card-border-3 {
        border: 1px solid rgba(50, 50, 51, 0.1);
    }

    .product-card-border-4 {
        background-color: transparent;
    }
}

.box-radius {
    border-radius: 6px;
}

.image-box-radius {
    border-top-left-radius: 6px;
    border-top-right-radius: 6px;
}
</style>
