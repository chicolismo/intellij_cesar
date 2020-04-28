package cesar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public class ApplicationProperties {
    private static final Properties PROP;
    private final static String filename = "/cesar/config.properties";

    static {
        InputStream stream = ApplicationProperties.class.getResourceAsStream(filename);
        PROP = new Properties();
        try {
            PROP.load(stream);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    String.format("Não foi possível ler arquivo de configuração %s", filename));
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ApplicationProperties() {
    }

    public static String getProperty(final String propertyName) {
        return PROP.getProperty(propertyName);
    }
}
