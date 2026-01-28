<template>
    <div class="menu-list">
        <NuxtLink class="ico" to="/article/issue/index"></NuxtLink>
        <div v-if="!loading" class="menu">
            <el-collapse v-model="activeName" accordion>
                <el-collapse-item
                    v-for="item in articleState"
                    :name="item.articleCategoryId"
                    :title="item.articleCategoryName"
                    :disabled="item.articles.length === 0"
                >
                    <div class="min-menu">
                        <NuxtLink
                            v-for="items in item?.articles"
                            :class="activeNameId === items.articleId ? 'pitch-on' : ''"
                            :to="'/article/issue/info?articleId=' + items.articleId"
                            class="item-menu"
                        >
                            {{ items.articleTitle }}
                        </NuxtLink>
                    </div>
                </el-collapse-item>
            </el-collapse>
        </div>
        <div v-else class="loading-data">
            {{ $t("加载中") }}
        </div>
    </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from "vue";
import { getBzzxCategoryList } from "~/api/article/category";
import type { CategoryFilterState } from "~/types/article/category";

const articleState = ref<CategoryFilterState[]>([]);
const activeName: any = ref();
const activeNameId: any = ref();

const loading = ref<boolean>(true);

const props = defineProps({
    dataOver: {
        type: Boolean,
        default: false
    },
    leftMenuList: {
        type: Array,
        default: []
    }
});
const over = ref<Boolean>(false);
const emit = defineEmits(["update:leftMenuList", "update:dataOver"]);

const loadBzzxList = async () => {
    loading.value = true;
    emit("update:leftMenuList", []);
    try {
        const result = await getBzzxCategoryList({ categorySize: 99, articleSize: 99 });
        Object.assign(articleState.value, result);
        emit("update:leftMenuList", articleState.value);
        emit("update:dataOver", true);
        over.value = true;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
loadBzzxList();
const router = useRouter();
watch(
    [() => router.currentRoute.value.query, () => over.value],
    () => {
        if (router.currentRoute.value.query.hasOwnProperty("articleId")) {
            let id: any = router.currentRoute.value.query.articleId;
            articleState.value?.forEach((item: any, index: number) => {
                item.articles.forEach((item2: any, index2: number) => {
                    if (item2.articleId === Number(id)) {
                        activeName.value = item.articleCategoryId;
                        activeNameId.value = item2.articleId;
                    }
                });
            });
        } else if (router.currentRoute.value.query.hasOwnProperty("categoryId")) {
            let id: any = router.currentRoute.value.query.categoryId;
            articleState.value?.forEach((item: any, index: number) => {
                if (item.categorySn === id) {
                    activeName.value = item.articleCategoryId;
                }
            });
        }
    },
    { deep: true }
);
</script>
<style lang="less" scoped>
.menu-list {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;
    overflow: hidden;

    :deep(.el-collapse-item__header) {
        box-sizing: border-box;
    }

    .ico {
        width: 200px;
        height: 80px;
        border: 1px solid #e4e4e4;
        background: white url("/assets/images/issue/helptip.png") no-repeat scroll;
    }

    .menu {
        border-left: 1px solid #e4e4e4;
        border-right: 1px solid #e4e4e4;

        :deep(.el-collapse-item__header) {
            padding-left: 20px;
            border-bottom: 1px solid #ebeef5;
            height: 40px;
            background: linear-gradient(to bottom, white, #f7f7f7);
        }

        :deep(.el-collapse-item__content) {
            padding-bottom: 0;
        }

        .min-menu {
            padding-left: 40px;
            font-size: 12px;
            display: flex;
            flex-direction: column;

            .item-menu {
                height: 40px;
                line-height: 40px;
                width: 100%;
            }

            .pitch-on {
                color: var(--general);
            }
        }
    }
}

.loading-data {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: white;
    min-height: 400px;
    color: #999999;
    font-size: 20px;
    border: 1px solid #e4e4e4;
    font-weight: normal;
}
</style>
