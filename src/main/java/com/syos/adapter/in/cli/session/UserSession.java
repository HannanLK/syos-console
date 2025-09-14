package com.syos.adapter.in.cli.session;

import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Represents an active user session
 */
public class UserSession {
    private final String userId;
    private final String username;
    private final String name;
    private final UserRole role;
    private final LocalDateTime loginTime;
    private final double synexPoints;

    public UserSession(User user) {
        this.userId = user.getId() != null ? user.getId().getValue() : null;
        this.username = user.getUsername().getValue();
        this.name = user.getName().getValue();
        this.role = user.getRole();
        this.loginTime = LocalDateTime.now();
        this.synexPoints = user.getSynexPoints().getValue();
    }

    public String getUserId() { 
        return userId; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public UserRole getRole() { 
        return role; 
    }
    
    public LocalDateTime getLoginTime() { 
        return loginTime; 
    }
    
    public double getSynexPoints() { 
        return synexPoints; 
    }
}