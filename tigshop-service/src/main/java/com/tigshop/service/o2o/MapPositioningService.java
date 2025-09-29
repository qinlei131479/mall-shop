// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.o2o;


import com.tigshop.bean.vo.sys.RegionVO;

/**
 * @author Tigshop团队
 * @create 2025/8/27 14:35
 */
public interface MapPositioningService {

    RegionVO getLocationByIp(String ip);

    RegionVO getLocationByLocation(String location);
}
