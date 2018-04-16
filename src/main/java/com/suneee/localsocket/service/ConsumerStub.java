package com.suneee.localsocket.service;

import com.suneee.localsocket.bo.RabbitMQConf;
import com.suneee.localsocket.globaldata.GlobalDataConf;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by QueRenJie on ${date}
 */
@Service
public class ConsumerStub {
    private Map<String, RabbitMQConf> rabbitMQConfMap;

    public void init() {
        this.rabbitMQConfMap = GlobalDataConf.LOCAL_SOCKET_CONF.getRabbitMQConfMap();
    }

    /**
     * 启动所有消息队列监听
     */
    public void startAllConsumers() {
        if (this.rabbitMQConfMap != null) {
            for (Map.Entry<String, RabbitMQConf> entry : this.rabbitMQConfMap.entrySet()) {
                String subject = entry.getKey();
                new Thread(new ConsumerOfRabbitMQ(subject, this.rabbitMQConfMap)).start();
            }
        }
    }

}
