package cesar;

import cesar.controllers.ApplicationController;
import cesar.utils.Defaults;

import javax.swing.*;
import java.util.Locale;

public class Main {
    public static void main(final String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt_BR"));

        try {
            if (Defaults.IS_APPLE) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ToolTipManager.sharedInstance().setInitialDelay(300);
            new ApplicationController().run();
        });
    }
}
