package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册信息")
public class GalleryVideoInfoDetailVO {

    @Schema(description = "视频相册信息id")
    private Integer id;

    @Schema(description = "视频相册分组id")
    private List<Integer> galleryId;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "视频封面")
    private String videoCover;

}
