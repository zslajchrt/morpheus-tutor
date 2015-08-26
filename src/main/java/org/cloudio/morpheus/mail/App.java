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
        if (registeredUser.isPremium()) {
            userMail2 = new DefaultFaxByMail(registeredUserAdapter, userMail2);
        }

        // The type of account is still discoverable from the type of both userMail1 and userMail2

        AlternatingUserMail altUserMail = new AlternatingUserMail(userMail1, userMail2);
        // The instance lost the track of the account, which is actually used; the account type is no longer detectable from
        // the instance's type.
        // The preservation of the type would loosen the coupling between the instance and its client. For example,
        // the user interface would be able to adapt itself to the type of the account just on the basis of the mail
        // service instance's type. Now, the UI would have to consult the state of the instance and retrieve the user account instance
        // from some getter. Unfortunately, the UserMail interface has no method for getting the user account. Thus it would
        // be practically impossible to determine the actual user account type without using some reflection tricks
        // or introducing a new getUserAccount method in the UserMail interface. Both solutions are bad. The former
        // would be a plain hack, while the latter purblindly introduces a backward incompatible change to the
        // general contract declared by the UserMail interface. It would force all UserMail implementations to add the new method
        // even if it would make no sense for them, since some implementation may not wrap any user account object at all.
        // Such classes would have to return null as an indication there is no underlying user account object. Moreover,
        // the return type of the getUserAccount method would have to be Object, since there is no common ground shared
        // by all potential user accounts; the diversity of user accounts is actually the main presupposition of this
        // case study.

        // The client must be fixed to AlternatingUserMail through which it can determine whether the service supports fax.

        Message msg = new Message();
        msg.setRecipients(Arrays.asList("pepa@gmail.com"));
        msg.setSubject("Hello");
        msg.setBody("Hi, Pepa!");

        // VirusDetector must be implemented by means of delegation, since there is no single direct ancestor. The possible ones are
        // DefaultUserMail, RegisteredUser and EmployeeUserMail.
        // Its validateEmail method is lame since it is not called from with the delegatee's sendMail method.
        // Were VirusDetector implemented by extending and not delegating, method validateEmail would be invoked and
        // VirusDetector would not have to override the sendMail method.
        UserMail userMail = new VirusDetector(altUserMail);

        userMail.sendEmail(msg);

        altUserMail.setCurrent(false);

        userMail.sendEmail(msg);


    }

}
