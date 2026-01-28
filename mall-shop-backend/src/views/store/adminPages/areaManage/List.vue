<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>区域名称：</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            placeholder="输入区域名称"
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
                                path="store/adminPages/areaManage/Info"
                                title="新增区域"
                                width="800px"
                                @okCallback="loadFilter"
                            >
                                <el-button type="primary">新增区域</el-button>
                            </DialogForm>
                            <el-popconfirm title="您确认要批量删除所选数据吗？" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button :disabled="selectedIds.length === 0">批量删除</el-button>
                                </template>
                            </el-popconfirm>
                            <span v-if="selectedIds.length > 0">
                                已选择：<b>{{ selectedIds.length }}</b> 项
                            </span>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="areaStoreManagerId"
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
                            <el-table-column label="区域名称" prop="areaStoreName" sortable="custom">
                                <template #default="{ row }">
                                    {{ row.areaStoreName }}
                                </template>
                            </el-table-column>
                            <el-table-column label="区域门店数量" prop="shopCount" sortable="custom">
                                <template #default="{ row }">
                                    <div>{{ row.shopCount || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column label="排序" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <!-- <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.areaStoreManagerId, field: 'sortOrder' }"
                                        :requestApi="updateBrandField"
                                        extra="默认值为50，数值越小，排序越靠前"
                                        label="排序"
                                        type="integer"
                                    > -->
                                    <div>{{ row.sortOrder }}</div>
                                    <!-- </PopForm> -->
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" fixed="right" label="操作">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.areaStoreManagerId }"
                                        isDrawer
                                        :showClose="false"
                                        :showOnOk="false"
                                        path="store/adminPages/areaManage/ShopList"
                                        :title="row.areaStoreName + '门店列表'"
                                        width="1000px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">门店列表</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.areaStoreManagerId }"
                                        isDrawer
                                        path="store/adminPages/areaManage/Info"
                                        title="编辑区域"
                                        width="700px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">编辑</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.areaStoreManagerId }" :requestApi="delAreaStoreManager" @afterDelete="loadFilter">删除 </DeleteRecord>
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
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { RequestList, AreaStoreManagerVO } from "@/types/store/areaManage";
import { areaStoreManagerList, batchSubmit, delAreaStoreManager } from "@/api/store/areaManage";
import { useListRequest } from "@/hooks/useListRequest";
import { isOverseas, isMerchant } from "@/utils/version";
import { getAdminType } from "@/utils/storage";
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
} = useListRequest<AreaStoreManagerVO, RequestList>({
    apiFunction: areaStoreManagerList,
    idKey: "areaStoreManagerId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>
