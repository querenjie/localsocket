package com.suneee.localsocket.service;

import com.suneee.localsocket.bo.LocalsocketConf;
import com.suneee.localsocket.bo.RabbitMQConf;
import com.suneee.localsocket.globaldata.GlobalDataConf;
import com.suneee.localsocket.service.core.AbstractInitialCore;
import com.suneee.localsocket.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 服务启动时预加载内容到内存
 * Created by QueRenJie on ${date}
 */
@Service
public class InitService extends AbstractInitialCore {
    @Autowired
    private ConsumerStub consumerStub;

    @Override
    protected void buildConfig() {
        loadConfigFile();
    }

    @Override
    protected void startListener() {
        startRabbitMQListener();
    }

    /**
     * 加载配置文件中的内容到内存
     */
    private void loadConfigFile() {
        LocalsocketConf localsocketConf = FileUtil.readConf();
        GlobalDataConf.LOCAL_SOCKET_CONF = localsocketConf;
    }


    private void startRabbitMQListener() {
        consumerStub.init();
        consumerStub.startAllConsumers();
    }
}
