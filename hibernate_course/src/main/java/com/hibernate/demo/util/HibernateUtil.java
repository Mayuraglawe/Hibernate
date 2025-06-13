package com.hibernate.demo.util;

import com.hibernate.demo.entity.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import java.lang.System;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    static {
        initializeSessionFactory();
    }

    private static void initializeSessionFactory() {
        System.out.println("\n" + createLine('>', 50));
        System.out.println("  INITIALIZING HIBERNATE SESSION FACTORY");
        System.out.println(createLine('>', 50));

        try {
            System.out.println("→ Creating Hibernate Configuration...");
            Configuration configuration = new Configuration();

            // Database connection properties
            System.out.println("→ Setting up database connection properties...");
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url",
                    "jdbc:mysql://localhost:3306/student1_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "root"); // Change this to your actual password

            // Hibernate properties
            System.out.println("→ Configuring Hibernate properties...");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            // Connection pool settings (HikariCP is recommended)
            configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            configuration.setProperty("hibernate.hikari.minimumIdle", "5");
            configuration.setProperty("hibernate.hikari.maximumPoolSize", "20");
            configuration.setProperty("hibernate.hikari.idleTimeout", "300000");
            configuration.setProperty("hibernate.hikari.connectionTimeout", "20000");
            configuration.setProperty("hibernate.hikari.validationTimeout", "5000");
            configuration.setProperty("hibernate.hikari.leakDetectionThreshold", "60000");

            // Alternative: C3P0 connection pool (if HikariCP is not available)
            // configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
            // configuration.setProperty("hibernate.c3p0.min_size", "5");
            // configuration.setProperty("hibernate.c3p0.max_size", "20");
            // configuration.setProperty("hibernate.c3p0.timeout", "300");
            // configuration.setProperty("hibernate.c3p0.max_statements", "50");
            // configuration.setProperty("hibernate.c3p0.idle_test_period", "3000");

            // Additional performance and reliability settings
            configuration.setProperty("hibernate.connection.autocommit", "false");
            configuration.setProperty("hibernate.current_session_context_class", "thread");
            configuration.setProperty("hibernate.jdbc.batch_size", "20");
            configuration.setProperty("hibernate.order_inserts", "true");
            configuration.setProperty("hibernate.order_updates", "true");
            configuration.setProperty("hibernate.jdbc.batch_versioned_data", "true");

            // Add entity classes
            System.out.println("→ Registering entity classes...");
            configuration.addAnnotatedClass(Student.class);
            System.out.println("   ✓ Student entity registered");

            // Build service registry
            System.out.println("→ Building service registry...");
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Build session factory
            System.out.println("→ Building SessionFactory...");
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            System.out.println("✓ SUCCESS: SessionFactory created successfully!");
            System.out.println(createLine('>', 50));

        } catch (Exception e) {
            System.out.println("✗ FATAL ERROR: Failed to create SessionFactory!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Root cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
            System.out.println(createLine('>', 50));
            e.printStackTrace();

            // Clean up service registry if it was created
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }

            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            throw new IllegalStateException("SessionFactory is not initialized or has been closed!");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        System.out.println("\n" + createLine('<', 50));
        System.out.println("  SHUTTING DOWN HIBERNATE SESSION FACTORY");
        System.out.println(createLine('<', 50));

        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                System.out.println("→ Closing SessionFactory...");
                sessionFactory.close();
                System.out.println("✓ SUCCESS: SessionFactory closed successfully!");
            } catch (Exception e) {
                System.out.println("✗ ERROR: Failed to close SessionFactory!");
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("→ SessionFactory is already closed or null");
        }

        // Clean up service registry
        if (serviceRegistry != null) {
            try {
                System.out.println("→ Destroying ServiceRegistry...");
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
                System.out.println("✓ SUCCESS: ServiceRegistry destroyed successfully!");
            } catch (Exception e) {
                System.out.println("✗ ERROR: Failed to destroy ServiceRegistry!");
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println(createLine('<', 50));
    }

    // Helper method to create separator lines
    private static String createLine(char character, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    // Method to test database connection
    public static boolean testConnection() {
        try {
            System.out.println("→ Testing database connection...");
            SessionFactory factory = getSessionFactory();
            if (factory != null && !factory.isClosed()) {
                // Try to open and close a session to verify connectivity
                try (Session session = factory.openSession()) {
                    session.beginTransaction().commit();
                    System.out.println("✓ Database connection test successful!");
                    return true;
                }

            }
        } catch (Exception e) {
            System.out.println("✗ Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Method to check if SessionFactory is available
    public static boolean isSessionFactoryAvailable() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}