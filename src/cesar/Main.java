package cesar;

import cesar.gui.windows.MainWindow;
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
            //            for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            //                if ("CDE/Motif".equals(info.getName())) {
            //                    UIManager.setLookAndFeel(info.getClassName());
            //                    break;
            //                }
            //            }
        }
        catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ToolTipManager ttm = ToolTipManager.sharedInstance();
                ttm.setInitialDelay(300);

                final MainWindow mainWindow = new MainWindow();
                final ApplicationController applicationController = new ApplicationController(mainWindow);
                applicationController.run();
            }
        });
    }
}
