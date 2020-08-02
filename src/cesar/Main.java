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
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            // if ("CDE/Motif".equals(info.getName())) {
            // UIManager.setLookAndFeel(info.getClassName());
            // break;
            // }
            // }
        }
        catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolTipManager.sharedInstance().setInitialDelay(300);
                new ApplicationController().run();
            }
        });
    }
}
