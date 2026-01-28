<template>
    <div class="tab">
        <div class="tab-item" :class="{ active: tabIndex === 0 }" @click="handleTabChange(0)">
            {{ $t("单品采购") }}
            <img class="image" style="right: 0" v-show="tabIndex === 1" src="@/assets/images/product/shadow-right.png" />
        </div>
        <div class="tab-item" :class="{ active: tabIndex === 1 }" @click="handleTabChange(1)">
            {{ $t("批量采购") }}
            <img class="image" style="left: 0" v-show="tabIndex === 0" src="@/assets/images/product/shadow-left.png" />
        </div>
        <div class="line" :style="`${tabIndex === 0 ? 'left: 0' : 'right: 0'}`"></div>
        <div class="subline" :style="`${tabIndex === 0 ? 'right: 0' : 'left: 0'}`"></div>
    </div>
</template>

<script setup lang="ts">
const tabIndex = defineModel("tabIndex", { type: Number });
const handleTabChange = (index: number) => {
    tabIndex.value = index;
};
</script>

<style lang="less" scoped>
.tab {
    width: 100%;
    display: flex;
    position: relative;
    margin-top: 10px;
    cursor: pointer;
    .line,
    .subline {
        position: absolute;
        width: 50%;
        height: 2px;
        background-color: var(--general);
    }
    .line {
        top: 0;
    }
    .subline {
        bottom: 0;
        height: 1px;
        background-color: #eee;
    }

    .tab-item {
        width: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 42px;
        font-size: 14px;
        font-weight: 500;
        position: relative;

        .image {
            width: 10px;
            height: 100%;
            position: absolute;
            top: 0;
        }
        &.active {
            color: var(--general);
        }
    }
}
</style>
