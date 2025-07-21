import java.util.ArrayList;
import java.sql.*;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Schedular_backend_program {
    public static int day;
    public static int period;

    // public static void main(String[] args) {
    // try {
    // Schedular_backend();
    // } catch (Exception e) {
    // System.out.println(e);
    // }
    // }

    public static void Schedular_backend() throws ClassNotFoundException, SQLException {
        int i, j;
        // int day, period;
        String url = "jdbc:mysql://localhost:3306/timesync";
        String user = "root";
        String password = "aswin@2004";
        Random rand = new Random();

        System.out.println("God is Love");

        // Load MySQL JDBC Driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Establish Connection
        Connection conn = DriverManager.getConnection(url, user, password);

        ArrayList<String> facultyNames = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            // Fetch distinct faculty names
            String query = "select day,period from dayperiod";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                day = rs.getInt("day");
                period = rs.getInt("period");
            }

            query = "SELECT DISTINCT faculty FROM map";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                facultyNames.add(rs.getString("faculty"));
            }
            String truncate_table = "truncate table faculty_time_table";
            stmt.executeUpdate(truncate_table);
            truncate_table = "truncate table class_time_table";
            stmt.executeUpdate(truncate_table);

            rs.close();
            stmt.close();

            // Fetch distinct class names
            query = "SELECT DISTINCT class FROM map";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                classNames.add(rs.getString("class"));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Database connection error!");
            e.printStackTrace();
        }

        // Faculty List
        ArrayList<Faculty> originalFacultyList = new ArrayList<>();
        for (String s : facultyNames) {
            originalFacultyList.add(new Faculty(s));
        }

        // Class List
        ArrayList<ClassDivision> classList = new ArrayList<>();
        for (String c : classNames) {
            classList.add(new ClassDivision(c));
        }

        // Scheduling process
        for (ClassDivision classDiv : classList) {
            ArrayList<Faculty> selectedFacultyList = new ArrayList<>();
            ArrayList<String> subjects = new ArrayList<>();
            ArrayList<Integer> subjectHours = new ArrayList<>();

            try {
                // Fetch subjects, faculty, and hours for the current class
                String query = "SELECT subject, faculty, hours FROM map WHERE class = '" + classDiv.name + "'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String facultyName = rs.getString("faculty");
                    for (Faculty f : originalFacultyList) {
                        if (facultyName.equals(f.name)) {
                            selectedFacultyList.add(f);
                            break;
                        }
                    }
                    subjects.add(rs.getString("subject"));
                    subjectHours.add(rs.getInt("hours"));
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("Database connection error!");
                e.printStackTrace();
            }

            boolean allClear = false;
            while (!allClear) {
                // Copy faculty list to avoid modifying original data
                ArrayList<Faculty> currentFacultyList = new ArrayList<>();
                for (Faculty f : selectedFacultyList) {
                    currentFacultyList.add(new Faculty(f));
                }

                classDiv.connector = new HashMap<>();
                for (i = 0; i < subjects.size(); i++) {
                    classDiv.connector.put(subjects.get(i),
                            new SubjectAllocation(currentFacultyList.get(i).name, subjectHours.get(i)));
                }

                int hourCount = 0;
                while (hourCount < 40) {
                    i = hourCount / 8;
                    j = hourCount % 8;

                    // Get available subjects
                    ArrayList<String> availableSubjects = new ArrayList<>();
                    for (String sbj : subjects) {
                        if (classDiv.connector.get(sbj).hours > 0) {
                            availableSubjects.add(sbj);
                        }
                    }

                    if (availableSubjects.isEmpty()) {
                        break; // Exit if no subjects are left
                    }

                    boolean isScheduled = false;
                    while (!isScheduled && !availableSubjects.isEmpty()) {
                        int randomIndex = rand.nextInt(availableSubjects.size());
                        String randomSubject = availableSubjects.get(randomIndex);

                        int subjectIndex = subjects.indexOf(randomSubject);
                        if (currentFacultyList.get(subjectIndex).timeArrangement[i][j] == null) {
                            classDiv.connector.get(randomSubject).hours -= 1;
                            currentFacultyList.get(subjectIndex).timeArrangement[i][j] = classDiv.name;
                            classDiv.classSchedule[i][j] = randomSubject;
                            hourCount++;
                            isScheduled = true;
                        } else {
                            availableSubjects.remove(randomIndex);
                        }
                    }

                    if (!isScheduled) {
                        allClear = false;
                        break;
                    }
                }

                // Validate schedule
                allClear = true;
                for (i = 0; i < day; i++) {
                    HashMap<String, Integer> checker = new HashMap<>();
                    for (j = 0; j < period; j++) {
                        String sub = classDiv.classSchedule[i][j];
                        if (sub != null) {
                            checker.put(sub, checker.getOrDefault(sub, 0) + 1);
                            if (checker.get(sub) > 2) {
                                allClear = false;
                                break;
                            }
                        }
                    }
                    if (!allClear) {
                        break;
                    }
                }

                if (allClear) {
                    selectedFacultyList = currentFacultyList;

                    // Print Class Schedule
                    System.out.println("\nClass Schedule for " + classDiv.name);
                    for (i = 0; i < day; i++) {
                        for (j = 0; j < period; j++) {
                            System.out.print(
                                    (classDiv.classSchedule[i][j] != null ? classDiv.classSchedule[i][j] : "Free")
                                            + "\t");

                        }
                        System.out.println();
                    }
                    try {
                        // Database Connection
                        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/timesync",
                                "root", "aswin@2004");
                        String sql = "INSERT INTO class_time_table (name,day,period1,period2,period3,period4,period5,period6,period7,period8) VALUES (?, ?,?,?,?,?,?,?,?,?)";
                        PreparedStatement pst = con.prepareStatement(sql);
                        for (i = 0; i < day; i++) {
                            pst.setString(1, classDiv.name);
                            pst.setInt(2, i + 1);
                            for (j = 0; j < period; j++) {
                                String temp_subject_name_db = (classDiv.classSchedule[i][j] != null
                                        ? classDiv.classSchedule[i][j]
                                        : "Free");
                                pst.setString(j + 3, temp_subject_name_db);
                            }
                            int rowsInserted = pst.executeUpdate();
                            if (rowsInserted < 0) {
                                System.out.println("Not data insertted...!");
                                break;
                            }

                        }

                        // Close resources
                        pst.close();
                        con.close();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    // Print Faculty Schedule
                    for (Faculty f : selectedFacultyList) {
                        System.out.println("\nFaculty Schedule for " + f.name);
                        for (i = 0; i < day; i++) {
                            for (j = 0; j < period; j++) {
                                System.out.print(
                                        (f.timeArrangement[i][j] != null ? f.timeArrangement[i][j] : "Free") + "\t");
                            }
                            System.out.println();
                        }
                    }
                    for (int f_selected = 0; f_selected < selectedFacultyList.size(); f_selected++) {
                        for (int f_original = 0; f_original < originalFacultyList.size(); f_original++) {
                            if ((selectedFacultyList.get(f_selected).name)
                                    .equals(originalFacultyList.get(f_original).name)) {
                                // selectedFacultyList.set(f_original, selectedFacultyList.get(f_selected));
                                originalFacultyList.set(f_original, selectedFacultyList.get(f_selected)); // ✅ Corrected
                            }
                        }
                    }

                }
            }

        }
        System.out.println("The final List of Teachers: ");
        System.out.println("\n\n\n");
        for (Faculty f : originalFacultyList) {
            System.out.println("\nFaculty Schedule for " + f.name);
            for (i = 0; i < day; i++) {
                for (j = 0; j < period; j++) {
                    System.out.print(
                            (f.timeArrangement[i][j] != null ? f.timeArrangement[i][j] : "Free") + "\t");
                }
                System.out.println();
            }
            try {
                // Database Connection
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/timesync",
                        "root", "aswin@2004");
                String sql = "INSERT INTO faculty_time_table (name,day,period1,period2,period3,period4,period5,period6,period7,period8) VALUES (?, ?,?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                for (i = 0; i < day; i++) {
                    pst.setString(1, f.name);
                    pst.setInt(2, i + 1);
                    for (j = 0; j < period; j++) {
                        String temp_subject_name_db = (f.timeArrangement[i][j] != null
                                ? f.timeArrangement[i][j]
                                : "Free");
                        pst.setString(j + 3, temp_subject_name_db);
                    }
                    int rowsInserted = pst.executeUpdate();
                    if (rowsInserted < 0) {
                        System.out.println("Not data insertted...!");
                        break;
                    }

                }

                // Close resources
                pst.close();
                con.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        for (i = 0; i < 8; i++) {
            System.out.println();
        }
        System.out.println("Time Syncronized....!\nTimeSync");
        conn.close(); // Close connection at the end
    }
}

// Faculty Class
class Faculty {
    String name;
    String[][] timeArrangement;

    Faculty(String name) {
        this.name = name;
        this.timeArrangement = new String[Schedular_backend_program.day][Schedular_backend_program.period];
    }

    // Copy Constructor
    Faculty(Faculty original) {
        this.name = original.name;
        this.timeArrangement = new String[Schedular_backend_program.day][Schedular_backend_program.period];
        for (int i = 0; i < Schedular_backend_program.day; i++) {
            for (int j = 0; j < Schedular_backend_program.period; j++) {
                this.timeArrangement[i][j] = original.timeArrangement[i][j];
            }
        }
    }
}

// Class Division
class ClassDivision {
    String name;
    HashMap<String, SubjectAllocation> connector;
    String[][] classSchedule;

    ClassDivision(String name) {
        this.name = name;
        classSchedule = new String[Schedular_backend_program.day][Schedular_backend_program.period];
    }
}

// Subject Allocation
class SubjectAllocation {
    String teacher;
    int hours;

    SubjectAllocation(String teacher, int hours) {
        this.teacher = teacher;
        this.hours = hours;
    }
}