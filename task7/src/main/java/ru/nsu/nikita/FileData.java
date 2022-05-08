package ru.nsu.nikita;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.stream.Collectors;

public class FileData extends FolderData {

    private List<String> content;

    public FileData(String name, FolderData parent) throws IOException {
        super(name, parent, null);

        if (Files.isRegularFile(absolutePath)) {
            content = Files.lines(absolutePath).collect(Collectors.toList());
        } else {
            content = null;
        }
    }

    public List<String> getContent() throws NoSuchFileException {
        if (content == null) {
            throw new NoSuchFileException(
                    "File in this path does not exists or not readable: \n"
                    + absolutePath);
        }
        return content;
    }
}
