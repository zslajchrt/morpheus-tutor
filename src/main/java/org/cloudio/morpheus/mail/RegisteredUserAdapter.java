package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class RegisteredUserAdapter implements MailOwner {
    private final RegisteredUser regUser;

    public RegisteredUserAdapter(RegisteredUser regUser) {
        this.regUser = regUser;
    }

    public String nick() {
        return regUser.getNick();
    }

    public String firstName() {
        return regUser.getFirstName();
    }

    public String lastName() {
        return regUser.getLastName();
    }

    public String email() {
        return regUser.getEmail();
    }

    public boolean isMale() {
        return regUser.isMale();
    }

    public Date birthDate() {
        return regUser.getBirthDate();
    }

    public RegisteredUser getRegUser() {
        return regUser;
    }
}
