import type { ModuleListType } from "@/types/api.d";

export const useHomeStore = defineStore("home", () => {
    const moduleList = ref<ModuleListType[]>([]);
    const moduleLoaded = ref(false);
    const decorateId = ref(0);
    const headKeywords = ref("");
    const headDescription = ref("");
    const previewId = ref(0);

    return {
        moduleList,
        moduleLoaded,
        decorateId,
        headKeywords,
        headDescription,
        previewId
    };
});
