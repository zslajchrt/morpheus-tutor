package org.cloudio.morpheus.mail;

import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class DefaultUserMail implements UserMail {

    private final MailOwner mailOwner;

    public DefaultUserMail(MailOwner mailOwner) {
        this.mailOwner = mailOwner;
    }

    public void sendEmail(List<String> recipients, String subject, String message, List<Attachment> attachments) {
        String fromHeader = mailOwner.email();
        // todo
    }

    public MailOwner getMailOwner() {
        return mailOwner;
    }
}
