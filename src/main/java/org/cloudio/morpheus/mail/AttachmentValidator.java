package org.cloudio.morpheus.mail;

import java.util.List;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class AttachmentValidator implements UserMail {

    private final UserMail userMail;

    public AttachmentValidator(UserMail userMail) {
        this.userMail = userMail;
    }

    public void sendEmail(List<String> recipients, String subject, String message, List<Attachment> attachments) {

        for (Attachment attachment : attachments) {
            validateAttachment(attachment);
        }
        userMail.sendEmail(recipients, subject, message, attachments);
    }

    private void validateAttachment(Attachment attachment) {
        // todo
    }
}
