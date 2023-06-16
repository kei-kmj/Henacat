package tcp;

import java.io.*;
import java.net.*;

public class TcpServer {
    public static void main(String[] argv) throws Exception {
        // socketというライブラリを使ってTCP通信を行う
        // ソケットによる通信は、
        // サーバ側でServerSocketクラスを使ってソケットを生成し、クライアントからの接続を待ち、
        // クライアント側でソケットを生成し、サーバのホストとポートを指定して接続を行う
        // これで任意のデータを双方向に送ることが出来る伝送路が作られる

        // ソケットを生成
        try (ServerSocket server = new ServerSocket(8001);
             FileOutputStream fos = new FileOutputStream("server_recv.txt");
             FileInputStream fis = new FileInputStream("server_send.txt")) {
            System.out.println("wait");
            Socket socket = server.accept();
            System.out.println("connected");

            int ch;
            InputStream input = socket.getInputStream();
            while ((ch = input.read()) != 0) {
                fos.write(ch);
            }
            OutputStream output = socket.getOutputStream();
            while ((ch = fis.read()) != -1) {
                output.write(ch);
            }
            socket.close();
            System.out.println("finished");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}