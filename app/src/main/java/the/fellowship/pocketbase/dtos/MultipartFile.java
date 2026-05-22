package the.fellowship.pocketbase.dtos;

import java.io.FileDescriptor;

import okhttp3.MediaType;

public class MultipartFile {
    private final String field;
    private final String name;
    private String content;
    private FileDescriptor descriptor;
    private MediaType type;

    public MultipartFile(String field, String name, FileDescriptor descriptor) {
        this.field = field;
        this.name = name;
        this.descriptor = descriptor;
        this.type = MediaType.parse("application/octet-stream");
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
        return descriptor != null;
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

    public FileDescriptor getFileDescriptor() {
        return descriptor;
    }

    public MediaType getType() {
        return type;
    }
}