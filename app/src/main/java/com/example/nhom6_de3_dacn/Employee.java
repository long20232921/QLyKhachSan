package com.example.nhom6_de3_dacn;

public class Employee {
    private String id;
    private String name;
    private String role;
    private String phone;
    private String currentTask;
    private String image;

    public Employee() { }

    public Employee(String id, String name, String role, String phone, String currentTask, String image) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.currentTask = currentTask;
        this.image = image;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCurrentTask() { return currentTask; }
    public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}