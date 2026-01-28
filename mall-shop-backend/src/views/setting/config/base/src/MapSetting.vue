<template>
    <div class="content_wrapper">
        <div class="title">高德地图设置</div>
        <el-form-item label="WebKey" prop="amapWebKey">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapWebKey }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapWebKey }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapWebKey = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图WebKey</div>
            </div>
        </el-form-item>
        <el-form-item label="WebSecret" prop="amapWebSecret">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapWebSecret }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapWebSecret }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapWebSecret = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图WebSecret</div>
            </div>
        </el-form-item>

        <el-form-item label="JsKey" prop="amapJsKey">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapJsKey }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapJsKey }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapJsKey = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图JsKey</div>
            </div>
        </el-form-item>
        <el-form-item label="JsSecret" prop="amapJsSecret">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapJsSecret }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapJsSecret }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapJsSecret = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图JsSecret</div>
            </div>
        </el-form-item>


        <el-form-item label="微信小程序Key" prop="amapMiniKey">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapMiniKey }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapMiniKey }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapMiniKey = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图微信小程序Key</div>
            </div>
        </el-form-item>
        <el-form-item label="微信小程序Secret" prop="amapMiniSecret">
            <div>
                <div class="flex flex-align-center" style="gap: 10px">
                    <div class="secret-txt line1" style="max-width: 380px">
                        {{ formState.amapMiniSecret }}
                    </div>
                    <DialogForm
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{ title: '火山翻译AssessKey', content: formState.amapMiniSecret }"
                        path="setting/config/src/EditSecret"
                        title="火山翻译AssessKey"
                        width="600px"
                        @okCallback="formState.amapMiniSecret = $event"
                    >
                        <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                    </DialogForm>
                </div>
                <div class="extra">编辑完成后，点击下方提交按钮确认修改，请确保填写正确的高德地图微信小程序Secret</div>
            </div>
        </el-form-item>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { AmapAllResponse } from "@/types/setting/config";
import { getConfigAmapAllSettings, saveAllAmapSettings } from "@/api/setting/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<AmapAllResponse>>({
    amapWebKey: "",
    amapWebSecret: "",
    amapJsKey: "",
    amapJsSecret: "",
    amapMiniKey: "",
    amapMiniSecret: ""
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigAmapAllSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    try {
        const result = await saveAllAmapSettings(formState.value);
        message.success("修改成功");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
defineExpose({ onSubmit });
</script>
<style lang="less" scoped>
.content {
    padding: 0 20px;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
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
