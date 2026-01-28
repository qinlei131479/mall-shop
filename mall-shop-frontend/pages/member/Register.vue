<template>
    <CommonHeader title="注册"></CommonHeader>
    <CommonLoginHeader type="register"></CommonLoginHeader>
    <div class="full-height-div">
        <div class="register-info">
            <!-- <template v-if="commonStore.openEmailRegister === 1">
                <div class="register-menu">
                    <div class="menu-box">
                        <template v-for="(item, index) in menuList" :key="index">
                            <div class="menu-item" :class="{ active: formState.registType === item.value }" @click="registerTypeChange(item.value)">
                                {{ item.label }}
                            </div>
                        </template>
                    </div>
                </div>
            </template> -->
            <div class="title">{{ $t("会员注册") }}</div>

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
                <el-form-item v-if="!formState.registType" prop="mobile">
                    <div class="label">{{ $t("手机号") }}</div>
                    <div class="area-code-input">
                        <el-input v-model="formState.mobile" class="input-border-style" @focus="focusFun('mobile')">
                            <template #prefix v-if="commonStore.isOpenMobileAreaCode">
                                <div class="area-code-select">
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
                <el-form-item :prop="formState.registType ? 'emailCode' : 'mobileCode'">
                    <div class="label">{{ $t("验证码") }}</div>
                    <template v-if="formState.registType">
                        <el-input v-model="formState.emailCode" class="input-border-style" @focus="focusFun('emailCode')">
                            <template #suffix>
                                <MobileCode
                                    class="mobile-code-text"
                                    :email="formState.email"
                                    v-model:mobileCodeSended="mobileCodeSended"
                                    :requestApi="sendEmailCode"
                                    @mobileErrorCallback="mobileErrorCallback"
                                ></MobileCode>
                            </template>
                        </el-input>
                    </template>
                    <template v-else>
                        <el-input v-model="formState.mobileCode" class="input-border-style" @focus="focusFun('mobileCode')">
                            <template #suffix>
                                <MobileCode
                                    class="mobile-code-text"
                                    :mobile="moblieNum"
                                    v-model:mobileCodeSended="mobileCodeSended"
                                    :requestApi="sendMobileCode"
                                    @mobileErrorCallback="mobileErrorCallback"
                                ></MobileCode>
                            </template>
                        </el-input>
                    </template>
                    <TigTips :info="formState.tigTips.mobileCode"></TigTips>
                </el-form-item>
                <el-form-item prop="password">
                    <div class="label">{{ $t("设置密码") }}</div>
                    <el-input v-model="formState.password" class="input-border-style" show-password type="password" @focus="focusFun('password')" />
                    <TigTips :info="formState.tigTips.password"></TigTips>
                </el-form-item>
                <div class="extra" style="width: 330px">
                    {{ $t("点击注册，表示您同意") }}
                    <NuxtLink class="font-color" target="_blank" :to="'/article/issue/info?articleSn=zcxy'">
                        《{{ isOverseas() ? $t("{0}注册协议！", [commonStore.shopName]) : `${commonStore.shopName}注册协议！` }}》
                    </NuxtLink>
                </div>
                <el-form-item label="" prop="firstWord">
                    <el-button :loading="loading" type="primary" class="submit-button" style="width: 330px" @click="onSubmit">{{
                        $t("同意协议并注册")
                    }}</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
    <CommonPageFooter :bg="'white'" :type="0"></CommonPageFooter>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import MobileAreaCode from "@/components/form/src/MobileAreaCode.vue";
import { sendMobileCode, userRegist, sendEmailCode } from "@/api/user/regist";
import { MobileCode } from "@/components/form";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user";
import { useCommonStore } from "~/store/common";
import { isOverseas } from "@/utils/util";

const commonStore = useCommonStore();

const userStore = useUserStore();

const { t } = useI18n();

const mobileCodeSended = ref(false);

const mobileAreaCode = ref("86");

const formRef = shallowRef();

const menuList = [
    { label: t("手机号注册"), value: "" },
    { label: t("邮箱注册"), value: "email" }
];

const validateFun = (prop: any, isValid: boolean, message: string) => {
    if (!isValid) {
        formState.value.tigTips[prop] = { type: "error", message: message };
    } else {
        formState.value.tigTips[prop] = { type: "valid", message: message };
    }
};

const focusFun = (val: any) => {
    formState.value.tigTips[val] = { type: "info", message: focusList[val] };
};

const registerTypeChange = (type: string) => {
    formState.value.registType = type;
    formState.value.tigTips = {};
    if (type === "email") {
        formState.value.mobile = "";
        formState.value.mobileCode = "";
        formState.value.tigTips.mobile = { type: "info", message: focusList.mobile };
    } else {
        formState.value.email = "";
        formState.value.tigTips.email = { type: "info", message: focusList.email };
    }
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
const focusList: any = {
    mobile: t("请填写正确的手机号码，以便接收订单通知，找回密码等"),
    email: t("请填写正确的邮箱地址，以便接收订单通知，找回密码等"),
    mobileCode: t("请输入验证码"),
    password: t("6-20个大小写英文字母、符号或数字的组合")
};
const formRules = {
    mobile: [{ validator: validateMobile, trigger: "blur", required: true }],
    email: [{ validator: validateMail, trigger: "blur", required: true }],
    mobileCode: [{ validator: validateCode, trigger: "blur", required: true }],
    emailCode: [{ validator: validateCode, trigger: "blur", required: true }],
    password: [{ validator: validatePassword, trigger: "blur", required: true }]
};
const loading = ref<boolean>(false);

const formState: any = ref({
    tigTips: {},
    registType: ""
});

const moblieNum = computed(() => {
    return commonStore.isOpenMobileAreaCode ? mobileAreaCode.value + formState.value.mobile : formState.value.mobile;
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        const result = await userRegist({
            ...formState.value,
            mobile: moblieNum.value
        });
        ElMessage.success(t("注册成功"));
        userStore.setToken(result.token);
        userStore.userInfoLoaded = false;
        navigateTo("/member/");
    } catch (error: any) {
        ElMessage.error(error.message);
    } finally {
        loading.value = false;
    }
};
const mobileErrorCallback = (e: any) => {
    ElMessage.error(e);
};
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.full-height-div {
    display: flex;
    min-height: calc(100vh - 170px - 104px); /* 设置 div 的高度为视口的 100% */
    background-color: white;
    justify-content: center;

    .register-info {
        width: 500px;
        margin: 50px 0;

        .title {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
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
    }

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
</style>
