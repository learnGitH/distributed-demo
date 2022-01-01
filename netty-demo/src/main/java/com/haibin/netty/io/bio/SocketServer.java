package com.haibin.netty.io.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * bio服务端
 * @author shb
 */
public class SocketServer {

    static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        while(true){
            System.out.println("等待客户端的连接。。");
            //阻塞方法,如果没有客户端连接就会一直阻塞在这里
            Socket clientSocket = serverSocket.accept();
            System.out.println("有客户端连接了。。。。。");
            //单线程的方式
            handler(clientSocket);

            //多线程的方式
           // threadPool.execute(() -> {
            //    try {
            //        handler(clientSocket);
            //    } catch (IOException e) {
            //        e.printStackTrace();
            //    }
            //});
        }
    }

    private static void handler(Socket clientSocket) throws IOException {
        byte[] bytes = new byte[12];
        System.out.println("准备read。。");
        //接收客户端的数据，阻塞方法，没有数据可读时就阻塞
        int read = clientSocket.getInputStream().read(bytes);
        System.out.println("read完毕。。");
        if (read != -1){
            System.out.println("接收到客户端的数据：" + new String(bytes,0,read));
        }
        clientSocket.getOutputStream().write("HelloClient".getBytes());
        clientSocket.getOutputStream().flush();
    }

}
