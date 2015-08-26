package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 26/08/15.
 */
public class AlternatingUserMail implements UserMail, FaxByMail {

    private final UserMail userMail1;
    private final UserMail userMail2;
    private boolean left = true;

    public AlternatingUserMail(UserMail userMail1, UserMail userMail2) {
        this.userMail1 = userMail1;
        this.userMail2 = userMail2;
    }

    public void setCurrent(boolean left) {
        this.left = left;
    }

    private UserMail getDelegate() {
        return left ? userMail1 : userMail2;
    }

    public void sendEmail(Message message) {
        getDelegate().sendEmail(message);
    }

    public void validateEmail(Message message) {
        getDelegate().validateEmail(message);
    }

    public boolean canFaxEmail(Message message) {
        return getDelegate() instanceof FaxByMail;
    }

    public void faxEmail(Message message) {
        if (getDelegate() instanceof FaxByMail) {
            ((FaxByMail) getDelegate()).faxEmail(message);
        } else {
            throw new IllegalStateException("The current service does not support fax");
        }
    }
}
