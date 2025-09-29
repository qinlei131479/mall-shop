// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.nio.file.Files;

/**
 * 获取ip2region.xdb文件
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
public class Ip2RegionUtil {

    public static String extractIp2RegionFile() throws IOException {
        // 读取 `resources` 里的 `ip2region.xdb`
        ClassPathResource resource = new ClassPathResource("ip2region.xdb");

        // 创建临时文件
        File tempFile = File.createTempFile("ip2region", ".xdb");
        tempFile.deleteOnExit(); // JVM 退出时删除

        // 拷贝文件到临时路径
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            inputStream.transferTo(outputStream);
        }

        // 返回临时文件的绝对路径
        return tempFile.getAbsolutePath();
    }

    /**
     * 根据ip获取地区信息
     * @return String
     * @throws Exception 抛出异常
     */
    public static String getRegionByIp() throws Exception {
        Searcher searcher = Searcher.newWithFileOnly(extractIp2RegionFile());
        String ipAddr = searcher.search(IpUtils.getIpAddr());
        searcher.close();
        return ipAddr.substring(2, 2);
    }
}
