package org.cloudio.morpheus.mail;

import java.util.Date;

/**
 * Created by zslajchrt on 24/08/15.
 */
public interface MailOwner {
    String nick();

    String firstName();

    String lastName();

    String email();

    boolean isMale();

    Date birthDate();
}

