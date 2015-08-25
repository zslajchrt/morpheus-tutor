package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class DefaultUserMail implements UserMail {

    private final MailOwner mailOwner;

    public DefaultUserMail(MailOwner mailOwner) {
        this.mailOwner = mailOwner;
    }

    public void sendEmail(Message message) {
        try {
            validateEmail(message);
            send(message);
        } catch (IllegalArgumentException e) {
            store(message);
        }
    }

    public void validateEmail(Message message) {
        // todo
    }

    private void send(Message message) {
        // todo
    }

    private void store(Message message) {
        // todo
    }

    public MailOwner getMailOwner() {
        return mailOwner;
    }
}
