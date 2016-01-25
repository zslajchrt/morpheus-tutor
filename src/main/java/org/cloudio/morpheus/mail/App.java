package org.cloudio.morpheus.mail;

import java.util.*;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class App {

    public static void useMailService(Map<String, Object> employeeData, Map<String, Object> regUserData) {

        Employee employee = initEmployee(employeeData);
        RegisteredUser registeredUser = initRegisteredUser(regUserData);

        // The client must use the concrete type AlternatingUserMail through
        // which the client can determine whether the service supports fax or not.
        AlternatingUserMail userMail = initMailService(employee, registeredUser);

        Message msg = new Message();
        msg.setRecipients(Collections.singletonList("pepa@gmail.com"));
        msg.setSubject("Hello");
        msg.setBody("Hi, Pepa!");

        userMail.sendEmail(msg);
        if (userMail.canFaxEmail()) {
            userMail.faxEmail(msg);
        }
    }

    public static Employee initEmployee(Map<String, Object> employeeData) {
        Employee employee = new Employee();
        employee.load(employeeData);
        return employee;
    }

    public static RegisteredUser initRegisteredUser(Map<String, Object> regUserData) {
        RegisteredUser registeredUser;
        if (Boolean.TRUE.equals(regUserData.get("isPremium"))) {
            class Premium extends RegisteredUser implements PremiumUser {}
            registeredUser = new Premium();
        } else {
            registeredUser = new RegisteredUser();
        }
        registeredUser.load(regUserData);

        return registeredUser;
    }

    public static AlternatingUserMail initMailService(final Employee employee, final RegisteredUser regUser) {

        EmployeeAdapter employeeAdapter = new EmployeeAdapter(employee);
        final UserMail employeeMail = new VirusDetector(new EmployeeUserMail(employeeAdapter));

        final RegisteredUserAdapter registeredUserAdapter = new RegisteredUserAdapter(regUser);
        final UserMail regUserMail = new VirusDetector(new RegisteredUserMail(registeredUserAdapter));
        final UserMail regUserMailPremium = new DefaultFaxByMail(registeredUserAdapter, regUserMail);

        return new AlternatingUserMail() {
            @Override
            protected UserMail getDelegate() {
                Calendar c = Calendar.getInstance();
                int h = c.get(Calendar.HOUR_OF_DAY);
                if (!(h >= 8 && h < 17)) {
                    return getEmployeeMail();
                } else {
                    return getRegUserMail();
                }
            }

            UserMail getEmployeeMail() {
                return employeeMail;
            }

            UserMail getRegUserMail() {
                if (regUser.isPremium() &&
                        regUser.getValidTo() != null &&
                        regUser.getValidTo().compareTo(Calendar.getInstance().getTime()) >= 0)
                    return regUserMailPremium;
                else
                    return regUserMail;
            }
        };
    }

}
