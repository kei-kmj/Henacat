package modoki01;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Modoki01 {
    // ドキュメントルートの設定。wsl側から見たパスを指定する。
    private static final String DOCUMENT_ROOT = "/mnt/c/mywebsite";

    // クライアントとの通信を、InputStreamとOutputStreamを使ってバイト単位で行う。
    // InputStreamから1行読み込むユーティリティメソッド
    private static String readLine(InputStream input) throws Exception {
        int ch;
        StringBuilder ret = new StringBuilder();
        while ((ch = input.read()) != -1) {
            if (ch == '\r') {
                // nothing
            } else if (ch == '\n') {
                break;
            } else {
                ret.append((char) ch);
            }
        }
        if (ch == -1) {
            return null;
        } else {
            return ret.toString();
        }
    }

    // OutputStreamに1行書き出すユーティリティメソッド
    private static void writeLine(OutputStream output, String str) throws Exception {
        for (char ch : str.toCharArray()) {
            output.write((int) ch);
        }
        output.write((int) '\r');
        output.write((int) '\n');
    }

    // 現在時刻からHTTP標準に合わせた日付文字列を返す
    private static String getDateStringUtc() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        df.setTimeZone(cal.getTimeZone());
        return df.format(cal.getTime()) + " GMT";
    }

    public static void main(String[] argv) throws Exception {
        try (ServerSocket server = new ServerSocket(8001)) {
            Socket socket = server.accept();

            InputStream input = socket.getInputStream();

            String line;
            String path = null;

            while ((line = readLine(input)) != null) {
                if (line.equals("")) {
                    break;
                }
                if (line.startsWith("GET")) {
                    path = line.split(" ")[1];
                }
            }
            OutputStream output = socket.getOutputStream();

            writeLine(output, "HTTP/1.1 200 OK");
            writeLine(output, "Date: " + getDateStringUtc());
            writeLine(output, "Server: Modoki/0.1");
            writeLine(output, "Connection: close");
            writeLine(output, "Content-type: text/html");
            writeLine(output, "");

            try (FileInputStream fis = new FileInputStream(DOCUMENT_ROOT + path)) {
                int ch;
                while ((ch = fis.read()) != -1) {
                    output.write(ch);
                }
            }
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}