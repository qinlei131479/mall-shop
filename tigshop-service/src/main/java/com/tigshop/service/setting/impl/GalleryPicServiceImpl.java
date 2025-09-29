// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.vo.config.GalleryListResVO;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.GalleryPicMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.common.constant.Constants.ALLOWED_IMAGE_TYPES;
import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.pic.PicConstants.*;
import static com.tigshop.common.constant.setting.GalleryConstants.GALLERY_ID_IS_NULL;
import static com.tigshop.common.constant.setting.GalleryPicConstants.FILE_NOT_EXIST;
import static com.tigshop.common.constant.setting.GalleryPicConstants.INVALID_FILE_PATH;
import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * 相册图片服务实现
 *
 * @author Jayce
 * @create 2024年11月12日 13:49
 */
@Service
public class GalleryPicServiceImpl extends BaseServiceImpl<GalleryPicMapper, GalleryPic> implements GalleryPicService {
    @Resource
    private GalleryService galleryService;

    @Resource
    private ConfigService configService;

    @Resource
    private OssService ossService;

    @Resource
    private CosService cosService;

    @Resource
    private LocalStorageService localStorageService;

    /**
     * 获取相册图片列表（分页）
     *
     * @param dto 查询条件和分页信息
     * @return 分页后的相册图片列表
     */
    @Override
    public GalleryListResVO list(GalleryPicListDTO dto) {
        // 创建分页对象
        Page<GalleryPic> page = new Page<>(dto.getPage(), dto.getSize());
        // 构建查询条件
        LambdaQueryWrapper<GalleryPic> queryWrapper = buildGalleryPicQueryWrapper(dto);
        // 执行分页查询
        Page<GalleryPic> galleryPicPage = this.page(page, queryWrapper);

        // 获取相册的详情
        GalleryDTO gallery = null;

        // 获取子相册列表
        List<GalleryPicResDTO> result = new ArrayList<>();

        if (dto.getGalleryId() > 0) {
            List<GalleryDTO> galleryList = galleryService.getGalleryByPid(dto.getGalleryId());
            // 构建结果，包含相册图片数据
            result = galleryList.stream()
                    .map(this::convertToGalleryPicResDTO)
                    .toList();

            gallery = galleryService.detail(dto.getGalleryId());
        }


        // 返回最终地响应结果
        return new GalleryListResVO(result, galleryPicPage, gallery);
    }

    /**
     * 构建 GalleryPic 查询条件
     *
     * @param dto 查询条件
     * @return QueryWrapper
     */
    private LambdaQueryWrapper<GalleryPic> buildGalleryPicQueryWrapper(GalleryPicListDTO dto) {
        Page<GalleryPic> page = new Page<>(dto.getPage(), 10000);
        LambdaQueryWrapper<GalleryPic> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = HeaderUtils.getShopId();
        queryWrapper.eq(shopId != null, GalleryPic::getShopId, shopId);

        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, GalleryPic::getVendorId, vendorId);


        // 如果指定了 galleryId，按 gallery_id 过滤
        if (dto.getGalleryId() != null && dto.getGalleryId() >= 0) {
            queryWrapper.eq(GalleryPic::getGalleryId, dto.getGalleryId());
        }

        queryWrapper.orderByDesc(GalleryPic::getPicId);

