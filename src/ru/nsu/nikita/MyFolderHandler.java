package ru.nsu.nikita;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.nsu.nikita.FileType.FILE;
import static ru.nsu.nikita.FileType.FOLDER;

public class MyFolderHandler extends MyFileHandler {

    protected List<MyFileHandler> children = new ArrayList<>();

    public MyFolderHandler(String name, MyFileHandler parent, List<MyFileHandler> children, FileType type) {
        super(name, parent, type);
        if (children != null) {
            this.children = children;
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response =
                "[" + name + "]" + " folder content: \n" +
                makeResponse();
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    protected String makeResponse() {
        StringBuilder response = new StringBuilder();
        for (MyFileHandler child : children) {
            response.append("\t- ").append(child.name).append("\n");
        }

        return response.toString();
    }

    public MyFileHandler addChild(String name, FileType type) {
        MyFileHandler child;
        switch (type) {
            case FILE -> child = new MyFileHandler(name, this, FILE);
            case FOLDER -> child = new MyFolderHandler(name, this, null, FOLDER);
            default -> {
                return null;
            }
        }

        children.add(child);
        return child;
    }

    public List<MyFileHandler> getChildren() {
        return children;
    }

    public void setChildren(List<MyFileHandler> children) {
        this.children = children;
    }
}
