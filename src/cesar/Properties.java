package cesar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

public class Properties {
    private static final java.util.Properties PROPERTIES;
    private final static String CONFIG_PATH = "/cesar/config.properties";

    static {
        InputStream stream = Properties.class.getResourceAsStream(CONFIG_PATH);
        PROPERTIES = new java.util.Properties();
        try {
            PROPERTIES.load(stream);

            // TODO: Encontrar um jeito de refatorar isto...
            FileWriter writer = new FileWriter(new File("./keys.txt"));
            for (Object key : PROPERTIES.keySet()) {
                writer.write((String) key);
                writer.write(System.lineSeparator());
            }
            writer.close();

        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    String.format("Não foi possível ler arquivo de configuração %s", CONFIG_PATH));
            System.exit(1);
        }
    }

    private Properties() {
    }

    public static String getProperty(final String key) {
        final String property = PROPERTIES.getProperty(key);
        if (property == null) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar ler a propriedade " + key, "Propriedade inválida",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
        return property;
    }
}
