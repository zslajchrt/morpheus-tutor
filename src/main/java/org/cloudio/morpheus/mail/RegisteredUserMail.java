package org.cloudio.morpheus.mail;

import java.util.Date;
import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class RegisteredUserMail extends DefaultUserMail {

    public RegisteredUserMail(RegisteredUserAdapter registeredUser) {
        super(registeredUser);
    }

    @Override
    public void validateEmail(Message message) {
        Date validTo = ((RegisteredUserAdapter) getMailOwner()).getValidTo();
        Date now = new Date();
        if (validTo.compareTo(now) < 0) {
            throw new IllegalArgumentException("User's account expired");
        }
        super.validateEmail(message);
    }
}
