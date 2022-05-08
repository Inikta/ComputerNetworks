package ru.nsu.nikita;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

//TO-DO:
/*  Добавить обобщенный способ создания httphandler, который:
    - если папка, то выведет ее содержимое списком
    - если файл, то его содержимое (html-оформление?)
        типы файлов: txt, html
* */

public class Main {

    private FolderData root;

    public static void main(String[] args) throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),8000);
        HttpServer server = HttpServer.create(socketAddress, 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
