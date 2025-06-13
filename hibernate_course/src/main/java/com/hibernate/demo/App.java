package com.hibernate.demo;

import com.hibernate.demo.entity.Student;
import com.hibernate.demo.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    // Helper method to create separator lines (replacement for String.repeat())
    private static String createLine(char character, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    private static void printHeader(String title) {
        System.out.println("\n" + createLine('=', 60));
        System.out.println("  " + title);
        System.out.println(createLine('=', 60));
    }

    private static void printStep(int step, String description) {
        System.out.println("\n" + createLine('-', 50));
        System.out.println("STEP " + step + ": " + description);
        System.out.println(createLine('-', 50));
    }

    private static void printSuccess(String message) {
        System.out.println("✓ SUCCESS: " + message);
    }

    private static void printError(String message) {
        System.out.println("✗ ERROR: " + message);
    }

    private static void printInfo(String message) {
        System.out.println("→ INFO: " + message);
    }

    public static void main(String[] args) {
        printHeader("HIBERNATE CRUD OPERATIONS DEMO");

        try {
            // Step 1: Create Student Object
            printStep(1, "Creating Student Object");
            Student student = new Student("Mayur Aglwe", "mayuraglawe@gmail.com", "Nagpur", "9th Grade");
            printSuccess("Student object created: " + student.getName());
            System.out.println("   Details: " + student);

            // Step 2: Add Student to Database
            printStep(2, "Adding Student to Database");
            addStudent(student);

            // Step 3: Read All Students
            printStep(3, "Reading All Students from Database");
            List<Student> allStudents = getAllStudents();
            displayStudents(allStudents, "All Students in Database");

            // Step 4: Update Student Name
            printStep(4, "Updating Student Information");
            if (allStudents != null && !allStudents.isEmpty()) {
                Integer studentId = allStudents.get(0).getId();
                updateStudentName(studentId, "Mayur Updated");
            } else {
                printError("No students found to update");
            }

            // Step 5: Read Students After Update
            printStep(5, "Reading Students After Update");
            allStudents = getAllStudents();
            displayStudents(allStudents, "Students After Update");

            // Step 6: Delete Student
            printStep(6, "Deleting Student");
            if (allStudents != null && !allStudents.isEmpty()) {
                Integer studentId = allStudents.get(0).getId();
                deleteStudent(studentId);
            } else {
                printError("No students found to delete");
            }

            // Step 7: Final Verification
            printStep(7, "Final Verification - Reading All Students");
            allStudents = getAllStudents();
            displayStudents(allStudents, "Final Student List");

        } catch (Exception e) {
            printError("Application encountered an exception!");
            System.out.println("Exception Type: " + e.getClass().getSimpleName());
            System.out.println("Exception Message: " + e.getMessage());
            logger.log(Level.SEVERE, "Application error", e);
        } finally {
            printStep(8, "Cleanup - Shutting Down Hibernate");
//            HibernateUtil.shutdown();
            printSuccess("Application completed successfully!");
            printHeader("END OF HIBERNATE DEMO");
        }
    }

    private static void displayStudents(List<Student> students, String title) {
        System.out.println("\n" + createLine('*', 40));
        System.out.println("  " + title);
        System.out.println(createLine('*', 40));

        if (students != null && !students.isEmpty()) {
            printSuccess("Found " + students.size() + " student(s)");
            for (int i = 0; i < students.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + students.get(i));
            }
        } else {
            printInfo("No students found in database");
        }
        System.out.println(createLine('*', 40));
    }

    public static void addStudent(Student student) {
        printInfo("Opening Hibernate session for ADD operation");
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            printInfo("Session opened successfully");

            tx = session.beginTransaction();
            printInfo("Transaction started");

            session.save(student);
            printInfo("Student saved to session");

            tx.commit();
            printSuccess("Student added to database: " + student.getName());

        } catch (Exception e) {
            printError("Failed to add student: " + e.getMessage());

            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    printInfo("Transaction rolled back successfully");
                } catch (Exception rollbackEx) {
                    printError("Rollback failed: " + rollbackEx.getMessage());
                    logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Error adding student", e);
            throw new RuntimeException("Failed to add student", e);
        }
    }

    public static List<Student> getAllStudents() {
        printInfo("Opening Hibernate session for READ operation");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            printInfo("Session opened successfully");

            List<Student> students = session.createQuery("FROM Student", Student.class).list();
            printSuccess("Retrieved " + students.size() + " students from database");

            return students;

        } catch (Exception e) {
            printError("Failed to retrieve students: " + e.getMessage());
            logger.log(Level.SEVERE, "Error retrieving students", e);
            return Collections.emptyList();
        }
    }

    public static void updateStudentName(Integer id, String newName) {
        printInfo("Opening Hibernate session for UPDATE operation");
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            printInfo("Session opened successfully");

            tx = session.beginTransaction();
            printInfo("Transaction started");

            Student student = session.get(Student.class, id);

            if (student != null) {
                String oldName = student.getName();
                student.setName(newName);
                session.update(student);

                printInfo("Updated student name: '" + oldName + "' → '" + newName + "'");

                tx.commit();
                printSuccess("Student updated successfully");
            } else {
                printError("Student not found with ID: " + id);
                tx.rollback();
            }

        } catch (Exception e) {
            printError("Failed to update student: " + e.getMessage());

            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    printInfo("Transaction rolled back successfully");
                } catch (Exception rollbackEx) {
                    printError("Rollback failed: " + rollbackEx.getMessage());
                    logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Error updating student with ID: " + id, e);
        }
    }

    public static void deleteStudent(Integer id) {
        printInfo("Opening Hibernate session for DELETE operation");
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            printInfo("Session opened successfully");

            tx = session.beginTransaction();
            printInfo("Transaction started");

            Student student = session.get(Student.class, id);

            if (student != null) {
                String studentName = student.getName();
                session.delete(student);

                printInfo("Deleted student: " + studentName + " (ID: " + id + ")");

                tx.commit();
                printSuccess("Student deleted successfully");
            } else {
                printError("Student not found with ID: " + id);
                tx.rollback();
            }

        } catch (Exception e) {
            printError("Failed to delete student: " + e.getMessage());

            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    printInfo("Transaction rolled back successfully");
                } catch (Exception rollbackEx) {
                    printError("Rollback failed: " + rollbackEx.getMessage());
                    logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Error deleting student with ID: " + id, e);
        }
    }
}