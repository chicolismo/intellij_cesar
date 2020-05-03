package cesar.utils;

import java.util.regex.Pattern;

public class FileUtils {
    private static final Pattern SPLIT_EXTENSIONS_PATTERN = Pattern.compile("\\s*,\\s*");

    public static String getExtension(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        String extension = "";
        if (index + 1 < fileName.length()) {
            extension = fileName.substring(index + 1);
        }
        return extension;
    }

    public static String[] splitExtensions(final String extensions) {
        return SPLIT_EXTENSIONS_PATTERN.split(extensions);
    }
}
