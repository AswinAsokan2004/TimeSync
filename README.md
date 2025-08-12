# 🕒 TimeSync – Faculty & Class Timetable Synchronization System

**TimeSync** is a desktop-based timetable synchronization application built with **Java (JSwing frontend, Java backend)** and **MySQL**.
It is designed to automatically generate **conflict-free timetables** for both faculty and classes, ensuring optimal scheduling in educational institutions.

---

## 📌 Features

* **Automated Timetable Generation** for faculty and classes.
* **Conflict-Free Scheduling** – One faculty, one subject, one class per period.
* **Balanced Workload Distribution** – Every teacher gets at least one working period per day.
* **Dedicated Preparation Time** – Ensures a long free period in a week for study material preparation.
* **Subject Repetition Limit** – Prevents more than two periods of the same subject in a single day.
* **Customizable Parameters** – Number of days, periods per week, and hours per subject can be set.

---

## 🏗️ Technology Stack

* **Frontend:** Java Swing
* **Backend:** Java
* **Database:** MySQL

---

## 📂 Project Structure

```
TimeSync/
│
├── HomeScreen.java       # Main entry point of the application
├── [Other Java Files for Scheduling Logic]
├── resources/            # Icons, UI assets, configuration files
├── database/             # SQL scripts for table creation
└── README.md              # Project documentation
```

---

## ▶️ How to Run

1. **Clone the repository**:

   ```bash
   git clone https://github.com/yourusername/timesync.git
   cd timesync
   ```

2. **Set up the database**:

   * Create a MySQL database (e.g., `timesync_db`).
   * Import the SQL scripts from the `database/` folder.

3. **Configure database connection**:

   * Update your DB credentials in the database connection file.

4. **Compile and run the program**:

   ```bash
   javac HomeScreen.java
   java HomeScreen
   ```

---

## 💡 Example Use Case

**Scenario:**
Faculty member **Anu** teaches:

* Subject 1 → Class A1 & A2
* Subject 3 → Class C1 & C2
* Subject 5 → Class D1

TimeSync ensures that:

* Anu is not double-booked for different subjects/classes in the same period.
* Classes receive their required subject hours.
* Anu gets a balanced schedule with preparation time.

---

## 📜 License

This project is licensed under the MIT License – feel free to use, modify, and distribute it.

---

## 👨‍💻 Author

**Aswin Asokan**
Passionate about building intelligent, efficient software systems for real-world problems.
