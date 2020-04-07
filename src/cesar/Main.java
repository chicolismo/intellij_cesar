package cesar;

import javax.swing.*;
import cesar.gui.MainWindow;

import java.awt.*;

public class Main {
    public static void centerComponent(Component c) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension componentSize = c.getSize();
        int x = (int) ((screenSize.getWidth() - componentSize.getWidth()) / 2);
        int y = (int) ((screenSize.getHeight() - componentSize.getHeight()) / 2);
        c.setLocation(x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
            centerComponent(window);
        });
    }
}
