package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class EmployeeAdapter extends Employee implements MailOwner {
    public EmployeeAdapter() {
    }

    public EmployeeAdapter(Employee other) {
        super(other);
    }

    public String nick() {
        return getEmployeeCode();
    }

    public String firstName() {
        return getFirstName();
    }

    public String lastName() {
        return getLastName();
    }

    public String email() {
        return getEmployeeCode() + "@bigcompany.com";
    }

    public Date birthDate() {
        return getBirth();
    }
}
