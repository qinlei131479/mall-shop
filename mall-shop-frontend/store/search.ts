export const useSearchStore = defineStore("search", () => {
    const total = ref(0);

    return {
        total
    };
});
