package com.suneee.localsocket.service.handler;

import com.myself.deployrequester.bo.DBScriptInfoForFileGenerate;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by QueRenJie on ${date}
 */
public class SocketMsgHandler implements Runnable {
    Socket socket = null;

    public SocketMsgHandler(Socket socket) {
        this.socket = socket;
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
//        OutputStream os = null;
//        PrintWriter pw = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("客户端发送的对象：" + (List<DBScriptInfoForFileGenerate>) ois.readObject());
            socket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
