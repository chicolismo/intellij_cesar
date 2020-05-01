package cesar.utils;

import java.util.regex.Pattern;

public class FileUtils {
    private static final Pattern SPLIT_EXTENSIONS_PATTERN = Pattern.compile("\\s*,\\s*");

    public static String[] splitExtensions(final String extensions) {
        return SPLIT_EXTENSIONS_PATTERN.split(extensions);
    }
}
