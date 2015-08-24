package org.cloudio.morpheus.mail;

import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class EmployeeUserMail implements UserMail {
    private final UserMail userMail;

    public EmployeeUserMail(UserMail userMail) {
        this.userMail = userMail;
    }

    public void sendEmail(List<String> recipients, String subject, String message, List<Attachment> attachments) {

        if (userMail instanceof DefaultUserMail &&
                ((DefaultUserMail) userMail).getMailOwner() instanceof EmployeeAdapter) {
            String department = ((EmployeeAdapter) ((DefaultUserMail) userMail).getMailOwner()).getEmployee().getDepartment();
            // todo: a specific handling of the action for the employee
        }
        userMail.sendEmail(recipients, subject, message, attachments);
    }

    // If MailService contained more methods then the following space would be filled by plenty of boilerplate
    // caused by simply delegating methods.

    //...
}
