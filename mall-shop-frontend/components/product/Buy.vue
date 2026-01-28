<template>
    <div :class="'buy ' + (disabled == true ? 'disabled' : '')" @click="_addToCart">
        <slot></slot>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { addExchangeToCart, addToCart } from "~/api/product/product";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { useCartStore } from "~/store/cart";
import { useCommonStore } from "~/store/common";

const cartStore = useCartStore();

const commonStore = useCommonStore();

const router = useRouter();

const props = defineProps({
    id: Number,
    number: { type: Number },
    skuId: { type: Number },
    extraAttrIds: { type: String, default: "" },
    disabled: { type: Boolean, default: false },
    isQuick: { type: Boolean, default: false },
    productType: { type: String, default: "product" },
    skuItem: { type: Array }
});
const { t } = useI18n();
const emit = defineEmits(["callback"]);
const loading = ref(false);
const _addToCart = async () => {
    try {
        if (props.disabled) return;

        if (loading.value) return;

        loading.value = true;
        let result: any = {};
        const filterParams: any = {
            id: props.id ?? 0,
            number: props.number,
            skuId: props.skuId,
            isQuick: props.isQuick ? 1 : 0
        };
        if (props.extraAttrIds) {
            filterParams.extraAttrIds = props.extraAttrIds;
        }
        if (isB2B()) {
            filterParams.skuItem = props.skuItem;
        }
        if (props.productType == "product") {
            result = await addToCart(filterParams);
        } else if (props.productType == "exchange") {
            result = await addExchangeToCart(filterParams);
        }
        if (props.isQuick == true) {
            if (commonStore.closeOrder === 1) return message.error(t("商城已关闭下单"));

            router.push({ path: "/order/check", query: { flowType: result.flowType } });
        } else {
            cartStore.changeCartList();
            message.success(t("加入购物车成功"));
            emit("callback");
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
    return;
};
</script>
<style lang="less" scoped>
.buy {
    display: inline-flex;
}
</style>
