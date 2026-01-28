<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="120px" style="margin-left: 22px">
                <div class="content_wrapper">
                    <div class="title" id="weChatofficialaccount">猜你喜欢</div>
                    <el-form-item label="自定义名称" prop="guestLikeGoodsName">
                        <div>
                            <TigInput width="300px" v-model="formState.guestLikeGoodsName" :maxlength="10" showWordLimit />
                            <div class="extra">该名称会显示在移动端买家个人中心及购物车下方，默认名称“猜你喜欢”</div>
                        </div>
                    </el-form-item>
                    <el-form-item label="猜你喜欢" prop="isGuestLikeGoods">
                        <div>
                            <el-checkbox v-model="formState.isGuestLikeGoods" label="启用猜你喜欢" :true-label="1" :false-label="0" />
                            <div class="extra">启用后，移动端买家商城个人中心及购物车将会显示此模块</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 30px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">提 交 </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { PersonalizedFormState } from "@/types/setting/config";
import { getPersonalizedSetting, savePersonalizedSetting } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useRoute } from "vue-router";
import { useThemeStore } from "@/store/theme";
const { themeInfo } = useThemeStore();
const configStore = useConfigStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const formState = ref<PersonalizedFormState>({
    isGuestLikeGoods: 0,
    guestLikeGoodsName: ""
});

// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getPersonalizedSetting();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await savePersonalizedSetting(formState.value);
        message.success("修改成功");
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-top: 20px;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}
.error {
    color: red;
}
</style>
