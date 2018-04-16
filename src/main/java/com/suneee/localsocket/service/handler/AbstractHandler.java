package com.suneee.localsocket.service.handler;

/**
 * Created by QueRenJie on ${date}
 */
public class AbstractHandler implements Handler {
    @Override
    public void execute() {
        handleDBScriptFile();
        feedbackToServer();
    }

    /**
     * 处理脚本文件
     */
    protected void handleDBScriptFile() {}

    /**
     * 反馈脚本处理完后的消息
     */
    protected void feedbackToServer() {}
}
