// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryPicResDTO;
import com.tigshop.bean.model.setting.GalleryPic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 相册图片列表返回参数
 *
 * @author Jayce
 * @create 2024年11月13日 16:00
 */
@Schema(description = "相册图片列表返回参数")
@Setter
@Getter
public class GalleryListResVO {
    @Schema(description = "子相册列表")
    private List<GalleryPicResDTO> childGalleryList;

    @Schema(description = "过滤结果")
    private Page<GalleryPic> galleryPicPage;

    @Schema(description = "相册信息")
    private GalleryDTO galleryInfo;

    public GalleryListResVO(List<GalleryPicResDTO> childGalleryList, Page<GalleryPic> galleryPicPage, GalleryDTO galleryInfo) {
        this.childGalleryList = childGalleryList;
        this.galleryPicPage = galleryPicPage;
        this.galleryInfo = galleryInfo;
    }
}
