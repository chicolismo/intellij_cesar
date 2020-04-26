package cesar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void centerComponent(final Component c) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension componentSize = c.getSize();
        final int x = (int) ((screenSize.getWidth() - componentSize.getWidth()) / 2);
        final int y = (int) ((screenSize.getHeight() - componentSize.getHeight()) / 2);
        c.setLocation(x, y);
    }

    public static void main(final String[] args) {
        try {
            if (System.getProperty("os.name").equals("Mac OS X")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

                ApplicationController controller = new ApplicationController();
                controller.run();
//                final Cpu cpu = new Cpu();
//                final MainWindow window = new MainWindow(cpu);
//                window.setLocationRelativeTo(null);
//                window.setVisible(true);
//                centerComponent(window);
//
            }
        });
    }
}
