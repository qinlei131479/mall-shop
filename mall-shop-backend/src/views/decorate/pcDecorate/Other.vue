<template>
    <div class="container">
        <div class="content_wrapper decoratePgaeWrap">
            <div class="top-wrapper">
                <div class="flex">
                    <div class="left-box tab-name" @click="type = 'pcUserLogin'">
                        <div :class="'left-cont tab-item ' + (type == 'pcUserLogin' ? 'current' : '')">会员登录背景</div>
                    </div>
                    <div class="left-box tab-name" @click="type = 'PcIndexTips'" v-if="isOverseas()">
                        <div :class="'left-cont tab-item ' + (type == 'PcIndexTips' ? 'current' : '')">首页顶部提示</div>
                    </div>
                </div>
            </div>
            <div class="flex box-wrapper">
                <div class="left-wrapper">
                    <div class="left-box tab-name" @click="type = 'pcUserLogin'">
                        <div :class="'left-cont tab-item ' + (type == 'pcUserLogin' ? 'current' : '')">会员登录背景</div>
                    </div>
                    <div class="left-box tab-name" @click="type = 'PcIndexTips'" v-if="isOverseas()">
                        <div :class="'left-cont tab-item ' + (type == 'PcIndexTips' ? 'current' : '')">首页顶部提示</div>
                    </div>
                </div>
                <div class="tab-box" v-if="type == 'pcUserLogin'">
                    <PcUserLogin></PcUserLogin>
                </div>
                <div class="tab-box" v-if="type == 'PcIndexTips' && isOverseas()">
                    <PcIndexTips></PcIndexTips>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { isOverseas } from "@/utils/version";
import { ref } from "vue";
import PcUserLogin from "./src/PcUserLogin.vue";
import PcIndexTips from "./src/PcIndexTips/Index.vue";
const type = ref("pcUserLogin");
</script>
