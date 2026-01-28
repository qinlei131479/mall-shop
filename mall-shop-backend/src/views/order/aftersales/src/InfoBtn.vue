<template>
    <div class="btn flex" v-if="type != 2">
        <!-- 退货退款按钮 -->
        <div v-if="formState.aftersaleType == 1">
            <template v-if="isS2b2c()">
                <DialogForm
                    v-if="(formState.status === 1 && adminType !== 'vendor') || (formState.status === 21 && adminType === 'vendor')"
                    :params="{ status: 2, formData: formState }"
                    isDrawer
                    path="order/aftersales/Handle"
                    title="处理售后"
                    width="600px"
                    :showClose="false"
                    :showOnOk="false"
                    @okCallback="emit('callback')"
                >
                    <el-button type="primary" style="margin-right: 10px">
                        {{ adminType === 'vendor' ? '同意并发送退货地址' : '同意退货退款' }}
                    </el-button>
                </DialogForm>
                <DialogForm
                    v-if="(formState.status === 1 && adminType !== 'vendor') || (formState.status === 21 && adminType === 'vendor')"
                    :params="{ status: 3, formData: formState }"
                    isDrawer
                    path="order/aftersales/Handle"
                    title="处理售后"
                    width="600px"
                    :showClose="false"
                    :showOnOk="false"
                    @okCallback="emit('callback')"
                >
                    <el-button> 拒绝退货退款 </el-button>
                </DialogForm>
            </template>
            <template v-else>
                <DialogForm
                    v-if="formState.status === 1"
                    :params="{ status: 2, formData: formState }"
                    isDrawer
                    path="order/aftersales/Handle"
                    title="处理售后"
                    width="600px"
                    :showClose="false"
                    :showOnOk="false"
                    @okCallback="emit('callback')"
                >
                    <el-button type="primary" style="margin-right: 10px"> 同意并发送退货地址 </el-button>
                </DialogForm>
                <DialogForm
                    v-if="formState.status === 1"
                    :params="{ status: 3, formData: formState }"
                    isDrawer
                    path="order/aftersales/Handle"
                    title="处理售后"
                    width="600px"
                    :showClose="false"
                    :showOnOk="false"
                    @okCallback="emit('callback')"
                >
                    <el-button> 拒绝退货退款 </el-button>
                </DialogForm>
            </template>
        </div>

        <!-- 仅退款按钮 -->
        <div v-if="formState.aftersaleType == 2">
            <DialogForm
                v-if="isS2b2c() ? (formState.status === 1 || formState.status === 21) : formState.status === 1"
                :params="{ status: 2, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                title="处理售后"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button type="primary" style="margin-right: 10px"> 同意仅退款 </el-button>
            </DialogForm>
            <DialogForm
                v-if="isS2b2c() ? (formState.status === 1 || formState.status === 21) : formState.status === 1"
                :params="{ status: 3, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                title="处理售后"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button> 拒绝仅退款 </el-button>
            </DialogForm>
        </div>

        <!-- 关闭售后按钮 -->
        <div v-if="formState.status == 3">
            <el-popconfirm 
                width="220" 
                confirm-button-text="确认" 
                cancel-button-text="取消" 
                title="确认永久关闭售后?" 
                @confirm="emit('closeOrder')"
            >
                <template #reference>
                    <el-button style="margin-left: 10px"> 关闭售后 </el-button>
                </template>
            </el-popconfirm>
        </div>

        <!-- 确认收货按钮 -->
        <div v-if="formState.aftersaleType == 1">
            <template v-if="isS2b2c()">
                <div v-if="formState.vendorId">
                    <el-popconfirm 
                        v-if="(formState.status == 5 && adminType !== 'vendor') || (formState.status == 22 && adminType === 'vendor')"
                        width="220" 
                        confirm-button-text="确认" 
                        cancel-button-text="取消" 
                        title="确认已收到货?" 
                        @confirm="emit('confirmReceipt')"
                    >
                        <template #reference>
                            <el-button type="primary"> 确认收货 </el-button>
                        </template>
                    </el-popconfirm>
                </div>
                <div v-else>
                    <el-popconfirm 
                        v-if="formState.status == 5"
                        width="220" 
                        confirm-button-text="确认" 
                        cancel-button-text="取消" 
                        title="确认已收到货?" 
                        @confirm="emit('confirmReceipt')"
                    >
                        <template #reference>
                            <el-button type="primary"> 确认收货 </el-button>
                        </template>
                    </el-popconfirm>
                </div>
            </template>
            <template v-else>
                <el-popconfirm 
                    v-if="formState.status == 5"
                    width="220" 
                    confirm-button-text="确认" 
                    cancel-button-text="取消" 
                    title="确认已收到货?" 
                    @confirm="emit('confirmReceipt')"
                >
                    <template #reference>
                        <el-button type="primary"> 确认收货 </el-button>
                    </template>
                </el-popconfirm>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { isS2b2c } from "@/utils/version";

const adminType = ref(localStorage.getItem("adminType"));
const emit = defineEmits(["callback", "closeOrder", "confirmReceipt"]);

const props = defineProps({
    formState: {
        type: Object,
        default: {}
    },
    type: {
        type: Number,
        default: 0
    }
});
</script>