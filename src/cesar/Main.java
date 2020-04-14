package cesar;

import cesar.gui.windows.MainWindow;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void centerComponent(Component c) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension componentSize = c.getSize();
        final int x = (int) ((screenSize.getWidth() - componentSize.getWidth()) / 2);
        final int y = (int) ((screenSize.getHeight() - componentSize.getHeight()) / 2);
        c.setLocation(x, y);
    }

    public static void main(String[] args) {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainWindow window = new MainWindow();
                window.setVisible(true);
                centerComponent(window);
            }
        });
    }
}
