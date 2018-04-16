package com.suneee.localsocket.util;

import com.myself.deployrequester.bo.StatusMessageCarrier;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.suneee.localsocket.bo.RabbitMQConf;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by QueRenJie on ${date}
 */
public class RabbitMQUtil {
    private Map<String, RabbitMQConf> rabbitMQConfMap = null;
    private RabbitMQConf rabbitMQConf = null;
    private String clientIpAddr = null;
    private String subject = null;

    public RabbitMQUtil(Map<String, RabbitMQConf> rabbitMQConfMap) {
        this.rabbitMQConfMap = rabbitMQConfMap;
        try {
            this.clientIpAddr = InetAddress.getLocalHost().getHostAddress();//获得本机IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查实例化本类的对象后相关属性中是否已有数据，只要有其中一项属性没数据就返回false。
     * @return
     */
    private boolean checkParams() {
        if (this.rabbitMQConfMap == null) {
            System.out.println("未能获取到配置文件中的RabbitMQ的配置");
            return false;
        }
        if (this.clientIpAddr == null) {
            System.out.println("未能获取到本机的IP地址");
            return false;
        }
        return true;
    }

    /**
     * 创建channel
     * @param subject
     * @return
     */
    private Channel createChannel(String subject) throws IOException, TimeoutException {
        if (!checkParams()) {
            return null;
        }
        if (StringUtils.isBlank(subject)) {
            System.out.println("主题不能为空");
            return null;
        }
        this.subject = subject;

        rabbitMQConf = rabbitMQConfMap.get(subject);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQConf.getRabbitmqHost());
        factory.setUsername(rabbitMQConf.getUserName());
        factory.setPassword(rabbitMQConf.getPassword());
        factory.setPort(rabbitMQConf.getRabbitmqPort());
        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

    /**
     * 生成一个用于接收消息的channel
     * @param subject               消息主题，由此可定位消息队列是哪个
     * @return
     */
    public Channel getChannelForReceiveMsg(String subject) throws IOException, TimeoutException {
        Channel channel = createChannel(subject);
        if (channel == null) {
            return null;
        }

        // 声明队列，主要为了防止消息接收者先运行此程序，队列还不存在时创建队列。
        try {
            channel.queueDeclare(rabbitMQConf.getQueueName() + "_" + clientIpAddr, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return channel;
    }

    /**
     * 启动RabbitMQ消息接收的监听服务
     * @param channelForReceiveMsg
     * @param consumer
     * @throws IOException
     */
    public void receiveListening(Channel channelForReceiveMsg, Consumer consumer) throws IOException {
        if (channelForReceiveMsg == null || consumer == null) {
            return;
        }
        channelForReceiveMsg.basicConsume(this.rabbitMQConf.getQueueName() + "_" + clientIpAddr, true, consumer);
    }

    /**
     * 创建用于向服务器端发状态消息的Channel
     * @param subject
     * @return
     */
    private Channel getChannelForSendStatusMsg(String subject) throws IOException, TimeoutException {
        Channel channel = createChannel(subject);
        if (channel == null) {
            return null;
        }

        // 指定一个队列
        channel.queueDeclare(rabbitMQConf.getQueueName() + "_status_toserver", false, false, false, null);
        return channel;

    }

    /**
     * 向服务器端发送状态消息
     * @param subject
     * @param infoList
     * @throws IOException
     * @throws TimeoutException
     */
    public void sendStatusInfo(String subject, List<String> infoList) throws IOException, TimeoutException {
        Channel channelForSendStatusMsg = getChannelForSendStatusMsg(subject);
        if (channelForSendStatusMsg == null) {
            System.out.println("channelForSendStatusMsg is null");
            return;
        }
        StatusMessageCarrier statusMessageCarrier = new StatusMessageCarrier();
        statusMessageCarrier.setClientIp(clientIpAddr);
        statusMessageCarrier.setMessageList(infoList);
        statusMessageCarrier.setSubject(subject);

        channelForSendStatusMsg.basicPublish("", rabbitMQConf.getQueueName() + "_status_toserver", null, SerializationUtils.serialize(statusMessageCarrier));
    }
}
