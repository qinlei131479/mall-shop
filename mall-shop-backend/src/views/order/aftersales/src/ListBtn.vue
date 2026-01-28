<template>
    <DialogForm
        :params="{ act: 'detail', id: item.aftersaleId }"
        isDrawer
        path="order/aftersales/Info"
        :title="showDetail ? ('售后详情 ' + item.aftersalesSn) : ('处理售后申请 ' + item.aftersalesSn)"
        width="800px"
        @okCallback="emit('callback')"
        :showClose="false"
        :showOnOk="false"
    >
        <el-button size="small" text :type="showDetail ? 'primary' : 'danger'">
            {{ showDetail ? '售后详情' : '处理退款' }}
        </el-button>
    </DialogForm>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { DialogForm } from "@/components/dialog";
import { isS2b2c } from "@/utils/version";

const adminType = localStorage.getItem("adminType");
const emit = defineEmits(["callback"]);

const props = defineProps({
    item: {
        type: Object,
        default: {}
    }
});

// 计算是否显示详情
const showDetail = computed(() => {
    if (props.item.vendorId && props.item.vendorId > 0 && isS2b2c()) {
        // S2b2c 模式下的判断逻辑
        if ((props.item.status === 4 || props.item.status === 21 || props.item.status === 22) && adminType !== 'vendor') {
            return true;
        } else if (props.item.status === 5 || props.item.status === 6 || props.item.status === 7) {
            return true;
        }
        return false;
    } else {
        // 非 S2b2c 模式下的判断逻辑
        if (props.item.status === 3 || props.item.status === 6 || props.item.status === 7) {
            return true;
        }
        return false;
    }
});
</script>