<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>标签名称：</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            placeholder="输入标签名称"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>状态 ：</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in configData" :key="item.status" :label="item.description" :value="item.status" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">搜索</el-button>
                                        <el-button plain @click="resetParams">重置</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <DialogForm
                                :params="{ act: 'add' }"
                                isDrawer
                                path="store/adminPages/storeTipManage/Info"
                                title="新增标签"
                                width="600px"
                                @okCallback="loadFilter"
                            >
                                <el-button type="primary">新增标签</el-button>
                            </DialogForm>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="tipId"
                            show-overflow-tooltip
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="80" label="序号">
                                <template #default="{ row, $index }">
                                    {{ $index + 1 }}
                                </template>
                            </el-table-column>
                            <el-table-column label="标签名称" prop="tipName">
                                <template #default="{ row }">
                                    {{ row.tipName }}
                                </template>
                            </el-table-column>
                            <el-table-column label="创建时间" prop="addTime">
                                <template #default="{ row }">
                                    <div>{{ row.addTime || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column label="状态" prop="status">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.status" :params="{ id: row.tipId, field: 'status' }" :requestApi="updateTipFiled" />
                                </template>
                            </el-table-column>
                            <el-table-column :width="130" fixed="right" label="操作">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.tipId }"
                                        isDrawer
                                        path="store/adminPages/storeTipManage/Info"
                                        title="编辑区域"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">编辑</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.tipId }" :requestApi="delTip" @afterDelete="loadFilter">删除 </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">暂无数据</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { RequestList, Record } from "@/types/store/storeTipManage";
import { tipList, delTip, updateTipFiled, tipConfig } from "@/api/store/storeTipManage";
import { useListRequest } from "@/hooks/useListRequest";
import { getAdminType } from "@/utils/storage";
import { message } from "ant-design-vue";
import { ref } from "vue";
const adminType = getAdminType();
const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<Record, RequestList>({
    apiFunction: tipList,
    idKey: "tipId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: ""
    }
});

const configData = ref<any>({})
const _tipConfig = async () => {
    try {
        const result = await tipConfig();
        console.log(result)
        configData.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
// 初始化加载
_tipConfig();
loadFilter();
</script>
