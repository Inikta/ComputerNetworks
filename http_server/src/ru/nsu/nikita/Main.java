package ru.nsu.nikita;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

import static ru.nsu.nikita.FileType.FILE;
import static ru.nsu.nikita.FileType.FOLDER;

public class Main {

    public static HttpServer server;
    public static MyFolderHandler root = new MyFolderHandler("root", null, null, FOLDER);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/root", root);
        init(server);
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started!");

    }

    public static void init(HttpServer server) {

        addContext("Vasya", root, FOLDER, server);
        addContext("Main", root, FOLDER, server);
        addContext("folder3", root, FOLDER, server);
        addContext("readme.txt", root, FILE, server);
        addContext("readme.html", root, FILE, server);

        addContext("Vasya.html", root.children.get(0), FILE, server);
        addContext("Vasya.txt", root.children.get(0), FILE, server);

        addContext("Main.html", root.children.get(1), FILE, server);

        //addContext("Main.html", root.children.get(1), FOLDER, server);
    }

    public static void addContext(String name, MyFileHandler parent, FileType type, HttpServer server) {
        MyFileHandler child = ((MyFolderHandler) parent).addChild(name, type);
        server.createContext(child.getPathServer(), child);
    }
}
