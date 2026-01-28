import { getUser } from "~/api/user/user";
import { userLogout } from "~/api/user/login";
import type { UserFormState } from "~/types/user/user";
import { useCartStore } from "~/store/cart";

export const useUserStore = defineStore("user", () => {
    const userInfo = ref({} as UserFormState);

    const userInfoLoaded = ref(false);

    const openData = ref(null);

    const token = useCookie<string | null>("accessToken", {
        maxAge: 60 * 60 * 24 * 30
    });

    const setToken = (newToken: string) => {
        token.value = newToken;
    };

    const clearToken = () => {
        token.value = null;
    };

    const isAuthenticated = computed(() => !!token.value);

    async function getUserInfo() {
        if (userInfoLoaded.value == true) return;
        try {
            const result = await getUser();
            userInfo.value = result;
        } catch (error) {
        } finally {
            userInfoLoaded.value = true;
        }
    }

    async function loginOut() {
        try {
            await userLogout();
            clearToken();
            const cartStore = useCartStore();
            userInfo.value = {} as UserFormState;
            cartStore.resetCart();
            navigateTo("/member/login");
        } catch (error) {
            console.error(error);
        }
    }

    return {
        userInfo,
        userInfoLoaded,
        openData,
        token,
        isAuthenticated,
        setToken,
        clearToken,
        getUserInfo,
        loginOut
    };
});
