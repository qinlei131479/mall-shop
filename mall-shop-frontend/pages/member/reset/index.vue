<template>
    <CommonHeader title="找回密码"></CommonHeader>
    <CommonLoginHeader type="register"></CommonLoginHeader>
    <div class="full-height-div">
        <div class="register-info">
            <div class="title">{{ $t("找回密码") }}</div>
            <Transition name="fadex">
                <StepOne v-model:step="step" @success="getMobileKey" v-show="step === 1"></StepOne>
            </Transition>
            <Transition name="fadey">
                <StepTow v-model:step="step" v-model="mobileKey" v-show="step === 2"></StepTow>
            </Transition>
        </div>
    </div>
    <CommonPageFooter :bg="'white'" :type="0"></CommonPageFooter>
</template>

<script lang="ts" setup>
import StepOne from "./src/StepOne.vue";
import StepTow from "./src/StepTow.vue";
const mobileKey = ref("");
const step = ref(1);

const getMobileKey = (val: string) => {
    mobileKey.value = val;
};
</script>

<style lang="less" scoped>
@import "/assets/css/member/member";
.fadex-leave-active {
    transition: all 0.5s ease;
    transform: translateX(50px);
}
.fadex-leave-to {
    opacity: 0;
}

.fadey-enter-from {
    opacity: 0;
    transform: translateY(50px);
}
.fadey-enter-active {
    transition: all 0.5s ease;
    transition-delay: 0.5s;
}
.fadey-leave-to {
    opacity: 0;
    transform: translateY(0px);
}

.full-height-div {
    display: flex;
    min-height: 600px;
    background-color: white;
    justify-content: center;

    .register-info {
        width: 500px;
        margin: 50px 0;

        .title {
            font-size: 22px;
            font-weight: bold;
            text-align: center;
            margin-bottom: 50px;
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