        return queryWrapper;
    }

    /**
     * 将 GalleryDTO 转换为 GalleryPicResDTO
     *
     * @param galleryDTO GalleryDTO
     * @return GalleryPicResDTO
     */
    private GalleryPicResDTO convertToGalleryPicResDTO(GalleryDTO galleryDTO) {
        GalleryPicResDTO galleryPicResDTO = new GalleryPicResDTO();
        BeanUtils.copyProperties(galleryDTO, galleryPicResDTO);
        // 获取该相册的图片数据
        List<GalleryPicDTO> galleryPics = getPicsByGid(galleryDTO.getGalleryId());
        galleryPicResDTO.setGalleryPics(galleryPics);
        return galleryPicResDTO;
    }

    /**
     * 获取指定 GalleryId 的所有图片
     *
     * @param galleryId 相册 ID
     * @return GalleryPicDTO 列表
     */
    @Override
    public List<GalleryPicDTO> getPicsByGid(Integer galleryId) {
        Page<GalleryPic> page = new Page<>(1, 4);
        LambdaQueryWrapper<GalleryPic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GalleryPic::getGalleryId, galleryId);
        List<GalleryPic> galleryPicList = this.list(page, queryWrapper);
        // 将图片实体列表转换为 DTO 列表
        return galleryPicList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 将 GalleryPic 实体转换为 DTO
     *
     * @param galleryPic GalleryPic 实体
     * @return GalleryPicDTO
     */
    private GalleryPicDTO convertToDTO(GalleryPic galleryPic) {
        GalleryPicDTO dto = new GalleryPicDTO();
        BeanUtils.copyProperties(galleryPic, dto);
        return dto;
    }

    @Override
    public GalleryPicUploadVO uploadImg(Integer galleryId, MultipartFile file) {
        // 校验文件类型和相册参数
        filterImageType(file);
        if (galleryId == null) {
            throw new GlobalException(GALLERY_ID_IS_NULL);
        }
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);

        // 生成随机文件名
        String fileName = StringUtils.randomFileName();
        String filePath = StrUtil.format("img/gallery/{}/{}.jpeg", StringUtils.getCurrentYearMonth(), fileName);
        try {
            // 根据不同的 storageType 执行不同的上传操作
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    String url = localStorageService.upload(file, filePath);
                    // 本地暂时没有做尺寸功能
                    GalleryPic pic = createPic(url, configStorage.getStorageLocalUrl(), galleryId, fileName);
                    return buildResult(pic);
                }
                case STORAGE_OSS -> {
                    // 字节上传
                    String url = ossService.uploadFile(file.getInputStream(), filePath);
                    GalleryPic pic = createPic(url, configStorage.getStorageOssUrl(), galleryId, fileName);
                    return buildResult(pic);
                }
                case STORAGE_COS -> {
                    String url = cosService.uploadFileStream(filePath, file.getInputStream());
                    GalleryPic pic = createPic(url, configStorage.getStorageCosUrl(), galleryId, fileName);
                    return buildResult(pic);
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    /**
     * 校验文件类型
     *
     * @param file 文件
     */
    public void filterImageType(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL, SERVICE_DATA_ERROR);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new GlobalException(UPLOAD_FILE_TYPE_ERROR, SERVICE_DATA_ERROR);
        }
    }

    /**
     * 构建配置存储对象
     *
     * @return ConfigStorageDTO
     */
    public ConfigStorageDTO buildConfigStorage() {
        List<SettingsEnum> settings = List.of(
                STORAGE_TYPE, STORAGE_LOCAL_URL, STORAGE_OSS_URL,
                STORAGE_OSS_ACCESS_KEY_ID, STORAGE_OSS_ACCESS_KEY_SECRET,
                STORAGE_OSS_BUCKET, STORAGE_OSS_REGION,
                STORAGE_COS_URL, STORAGE_COS_SECRET_ID,
                STORAGE_COS_SECRET_KEY, STORAGE_COS_BUCKET,
                STORAGE_COS_REGION
        );

        Map<String, String> storageConfigMap = configService.getConfigMapBySettings(settings);
        return new ConfigStorageDTO(storageConfigMap);
    }

    /**
     * 保存图片
     *
     * @param url        图片地址
     * @param storageUrl 存储地址
     * @param galleryId  相册ID
     * @param fileName   文件名
     * @return Boolean
     */
    public GalleryPic createPic(String url, String storageUrl, Integer galleryId, String fileName) {
        // 判断是否保存完整路径
        url = StrUtil.format("{}{}", storageUrl, url);

        // 构建图片实体
        GalleryPic galleryPic = new GalleryPic();
        galleryPic.setGalleryId(galleryId);
        galleryPic.setPicUrl(url);
        galleryPic.setPicName(fileName);
        galleryPic.setPicThumb(StrUtil.format("{}{}", url, getImageSize().get("size_200")));
        galleryPic.setAddTime(String.valueOf(StringUtils.getCurrentTime()));
        galleryPic.setPicOwerId(SecurityUtils.getCurrentAdminId());
        galleryPic.setShopId(getShopId());
        Integer vendorId = HeaderUtils.getVendorId();
        galleryPic.setVendorId(vendorId);

        // 保存图片到数据库
        boolean isCreated = this.save(galleryPic);
        if (isCreated) {
            return galleryPic;
        }

        throw new GlobalException(UPLOAD_FAIL);
    }

    /**
     * 构建上传图片结果
     *
     * @param galleryPic 图片实体
     * @return GalleryPicUploadVO
     */
    public GalleryPicUploadVO buildResult(GalleryPic galleryPic) {
        return new GalleryPicUploadVO(galleryPic.getPicUrl(), galleryPic.getPicName(), galleryPic.getPicId(), galleryPic.getPicThumb());
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> selectFile(String timestamp, String filename, String fileType) {
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();
        // 在 JAR 目录下创建 uploadDir 文件夹
        File pemDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String absolutePath = pemDir.getAbsolutePath();
        // 获取文件存储的根目录
        String uploadPath = "";
        if (fileType.equals("app")) {
            uploadPath = StrUtil.format("{}/{}/update/{}", absolutePath, fileType, timestamp);
        } else {
            uploadPath = StrUtil.format("{}/{}/gallery/{}", absolutePath, fileType, timestamp);
        }

        // 创建文件路径
        String filePath = Paths.get(uploadPath, filename).toString();

        // 创建资源
        try {
            UrlResource urlResource = new UrlResource(Paths.get(filePath).toUri());
            // 检查文件是否存在
            if (!urlResource.exists()) {
                throw new GlobalException(FILE_NOT_EXIST);
            }

            // 获取文件的 MIME 类型
            String contentType = URLConnection.guessContentTypeFromName(filename);
            contentType = contentType == null ? "application/octet-stream" : contentType;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(urlResource);
        } catch (MalformedURLException e) {
            log.error(StrUtil.format("{}{}", INVALID_FILE_PATH, filePath));
        }
        return ResponseEntity.notFound().build();
    }


    @Override
    public Map<String, String> getImageSize() {
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);
        // 根据不同的 storageType 执行不同的上传操作
        Map<String, String> map = new HashMap<>();
        switch (storageType) {
            case STORAGE_LOCAL -> {
                map.put("size_200", "");
                map.put("size_400", "");
                map.put("size_800", "");
                return map;
            }
            case STORAGE_COS -> {
                map.put("size_200", COS_SIZE_200);
                map.put("size_400", COS_SIZE_400);
                map.put("size_800", COS_SIZE_800);
                return map;
            }
            default -> {
                // 默认使用阿里云OSS
                map.put("size_200", OSS_SIZE_200);
                map.put("size_400", OSS_SIZE_400);
                map.put("size_800", OSS_SIZE_800);
                return map;
            }
        }
    }
}
