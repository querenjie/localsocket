package com.suneee.localsocket.bo;

import java.util.Map;

/**
 * 其中的内容参见localsocket.conf文件
 * Created by QueRenJie on ${date}
 */
public class LocalsocketConf {
    public Map<String, String> projectCodeMapPath;      //每个项目对应的本地的脚本文件的目录（目录中不包括日期目录）
    public String errorMsg;                             //记录读配置文件时发生的错误提示
    public Map<String, RabbitMQConf> rabbitMQConfMap;   //主题和RabbitMQ配置关系映射

    public Map<String, String> getProjectCodeMapPath() {
        return projectCodeMapPath;
    }

    public void setProjectCodeMapPath(Map<String, String> projectCodeMapPath) {
        this.projectCodeMapPath = projectCodeMapPath;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Map<String, RabbitMQConf> getRabbitMQConfMap() {
        return rabbitMQConfMap;
    }

    public void setRabbitMQConfMap(Map<String, RabbitMQConf> rabbitMQConfMap) {
        this.rabbitMQConfMap = rabbitMQConfMap;
    }
}
