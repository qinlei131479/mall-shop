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
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.LocalStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FILE_IS_NULL;
import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * 本地存储
 *
 * @author Tigshop团队
 * @create 2025年01月08日 10:50
 */
@Service
public class LocalStorageServiceImpl implements LocalStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file, String filePath) {
        // 检查文件是否为空
        if(file.isEmpty()){
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }
        // 处理文件存储
        try {
            Path targetLocation = this.getPath(filePath);

            try (InputStream inputStream = file.getInputStream()) {
                // 写入文件
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }


            // 将上传的文件写入目标路径
            file.transferTo(targetLocation.toFile());

            return filePath;

        }catch (Exception e){
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }
    }

    private Path getPath(String filePath) throws IOException {
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();
        // 在 JAR 目录下创建 uploadDir 文件夹
        File pemDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String absolutePath = pemDir.getAbsolutePath();
        // 构建目标文件的完整路径
        Path targetLocation = Paths.get(absolutePath + File.separator + filePath);

        // 创建文件存储目录（如果不存在）
        Files.createDirectories(targetLocation.getParent());
        return targetLocation;
    }

    @Override
    public String upload(InputStream inputStream, String filePath) {
        // 检查文件是否为空
        if(inputStream == null){
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }
        // 处理文件存储
        try {
            Path targetLocation = this.getPath(filePath);
            try (inputStream) {
                // 写入文件
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return filePath;
        }catch (Exception e){
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }
    }
}
