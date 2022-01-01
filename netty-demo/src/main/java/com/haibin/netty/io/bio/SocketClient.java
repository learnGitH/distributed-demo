package com.haibin.netty.io.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * bio客户端
 * @author shb
 */
public class SocketClient {

    public static void main(String[] args) throws IOException {
        //连接服务器的地址和端口
        Socket socket = new Socket("localhost",8888);
        //向服务器发送数据
        socket.getOutputStream().write("bio客户端测试".getBytes());
        socket.getOutputStream().flush();
        System.out.println("向服务端发送数据结束!!!!!");
        byte[] bytes = new byte[1024];
        //接收服务端回传的数据
        socket.getInputStream().read(bytes);
        System.out.println("接收到服务端的数据：" + new String(bytes));
        socket.close();
    }

}
