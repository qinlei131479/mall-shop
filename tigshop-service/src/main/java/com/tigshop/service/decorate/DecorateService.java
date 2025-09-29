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

package com.tigshop.service.decorate;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.DecorateCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateListDTO;
import com.tigshop.bean.dto.decorate.DecorateUpdateDTO;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.service.common.BaseService;

import java.util.Map;

/**
 * 页面管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface DecorateService extends BaseService<Decorate> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<DecorateVO> list(DecorateListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    DecorateVO detail(Integer id, Integer localeId, Integer parentId, Integer decorateType);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(DecorateCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(DecorateUpdateDTO updateDTO);

    /**
     * 复制
     *
     * @param id 更新参数
     * @return boolean
     */
    boolean copy(Integer id);

    /**
     * 设置为首页
     *
     * @param id 更新参数
     * @return boolean
     */
    boolean setHome(Integer id);

    /**
     * 保存为草稿
     *
     * @param id 更新参数
     * @return boolean
     */
    boolean saveDecorateToDraft(Integer id, JSONObject data, Integer parentId, Integer localeId);

    /**
     * 发布
     * @param id 装修id
     * @param data 数据
     * @return boolean
     */
    boolean publishDecorate(Integer id, JSONObject data, Integer parentId, Integer localeId);

    /**
     * 初始化装修
     * @param shopId 店铺id
     */
    void initDecorate(Integer shopId);

    /**
     * 获取PC首页装修页面配置
     * @param id 装修id
     * @return JSONObject
     */
    JSONObject getPcIndexDecoratePageConfig(Integer id);

    /**
     * 店铺装修
     * @param shopId 店铺id
     * @return item
     */
    JSONObject getShopIndexDecorateData(Integer shopId);

    /**
     * 获取页面模块
     * @param type 类型
     * @param isHome 是否是首页
     * @param status 状态
     * @return JSONObject
     */
    DecorateModuleListVO getDecorateModule(Integer type, Boolean isHome, Integer status);

    /**
     * 获取App首页装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getAppHomeDecorate();

    /**
     * 获取PC首页装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getPcHomeDecorate();

    /**
     * 获取PC预览装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getPcPreviewDecorate(Integer decorateId);

    /**
     * 获取App预览装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getAppPreviewDecorate(Integer decorateId);

    /**
     * 获取预览装修页面配置
     * @param type 类型
     * @param decorateId 装修id
     * @return JSONObject
     */
    DecorateModuleListVO getPreviewDecorate(Integer type, Integer decorateId);

    JSONObject loadDraftData(Integer id);

    Object getPreviewDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params);

    Object getDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params);
}
