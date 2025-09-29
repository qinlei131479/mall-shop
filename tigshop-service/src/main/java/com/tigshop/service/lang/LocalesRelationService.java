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

package com.tigshop.service.lang;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.lang.LocalesRelationDTO;
import com.tigshop.bean.model.lang.LocalesRelation;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.bean.vo.setting.LocalesRelationVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.common.BaseService;

/**
 * 语言关联服务
 *
 * @author Tigshop团队
 * @create 2024年12月31日 15:30
 */
public interface LocalesRelationService extends BaseService<LocalesRelation> {
    /**
     * 获取列表
     *
     * @param page 列表参数
     * @return ListResVO
     */
    Page<LocalesRelationVO> list(BasePage page);

    /**
     * 获取详情
     *
     * @param id 主键
     * @return LocalesRelationVO
     */
    LocalesRelationVO detail(Integer id);

    /**
     * 创建
     *
     * @param dto 创建参数
     * @return boolean
     */
    boolean create(LocalesRelationDTO dto);

    /**
     * 更新
     *
     * @param dto 更新参数
     * @return boolean
     */
    boolean update(LocalesRelationDTO dto);

    /**
     * 获取默认语言
     * @param code 语言代码
     * @return LocalesVO
     */
    LocalesVO getDefaultLocale(String code);
}
