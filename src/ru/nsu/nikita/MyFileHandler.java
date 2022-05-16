package ru.nsu.nikita;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Scanner;

public class MyFileHandler implements HttpHandler {

    protected String name;
    protected String pathStr;
    protected MyFileHandler parent;
    private FileType type;

    public MyFileHandler(String name, MyFileHandler parent, FileType type) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            pathStr = parent.pathStr + "/" + name;
        } else {
            pathStr = "/" + name;
        }
        this.type = type;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = makeResponse();
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    protected String makeResponse() throws FileNotFoundException {
        StringBuilder response = new StringBuilder();
        Scanner scanner = new Scanner(Path.of(pathStr).toFile());
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine());
        }

        return response.toString();
    }

    public String getPathStr() {
        return pathStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyFileHandler getParent() {
        return parent;
    }

    public void setParent(MyFileHandler parent) {
        this.parent = parent;
    }

    public FileType getType() {
        return type;
    }
}
