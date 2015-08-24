package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class EmployeeAdapter implements MailOwner {
    private final Employee employee;

    public EmployeeAdapter(Employee employee) {
        this.employee = employee;
    }

    public String nick() {
        return employee.getEmployeeCode();
    }

    public String firstName() {
        return employee.getFirstName();
    }

    public String lastName() {
        return employee.getLastName();
    }

    public String email() {
        return employee.getEmployeeCode() + "@bigcompany.com";
    }

    public boolean isMale() {
        return employee.isMale();
    }

    public Date birthDate() {
        return employee.getBirth();
    }

    public Employee getEmployee() {
        return employee;
    }
}
