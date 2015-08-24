package org.cloudio.morpheus.mail;

import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public interface UserMail {

    void sendEmail(List<String> recipients, String subject, String message, List<Attachment> attachments);

}
