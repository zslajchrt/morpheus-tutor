package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class Employee {

    private String firstName;
    private String middleName;
    private String lastName;
    private String title;
    private boolean isMale;
    private Date birth;

    private String employeeCode;
    private String position;
    private String department;

    public Employee() {
    }

    public Employee(Employee other) {
        this.firstName = other.firstName;
        this.middleName = other.middleName;
        this.lastName = other.lastName;
        this.title = other.title;
        this.isMale = other.isMale;
        this.birth = other.birth;
        this.employeeCode = other.employeeCode;
        this.position = other.position;
        this.department = other.department;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setIsMale(boolean isMale) {
        this.isMale = isMale;
    }

    public Date getBirth() {
        return birth;
    }

    public void setBirth(Date birth) {
        this.birth = birth;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
