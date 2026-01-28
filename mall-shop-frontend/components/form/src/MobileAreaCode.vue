<template>
    <el-select
        class="mobile-area-code"
        append-to=".app"
        :teleported="true"
        v-if="list.length > 0"
        :style="customStyle"
        v-model="mobileAreaCode"
        :placeholder="placeholder"
    >
        <template v-for="item in list" :key="item">
            <el-option :label="item.label" :value="item.code">
                <span style="font-size: 13px">{{ item.label }} ({{ $t(item.name as string) }})</span>
            </el-option>
        </template>
    </el-select>
</template>

<script setup lang="ts">
import { getmobileAreaCode } from "~/api/common";
import type { MobileAreaCodeList } from "@/types/common/common.d.ts";
import { ref } from "vue";
import { computed } from "@vue/reactivity";

const props = defineProps({
    customStyle: {
        type: Object,
        default: () => ({
            width: "75px"
        })
    },
    placeholder: {
        type: String,
        default: "请选择区号"
    },
    modelValue: {
        type: String,
        default: ""
    }
});
const emit = defineEmits(["update:modelValue"]);
const mobileAreaCode = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});
const list = ref<MobileAreaCodeList[]>([]);
const loading = ref<boolean>(true);
const getmobileAreaCodeList = async () => {
    loading.value = true;
    list.value = [];
    try {
        const result = await getmobileAreaCode();
        list.value = result;
        emit("update:modelValue", result[0].code);
    } catch (error) {
        console.log(error);
    } finally {
        loading.value = false;
    }
};
getmobileAreaCodeList();
</script>

<style lang="less">
:deep(.el-select__wrapper) {
    box-shadow: none;
}

.el-select-dropdown__wrap {
    .el-select-dropdown__item {
        &:deep(.is-selected) {
            color: var(--general);
        }
    }
}
</style>
