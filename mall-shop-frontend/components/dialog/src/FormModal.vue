<template>
    <MyModal
        v-model:open="visible"
        :confirm-loading="confirmLoading"
        width="800px"
        :okText="$t('确认')"
        :cancelText="$t('取消')"
        :destroyOnClose="true"
        @ok="onOk"
        :ok-button-props="{ disabled: okDisabled }"
        :footer-style="{ textAlign: 'right' }"
    >
        <a-config-provider :locale="zhCN">
            <Component
                ref="submitForm"
                @submitCallback="submitCallback"
                @show="show"
                @close="close"
                @okType="okType"
                v-bind="params"
                v-model:confirmLoading="confirmLoading"
            >
            </Component>
        </a-config-provider>
        <template #footer>
            <a-button style="margin-right: 8px" @click="close">{{ $t("取消") }}</a-button>
            <a-button :disabled="okDisabled" :loading="confirmLoading" type="primary" @click="onOk">{{ $t("确认") }}</a-button>
        </template>
    </MyModal>
</template>
<script setup lang="ts">
import { ref, defineAsyncComponent, h, toRaw, Comment } from "vue";
import zhCN from "ant-design-vue/es/locale/zh_CN";
import { Modal } from "ant-design-vue";
import { Drawer } from "ant-design-vue";

const props = defineProps({
    path: String, //路径
    params: Object, //绑定给页面的参数
    okCallback: Function,
    isDrawer: {
        type: Boolean,
        default: false
    }
});
const MyModal = props.isDrawer == true ? Drawer : Modal;

// const modules: any = import.meta.glob('../../../views/*/.vue')
// const Component = defineAsyncComponent(modules[`../../../views/${props.path}.vue`])

// 加载多级目录，其中**代表多级
const components = import.meta.glob("../../../views/**/*.vue");
// 异步加载动态组件
const Component = defineAsyncComponent(components["../../../views/" + props.path + ".vue"]);

const visible = ref<boolean>(false);
const confirmLoading = ref<boolean>(false);
const okDisabled = ref<boolean>(false);
const params = Object.assign(props.params, { isDialog: true });
function show(e) {
    visible.value = true;
}
function close(e) {
    visible.value = false;
}
const submitForm = ref();

const submitCallback = (result: any) => {
    if (!result.errcode) {
        // 如果返回结果无报错，关闭弹窗
        visible.value = false;
        // 执行父组件传来的确认回调，如刷新列表
        props.okCallback(result);
    }
};
//
const onOk = () => {
    // 执行子组件的表单提交方法，方法名称固定为onFormSubmit
    submitForm.value.onFormSubmit();
};

const okType = (type: boolean) => {
    okDisabled.value = !type;
};

defineExpose({
    show,
    close
});
</script>
