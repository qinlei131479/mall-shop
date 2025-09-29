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

package com.tigshop.service.setting;

import com.tigshop.bean.dto.setting.GalleryPicDTO;
import com.tigshop.bean.dto.setting.GalleryPicListDTO;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.vo.config.GalleryListResVO;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.service.common.BaseService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 相册图片服务
 *
 * @author Jayce
 * @create 2024年11月12日 13:47
 */
public interface GalleryPicService extends BaseService<GalleryPic> {
    /**
     * 相册图片列表
     *
     * @param dto 查询参数
     * @return GalleryListResVO
     */
    GalleryListResVO list(GalleryPicListDTO dto);

    /**
     * 相册图片列表
     *
     * @param galleryId 相册id
     * @return List<GalleryPicDTO>
     */
    List<GalleryPicDTO> getPicsByGid(Integer galleryId);

    /**
     * 上传图片
     * @param galleryId 相册id
     * @param file 文件
     * @return GalleryPicUploadVO
     */
    GalleryPicUploadVO uploadImg(Integer galleryId, MultipartFile file);

    /**
     * 查看图片
     * @param timestamp 时间戳
     * @param filename 文件名
     * @return ResponseEntity<Resource>
     */
    ResponseEntity<Resource> selectFile(String timestamp, String filename, String fileType);


    /**
     * 获取图片尺寸
     * @return Map<String, String>
     */
    Map<String, String> getImageSize();
}
