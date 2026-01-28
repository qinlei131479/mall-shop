// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    devtools: { enabled: true },

    ssr: process.env.VITE_SSR === "1",

    devServer: {
        host: "0.0.0.0",
        port: 3000
    },

    app: {
        baseURL: process.env.VITE_APP_BASE_URL // 配置基础 URL
    },

    nitro: {
        // 该配置用于服务端请求转发
        routeRules: {
            "/api/**": {
                //  如果需要做代理转发，使用proxy
                proxy: process.env.VITE_PROXY === "1" ? process.env.VITE_API_URL + "/api/**" : undefined,
                ssr: false
            }
        }
    },

    modules: ["@element-plus/nuxt", "@pinia/nuxt", "@nuxtjs/i18n"],

    imports: {
        presets: [
            {
                from: "ant-design-vue",
                imports: ["message"]
            }
        ]
    },

    css: [
        "~/assets/css/global.less" // 相对路径，从项目根目录开始
    ],

    i18n: {
        strategy: "no_prefix", // 添加路由前缀的方式
        vueI18n: "~/i18n/config.ts"
    },

    vite: {
        build: {
            minify: "terser",
            terserOptions: {
                compress: {
                    drop_console: false,
                    drop_debugger: false
                }
            },
            sourcemap: false
        }
    },

    compatibilityDate: "2024-10-25"
});
