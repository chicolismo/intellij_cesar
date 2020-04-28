package cesar;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class Properties {
    private static final java.util.Properties properties;
    private final static String filename = "/cesar/config.properties";

    static {
        InputStream stream = Properties.class.getResourceAsStream(filename);
        properties = new java.util.Properties();
        try {
            properties.load(stream);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    String.format("Não foi possível ler arquivo de configuração %s", filename));
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Properties() {
    }

    public static String getProperty(final String propertyName) {
        return properties.getProperty(propertyName);
    }
}
