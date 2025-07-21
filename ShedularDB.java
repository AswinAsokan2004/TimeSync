import java.sql.*;
import java.util.*;

public class ShedularDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/timesync", "root", "aswin@2004");

            ArrayList<Faculty> facultyList = fetchFaculty(conn);
            ArrayList<String> classNames = fetchClasses(conn);
            ArrayList<String> subjectNames = fetchSubjects(conn);
            HashMap<String, SubjectAllocation> subjectAllocations = fetchSubjectAllocations(conn);

            Random rand = new Random();
            ArrayList<Faculty> previousFacultyList = new ArrayList<>(facultyList);
            ArrayList<ClassDivision> classList = new ArrayList<>();

            for (String className : classNames) {
                classList.add(new ClassDivision(className));
            }

            for (ClassDivision classDiv : classList) {
                boolean allClear = false;
                while (!allClear) {
                    ArrayList<Faculty> currentFacultyList = new ArrayList<>();
                    for (Faculty f : previousFacultyList) {
                        currentFacultyList.add(new Faculty(f));
                    }

                    classDiv.connector = new HashMap<>(subjectAllocations);

                    int hourCount = 0;
                    while (hourCount < 40) {
                        int i = hourCount / 8;
                        int j = hourCount % 8;

                        ArrayList<String> availableSubjects = new ArrayList<>();
                        for (String sub : subjectNames) {
                            if (classDiv.connector.get(sub).hours > 0) {
                                availableSubjects.add(sub);
                            }
                        }

                        if (availableSubjects.isEmpty())
                            break;

                        boolean isScheduled = false;
                        while (!isScheduled && !availableSubjects.isEmpty()) {
                            int randomIndex = rand.nextInt(availableSubjects.size());
                            String randomSubject = availableSubjects.get(randomIndex);
                            int subjectIndex = subjectNames.indexOf(randomSubject);

                            if (currentFacultyList.get(subjectIndex).time_arrangement[i][j] == null) {
                                classDiv.connector.get(randomSubject).hours -= 1;
                                currentFacultyList.get(subjectIndex).time_arrangement[i][j] = classDiv.name;
                                classDiv.class_schedule[i][j] = randomSubject;
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

                    allClear = validateSchedule(classDiv);
                    if (allClear) {
                        previousFacultyList = currentFacultyList;
                        printSchedule(classDiv);
                        printFacultySchedules(previousFacultyList);
                    }
                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Faculty> fetchFaculty(Connection conn) throws SQLException {
        ArrayList<Faculty> facultyList = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT distinct(faculty) FROM map");
        while (rs.next()) {
            facultyList.add(new Faculty(rs.getString("faculty")));
        }
        return facultyList;
    }

    public static ArrayList<String> fetchClasses(Connection conn) throws SQLException {
        ArrayList<String> classList = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT distinct(class) FROM map");
        while (rs.next()) {
            classList.add(rs.getString("class"));
        }
        return classList;
    }

    public static ArrayList<String> fetchSubjects(Connection conn) throws SQLException {
        ArrayList<String> subjectList = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT distinct(subject) FROM map");
        while (rs.next()) {
            subjectList.add(rs.getString("subject"));
        }
        return subjectList;
    }

    public static HashMap<String, SubjectAllocation> fetchSubjectAllocations(Connection conn) throws SQLException {
        HashMap<String, SubjectAllocation> allocations = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select * from map");

        while (rs.next()) {
            String subject = rs.getString("subject");
            String faculty = rs.getString("faculty");
            int hours = rs.getInt("hours");
            allocations.put(subject, new SubjectAllocation(faculty, hours));
        }
        return allocations;
    }

    public static boolean validateSchedule(ClassDivision classDiv) {
        for (int i = 0; i < 5; i++) {
            HashMap<String, Integer> checker = new HashMap<>();
            for (int j = 0; j < 8; j++) {
                String sub = classDiv.class_schedule[i][j];
                if (sub != null) {
                    checker.put(sub, checker.getOrDefault(sub, 0) + 1);
                    if (checker.get(sub) > 2)
                        return false;
                }
            }
        }
        return true;
    }

    public static void printSchedule(ClassDivision classDiv) {
        System.out.println("\nClass Schedule for " + classDiv.name);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                System.out
                        .print((classDiv.class_schedule[i][j] != null ? classDiv.class_schedule[i][j] : "Free") + "\t");
            }
            System.out.println();
        }
    }

    public static void printFacultySchedules(ArrayList<Faculty> facultyList) {
        for (Faculty f : facultyList) {
            System.out.println("\nFaculty Schedule for " + f.name);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 8; j++) {
                    System.out.print((f.time_arrangement[i][j] != null ? f.time_arrangement[i][j] : "Free") + "\t");
                }
                System.out.println();
            }
        }
    }
}

class Faculty {
    String name;
    String[][] time_arrangement;

    Faculty(String name) {
        this.name = name;
        this.time_arrangement = new String[5][8];
    }

    Faculty(Faculty original) {
        this.name = original.name;
        this.time_arrangement = new String[5][8];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                this.time_arrangement[i][j] = original.time_arrangement[i][j];
            }
        }
    }
}

class ClassDivision {
    String name;
    HashMap<String, SubjectAllocation> connector;
    String[][] class_schedule;

    ClassDivision(String name) {
        this.name = name;
        this.class_schedule = new String[5][8];
    }
}

class SubjectAllocation {
    String teacher;
    int hours;

    SubjectAllocation(String teacher, int hours) {
        this.teacher = teacher;
        this.hours = hours;
    }
}
