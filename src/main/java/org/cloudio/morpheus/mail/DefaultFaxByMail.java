package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 26/08/15.
 */
public class DefaultFaxByMail implements FaxByMail, UserMail {

    private final MailOwner mailOwner;
    private final UserMail delegatee;

    public DefaultFaxByMail(MailOwner mailOwner, UserMail delegatee) {
        this.mailOwner = mailOwner;
        this.delegatee = delegatee;
    }

    public void faxEmail(Message message) {
        // todo
    }

    public void sendEmail(Message message) {
        this.delegatee.sendEmail(message);
    }

    public void validateEmail(Message message) {
        this.delegatee.validateEmail(message);
    }
}
