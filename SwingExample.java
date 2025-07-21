import javax.swing.*;

public class SwingExample {
    public static void main(String[] args) {
        // Create a JFrame (window)
        JFrame frame = new JFrame("Swing Example");

        // Create a JLabel (message label)
        JLabel label = new JLabel("Hello, Welcome to Java Swing!", SwingConstants.CENTER);

        // Set window size and close operation
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add the label to the frame
        frame.add(label);

        // Make the window visible
        frame.setVisible(true);
    }
}
