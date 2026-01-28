<template>
    <div class="b2b-sku">
        <div class="b2b-sku-content" :style="{ height: height }">
            <template v-for="item in b2bSkuList" :key="item">
                <div class="sku-item">
                    <div class="title">{{ item.title }}</div>
                    <div class="price-box">
                        <FormatPrice :fontStyle="{ fontSize: '13px', color: '#666', 'font-weight': 700 }" v-model="item.price"></FormatPrice>
                        <div class="stock">{{ item.stock }}件可售</div>
                    </div>
                    <div class="num">
                        <InputNumber v-model="item.num" :max="item.stock" :min="0" @change="handleNumberChange"></InputNumber>
                    </div>
                </div>
            </template>
        </div>
        <template v-if="props.b2bSkuList.length > 4 && !show">
            <img @click="show = !show" class="sku-wrapper-expend-button" src="@/assets/images/product/pulldown_sku.png" alt="" srcset="" />
        </template>
    </div>
</template>

<script setup lang="ts">
import { InputNumber } from "@/components/product";
import { getProductAmount } from "@/api/product/product";
import type { ProductAmountItem, SkuList } from "@/types/product/product.d.ts";

const props = defineProps({
    b2bSkuList: {
        type: Array as PropType<SkuList[]>,
        default: []
    },
    id: {
        type: Number
    },
    skuItem: {
        type: Array,
        default: []
    },
    selectSkus: {
        type: Array,
        default: []
    }
});
const productAmount = defineModel<ProductAmountItem>("productAmount", {
    type: Object as PropType<ProductAmountItem>,
    default: () => ({
        count: 0,
        total: "0"
    })
});
const show = ref(false);
const height = computed(() => {
    let length = props.b2bSkuList.length;
    let num = length < 5 ? length * 63 : 4 * 63;

    if (length > 4 && show.value) {
        num = length * 63;
    }
    return num + "px";
});

const __getProductAmount = async () => {
    try {
        const result = await getProductAmount({
            id: props.id,
            skuItem: props.skuItem
        });
        productAmount.value = result;
    } catch (error) {
        console.error(error);
    }
};

watchEffect(() => {
    __getProductAmount();
});

const emit = defineEmits(["update:skuItem", "update:selectSkus"]);

let delayTimer: NodeJS.Timeout | null = null; // 延时定时器
const handleNumberChange = () => {
    if (delayTimer) {
        clearTimeout(delayTimer);
    }
    delayTimer = setTimeout(() => {
        const skus = props.b2bSkuList.filter((item: any) => item.num);
        const data = skus.map((item: any) => {
            return {
                skuId: item.skuId,
                num: item.num
            };
        });
        emit("update:skuItem", data);
        const data2 = skus.map((item: any) => {
            return {
                skuData: item.skuData,
                num: item.num
            };
        });
        emit("update:selectSkus", data2);
    }, 300);
};
</script>

<style lang="less" scoped>
.b2b-sku {
    margin-bottom: 35px;
    position: relative;
    .b2b-sku-content {
        overflow: hidden;
        transition: height 0.3s ease-in-out;
    }
    .sku-item {
        display: flex;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;
        font-size: 13px;
        color: #666;
        word-break: break-all;
        font-weight: 700;
        .title {
            flex: 2;
        }
        .price-box {
            flex: 1;
            display: flex;
            align-items: center;
            .stock {
                padding-left: 20px;
                font-weight: 500;
                color: #999;
            }
        }
        .num {
            flex: 1;
        }
    }
    .sku-wrapper-expend-button {
        cursor: pointer;
        width: 60px;
        height: 20px;
        position: absolute;
        bottom: -18px;
        left: 88px;
    }
}
</style>
