<template>
    <el-dialog v-model="dialogTableVisible" :title="$t('新增留言')" width="600">
        <el-form ref="formRef" :model="formState" class="form-body" label-suffix="：" label-width="auto">
            <el-form-item v-if="type" :label="$t('订单编号')" prop="type">
                {{ router.currentRoute.value.query.orderSn }}
            </el-form-item>
            <el-form-item v-else :rules="[{ required: true, message: $t('请选择留言类型!') }]" :label="$t('留言类型')" prop="type">
                <el-radio-group v-model="formState.type">
                    <el-radio :value="0">{{ $t("建议") }}</el-radio>
                    <el-radio :value="1">{{ $t("投诉") }}</el-radio>
                    <el-radio :value="3">{{ $t("其他") }}</el-radio>
                    <el-radio :value="4">{{ $t("店铺投诉") }}</el-radio>
                    <el-radio :value="5">{{ $t("订单问题") }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :rules="[{ required: true, message: $t('请输入主题') }]" :label="$t('主题')" prop="title">
                <el-input v-model="formState.title" clearable :placeholder="$t('请输入主题')" />
            </el-form-item>
            <el-form-item :rules="[{ required: true, message: $t('请输入内容') }]" :label="$t('留言内容')" prop="content">
                <el-input v-model="formState.content" :row="4" clearable :placeholder="$t('请输入内容')" type="textarea" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="onSubmit">{{ $t("提交留言") }}</el-button>
            </div>
        </template>
    </el-dialog>
    <div @click="show">
        <slot></slot>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import type { FeedbackFormState } from "~/types/user/feedback";
import { updateFeedback } from "~/api/user/feedback";
import { useRouter } from "vue-router";
const router = useRouter();
const emit = defineEmits(["loadFilter"]);
const props = defineProps({
    orderId: {
        type: Number,
        default: 0
    },
    type: {
        type: String,
        default: ""
    }
});
definePageMeta({
    middleware: "auth"
});

const { t } = useI18n();

const loading = ref<boolean>(true);
const formState = ref<FeedbackFormState>({});
const formRef = shallowRef();
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        if (props.type) {
            formState.value.orderId = Number(router.currentRoute.value.query.orderId);
            formState.value.type = 6;
        }
        const result = await updateFeedback({ ...formState.value });
        await formRef.value.resetFields();
        message.success(t("提交成功"));
        dialogTableVisible.value = false;
        emit("loadFilter");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const dialogTableVisible = ref(false);
const show = async () => {
    dialogTableVisible.value = true;
    if (formRef.value) await formRef.value.resetFields();
};
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";
</style>
