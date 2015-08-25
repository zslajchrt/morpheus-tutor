package org.cloudio.morpheus.mail;

import java.util.*;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class App {

    public static void main(String[] args) {

        boolean isEmployee = true;
        UserMail userMail;

        if (isEmployee) {
            Employee employee = new Employee();
            userMail = new EmployeeUserMail(employee);
        } else {
            RegisteredUser registeredUser = new RegisteredUser();
            userMail = new RegisteredUserMail(registeredUser);
        }

        userMail = new VirusDetector(userMail);

        Message msg = new Message();
        msg.setRecipients(Arrays.asList("pepa@gmail.com"));
        msg.setSubject("Hello");
        msg.setBody("Hi, Pepa!");

        userMail.sendEmail(msg);

    }

}
