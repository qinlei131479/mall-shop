<template>
    <div class="map-page">
        <div class="map-content">
            <div class="map-card">
                <div id="container" class="map-container" ref="mapContainer">
                    <div v-if="loading" class="loading-overlay">
                        <div class="spinner"></div>
                        <p>地图加载中，请稍候...</p>
                    </div>
                </div>
                <div v-if="error" class="error-message">
                    地图加载失败，请检查网络连接或刷新页面重试。
                    <p>错误信息: {{ errorMessage }}</p>
                </div>
            </div>

            <!-- 自定义搜索结果的显示 -->
            <div v-if="searchResults.length > 0" class="custom-search-results">
                <div class="results-header">
                    <h3>搜索结果</h3>
                    <!-- <button class="clear-btn" @click="clearSearch">清除</button> -->
                </div>
                <div class="results-list">
                    <div v-for="(result, index) in searchResults" :key="index" class="result-item" @click="selectSearchResult(result)">
                        <div class="result-name">{{ result.name }}</div>
                        <div class="result-address">{{ result.address }}</div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 选点信息显示区域 -->
        <!-- <div v-if="selectedMarker" class="marker-info">
            <strong>标题:</strong> {{ selectedMarker.title }}<br />
            <strong>位置:</strong> {{ selectedMarker.position.toString() }}<br />
            <strong>时间:</strong> {{ selectedMarker.time }}
        </div> -->
    </div>
</template>

