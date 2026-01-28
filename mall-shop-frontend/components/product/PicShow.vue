<template>
    <div class="goods-image">
        <!-- 大图 -->
        <template v-if="images && (images.picLarge || images.picUrl)">
            <div
                v-show="show"
                class="large"
                :style="[{ backgroundImage: `url(${imageFormat(images.picLarge ? images.picLarge : images.picUrl)})` }, largePosition]"
            ></div>
        </template>

        <!-- 中图 -->
        <div class="middle" :class="{ video: images?.videoUrl }">
            <template v-if="images && (images.picLarge || images.picUrl)">
                <template v-if="!images?.videoUrl">
                    <img ref="target" :src="imageFormat(images.picLarge ? images.picLarge : images.picUrl)" alt="" />
                </template>
                <template v-else> <img @click="$emit('play')" :src="imageFormat(images.videoCover ? images.videoCover : images.picUrl)" alt="" /></template>
            </template>
            <template v-if="images?.videoUrl">
                <div class="video-icon" @click="$emit('play')"></div>
            </template>

            <!-- 遮罩色块 -->
            <!-- <div v-show="show" class="layer" :style="layerPosition"></div> -->
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, watch, reactive } from "vue";
import { useMouseInElement } from "@vueuse/core";
import { imageFormat } from "@/utils/format";

defineProps<{
    images: any;
}>();

const emit = defineEmits(["play"]);

// 1. 是否显示遮罩和大图
const show = ref(false);

// 2. 遮罩的坐标(样式)
// let layerPosition = reactive({
//   top: '0',
//   left: '0'
// })

// 3. 大图的背景坐标(样式)
const largePosition = reactive({
    backgroundPositionX: "0",
    backgroundPositionY: "0"
});

// 4. 获取监听元素的实例对象, 使用useMouseInElement函数进行获取坐标
const target = ref(null);
const { elementX, elementY, isOutside } = useMouseInElement(target);

// 5. 监听元素坐标值的变化
watch([elementX, elementY, isOutside], () => {
    show.value = !isOutside.value;
    const position = { X: 0, Y: 0 };

    if (elementX.value < 100) position.X = 0;
    else if (elementX.value > 250) position.X = 160;
    else position.X = elementX.value - 100;

    if (elementY.value < 100) position.Y = 0;
    else if (elementY.value > 250) position.Y = 160;
    else position.Y = elementY.value - 100;

    // layerPosition.left = position.X + 'px'
    // layerPosition.top = position.Y + 'px'
    largePosition.backgroundPositionX = -2 * position.X + "px";
    largePosition.backgroundPositionY = -2 * position.Y + "px";
});
</script>

<style lang="less" scoped>
.goods-image {
    position: relative;
    display: flex;
    z-index: 6;

    .large {
        position: absolute;
        top: 0;
        left: 370px;
        width: 500px;
        height: 500px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        background-repeat: no-repeat;
        background-size: 800px 800px;
        background-color: #f8f8f8;
        z-index: 999;
    }

    .middle {
        width: 358px;
        height: 358px;
        background: #f5f5f5;
        position: relative;
        cursor: move;

        &.video {
            cursor: pointer;
        }

        .video-icon {
            position: absolute;
            top: 50%;
            left: 50%;
            width: 68px;
            height: 68px;
            transform: translate(-50%, -50%);
            background-image: url("~/assets/images/product/video-playing.png");
            background-size: cover;
        }

        img {
            width: 358px;
            height: 358px;
        }

        .layer {
            width: 160px;
            height: 160px;
            background: rgba(0, 0, 0, 0.2);
            left: 0;
            top: 0;
            position: absolute;
        }
    }
}
</style>
