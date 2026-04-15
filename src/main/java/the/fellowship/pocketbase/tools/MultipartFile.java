package the.fellowship.pocketbase.tools;

import okhttp3.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MultipartFile {
    private final String field;
    private final File file;
    private MediaType type;

    public MultipartFile(String field, File file) {
        this.field = field;
        this.file = file;

        try {
            this.type = MediaType.parse(Files.probeContentType(file.toPath()));
        } catch (IOException e) {
            this.type = MediaType.parse("application/octet-stream");
        }
    }

    public String getField() {
        return field;
    }

    public String getName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public MediaType getType() {
        return type;
    }
}