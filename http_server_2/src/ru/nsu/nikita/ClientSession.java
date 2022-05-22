package ru.nsu.nikita;

import javax.naming.spi.DirectoryManager;
import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import static ru.nsu.nikita.RequestType.*;

/**
 * Обрабатывает запрос клиента.
 */
public class ClientSession implements Runnable {

    private Socket socket;
    private InputStream in = null;
    private OutputStream out = null;

    private String DEFAULT_FILES_DIR = "./server_files";
    private Path DEFAULT_PATH;
    private static final String MAIN_REQUEST_PATTERN = "?name=";

    private RequestType requestType;

    @Override
    public void run() {
        try {
            /* Получаем заголовок сообщения от клиента */
            String header = readHeader();
            System.out.println(header + "\n");
            /* Получаем из заголовка указатель на интересующий ресурс */
            String request = parseHeader(header);
            System.out.println("Resource: " + request + "\n");
            /* Отправляем содержимое ресурса клиенту */
            int code = send(request);
            System.out.println("Result code: " + code + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ClientSession(Socket socket) throws IOException {
        requestType = ERROR;
        this.socket = socket;
        initialize();
    }

    private void initialize() throws IOException {
        /* Получаем поток ввода, в который помещаются сообщения от клиента */
        in = socket.getInputStream();
        /* Получаем поток вывода, для отправки сообщений клиенту */
        out = socket.getOutputStream();
        DEFAULT_PATH = computeDefaultPath();
    }

    private Path computeDefaultPath() {
        Path path = Path.of(DEFAULT_FILES_DIR);
        return path.normalize().toAbsolutePath();
    }
    /**
     * Считывает заголовок сообщения от клиента.
     *
     * @return строка с заголовком сообщения от клиента.
     * @throws IOException
     */
    private String readHeader() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String ln;
        while (true) {
            ln = reader.readLine();
            if (ln == null || ln.isEmpty()) {
                break;
            }
            builder.append(ln).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String parseHeader(String header) {
        String uri = getURIFromHeader(header);
        if (uri.contains("?")) {
            requestType = NAME;
            return getUserString(uri);
        } else {
            requestType = PATH;
            return DEFAULT_FILES_DIR + uri;
        }
    }

    private String getUserString(String uri) {
        int begin = uri.indexOf("=");
        if (uri.contains(MAIN_REQUEST_PATTERN)) {
            return uri.substring(begin + 1);
        } else {
            return "ERROR: Inappropriate parameter \"" + uri.substring(uri.indexOf("?"), begin + 1) + "\".";
        }
    }

    /**
     * Вытаскивает идентификатор запрашиваемого ресурса из заголовка сообщения от
     * клиента.
     *
     * @param header заголовок сообщения от клиента.
     * @return идентификатор ресурса.
     */
    private String getURIFromHeader(String header) {
        int from = header.indexOf(" ") + 1;
        int to = header.substring(from).indexOf(" ");

        return header.substring(from, to + from);
    }

    /**
     * Отправляет ответ клиенту. В качестве ответа отправляется http заголовок и
     * содержимое указанного ресурса. Если ресурс не указан, отправляется
     * перечень доступных ресурсов.
     *
     * @param request идентификатор запрашиваемого ресурса.
     * @return код ответа. 200 - если ресурс был найден, 404 - если нет.
     * @throws IOException
     */
    private int send(String request) throws IOException {
        int code;
        String answer = "";
        if (requestType == PATH) {
            Path path = Path.of(request).normalize().toAbsolutePath();
            if (Files.exists(path)) {
                answer = createFileAnswer(path);
                code = 200;
            } else {
                code = 404;
            }
        } else if (requestType == NAME) {
            answer = request;
            code = 200;
        } else {
            code = 404;
        }

        String header = getHeader(code);
        PrintStream answerStream = new PrintStream(out, true, "UTF-8");
        answerStream.print(header);
        if (code == 200) {
            int size = answer.getBytes().length;
            int chunk = 1024;
            int pos = 0;
            while (pos < size) {
                out.write(answer.getBytes(), pos, (pos + chunk) % answer.length());
                pos += chunk;
            }
        }
        return code;
    }

    private String createFileAnswer(Path path) throws IOException {

        Scanner scanner = new Scanner(path);
        StringBuilder answer = new StringBuilder();

        if (path.toString().contains(".txt") || path.toString().contains(".html")) {
            while (scanner.hasNextLine()) {
                answer.append(scanner.nextLine());
            }
        } else {
            DirectoryStream.Filter<Path> directoryContent = entry -> entry.getParent().equals(path);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, directoryContent)) {
                for (Path p : stream) {
                    answer.append(p.getFileName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return answer.toString();
    }

    /**
     * Возвращает http заголовок ответа.
     *
     * @param code код результата отправки.
     * @return http заголовок ответа.
     */
    private String getHeader(int code) {
        return "HTTP/1.1 " + code + " " + getAnswer(code) + "\n" +
                "Date: " + new Date() + "\n" +
                "Accept-Ranges: none\n" +
                "Content-Type: " + "?" + "\n" +
                "\n";
    }

    /**
     * Возвращает комментарий к коду результата отправки.
     *
     * @param code код результата отправки.
     * @return комментарий к коду результата отправки.
     */
    private String getAnswer(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            default:
                return "Internal Server Error";
        }
    }
}

