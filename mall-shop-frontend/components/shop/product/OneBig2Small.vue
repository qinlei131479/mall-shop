<template>
    <div class="one-style">
        <template v-for="(item, index) in groupedList">
            <div class="item-car">
                <div class="big">
                    <ProductCard v-if="item[0]" :module="module" :size="1" :product="item[0]"></ProductCard>
                </div>
                <div class="small">
                    <div class="it">
                        <ProductCard v-if="item[1]" :module="module" :size="1" :product="item[1]"></ProductCard>
                    </div>
                    <div class="it">
                        <ProductCard v-if="item[2]" :module="module" :size="1" :product="item[2]"></ProductCard>
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>
<script setup lang="ts">
import ProductCard from "~/components/shop/product/ProductCard.vue";

const props = defineProps({
    module: { type: Object, default: {} },
    productList: { type: Array, default: [] }
});

const groupedList = computed(() => {
    let result = [];
    for (let i = 0; i < props.productList.length; i += 3) {
        // 每次取三个元素 push到result中，形成新的子数组
        result.push(props.productList.slice(i, i + 3));
    }
    return result;
});
</script>
<style scoped lang="less">
.one-style {
    display: flex;
    flex-direction: column;

    .item-car {
        display: flex;
        flex-direction: column;

        .big {
            flex: 1;
        }

        .small {
            display: flex;
            flex-direction: row;

            .it {
                flex: 1;
            }
        }
    }
}
</style>
