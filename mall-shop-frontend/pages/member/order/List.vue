<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="我的订单"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs">
                <span>{{ $t("我的订单") }}</span>
            </div>
            <div class="page-info-body">
                <MemberOrderList></MemberOrderList>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
definePageMeta({
    middleware: "auth"
});
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";
</style>
