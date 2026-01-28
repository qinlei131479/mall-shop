<template>
    <div class="product-sku-info" v-if="modelValue && modelValue.extra.length > 0">
        <div class="sku-item flex align-start" v-for="(item, index) in modelValue.extra" :key="item">
            <div class="tit">{{ item.attrName }}：</div>
            <div class="sku-tag flex-wrap">
                <template v-for="(subItem, i) in item.attrList" :key="subItem.attributesId">
                    <div
                        @click.stop="handleAttrClick(index, subItem.attributesId)"
                        :class="{
                            tag: true,
                            checked: checkedAttrs[index] === subItem.attributesId
                        }"
                    >
                        <p>{{ subItem.attrValue }}</p>
                        <FormatPrice class="price" v-model="subItem.attrPrice"></FormatPrice>
                        <i class="icon-gou"></i>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
const emit = defineEmits(["change"]);

const props = defineProps({
    modelValue: {
        type: Object,
        default: {}
    }
});

const checkedAttrs = ref<any[]>([]);
const handleAttrClick = (index: number, id: number) => {
    if (checkedAttrs.value[index] && checkedAttrs.value[index] === id) {
        checkedAttrs.value[index] = null;
    } else {
        checkedAttrs.value[index] = id;
    }
    emit("change", checkedAttrs.value.length > 0 ? checkedAttrs.value.filter((item) => !!item).join(",") : "");
};
</script>
<style lang="less" scoped>
.outstock {
    color: #c5c5c5 !important;
}
.product-sku-info {
    padding: 0px 1px 0 0;
    margin-bottom: 25px;
    .sku-item {
        margin-bottom: 5px;
        .tit {
            margin-bottom: 0.625rem;
            height: 33px;
            line-height: 32px;
            padding-right: 10px;
            color: #999;
        }

        .price {
            padding-left: 10px;
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
