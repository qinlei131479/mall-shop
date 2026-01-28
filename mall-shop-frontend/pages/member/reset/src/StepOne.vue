<template>
    <div>
        <template v-if="commonStore.openEmailRegister === 1">
            <div class="register-menu">
                <div class="menu-box">
                    <template v-for="(item, index) in menuList" :key="index">
                        <div class="menu-item" :class="{ active: formState.registType === item.value }" @click="registerTypeChange(item.value)">
                            {{ item.label }}
                        </div>
                    </template>
                </div>
            </div>
        </template>
        <el-form
            ref="formRef"
            :hide-required-asterisk="true"
            :model="formState"
            :rules="formRules"
            :show-message="false"
            class="form-body"
            label-width="auto"
            @validate="validateFun"
        >
            <el-form-item v-if="formState.registType === 'mobile' || !formState.registType" prop="mobile">
                <div class="label">{{ $t("手机号") }}</div>
                <div class="area-code-input">
                    <el-input v-model="formState.mobile" class="input-border-style" @focus="focusFun('mobile')">
                        <template #prefix>
                            <div class="area-code-select" v-if="commonStore.isOpenMobileAreaCode">
                                <MobileAreaCode v-model="mobileAreaCode"></MobileAreaCode>
                            </div>
                        </template>
                    </el-input>
                </div>
                <TigTips :info="formState.tigTips.mobile"></TigTips>
            </el-form-item>
            <el-form-item v-else prop="email">
                <div class="label">{{ $t("邮箱") }}</div>
                <el-input v-model="formState.email" class="input-border-style" @focus="focusFun('email')" />
                <TigTips :info="formState.tigTips.email"></TigTips>
            </el-form-item>
            <el-form-item prop="code">
                <div class="label">{{ $t("验证码") }}</div>
                <template v-if="formState.registType === 'mobile' || !formState.registType">
                    <el-input v-model="formState.code" class="input-border-style" @focus="focusFun('code')">
                        <template #suffix>
                            <MobileCode
                                class="mobile-code-text"
                                event="forgetPassword"
                                :mobile="commonStore.isOpenMobileAreaCode ? mobileAreaCode + formState.mobile : formState.mobile"
                                v-model:mobileCodeSended="mobileCodeSended"
                                :requestApi="sendMobileCode"
                                @mobileErrorCallback="mobileErrorCallback"
                            ></MobileCode>
                        </template>
                    </el-input>
                </template>
                <template v-else>
                    <el-input v-model="formState.code" class="input-border-style" @focus="focusFun('code')">
                        <template #suffix>
                            <MobileCode
                                event="forgetPassword"
                                class="mobile-code-text"
                                :email="formState.email"
                                v-model:mobileCodeSended="mobileCodeSended"
                                :requestApi="sendEmailCode"
                                @mobileErrorCallback="mobileErrorCallback"
                            ></MobileCode>
                        </template>
                    </el-input>
                </template>
                <TigTips :info="formState.tigTips.code"></TigTips>
            </el-form-item>
            <el-form-item label="" prop="firstWord">
                <el-button :loading="loading" type="primary" class="submit-button" style="width: 330px" @click="onSubmit">{{ $t("下 一 步") }}</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import MobileAreaCode from "@/components/form/src/MobileAreaCode.vue";
import { checkMobile, sendMobileCode, sendEmailCode, checkEmail } from "@/api/user/login";
import { MobileCode } from "@/components/form";
import { ElMessage } from "element-plus";
import { useCommonStore } from "~/store/common";
import { isOverseas } from "~/utils/util";

const step = defineModel("step");

const emit = defineEmits(["success"]);

const commonStore: any = useCommonStore();
const { t } = useI18n();
const mobileCodeSended = ref(false);
const mobileAreaCode = ref("86");
const formRef = shallowRef();
const validateFun = (prop: any, isValid: boolean, message: string) => {
    if (!isValid) {
        formState.value.tigTips[prop] = { type: "error", message: message };
    } else {
        formState.value.tigTips[prop] = { type: "valid", message: message };
    }
};
const menuList = [
    { label: t("手机号找回"), value: "" },
    { label: t("邮箱找回"), value: "email" }
];

const focusFun = (val: any) => {
    formState.value.tigTips[val] = { type: "info", message: focusList[val] };
};

