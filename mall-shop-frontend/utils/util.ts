// 下划线转大驼峰
export function toPascalCase(str: string): string {
    const words = str.split("_").map((word) => word.charAt(0).toUpperCase() + word.slice(1));
    return words.join("");
}
// 首字母转大写
export function capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

//校验是否有token
export function hasToken() {
    const token = useCookie("accessToken");
    if (token.value) {
        return true;
    } else {
        navigateTo("/member/login");
    }
}
// 获取当前路由
export function getCurrentRoute() {
    const router = useRouter();
    return router.currentRoute.value;
}
export const isMerchant = () => {
    return import.meta.env.VITE_IS_MERCHANT == 1;
};
export const isPro = () => {
    return import.meta.env.VITE_IS_PRO == 1;
};
export const isB2B = () => {
    return import.meta.env.VITE_IS_B2B == 1;
};
export const isOverseas = () => {
    return import.meta.env.VITE_IS_OVERSEAS == 1;
};
export const isDemo = () => {
    return import.meta.env.VITE_IS_DEMO == 1;
};
export const isStore = () => {
    return import.meta.env.VITE_IS_STORE == 1;
};

export const moneyFormat = (money: string): string => {
    return isNaN(parseFloat(money)) ? money : parseFloat(money).toFixed(2);
};
/**
 *
 * @param data
 * @returns  {any}
 */
export const formatArguments = (data: any) => {
    const result: any = {};
    if (typeof data === "object") {
        const dataKeys = Object.keys(data);
        for (const key of dataKeys) {
            const value = data[key];
            // 检查值是否为 null 或 undefined
            if (value === null || value === undefined || value === "") {
                continue;
            }
            if (typeof value === "object") {
                // 如果值是数组，检查其长度是否为 0
                if (Array.isArray(value) && value.length === 0) {
                    continue;
                } else if (Object.keys(value).length === 0) {
                    continue;
                }
            }
            result[key] = value;
        }
    }
    return result;
};

/**
 * 复制文本到剪贴板
 * @param text 要复制的文本
 * @param callback 复制成功后的回调函数
 */
export const copyText = (text: string, callback?: () => void): void => {
    const input = document.createElement("input");
    input.value = text;
    document.body.appendChild(input);
    input.select();
    document.execCommand("copy");
    document.body.removeChild(input);
    if (callback) {
        callback();
    }
};

/**
 * 判断是否是url
 * @param str
 * @returns
 */
export function isUrl(str: string): boolean {
    return str.indexOf("http://") != -1 || str.indexOf("https://") != -1;
}


export const formatRichText = (html: string) => {
    let newContent= html.replace(/<img[^>]*>/gi,function(match,capture){
        match = match.replace(/style="[^"]+"/gi, '').replace(/style='[^']+'/gi, '');
        match = match.replace(/width="[^"]+"/gi, '').replace(/width='[^']+'/gi, '');
        match = match.replace(/height="[^"]+"/gi, '').replace(/height='[^']+'/gi, '');
        return match;
    });
    newContent = newContent.replace(/style="[^"]+"/gi,function(match,capture){
        match = match.replace(/width:[^;]+;/gi, 'max-width:100%;').replace(/width:[^;]+;/gi, 'max-width:100%;');
        return match;
    });
    newContent = newContent.replace(/<br[^>]*\/>/gi, '');
    newContent = newContent.replace(/\<img/gi, '<img style="max-width: 100%;height: auto;display: block;margin-top: 0;margin-bottom: 0;"');
    return newContent;
}


export const deepClone = (obj: any): any => {
  // 处理 null、undefined 或非对象类型
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  // 处理 Date 类型
  if (obj instanceof Date) {
    return new Date(obj.getTime());
  }
  // 处理 Array 类型
  if (Array.isArray(obj)) {
    return obj.map(item => deepClone(item));
  }
  // 处理 Object 类型
  if (typeof obj === "object") {
    const clonedObj: any = {};
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj;
  }
  
  return obj;
};