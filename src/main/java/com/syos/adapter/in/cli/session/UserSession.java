package com.syos.adapter.in.cli.session;

import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an active user session
 */
public class UserSession {
    private final Long userId; // may be null for unsaved users
    private final String username;
    private final String name;
    private final UserRole role;
    private final LocalDateTime loginTime;
    private final BigDecimal synexPoints;

    public UserSession(User user) {
        this.userId = user.getId() != null ? user.getId().getValue() : null;
        this.username = user.getUsername().getValue();
        this.name = user.getName().getValue();
        this.role = user.getRole();
        this.loginTime = LocalDateTime.now();
        this.synexPoints = user.getSynexPoints().getValue();
    }

    public Long getUserId() { 
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
    
    public BigDecimal getSynexPoints() { 
        return synexPoints; 
    }
}