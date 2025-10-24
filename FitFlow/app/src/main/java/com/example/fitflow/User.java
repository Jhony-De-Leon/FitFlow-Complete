package com.example.fitflow;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)}) // Asegura que el email sea único
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "surname") // Nuevo campo
    public String surname;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "age")      // Nuevo campo
    public int age;

    @ColumnInfo(name = "gender")   // Nuevo campo
    public String gender;

    @ColumnInfo(name = "weight")   // Nuevo campo
    public double weight;

    @ColumnInfo(name = "height")   // Nuevo campo
    public int height;

    @ColumnInfo(name = "main_goal")
    public String mainGoal;

    // Constructor vacío requerido por Room
    public User() {}

    // Constructor principal para crear usuarios
    public User(String name, String surname, String email, String passwordHash, int age, String gender, double weight, int height, String mainGoal) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.mainGoal = mainGoal;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMainGoal() {
        return mainGoal;
    }

    public void setMainGoal(String mainGoal) {
        this.mainGoal = mainGoal;
    }
}