<script setup>
import { message } from "ant-design-vue";
import { ref, onMounted, onUnmounted, watch } from "vue";
import AMapLoader from "@amap/amap-jsapi-loader";
import { getConfigAmapAllSettings } from "@/api/setting/config";
const props = defineProps({
    defaultPosition: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
// 给父组件传值
const emit = defineEmits(["positionChange"]);
// 地图实例
let map = null;
let placeSearch = null; // 地点搜索实例
const mapContainer = ref(null);

// 响应式状态
const loading = ref(true);
const error = ref(false);
const errorMessage = ref("");
const mapStatus = ref("未加载");
const zoomLevel = ref(10);
const mapCenter = ref([116.397428, 39.90923].join(", "));
const selectedMarker = ref(null);
const searchKeyword = ref("");
const searchResults = ref([]);
const recentSearches = ref([]);
const markers = ref([]); // 存储所有标记点

const formState = ref({
    amapWebKey: "",
    amapWebSecret: "",
    amapJsKey: "",
    amapJsSecret: "",
    amapMiniKey: "",
    amapMiniSecret: ""
});
// onMounted(async () => {
//     await loadFilter();
// });


// 添加标记点
const addMarker = (lng, lat, title) => {
    if (!map) return;
    const marker = new AMap.Marker({
        position: [lng, lat],
        title: title
    });
    map.add(marker);
    return marker;
};

// 更新地图信息
const updateMapInfo = () => {
    if (map) {
        zoomLevel.value = map.getZoom();
        const center = map.getCenter();
        mapCenter.value = [center.getLng(), center.getLat()].join(", ");
    }
};

// 初始化地图
const initMap = (initData) => {
    loading.value = true;
    error.value = false;
    mapStatus.value = "加载中";

    window._AMapSecurityConfig = {
        securityJsCode: initData.amapJsSecret
    };

    AMapLoader.load({
        key: initData.amapJsKey,
        version: "2.0",
        plugins: ["AMap.Scale", "AMap.ToolBar", "AMap.MapType", "AMap.PlaceSearch"]
    })
        .then((AMap) => {
            let centerPosition = props.defaultPosition || mapCenter.value;
            let [lng, lat] = centerPosition.split(",").map(Number);

            // 验证经纬度是否为有效数字
            if (isNaN(lng) || isNaN(lat)) {
                error.value = true;
                errorMessage.value = "无效的默认位置，请检查输入的坐标。";
                loading.value = false;
                return;
            }

            map = new AMap.Map(mapContainer.value, {
                zoom: zoomLevel.value,
                center: [lng, lat],
                viewMode: "2D"
            });

            // 初始化地点搜索实例
            placeSearch = new AMap.PlaceSearch({
                map: map
            });

            // 添加控件
            map.addControl(new AMap.Scale());
            map.addControl(new AMap.ToolBar());
            map.addControl(new AMap.MapType());

            if (props.defaultPosition) {
                const [lng, lat] = props.defaultPosition.split(",").map(Number);
                const marker = addMarker(lng, lat, "默认位置");
                markers.value.push(marker);
            }

            // 监听地图事件
            map.on("moveend", updateMapInfo);
            map.on("zoomchange", updateMapInfo);

            mapStatus.value = "已加载";
            loading.value = false;
        })
        .catch((e) => {
            console.error(e);
            error.value = true;
            errorMessage.value = e.message;
            mapStatus.value = "加载失败";
            loading.value = false;
        });
};

const search = (keyword) => {
    searchKeyword.value = keyword;
    searchLocation();
};

// 搜索地点
const searchLocation = () => {
    if (!placeSearch || !searchKeyword.value.trim()) return;

    placeSearch.search(searchKeyword.value, (status, result) => {
        if (status === "complete" && result.info === "OK") {
            searchResults.value = result.poiList.pois;

            // 保存到最近搜索
            if (!recentSearches.value.includes(searchKeyword.value)) {
                recentSearches.value.unshift(searchKeyword.value);
                // 只保留最近5个搜索
                if (recentSearches.value.length > 5) {
                    recentSearches.value.pop();
                }
            }
            // 直接标记第一个搜索结果的坐标
            if (searchResults.value.length > 0) {
                const firstResult = searchResults.value[0];
                markLocation(firstResult.location, firstResult.name);
            }
        } else {
            console.error("搜索失败:", result);
        }
    });
};

const markLocation = (location, name) => {
    if (map) {
        const marker = addMarker(location.lng, location.lat, name);
        if (marker) {
            // 可能需要在这里添加更多的逻辑来处理 marker
            // 更新选点信息
            selectedMarker.value = {
                title: name,
                position: marker.getPosition(),
                time: new Date().toLocaleTimeString()
            };
            // emit("positionChange", { position: [location.lng, location.lat], name: name });
        }
    } else {
        console.error("地图未加载，无法添加标记");
    }
};

const selectSearchResult = (result) => {
    if (!map) return;

    // 清除之前的标记点
    markers.value.forEach((marker) => marker.setMap(null));
    markers.value = [];

    // 移动到搜索结果位置
    map.setCenter([result.location.lng, result.location.lat]);
    map.setZoom(15);
    // 添加标记
    const marker = addMarker(result.location.lng, result.location.lat, result.name);
    // 更新选点信息
    selectedMarker.value = {
        title: result.name,
        position: marker.getPosition(),
        time: new Date().toLocaleTimeString()
    };
    emit("positionChange", { position: [result.location.lng, result.location.lat], name: result.name });

    // 添加新标记点到 markers 数组
    markers.value.push(marker);

};

// 清除搜索
const clearSearch = () => {
    searchResults.value = [];
    searchKeyword.value = "";

    // 清除之前的标记点
    markers.value.forEach((marker) => marker.setMap(null));
    markers.value = [];
    selectedMarker.value = null;

};

const loadFilter = async () => {
    try {
        const result = await getConfigAmapAllSettings({use:1});
        initMap(result);
    } catch (error) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadFilter()
});

onUnmounted(() => {
    if (map) {
        map.destroy();
    }
});

defineExpose({ search, clearSearch });
</script>

<style scoped lang="less">
.map-page {
    padding-bottom: 20px;
    color: #333;
}

.header {
    text-align: center;
    margin-bottom: 30px;
    padding: 20px;
    background: linear-gradient(135deg, #2466e9 0%, #1a4fbf 100%);
    color: white;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(36, 102, 233, 0.2);
}

.header h1 {
    font-size: 2.2rem;
    margin-bottom: 10px;
}

.subtitle {
    font-size: 1.1rem;
    opacity: 0.9;
}

.search-container {
    margin-bottom: 20px;
    background: white;
    padding: 20px;
    border-radius: 5px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.search-box {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
}

.search-input {
    flex: 1;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    transition: border-color 0.3s;
}

.search-input:focus {
    outline: none;
    border-color: #2466e9;
    box-shadow: 0 0 0 2px rgba(36, 102, 233, 0.2);
}

.search-btn {
    padding: 12px 20px;
    background: #2466e9;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 5px;
    transition: background 0.3s;
    z-index: 1; /* 确保按钮在其他元素之上 */
}

.search-btn:hover {
    background: #1a57c9;
}

.custom-search-results {
    width: 250px;
    border: 1px solid #eaeef2;
    border-radius: 6px;
    overflow: hidden;
    .results-list {
        height: 450px;
        overflow: auto;
    }
}

.results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    background: #f9fafc;
    border-bottom: 1px solid #eaeef2;
}

.results-header h3 {
    margin: 0;
    font-size: 16px;
    color: #2466e9;
}

.clear-btn {
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    font-size: 14px;
}

.clear-btn:hover {
    color: #2466e9;
}

.result-item {
    line-height: 18px;
    padding: 10px 15px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;
}

.result-item:hover {
    background: #f0f7ff;
}

.result-item:last-child {
    border-bottom: none;
}

.result-name {
    font-weight: 500;
    margin-bottom: 5px;
    color: #333;
}

.result-address {
    font-size: 14px;
    color: #666;
}

.map-content {
    display: flex;
    gap: 20px;
}

.map-card {
    width: 500px;
    height: 500px;
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.map-container {
    width: 100%;
    height: 500px;
    position: relative;
}

.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    flex-direction: column;
    gap: 15px;
}

.spinner {
    width: 50px;
    height: 50px;
    border: 5px solid #e8e8e8;
    border-top: 5px solid #2466e9;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

.error-message {
    background: #ffebee;
    color: #d32f2f;
    padding: 15px;
    border-radius: 8px;
    margin: 15px;
}

.controls {
    padding: 20px;
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    background: #f9fafc;
    border-top: 1px solid #eaeef2;
}

.btn {
    padding: 10px 18px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.2s ease;
}

.btn-primary {
    background: #2466e9;
    color: white;
}

.btn-primary:hover {
    background: #1a57c9;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(36, 102, 233, 0.3);
}

.btn-outline {
    background: transparent;
    border: 1px solid #2466e9;
    color: #2466e9;
}

.btn-outline:hover {
    background: #f0f5ff;
}

.info-panel {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.info-panel h2 {
    margin-bottom: 15px;
    color: #2466e9;
    font-size: 1.4rem;
}

.info-content {
    line-height: 1.6;
}

.marker-info {
    background: #f0f7ff;
    padding: 15px;
    border-radius: 8px;
    margin-top: 15px;
    border-left: 4px solid #2466e9;
}

.recent-searches {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

.recent-searches h3 {
    font-size: 16px;
    color: #2466e9;
    margin-bottom: 10px;
}

.recent-searches ul {
    list-style: none;
    padding: 0;
}

.recent-searches li {
    padding: 8px 12px;
    background: #f9f9f9;
    border-radius: 4px;
    margin-bottom: 5px;
    cursor: pointer;
    transition: background 0.2s;
}

.recent-searches li:hover {
    background: #f0f7ff;
}

.footer {
    text-align: center;
    margin-top: 40px;
    padding: 20px;
    color: #666;
    font-size: 0.9rem;
}

@media (min-width: 768px) {
    .map-content {
        flex-direction: row;
    }

    .map-card {
        flex: 2;
    }

    .info-panel {
        flex: 1;
    }
}

@media (max-width: 767px) {
    .map-container {
        height: 400px;
    }

    .controls {
        flex-direction: column;
    }

    .btn {
        width: 100%;
    }

    .search-box {
        flex-direction: column;
    }
}
</style>
