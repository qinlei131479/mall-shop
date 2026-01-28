import { asyncGetCartCount, getCart, removeCartItemData } from "@/api/cart";

export const useCartStore = defineStore("cart", () => {
    const cartList = ref<any[]>([]);
    const cartCount = ref(0);

    async function changeCartList() {
        try {
            const result = await getCart();
            cartList.value = result.cartList;
            cartCount.value = result.total.totalCount;
        } catch (error) {
        } finally {
        }
    }

    function resetCart() {
        cartList.value = [];
        cartCount.value = 0;
    }

    return {
        cartList,
        cartCount,
        changeCartList,
        resetCart
    };
});
