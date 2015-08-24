package org.cloudio.morpheus.mail;

import java.util.Date;
import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class RegisteredUserMail implements UserMail {

    private final UserMail userMail;

    public RegisteredUserMail(UserMail userMail) {
        this.userMail = userMail;
    }

    public void sendEmail(List<String> recipients, String subject, String message, List<Attachment> attachments) {

        if (userMail instanceof DefaultUserMail &&
                ((DefaultUserMail) userMail).getMailOwner() instanceof RegisteredUserAdapter) {
            Date validFrom = ((RegisteredUserAdapter) ((DefaultUserMail) userMail).getMailOwner()).getRegUser().getValidFrom();
            // todo: a specific handling of the action for the employee
        }
        userMail.sendEmail(recipients, subject, message, attachments);
    }
}
