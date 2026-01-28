<template>
    <div>
        <CommonHeader title="支付宝支付"></CommonHeader>
        <div ref="formContainer"></div>
    </div>
</template>
<script lang="ts" setup>
import { creatPay, orderPayInfo, checkPayStatus } from "~/api/order/pay";
const route = useRoute();
const id = ref(route.query.id);
const formContainer = ref<HTMLDivElement | null>(null);
const htmlContent = ref();
const _creatPay = async () => {
    try {
        const result = await creatPay({
            id: id.value,
            type: "alipay"
        });
        htmlContent.value = result.payInfo.html;
        if (formContainer.value !== null) {
            formContainer.value.innerHTML = htmlContent.value;
            const forms = document.forms;
            forms && forms[0].submit();
        }
    } catch (error: any) {
    } finally {
    }
};
_creatPay();
</script>
<style lang="less" scoped></style>
