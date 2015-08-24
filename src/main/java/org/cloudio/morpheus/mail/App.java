package org.cloudio.morpheus.mail;

import java.util.*;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class App {

    public static void main(String[] args) {

        RegisteredUser registeredUser = new RegisteredUser();
        RegisteredUserAdapter mailUser = new RegisteredUserAdapter(registeredUser);

        UserMail userMail = new AttachmentValidator(
                new EmployeeUserMail(
                        new RegisteredUserMail(
                                new DefaultUserMail(mailUser))));

        // "One-size fits all" way of assembling behavior
        // AttachmentValidator is needed only if the list of attachments is not empty
        // RegisteredUserMailService is needed only if the user is a registered user
        // EmployeeMailService is needed only if the user is an employee
        userMail.sendEmail(Arrays.asList("pepa@gmail.com"), "Hello", "Hi, Pepa!", Collections.<Attachment>emptyList());

    }

}
