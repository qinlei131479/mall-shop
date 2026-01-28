<template>
    <CommonHeader title="站内信"></CommonHeader>
    <CommonPageHeader></CommonPageHeader>
    <div class="message_center container flex">
        <div class="mc_side_bar">
            <h3 class="title">
                <i class="iconfont-pc icon-xiaoxi"></i>
                <span class="fl">{{ $t("消息中心") }}</span>
            </h3>
            <div class="nav_list">
                <i class="icon"></i>
                <span class="fl">{{ $t("账户信息") }}</span>
            </div>
        </div>
        <div class="msg_list">
            <div class="flex justify-end">
                <el-button @click="unreadReadFn">{{ $t("只看未读") }}</el-button>
                <el-button @click="addMessageAllReadFn">{{ $t("全部标记为已读") }}</el-button>
            </div>
            <div class="list">
                <template v-if="total > 0">
                    <div class="item flex" v-for="item in filterState" @click="readItem(item)">
                        <div class="time">{{ item.addTimeDate }}</div>
                        <div class="msg_info flex" :class="item.isRead ? 'unread' : ''">
                            <div class="icon msg"></div>
                            <div class="message flex justify-between">
                                <div>
                                    <h3 class="title flex align-center">
                                        <div class="icon"></div>
                                        <span>{{ item.title }}</span>
                                    </h3>
                                    <p class="txt">{{ item.content }}</p>
                                </div>
                                <div class="flex flex-column align-end" style="min-width: 50px">
                                    <p class="date">{{ item.addTimeHms }}</p>
                                    <i class="iconfont-pc icon-changyonggoupiaorenshanchu del" @click.stop="delMessageFn({ id: item.messageId })"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <el-empty :description="$t('暂无消息')" />
                </template>
                <div class="el-page flex justify-end" v-if="filterState.length > 0 && !loading">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref, onMounted } from "vue";
import { Pagination } from "~/components/list";
import { getMessageList, delMessage, addMessageRead, addMessageAllRead } from "~/api/user/userMessage";
import type { UserMsgFilterParams, UserMsgFilterState } from "~/types/user/userMessage.d";
import { urlFormat } from "~/utils/format";
// 用于存储查询条件
const filterState = ref<UserMsgFilterState[]>([]);
// 用于存储加载状态
const loading = ref<boolean>(true);
// 用于存储订单总数
const total = ref<number>(0);
// 用于存储查询参数
const filterParams = reactive<UserMsgFilterParams>({
    //初使化用于查询的参数
    page: 1,
    unread: 0
});
definePageMeta({
    middleware: "auth"
});
const loadFilter = async () => {
    // 设置加载状态
    // filterState.value = [];
    loading.value = true;
    try {
        const result = await getMessageList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        // 处理错误信息
        message.error(error.message);
    } finally {
        // 设置加载状态
        loading.value = false;
    }
};
const unreadReadFn = async () => {
    filterParams.unread = 1;
    try {
        await loadFilter();
    } catch (error: any) {
        // 处理错误信息
        message.error(error.message);
    }
};
const delMessageFn = async (data: Object) => {
    try {
        await delMessage(data);
        await loadFilter();
    } catch (error: any) {
        // 处理错误信息
        message.error(error.message);
    }
};
const readItem = async (data: UserMsgFilterState) => {
    try {
        if (data.isRead !== 1) {
            await addMessageRead(data.messageId);
            data.isRead = 1;
        }
        navigateTo(urlFormat(data.link), { open: { target: "_blank" } });
    } catch (error: any) {
        // 处理错误信息
        // message.error(error.message);
    }
};
const addMessageAllReadFn = async () => {
    try {
        await addMessageAllRead();
        filterParams.unread = 0;
        await loadFilter();
    } catch (error: any) {
        // 处理错误信息
        message.error(error.message);
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.message_center {
    .mc_side_bar {
        width: 220px;
        flex-shrink: 0;
        .title {
            height: 55px;
            color: #333;
            font-size: 1pc;
            display: flex;
            align-items: flex-end;
            i {
                font-size: 20px;
                margin-right: 8px;
                color: var(--icon);
                font-weight: 700;
            }
        }
        .nav_list {
            position: relative;
            overflow: hidden;
            margin: 20px 50px 0 0;
            padding-left: 40px;
            // border-bottom: 2px solid #fafafa;
            // color: #999;
            line-height: 36px;
            border-bottom: 2px solid var(--general);
            color: var(--general);
        }
        .nav_list::before {
            content: "";
            position: absolute;
            top: 50%; /* 使小三角形垂直居中 */
            left: 0; /* 放置在容器的左侧 */
            transform: translateY(-50%) rotate(180deg); /* 旋转180度 */
            width: 0;
            height: 0;
            border-top: 6px solid transparent; /* 设置三角形的样式 */
            border-bottom: 6px solid transparent;
            border-right: 6px solid var(--icon); /* 设置三角形的颜色 */
        }
    }
    .msg_list {
        flex: 1;
        margin-top: 20px;
        background-color: #fff;
        padding: 20px;
        .btn {
            padding: 8px 15px;
            margin: 10px 0 0 10px;
            border: 1px solid #e7e7e7;
            background-color: #fff;
            color: #666;
            text-align: center;
        }
        .list {
            margin-top: 20px;
            .item {
                margin-bottom: 15px;
                .time {
                    width: 110px;
                    font-size: 1pc;
                    margin-right: 20px;
                }
                .msg_info {
                    &:hover {
                        box-shadow: 0 0 4px #e7e7e7;
                    }
                    position: relative;
                    margin-top: 5px;
                    .msg {
                        width: 9px;
                        height: 9px;
                        background: url(/assets/images/user/mcSprit.png) no-repeat -90px 0;
                        position: absolute;
                        top: 0;
                        left: -5px;
                    }
                    .message {
                        background-color: #f4f4f4;
                        cursor: pointer;
                        padding: 18px 20px 15px;
                        width: 800px;
                        border: 1px solid #e7e7e7;
                        border-radius: 4px;
                        .title {
                            font-size: 14px;
                            font-weight: bold;
                            color: rgb(51, 51, 51);
                            position: relative;
                            .icon {
                                width: 4px;
                                height: 4px;
                                background: url(/assets/images/user/mcSprit.png) no-repeat -5pc 0;
                                position: absolute;
                                left: -10px;
                            }
                        }
                        .txt {
                            margin-right: 14px;
                            color: #666;
                            line-height: 18px;
                            margin-top: 5px;
                        }
                        .del {
                            width: 11px;
                            height: 14px;
                            margin-right: 10px;
                            margin-top: 15px;
                            opacity: 0;
                            cursor: pointer;
                        }
                        .del:hover {
                            color: var(--general);
                        }
                        &:hover {
                            .del {
                                opacity: 1;
                            }
                        }
                    }
                }
                .unread {
                    .msg {
                        background-position: -75pt 0;
                    }
                    .message {
                        background-color: #fff;
                        .title {
                            font-weight: 500;
                            .icon {
                                display: none;
                            }
                        }
                    }
                }
            }
            .el-page {
                margin-top: 20px;
            }
        }
    }
}
</style>
