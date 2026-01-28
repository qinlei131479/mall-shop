import { useAppManageStore } from "@/store/appManage";

export default function checkAppUpdate() {
    const appVersionStore = useAppManageStore();
    appVersionStore
        .getAppUpdateStatus()
        .then((res: string) => {
            console.log("App update check result:", res);
            if (res) {
                appVersionStore.setShowUpdateAppPop(true);
                appVersionStore.setUpdateUrl(res);
            }
        })
        .catch((err) => {
            console.error("App update check failed:", err);
        });
}
