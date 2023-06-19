package modok;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class ServerThread extends Thread {
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

    // 拡張子からコンテントタイプを返す
    private static final HashMap<String, String> contentTypeMap = new HashMap<String, String>() {
        {
            put("html", "text/html");
            put("htm", "text/html");
            put("txt", "text/plain");
            put("css", "text/css");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("gif", "image/gif");
        }
    };

    // 拡張子からコンテントタイプを返す
    private static String getContentType(String ext) {
        String ret = contentTypeMap.get(ext.toLowerCase());
        if (ret == null) {
            return "application/octet-stream";
        } else {
            return ret;
        }
    }

    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();

            String line;
            String path = null;
            String ext = null;

            while ((line = readLine(input)) != null) {
                if (line.equals("")) {
                    break;
                }
                if (line.startsWith("GET")) {
                    String[] parts = line.split(" ");
                    path = parts[1];
                    String[] pathParts = path.split("\\.");
                    ext = pathParts[pathParts.length - 1];
                }
            }
            OutputStream output = socket.getOutputStream();

            writeLine(output, "HTTP/1.1 200 OK");
            writeLine(output, "Date: " + getDateStringUtc());
            writeLine(output, "Server: Modoki/0.1");
            writeLine(output, "Connection: close");
            assert ext != null;
            writeLine(output, "Content-type: " + getContentType(ext));
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

    public static void main(String[] args) throws Exception {
        try (ServerSocket server = new ServerSocket(8001)){
            while (true) {
                Socket socket = server.accept();
                ServerThread thread = new ServerThread(socket);
                thread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}