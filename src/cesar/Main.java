package cesar;

import cesar.gui.windows.MainWindow;
import cesar.utils.Defaults;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.Locale;

public class Main {
    public static void main(final String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt_BR"));

        try {
            if (Defaults.isApple()) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException
                | ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ToolTipManager ttm = ToolTipManager.sharedInstance();
                ttm.setInitialDelay(300);

                MainWindow mainWindow = new MainWindow();
                ApplicationController applicationController = new ApplicationController(mainWindow);
                applicationController.run();
            }
        });
    }
}
