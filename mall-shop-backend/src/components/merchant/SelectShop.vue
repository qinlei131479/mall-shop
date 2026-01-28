<template>
    <LoginSelectShop
        v-if="!isIndex"
        :selectShopFlag="selectShopFlag"
        :myShopList="myShopList"
        :vendorList="vendorList"
        :loading="loading"
        :userinfo="userinfo"
        :adminType="adminType"
        @callBack="toIndex"
    ></LoginSelectShop>
    <IndexSelectShop
        v-if="isIndex"
        :selectShopFlag="selectShopFlag"
        :myShopList="myShopList"
        :vendorList="vendorList"
        :loading="loading"
        :userinfo="userinfo"
        :adminType="adminType"
        @callBack="toIndex"
        @changeShopList="_getShopMyShop"
        @close="closePopup"
    ></IndexSelectShop>
</template>
<script setup lang="ts">
import LoginSelectShop from "./src/LoginSelectShop.vue";
import IndexSelectShop from "./src/IndexSelectShop.vue";
import { onMounted, ref, nextTick } from "vue";
import { getShopMyShop, chooseShop } from "@/api/merchant/shop";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useMenusStore } from "@/store/menu";
import { useThemeStore } from "@/store/theme";
import { message, notification } from "ant-design-vue";
import { useRouter } from "vue-router";
import { updateMenu, getMenu } from "@/utils/menus";
import { isMerchant, isS2b2c, isStore } from "@/utils/version";
import dayjs from "dayjs";
import { processRoutes } from "@/utils/authorize";
const props = defineProps({
    isIndex: {
        type: Boolean,
        default: false
    },
    selectShopFlag: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["closePopup", "update:selectShopFlag"]);
const router = useRouter();
const toIndex = async (e: any) => {
    localStorage.setItem("adminType", e.type);
    localStorage.removeItem("shopId");
    localStorage.removeItem("vendorId");
    if (e.type == "shop") {
        localStorage.setItem("shopId", String(e.id));
    }
    if (e.type == "vendor") {
        localStorage.setItem("vendorId", String(e.id));
    }
    // await _chooseShop({ id: e.id, adminType: localStorage.getItem("adminType") });
    const userStore = useUserStore();
    const configStore = useConfigStore();
    const menusStore = useMenusStore();
    const themeStore = useThemeStore();
    // 更新后台设置项
    await Promise.all([userStore.updateUserInfo(), configStore.updateConfig()]);
    themeStore.getThemeInfo();
    notification["success"]({
        message: "登录成功",
        placement: "top",
        duration: 1.5,
        description: "您好，欢迎回来"
    });
    // 切换到商铺选择页面
    menusStore.clearMenus();
    const routers = await getMenu();
    menusStore.setRouters(routers || []);
    if (props.isIndex) {
        emit("closePopup");
        const path = `${router.options.history.base}${routers ? routers[0].path : "/"}`;
        window.history.pushState({ path }, "", path);
        setTimeout(() => {
            location.reload();
        }, 150);
        return;
    }
    localStorage.setItem("lastOpenTime", String(dayjs().unix()));
    router.push(routers ? firstRouter(routers[0]) : "/");
    // setTimeout(() => {
    //     location.reload();
    // }, 20);
};

const firstRouter = (router: any) => {
    if (router.children?.[0]) {
        const firstChild = router.children[0];
        if (router.path === firstChild.path) {
            return router.path;
        } else if (firstChild.children?.[0]) {
            return (router.path == "/" ? "" : router.path) + "/" + firstChild.children[0].path;
        }
    }
    return router.path;
};

const closePopup = () => {
    emit("closePopup");
};
const myShopList = ref<any[]>([]);
const vendorList = ref<any[]>([]);
const loading = ref(true);
const userinfo = ref({
    username: ""
});
const adminType = ref<any>("");
const _chooseShop = async (obj: any) => {
    try {
        loading.value = true;
        const result = await chooseShop(obj);
    } catch (error: any) {
    } finally {
        loading.value = false;
    }
};
const _getShopMyShop = async () => {
    try {
        loading.value = true;
        const result = await getShopMyShop({ sortField: "lastLoginTime" });
        if (result.shop.records) {
            myShopList.value = result.shop.records;
        }
        if (result.vendor.records) {
            vendorList.value = result.vendor.records;
        }
        if (result.userinfo) {
            userinfo.value = result.userinfo;
        }
    } catch (error: any) {
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    const accessToken = localStorage.getItem("accessToken");
    adminType.value = localStorage.getItem("adminType");
    if (accessToken && (isMerchant() || isStore() || isS2b2c())) {
        _getShopMyShop();
    } else {
        loading.value = false;
    }
});
</script>
