import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentTable {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField rollField, nameField;
    private JButton addButton;

    public StudentTable() {
        // JFrame
        frame = new JFrame("Student Records");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Panel for Input Fields & Button (Compact)
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        rollField = new JTextField(5);
        nameField = new JTextField(10);
        addButton = new JButton("Insert");
        inputPanel.add(new JLabel("Roll:"));
        inputPanel.add(rollField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(addButton);

        // Table Setup
        String[] columnNames = { "Roll No", "Name" };
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Load Data on Startup
        loadStudentData();

        // Button Action to Insert Data
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        // Add Components
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Display Frame
        frame.setVisible(true);
    }

    // Load Data from Database
    private void loadStudentData() {
        model.setRowCount(0); // Clear table before reloading
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/aswin", "root", "aswin@2004");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY roll_No ASC");

            while (rs.next()) {
                model.addRow(new Object[] { rs.getInt("roll_No"), rs.getString("name") });
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
    }

    // Insert Student Data
    private void addStudent() {
        try {
            int rollNo = Integer.parseInt(rollField.getText());
            String name = nameField.getText();

            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/aswin", "root", "aswin@2004");
            PreparedStatement ps = con.prepareStatement("INSERT INTO students(roll_No, name) VALUES (?, ?)");
            ps.setInt(1, rollNo);
            ps.setString(2, name);
            ps.executeUpdate();

            ps.close();
            con.close();

            // Refresh Table
            loadStudentData();

            // Clear Fields
            rollField.setText("");
            nameField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentTable());
    }
}