const validateMobile = (rule: any, value: any, callback: any) => {
    const regex = commonStore.isOpenMobileAreaCode ? /^(\+?[1-9]\d{1,14})$/ : /^(?:\+?86)?1[3-9]\d{9}$/;
    if (!value) {
        return callback(new Error(t("手机号不能为空")));
    } else if (!isOverseas() && !regex.test(value)) {
        return callback(new Error(t("格式错误，请输入正确的手机号码")));
    } else {
        callback();
    }
};
const validateMail = (rule: any, value: any, callback: any) => {
    if (!value) {
        return callback(new Error(t("邮箱不能为空")));
    } else if (!/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(value)) {
        return callback(new Error(t("格式错误，请输入正确的邮箱地址")));
    } else {
        callback();
    }
};
const validateCode = (rule: any, value: any, callback: any) => {
    if (!value) {
        return callback(new Error(t("验证码不能为空")));
    } else if (value.length != 6) {
        return callback(new Error(t("请输入6位验证码")));
    } else {
        callback();
    }
};
const registerTypeChange = (type: string) => {
    formState.value.registType = type;
    formState.value.tigTips = {};
    if (type === "email") {
        formState.value.mobile = "";
        formState.value.code = "";
        formState.value.tigTips.mobile = { type: "info", message: focusList.mobile };
    } else {
        formState.value.email = "";
        formState.value.code = "";
        formState.value.tigTips.email = { type: "info", message: focusList.email };
    }
};
const validatePassword = (rule: any, value: any, callback: any) => {
    if (!value) {
        return callback(new Error(t("密码不能为空")));
    } else if (value.length > 20 || value.length < 6) {
        return callback(new Error(t("密码应为6-20位字符")));
    } else if (/\s/.test(value)) {
        return callback(new Error(t("密码中不允许有空格")));
    } else if (/^\d+$/.test(value)) {
        return callback(new Error(t("密码不能全为数字")));
    } else if (!/^(?=.*[\d\W]).+$/.test(value)) {
        return callback(new Error(t("密码不能全为字母")));
    } else if (!/^(?=.*[a-zA-Z0-9]).+$/.test(value)) {
        return callback(new Error(t("密码不能全为符号")));
    } else if (!/^(?!([a-zA-Z0-9])\1*$).+$/.test(value)) {
        return callback(new Error(t("密码不能全为相同字符或数字")));
    } else {
        callback();
    }
};
const focusList = reactive<any>({
    mobile: t("请填写手机号码"),
    email: t("请填写邮箱地址"),
    code: t("请输入验证码"),
    password: t("6-20个大小写英文字母、符号或数字的组合")
});
const formRules = reactive<any>({
    mobile: [{ validator: validateMobile, trigger: "blur", required: true }],
    email: [{ validator: validateMail, trigger: "blur", required: true }],
    code: [{ validator: validateCode, trigger: "blur", required: true }],
    password: [{ validator: validatePassword, trigger: "blur", required: true }]
});
const loading = ref<boolean>(false);
const formState: any = ref({
    tigTips: {},
    mobile: "",
    registType: "",
    code: ""
});
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        let result;
        if (formState.value.registType === "email") {
            // 邮箱找回
            const tempEmail = {
                email: formState.value.email,
                code: formState.value.code
            };
            // 这里可以加邮箱校验接口，如有需要
            result = await checkEmail(tempEmail);
        } else {
            // 手机号找回
            const temp = {
                mobile: commonStore.isOpenMobileAreaCode ? mobileAreaCode.value + formState.value.mobile : formState.value.mobile,
                code: formState.value.code
            };
            result = await checkMobile(temp);
        }
        emit("success", result);
        step.value = 2;
    } catch (error: any) {
        ElMessage.error(error.message || t("操作失败"));
        formState.value.code = "";
        formState.value.tigTips = {};
    } finally {
        loading.value = false;
    }
};
const mobileErrorCallback = (e: any) => {
    ElMessage.error(e);
};
</script>

<style lang="less" scoped>
.form-body {
    margin-top: 30px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    gap: 15px;

    .get-check {
        width: 110px;
        background-color: #f3f3f3;
        cursor: pointer;
        margin-left: 10px;
        color: #666;
        height: 50px;
        text-align: center;
        line-height: 50px;
        border: 1px solid #dedede;
        border-radius: 4px;
    }
}

.input-border-style {
    height: 50px;
    width: 330px;
}
.area-code-input {
    :deep(.el-input__inner) {
        margin-left: 10px;
    }
}

.area-code-select {
    width: 60px;
    padding: 10px 0;
    :deep(.el-select__wrapper) {
        box-shadow: none !important;
    }
}

.label {
    position: absolute;
    left: -117px;
    width: 100px;
    text-align: right;
    color: #666;
}
.submit-button {
    height: 50px;
}

.register-menu {
    padding-left: 85px;
    padding-bottom: 10px;

    .menu-box {
        width: 330px;
        display: flex;
        align-items: center;
        border-bottom: 1px solid #dcdfe6;
        .menu-item {
            flex: 1;
            text-align: center;
            padding: 5px 0;
            font-size: 16px;
            cursor: pointer;

            &.active {
                border-bottom: 2px solid var(--general) !important;
            }
        }
    }
}
</style>
