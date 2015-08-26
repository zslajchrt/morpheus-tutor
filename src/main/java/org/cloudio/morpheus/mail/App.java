package org.cloudio.morpheus.mail;

import java.util.*;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class App {

    public static void main(String[] args) {

        Employee employee = new Employee();
        RegisteredUser registeredUser = new RegisteredUser();

        // We need to clone the state of both employee and registeredUser
        EmployeeAdapter employeeAdapter = new EmployeeAdapter(employee);
        UserMail userMail1 = new EmployeeUserMail(employeeAdapter);

        RegisteredUserAdapter registeredUserAdapter = new RegisteredUserAdapter(registeredUser);
        UserMail userMail2 = new RegisteredUserMail(registeredUserAdapter);

        userMail1 = new VirusDetector(userMail1);
        userMail2 = new VirusDetector(userMail2);

        if (registeredUser.isPremium()) {
            userMail2 = new DefaultFaxByMail(registeredUserAdapter, userMail2);
        }

        // The type of account is still discoverable from the type of both userMail1 and userMail2

        AlternatingUserMail userMail = new AlternatingUserMail(userMail1, userMail2);

        // The client must be fixed to AlternatingUserMail through which it can determine whether the service supports fax.

        Message msg = new Message();
        msg.setRecipients(Arrays.asList("pepa@gmail.com"));
        msg.setSubject("Hello");
        msg.setBody("Hi, Pepa!");

        userMail.sendEmail(msg);

        userMail.setCurrent(false);

        userMail.sendEmail(msg);


    }

}
