package com.leavemanagment.leave_app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true) 
    private String email;
    
    private String password; // Will be encrypted
    private String fullName;
    private String department;
    private String employeeCode;
    private Role role;
    private String profilePicture; // Path to profile picture
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password, String fullName, String department, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.department = department;
        this.role = role;
    }
    
    public User(String username, String email, String password, String fullName, String department, String employeeCode, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.department = department;
        this.employeeCode = employeeCode;
        this.role = role;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}