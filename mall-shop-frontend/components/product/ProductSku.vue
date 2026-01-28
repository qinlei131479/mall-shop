<template>
    <div class="product-sku-info" v-if="attrList.length > 0">
        <div class="sku-item flex align-start" v-for="(item, index) in attrList">
            <div class="tit">{{ item.attrName }}：</div>
            <div class="sku-tag flex-wrap">
                <template v-for="(value, i) in item.attrList">
                    <div
                        :class="{
                            tag: true,
                            checked: value.checked,
                            disabled: !value.checked && value.skuStock == 0,
                            outstock: value.skuStock == 0
                        }"
                        @click="onChecked(index, i)"
                    >
                        <img v-if="index === 0 && value.attrPic" :src="imageFormat(value.attrPic)" alt="" />
                        <p>{{ value.attrValue }}</p>
                        <i class="icon-gou"></i>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup>
import { imageFormat } from "@/utils/format";
const emit = defineEmits(["change"]);

const props = defineProps({
    modelValue: {
        type: Object,
        default: {}
    },
    skuList: {
        type: Array
    },
    checkedValue: {
        type: Array
    },
    productType: {
        type: String,
        default: "product"
    }
});

const attrList = computed(() => props.modelValue.spe);
const checkedValue = ref(props.checkedValue);
const updateSelectedValue = () => {
    if (attrList.value.length > 0) {
        if (checkedValue.value.length == 0) {
            attrList.value.forEach((item) => {
                checkedValue.value.push(item.attrList[0].attrName + ":" + item.attrList[0].attrValue);
            });
        }
        attrList.value.forEach((item, index) => {
            item.attrList.forEach((value) => {
                if (checkedValue.value[index] && checkedValue.value[index] == value.attrName + ":" + value.attrValue) {
                    value.checked = true;
                } else {
                    value.checked = false;
                }
                let skuValue = JSON.parse(JSON.stringify(checkedValue.value));
                skuValue[index] = value.attrName + ":" + value.attrValue;
                let result = props.skuList.find((item) => item.skuValue == skuValue.join("|"));
                value.skuStock = result ? result.skuStock : 0;
            });
        });
        const selectedItem = props.skuList.find((item) => item.skuValue == checkedValue.value.join("|"));
        emit("change", {
            skuStock: (selectedItem && selectedItem.skuStock) || 0,
            skuId: (selectedItem && selectedItem.skuId) || 0
        });
    } else if (props.skuList.length > 0) {
        emit("change", {
            skuStock: props.skuList[0].skuStock || 0,
            skuId: props.skuList[0].skuId || 0
        });
    } else {
        emit("change", null);
    }
};
updateSelectedValue();
const onChecked = (index, i) => {
    if (props.productType != "product") {
        return;
    }
    const item = attrList.value[index].attrList[i];
    checkedValue.value[index] = item.attrName + ":" + item.attrValue;
    updateSelectedValue();
};
</script>
<style lang="less" scoped>
.outstock {
    color: #c5c5c5 !important;
}
.product-sku-info {
    padding: 15px 1px 0 0;

    .sku-item {
        margin-bottom: 25px;

        .tit {
            color: #999;
            height: 33px;
            line-height: 32px;
            padding-right: 10px;
        }

        .sku-tag {
            flex: 1;
            gap: 10px;
            .tag {
                background-color: #ffffff;
                border: 1px solid #e7e7e7;
                color: #333;
                height: 30px;
                line-height: 32px;
                padding: 0 6px;
                cursor: pointer;
                min-width: 30px;
                text-align: center;
                position: relative;
                justify-content: center;
                display: flex;
                align-items: center;
                img {
                    width: 20px;
                    height: 20px;
                    margin-right: 5px;
                }
            }

            .checked {
                border-color: var(--general);
                color: #333;

                .icon-gou {
                    display: block;
                }
            }

            .disabled {
                border: 1px dashed #eee;
                color: #999;
            }
        }
    }
}
</style>
