<template>
    <div class="form_item">
        <div class="top_row">
            <div class="tit">{{ $t("确认支付方式") }}</div>
        </div>
        <div class="pay_type flex align-center">
            <template v-if="paymentTypeList.length > 0">
                <template v-for="(item, index) in paymentTypeList">
                    <div
                        v-if="item.isShow"
                        class="pay_tab flex align-center"
                        :class="{ disabled: item.disabled, active: item.typeId === payTypeId }"
                        @click="!item.disabled && onChangePayType(item.typeId)"
                    >
                        <span>{{ item.typeName }}</span>
                        <Tips v-if="item.disabledDesc != '' && item.disabled" top>
                            <div>{{ item.disabledDesc }}</div>
                        </Tips>
                        <i class="icon-gou"></i>
                    </div>
                </template>
            </template>

            <span class="errInfo red" v-if="payTypeId === 0" style="display: inline; margin: 0; line-height: 15px">
                {{ $t("请选择支付方式") }}
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { PaymentTypeItem } from "@/types/order/check";
import { Tips } from "@/components/tips";

const props = defineProps({
    payTypeId: {
        type: Number,
        required: true,
        default: 0
    },
    paymentTypeList: {
        type: Array as PropType<PaymentTypeItem[]>,
        required: true,
        default: () => []
    }
});

const emit = defineEmits(["changePayType"]);

const onChangePayType = (id: number) => {
    emit("changePayType", id);
};
</script>

<style lang="less" scoped></style>
