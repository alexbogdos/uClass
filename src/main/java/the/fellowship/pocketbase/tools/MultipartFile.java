package the.fellowship.pocketbase.tools;

import java.io.File;

public record MultipartFile(String fieldName, File file, String mediaType) {
}