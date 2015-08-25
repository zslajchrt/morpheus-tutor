package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 24/08/15.
 */
public interface UserMail {

    void sendEmail(Message message);

    void validateEmail(Message message);

}
