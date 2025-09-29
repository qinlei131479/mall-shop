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

package com.tigshop.service.msg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 管理员消息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface AdminMsgService extends BaseService<AdminMsg> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<AdminMsgVO> list(AdminMsgListDTO listDTO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     */
    void createMessage(AdminMsgCreateDTO createDTO);

    /**
     * 设置已读
     *
     * @param msgId id
     * @return boolean
     */
    boolean setReaded(Integer msgId);

    /**
     * 设置所有已读
     *
     * @return boolean
     */
    boolean setAllReaded();

    /**
     * 获取未读消息数量
     *
     * @return Long
     */
    Long getUnreadAdminMsgCount();

    /**
     * 获取消息类型列表
     *
     * @return List<AdminMsgTypeListVO>
     */
    List<AdminMsgTypeListVO> getAdminMsgTypeList();

}
