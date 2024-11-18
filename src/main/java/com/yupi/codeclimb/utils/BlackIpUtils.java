package com.yupi.codeclimb.utils;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.alibaba.nacos.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

@Slf4j
public class BlackIpUtils {

    private static BitMapBloomFilter bloomFilter;

    /**
     * 判断ip是否在黑名单中
     * @param ip
     * @return
     */
    public static boolean isBlackIp(String ip){
        return bloomFilter.contains(ip);
    }

    //重建黑名单ip
    public static void rebuildBlackIp(String configInfo) {
        if(StringUtils.isBlank(configInfo)){
            configInfo = "{}";
        }
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        List<String> blackIpList = (List<String>) map.get("blackIpList");
        //加锁解决并发问题
        synchronized (BlackIpUtils.class) {
            //重建黑名单
            if(CollectionUtils.isNotEmpty(blackIpList)){
                BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(100);
                for (String ip : blackIpList) {
                    bitMapBloomFilter.add(ip);
                }
                bloomFilter = bitMapBloomFilter;
            }else{
                bloomFilter = new BitMapBloomFilter(100);
            }
        }

    }

}
