package com.suneee.localsocket.service;

import com.myself.deployrequester.bo.DBScriptInfoForFileGenerate;
import com.myself.deployrequester.bo.TotalDBScriptInfoForFileGenerate;
import com.rabbitmq.client.*;
import com.suneee.localsocket.bo.RabbitMQConf;
import com.suneee.localsocket.globaldata.GlobalDataConf;
import com.suneee.localsocket.service.handler.AbstractHandler;
import com.suneee.localsocket.service.handler.CreateDbscriptFileHandler;
import com.suneee.localsocket.service.handler.Handler;
import com.suneee.localsocket.util.RabbitMQUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by QueRenJie on ${date}
 */
public class ConsumerOfRabbitMQ implements Runnable {
    private String subject;
    private Map<String, RabbitMQConf> rabbitMQConfMap = null;

    public ConsumerOfRabbitMQ(String subject, Map<String, RabbitMQConf> rabbitMQConfMap) {
        this.subject = subject;
        this.rabbitMQConfMap = rabbitMQConfMap;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (rabbitMQConfMap == null) {
            System.out.println("RabbitMQ服务信息没配好。");
            return;
        }

        RabbitMQUtil rabbitMQUtil = new RabbitMQUtil(rabbitMQConfMap);
        Channel channelForReceiveMsg = null;
        try {
            channelForReceiveMsg = rabbitMQUtil.getChannelForReceiveMsg(subject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        if (channelForReceiveMsg == null) {
            System.out.println("创建RabbitMQ Channel channelForReceiveMsg失败");
            return;
        }

        // 创建队列消费者
        final Consumer consumer = new DefaultConsumer(channelForReceiveMsg) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                if ("createDbscriptFile".equals(subject)) {
                    TotalDBScriptInfoForFileGenerate totalDBScriptInfoForFileGenerate = (TotalDBScriptInfoForFileGenerate) SerializationUtils.deserialize(body);
                    System.out.println(" [x] Received '" + totalDBScriptInfoForFileGenerate + "'");
                    System.out.println(" [x] Proccessing... at " + new Date().toLocaleString());

                    //TODO some work
                    Handler handler = new CreateDbscriptFileHandler(totalDBScriptInfoForFileGenerate, subject, rabbitMQConfMap);
                    handler.execute();

                    System.out.println(" [x] Done! at " + new Date().toLocaleString());
                }
            }
        };

        //监听消息队列
        try {
            rabbitMQUtil.receiveListening(channelForReceiveMsg, consumer);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RabbitMQConf rabbitMQConf = rabbitMQConfMap.get(subject);
        System.out.println("RabbitMQ（主题：" + subject + "|队列配置信息：" + rabbitMQConf.toString() + "） is started.");

    }
}
