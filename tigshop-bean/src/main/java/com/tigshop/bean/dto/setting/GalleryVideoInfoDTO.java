package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册信息保存参数")
public class GalleryVideoInfoDTO {
    @Schema(description = "视频相册信息id")
    private Integer id;

    @NotEmpty(message = "未选择视频库")
    @Schema(description = "视频相册分组id")
    private List<Integer> galleryId;

    @NotBlank(message = "视频地址不能为空")
    @Schema(description = "视频地址")
    private String videoUrl;

    @NotBlank(message = "视频名称不能为空")
    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频格式")
    private String format;

    @Schema(description = "视频封面（第一帧）")
    private String videoFirstFrame;

    @Schema(description = "视频时长")
    private String duration;

    @Schema(description = "视频大小")
    private String size;
}
