package com.syos.adapter.in.cli.session;

import com.syos.shared.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton Session Manager to handle user sessions
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    private UserSession currentSession;

    private SessionManager() {
        // Private constructor for singleton
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(UserSession session) {
        this.currentSession = session;
        logger.info("Session created for user: {} with role: {}", 
            session.getUsername(), session.getRole());
    }

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public boolean isLoggedIn() {
        return currentSession != null;
    }

    public boolean hasRole(UserRole role) {
        return isLoggedIn() && currentSession.getRole() == role;
    }

    public boolean isCustomer() {
        return hasRole(UserRole.CUSTOMER);
    }

    public boolean isEmployee() {
        return hasRole(UserRole.EMPLOYEE);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public void clearSession() {
        if (currentSession != null) {
            logger.info("Session cleared for user: {}", currentSession.getUsername());
            currentSession = null;
        }
    }
}