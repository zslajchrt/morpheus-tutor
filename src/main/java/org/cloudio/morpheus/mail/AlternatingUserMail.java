package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 26/08/15.
 */
public abstract class AlternatingUserMail implements UserMail, FaxByMail {

    protected abstract UserMail getDelegate();

    public void sendEmail(Message message) {
        getDelegate().sendEmail(message);
    }

    public void validateEmail(Message message) {
        getDelegate().validateEmail(message);
    }

    public boolean canFaxEmail() {
        return getDelegate() instanceof FaxByMail;
    }

    public void faxEmail(Message message) {
        UserMail delegate = getDelegate();
        if (delegate instanceof FaxByMail) {
            ((FaxByMail) delegate).faxEmail(message);
        } else {
            throw new IllegalStateException("The current service does not support fax");
        }
    }
}
