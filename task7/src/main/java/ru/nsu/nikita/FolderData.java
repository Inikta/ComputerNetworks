package ru.nsu.nikita;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FolderData {
    protected String name;
    protected FolderData parent;
    protected Path absolutePath;
    protected List<String> children;

    public FolderData(String name, FolderData parent, List<String> children) {
        this.name = name;
        this.parent = parent;
        this.children = children;
        absolutePath = Paths.get(makePathStr());
    }

    protected String makePathStr() {
        List<String> pathList = generatePath();
        StringBuilder path = new StringBuilder();
        for (String node : pathList) {
            path.append("/").append(node);
        }

        return path.toString();
    }

    protected List<String> generatePath() {
        List<String> pathStr = new ArrayList<>();
        if (parent != null) {
            pathStr.addAll(parent.generatePath());
        } else {
            pathStr.add("server_files");
        }
        pathStr.add(name);
        return pathStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FolderData getParent() {
        return parent;
    }

    public void setParent(FolderData parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
