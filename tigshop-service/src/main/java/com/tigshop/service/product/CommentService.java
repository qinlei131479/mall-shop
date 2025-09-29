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

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.product.comment.CommentEvaluateParam;
import com.tigshop.bean.dto.product.CommentReplyDTO;
import com.tigshop.bean.dto.product.CommentUpdateDTO;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.param.product.comment.CommentSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.vo.product.CommentSubNumVO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.bean.vo.product.ProductCommentStatisticVO;
import com.tigshop.service.common.BaseService;

/**
 * 评价管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CommentService extends BaseService<Comment> {

    /**
     * 列表
     */
    Page<CommentVO> list(CommentListPageQuery pageQuery);

    /**
     * 店铺评价列表
     * @param pageQuery
     * @return
     */
    Page<CommentVO> listByShopId(CommentListPageQuery pageQuery);

    /**
     * 详情
     */
    CommentVO detail(Integer id);

    /**
     * 创建
     */
    void create(CommentSaveParam param);

    /**
     * 更新
     */
    boolean update(CommentUpdateDTO updateDTO);

    /**
     * 统计商品评价
     */
    ProductCommentStatisticVO getProductCommentDetail(Integer productId, Integer shopId);

    /**
     * 评论晒单数量
     */
    CommentSubNumVO subNum(Integer userId);

    /**
     * 评论晒单
     */
    void evaluate(CommentEvaluateParam param);

    /**
     * 回复评论
     */
    boolean replyComment(CommentReplyDTO dto, Integer adminId);
}
