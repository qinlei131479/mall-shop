import { useAppManageStore } from "@/store/appManage";

// 权限类型枚举
export enum PermissionType {
    CAMERA = "camera",
    PHOTOS = "photos",
    MICROPHONE = "microphone",
    LOCATION = "location"
}

// 权限状态
export interface PermissionResult {
    granted: boolean;
    denied: boolean;
    undetermined: boolean;
    deniedAlways?: boolean;
}

/**
 * 检查指定权限状态
 * @param permissionType 权限类型
 * @returns Promise<PermissionResult>
 */
export const checkPermission = (permissionType: PermissionType): Promise<PermissionResult> => {
    return new Promise((resolve, reject) => {
        // #ifdef APP-PLUS
        const permission = getPermissionName(permissionType);
        if (!permission) {
            reject(new Error("不支持的权限类型"));
            return;
        }

        const appManageStore = useAppManageStore();

        plus.android.requestPermissions(
            [permission],
            (resultObj) => {
                appManageStore.setPermissionType("");
                appManageStore.setShowPermissionPop(false);

                const result = resultObj.granted.includes(permission);
                const denied = resultObj.deniedPresent.includes(permission);
                const deniedAlways = resultObj.deniedAlways.includes(permission);

                resolve({
                    granted: result,
                    denied: denied || deniedAlways,
                    undetermined: !result && !denied && !deniedAlways,
                    deniedAlways: deniedAlways
                });
            },
            (error) => {
                appManageStore.setPermissionType("");
                appManageStore.setShowPermissionPop(false);
                reject(error);
            }
        );

        appManageStore.setPermissionType(permissionType);
        appManageStore.setShowPermissionPop(true);
        // #endif

        // #ifndef APP-PLUS
        // 非APP端默认返回已授权
        resolve({
            granted: true,
            denied: false,
            undetermined: false
        });
        // #endif
    });
};

/**
 * 请求指定权限
 * @param permissionType 权限类型
 * @param showRationale 是否显示权限说明
 * @returns Promise<boolean>
 */
export const requestPermission = (permissionType: PermissionType, showRationale: boolean = true): Promise<boolean> => {
    return new Promise((resolve, reject) => {
        // #ifdef APP-PLUS
        const permission = getPermissionName(permissionType);
        if (!permission) {
            reject(new Error("不支持的权限类型"));
            return;
        }

        // 先检查权限状态
        checkPermission(permissionType)
            .then((status) => {
                if (status.granted) {
                    resolve(true);
                    return;
                }

                // 如果权限被永久拒绝，引导用户去设置页面
                if (status.deniedAlways) {
                    if (showRationale) {
                        showPermissionSettingsDialog(permissionType, resolve);
                    } else {
                        resolve(false);
                    }
                    return;
                }
            })
            .catch(reject);
        // #endif

        // #ifndef APP-PLUS
        // 非APP端默认返回已授权
        resolve(true);
        // #endif
    });
};

/**
 * 获取平台对应的权限名称
 */
const getPermissionName = (permissionType: PermissionType): string | null => {
    // #ifdef APP-PLUS
    const permissionMap: Record<PermissionType, string> = {
        [PermissionType.CAMERA]: "android.permission.CAMERA",
        [PermissionType.PHOTOS]: "android.permission.READ_EXTERNAL_STORAGE",
        [PermissionType.MICROPHONE]: "android.permission.RECORD_AUDIO",
        [PermissionType.LOCATION]: "android.permission.ACCESS_FINE_LOCATION"
    };

    return permissionMap[permissionType] || null;
    // #endif

    // #ifndef APP-PLUS
    return null;
    // #endif
};

/**
 * 显示跳转设置页面的对话框
 */
const showPermissionSettingsDialog = (permissionType: PermissionType, resolve: (value: boolean) => void) => {
    const permissionMessages = {
        [PermissionType.CAMERA]: {
            title: "相机权限被禁用",
            content: "相机权限已被禁用，请在手机设置中开启相机权限后再试。"
        },
        [PermissionType.PHOTOS]: {
            title: "相册权限被禁用",
            content: "相册权限已被禁用，请在手机设置中开启存储权限后再试。"
        },
        [PermissionType.MICROPHONE]: {
            title: "麦克风权限被禁用",
            content: "麦克风权限已被禁用，请在手机设置中开启麦克风权限后再试。"
        },
        [PermissionType.LOCATION]: {
            title: "位置权限被禁用",
            content: "位置权限已被禁用，请在手机设置中开启位置权限后再试。"
        }
    };

    const message = permissionMessages[permissionType];

    uni.showModal({
        title: message.title,
        content: message.content,
        confirmText: "去设置",
        cancelText: "取消",
        success: (res) => {
            if (res.confirm) {
                // 跳转到应用设置页面
                // #ifdef APP-PLUS
                plus.runtime.openURL("app-settings:");
                // #endif
                resolve(false);
            } else {
                resolve(false);
            }
        },
        fail: () => {
            resolve(false);
        }
    });
};

/**
 * 便捷方法：请求相机权限
 */
export const requestCameraPermission = (): Promise<boolean> => {
    return requestPermission(PermissionType.CAMERA);
};

/**
 * 便捷方法：请求相册权限
 */
export const requestPhotosPermission = (): Promise<boolean> => {
    return requestPermission(PermissionType.PHOTOS);
};

/**
 * 便捷方法：检查相机权限
 */
export const checkCameraPermission = (): Promise<PermissionResult> => {
    return checkPermission(PermissionType.CAMERA);
};

/**
 * 便捷方法：检查相册权限
 */
export const checkPhotosPermission = (): Promise<PermissionResult> => {
    return checkPermission(PermissionType.PHOTOS);
};
