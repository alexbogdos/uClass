package the.fellowship.pocketbase.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import okhttp3.MediaType;

public class MultipartFile {
    private final String field;
    private final String name;
    private String content;
    private File file;
    private MediaType type;

    public MultipartFile(String field, File file) {
        this.field = field;
        this.file = file;
        this.name = file.getName();

        try {
            this.type = MediaType.parse(Files.probeContentType(file.toPath()));
        } catch (IOException e) {
            this.type = MediaType.parse("application/octet-stream");
        }
    }

    public MultipartFile(String field, String content) {
        this(field, null, content);
    }

    public MultipartFile(String field, String name, String content) {
        this.field = field;
        this.name = name;
        this.content = content;
    }

    public boolean hasFile() {
        return file != null;
    }

    public String getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }

    public MediaType getType() {
        return type;
    }
}