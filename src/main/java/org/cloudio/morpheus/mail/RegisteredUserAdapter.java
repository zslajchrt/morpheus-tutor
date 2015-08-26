package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class RegisteredUserAdapter extends RegisteredUser implements MailOwner {
    public RegisteredUserAdapter() {
    }

    public RegisteredUserAdapter(RegisteredUser other) {
        super(other);
    }

    public String nick() {
        return getNick();
    }

    public String firstName() {
        return getFirstName();
    }

    public String lastName() {
        return getLastName();
    }

    public String email() {
        return getEmail();
    }

    public Date birthDate() {
        return getBirthDate();
    }
}
