package com.tigshop.service.video.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ConfigStorageDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoResDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.vo.config.GalleryVideoInfoVO;
import com.tigshop.bean.vo.setting.GalleryVideoInfoDetailVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.AWTUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.video.GalleryVideoInfoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.CosService;
import com.tigshop.service.setting.LocalStorageService;
import com.tigshop.service.setting.OssService;
import com.tigshop.service.video.GalleryVideoInfoService;
import com.tigshop.service.video.GalleryVideoService;
import jakarta.annotation.Resource;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * @author wzh
 * @description 针对表【gallery_video_info(视频相册)】的数据库操作Service实现
 * @createDate 2025-04-21 16:45:59
 */
@Service
public class GalleryVideoInfoServiceImpl extends BaseServiceImpl<GalleryVideoInfoMapper, GalleryVideoInfo>
        implements GalleryVideoInfoService {

    @Resource
    private  GalleryVideoService galleryVideoService;
    @Resource
    private  ConfigService configService;
    @Resource
    private  LocalStorageService localStorageService;
    @Resource
    private  OssService ossService;
    @Resource
    private  CosService cosService;

    @Value("${file.upload-dir}")
    private  String uploadDir;

    @Override
    public GalleryVideoInfoVO list(GalleryVideoInfoListDTO dto) {
        // 创建分页对象
        Page<GalleryVideoInfo> page = new Page<>(dto.getPage(), dto.getSize());
        // 构建查询条件
        LambdaQueryWrapper<GalleryVideoInfo> queryWrapper = buildGalleryVideoQueryWrapper(dto);
        // 执行分页查询
        Page<GalleryVideoInfo> galleryVideoPage = this.page(page, queryWrapper);
        GalleryVideo galleryVideo = null;

        List<GalleryVideoResDTO> result = new ArrayList<>();
        if (dto.getId() != null && dto.getId() > 0) {
            galleryVideo = galleryVideoService.getById(dto.getId());

            List<GalleryVideo> galleryVideoList = galleryVideoService.lambdaQuery()
                    .eq(GalleryVideo::getParentId, dto.getId())
                    .list();

            result = galleryVideoList.stream()
                    .map(this::convertToGalleryVideoResDTO)
                    .toList();
        }


        return new GalleryVideoInfoVO(result, galleryVideoPage, galleryVideo);
    }

    @Override
    public UploadVideoInfoVO uploadVideo(MultipartFile file) {
        // 校验文件类型和相册参数
        filterVideoType(file);
        //文件后缀名
        String originName = file.getOriginalFilename();
        if (StringUtils.isBlank(originName)) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }

        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);
        // 生成随机文件名
        String fileName = StringUtils.getFileName();
        InputStream inputStream = null;
        // 获取不含扩展名的文件名
        String fileNameWithoutExtension = originName.substring(0, originName.lastIndexOf(StrUtil.DOT));

        String format = originName.substring(originName.lastIndexOf(StrUtil.DOT));
        String filePath = StrUtil.format("video/gallery/{}/{}{}", StringUtils.getCurrentYearMonth(), fileName, format);
        try {
            String url = "";
            String storageUrl = "";
            inputStream = file.getInputStream();
            // 根据不同的 storageType 执行不同的上传操作
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    url = localStorageService.upload(file, filePath);
                    storageUrl = configStorage.getStorageLocalUrl();
                }
                case STORAGE_OSS -> {
                    url = ossService.uploadFile(file.getInputStream(), filePath);
                    storageUrl = configStorage.getStorageOssUrl();
                }
                case STORAGE_COS -> {
                    url = cosService.uploadFileStream(filePath, file.getInputStream());
                    storageUrl = configStorage.getStorageCosUrl();
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
            String videoUrl = StrUtil.format("{}{}", storageUrl, url);

            String imgUrl = this.uploadVideoFirstFrame(inputStream, storageType, configStorage);
            String suffix = originName.substring(originName.lastIndexOf(StrUtil.DOT) + 1);
            return new UploadVideoInfoVO(videoUrl, fileNameWithoutExtension, suffix, imgUrl);
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理视频并上传第一帧到OSS
     */
    public String uploadVideoFirstFrame(InputStream videoInputStream, StorageType storageType, ConfigStorageDTO configStorage) throws Exception {
        // 1. 从视频中提取第一帧
        BufferedImage firstFrame = extractFirstFrame(videoInputStream);

        // 2. 将图片转换为输入流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(firstFrame, "jpg", os);
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        String fileName = StringUtils.getFileName();
        String filePath = StrUtil.format("img/gallery/{}/{}{}", StringUtils.getCurrentYearMonth(), fileName, ".jpg");
        String url;
        // 根据不同的 storageType 执行不同的上传操作
        switch (storageType) {
            case STORAGE_LOCAL -> {
                url = localStorageService.upload(inputStream, filePath);
            }
            case STORAGE_OSS -> {
                url = ossService.uploadFile(inputStream, filePath);
            }
            case STORAGE_COS -> {
                url = cosService.uploadFileStream(filePath, inputStream);
            }
            default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
        }
        return StrUtil.format("{}{}", configStorage.getStorageOssUrl(), url);
    }

    private BufferedImage extractFirstFrame(InputStream inputStream) {
        File tempFile = FileUtil.createTempFile();
        try {
            IoUtil.copy(inputStream, FileUtil.getOutputStream(tempFile));
            // 提取第一帧
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(tempFile));
            Picture picture = grab.getNativeFrame();

            return AWTUtil.toBufferedImage(picture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(GalleryVideoInfoDTO dto) {
        GalleryVideoInfo galleryVideoInfo = new GalleryVideoInfo();
        BeanUtils.copyProperties(dto, galleryVideoInfo);
        galleryVideoInfo.setGalleryId(dto.getGalleryId().getLast());
        galleryVideoInfo.setAddTime(StringUtils.getCurrentTime());
        galleryVideoInfo.setAddUserId(SecurityUtils.getCurrentAdminId());
        galleryVideoInfo.setShopId(getShopId());
        galleryVideoInfo.setVendorId(getVendorId());
        this.save(galleryVideoInfo);
    }

    @Override
    public void update(GalleryVideoInfoDTO dto) {
        this.lambdaUpdate()
                .eq(GalleryVideoInfo::getId, dto.getId())
                .set(GalleryVideoInfo::getAddTime, StringUtils.getCurrentTime())
                .set(GalleryVideoInfo::getGalleryId, dto.getGalleryId().getLast())
                .set(GalleryVideoInfo::getVideoUrl, dto.getVideoUrl())
                .set(GalleryVideoInfo::getVideoName, dto.getVideoName())
                .set(GalleryVideoInfo::getVideoCover, dto.getVideoCover())
                .update();
    }

    @Override
    public GalleryVideoInfoDetailVO detail(Integer id) {
        GalleryVideoInfo galleryVideoInfo = this.getById(id);
        if (galleryVideoInfo == null) {
            throw new GlobalException("系统未找到视频信息");
        }
        return BeanUtil.copyProperties(galleryVideoInfo, GalleryVideoInfoDetailVO.class);
    }


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

    public void filterVideoType(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL, SERVICE_DATA_ERROR);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !"video/mp4".contains(contentType)) {
            throw new GlobalException(UPLOAD_FILE_TYPE_ERROR, SERVICE_DATA_ERROR);
        }
    }

    private GalleryVideoResDTO convertToGalleryVideoResDTO(GalleryVideo galleryVideo) {
        GalleryVideoResDTO galleryVideoResDTO = new GalleryVideoResDTO();
        BeanUtils.copyProperties(galleryVideo, galleryVideoResDTO);

        List<GalleryVideoInfo> galleryVideoInfoList = this.lambdaQuery()
                .eq(GalleryVideoInfo::getGalleryId, galleryVideo.getId())
                .last("limit 4")
                .list();
        galleryVideoResDTO.setGalleryVideoInfoList(galleryVideoInfoList);
        return galleryVideoResDTO;
    }

    private LambdaQueryWrapper<GalleryVideoInfo> buildGalleryVideoQueryWrapper(GalleryVideoInfoListDTO dto) {
        LambdaQueryWrapper<GalleryVideoInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideoInfo::getShopId, 0);
            queryWrapper.eq(GalleryVideoInfo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(getShopId() != null, GalleryVideoInfo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(getVendorId() != null, GalleryVideoInfo::getVendorId, getVendorId());
        }

        // 如果指定了 galleryId，按 gallery_id 过滤
        if (dto.getId() != null && dto.getId() > 0) {
            queryWrapper.eq(GalleryVideoInfo::getGalleryId, dto.getId());
        }

        queryWrapper.orderByDesc(GalleryVideoInfo::getId);

        return queryWrapper;
    }
}




