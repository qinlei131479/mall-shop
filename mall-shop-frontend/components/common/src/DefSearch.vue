<template>
    <div class="top-search">
        <div class="search-form" :class="{ 'search-form-focus': searchFocus || keyword != '' }">
            <div class="acea-row row-middle">
                <input
                    type="text"
                    autocomplete="off"
                    v-model="keyword"
                    :placeholder="$t('搜索')"
                    class="search-input"
                    name="keywords"
                    @focus="onSearchFocus"
                    @focusout="onSearchFocusout"
                    @keyup="onSearchKeyup"
                    @keydown.enter="onSearchSubmit"
                />
                <div type="submit" class="search-submit iconfont-pc icon-sousuo" @click="onSearchSubmit"></div>
            </div>
        </div>
        <div ref="guessBox" class="search-guess-box" v-if="guessBoxShow">
            <ul class="search-guess-ul">
                <li v-for="(item, index) in guessKeywordList" :key="index">
                    <NuxtLink :to="'/search/?keyword=' + item.keyword" class="serlink">
                        <span class="name">{{ item.keyword }}</span>
                    </NuxtLink>
                </li>
            </ul>
        </div>
    </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getSearchGuessData } from "~/api/common";
const guessKeywordList = ref<any>([]);

let searchGuessDisabled = false;
let setTime: any = null;
const onSearchKeyup = () => {
    if (searchGuessDisabled) {
        return false;
    }
    clearTimeout(setTime);
    searchGuessDisabled = true;
    setTime = setTimeout(async () => {
        try {
            const result = await getSearchGuessData(keyword.value);
            guessKeywordList.value = result;
            guessBoxShow.value = guessKeywordList.value.length > 0;
        } catch (error) {
        } finally {
            searchGuessDisabled = false;
        }
    }, 300);
};
const route = useRoute();
const routeKeyword = ref(route.query.keyword) as Ref<string>;
const searchFocus = ref(false);
const keyword = ref<string>("");
const _keyword = keyword.value;
if (routeKeyword.value && route.path === "/search/") {
    keyword.value = routeKeyword.value;
}

watch(
    () => route.query,
    () => {
        if (!route.query.keyword) {
            keyword.value = "";
        }
    },
    { deep: true }
);

const onSearchFocus = () => {
    searchFocus.value = true;
    if (keyword.value === _keyword) {
        keyword.value = "";
    }
};
const onSearchFocusout = () => {
    if (keyword.value === "") {
        keyword.value = _keyword;
    }
    searchFocus.value = false;
};
const onSearchSubmit = () => {
    location.href = "/search/?keyword=" + keyword.value;
};

const guessBox = ref<HTMLElement | null>(null);

const guessBoxShow = ref(false);
const handleOutsideClick = (event: MouseEvent) => {
    if (guessBox.value && !guessBox.value.contains(event.target as Node)) {
        guessBoxShow.value = false;
    }
};

onMounted(() => {
    window.addEventListener("click", handleOutsideClick);
});

onUnmounted(() => {
    window.removeEventListener("click", handleOutsideClick);
});
</script>
<style scoped lang="less">
.acea-row {
    display: flex;
    flex-wrap: wrap;
}

.acea-row.row-middle {
    align-items: center;
}

.acea-row.row-right {
    justify-content: flex-end;
}

.acea-row.row-center-wrapper {
    align-items: center;
    justify-content: center;
}

.acea-row.row-between-wrapper {
    align-items: center;
    justify-content: space-between;
}
.top-search {
    position: relative;
    width: 700px;

    .search-form-focus .search-input {
        color: #333 !important;
    }

    .search-form {
        background-color: #f2f2f2;
        border-radius: 0;
        overflow: hidden;
        height: 38px;

        .search-input {
            background-color: #f2f2f2;
            border: medium none;
            color: #bbbbbb;
            height: 26px;
            line-height: 26px;
            padding: 6px 10px;
            outline: medium none;
            width: 630px;
        }

        .search-submit {
            background: var(--main-bg);
            border: medium none;
            color: #ffffff;
            cursor: pointer;
            height: 38px;
            text-align: center;
            width: 50px;
            font-weight: normal;
            font-size: 22px;
            line-height: 36px;
        }
    }

    .search-hotkey {
        padding: 8px 0px;
        padding-left: 3px;
        height: 22px;
        line-height: 22px;
        overflow: hidden;

        a {
            margin-right: 15px;
            font-size: 12px;
            color: #999;

            b {
                color: var(--general);
            }
        }

        a:hover {
            color: var(--general);
        }
    }

    .search-guess-box {
        background: #fff none repeat scroll 0 0;
        box-shadow: 1px 2px 1px rgba(0, 0, 0, 0.2);
        border: 1px solid #ccc;
        left: 0px;
        position: absolute;
        top: 39px;
        width: 426px;
        z-index: 8995;

        li {
            line-height: 30px;
            height: 30px;
            padding: 0 10px;

            .serlink {
                display: flex;
                justify-content: left;
                align-items: center;

                .name {
                    width: 260px;
                    height: 30px;
                    overflow: hidden;
                    display: inline-block;
                }

                .conut {
                    width: 100px;
                    text-align: right;
                    height: 30px;
                    overflow: hidden;
                    display: inline-block;
                }
            }
        }
    }
}
</style>
