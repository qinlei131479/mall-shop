<template>
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
        <el-form-item prop="password">
            <div class="label">{{ $t("新密码") }}</div>
            <el-input v-model="formState.password" class="input-border-style" show-password type="password" @focus="focusFun('password')" />
            <TigTips :info="formState.tigTips.password"></TigTips>
        </el-form-item>
        <el-form-item prop="password2">
            <div class="label">{{ $t("确认新密码") }}</div>
            <el-input v-model="formState.password2" class="input-border-style" show-password type="password" @focus="focusFun('password2')" />
            <TigTips :info="formState.tigTips.password2"></TigTips>
        </el-form-item>
        <el-form-item label="" prop="firstWord">
            <el-button :loading="loading" type="primary" class="submit-button" style="width: 330px" @click="onSubmit">{{ $t("确 认") }}</el-button>
        </el-form-item>
    </el-form>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { forgetPassword } from "@/api/user/login";
import { ElMessage } from "element-plus";

const step = defineModel("step");
const mobileKey = defineModel({ type: String });

const { t } = useI18n();
const formRef = shallowRef();
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

const validatePasswordAgain = (rule: any, value: any, callback: any) => {
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
    } else if (value != formState.value.password) {
        return callback(new Error(t("两次输入密码不一致")));
    } else {
        callback();
    }
};
const focusList = reactive<any>({
    password: t("6-20个大小写英文字母、符号或数字的组合"),
    password2: t("请输入和上次相同的密码")
});
const formRules = reactive<any>({
    password: [{ validator: validatePassword, trigger: "blur", required: true }],
    password2: [{ validator: validatePasswordAgain, trigger: "blur", required: true }]
});
const loading = ref<boolean>(false);
const formState: any = ref({
    tigTips: {},
    password: "",
    password2: ""
});
const router = useRouter();
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        const result = await forgetPassword({
            mobileKey: mobileKey.value,
            password: formState.value.password //新密码
        });
        ElMessage.success(t("重置密码成功"));
        router.push("/member/login");
    } catch (error: any) {
        ElMessage.error(error.message);
        step.value = 1;
        mobileKey.value = "";
        formState.value.password = "";
        formState.value.password2 = "";
    } finally {
        loading.value = false;
    }
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
</style>
