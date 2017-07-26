package com.wiggins.retrofit.entity;

/**
 * @Description json数据解析工具类
 * @Author 一花一世界
 */
public class FromJsonUtils {

    private Class cls;
    private String json;

    public FromJsonUtils(Class cls, String json) {
        this.cls = cls;
        this.json = json;
    }

    public Common fromJson() {
        Common result = null;
        try {
            result = Common.fromJson(json, cls);
        } catch (Exception e) {
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    public CommonList fromJsonList() {
        CommonList listResult = null;
        try {
            listResult = CommonList.fromJson(json, cls);
        } catch (Exception e) {
            listResult = null;
            e.printStackTrace();
        }
        return listResult;
    }
}
