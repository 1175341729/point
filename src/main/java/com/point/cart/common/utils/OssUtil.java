package com.point.cart.common.utils;


import com.aliyun.oss.OSSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OssUtil {
    private static Logger logger = LoggerFactory.getLogger(OssUtil.class);

    private final static String ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
    private final static String ACCESSKEYID = "LTAISgmGDGFTmEE4";
    private final static String ACCESSKEYSECRET = "EXxOjx60rjCTD1AomCJrrrQsNyP9gW";
    private final static String BUCKETNAME = "dftcpoint";

    private OssUtil() {};

    private static OSSClient ossClient = null;

    private synchronized static OSSClient getInstance() {
        if (null == ossClient) {
            ossClient = new OSSClient(ENDPOINT,ACCESSKEYID,ACCESSKEYSECRET);
        }
        return ossClient;
    }

    /**
     * 上传文件
     * @param filePath
     */
    public static void putFile(String key,String filePath){
        getInstance();
        File file = new File(filePath);
        ossClient.putObject(BUCKETNAME,key,file);
    }

    /**
     * 生成bucket
     * @param bucketName
     */
    public static void createBucket(String bucketName){
        OSSClient client = getInstance();
        client.createBucket(bucketName);
        client.shutdown();
    }

//    public static void main(String[] args) {
//        getInstance();
//        // createBucket("dftcpoint");
//        // putFile("151133561770.xls");
//    }
}
