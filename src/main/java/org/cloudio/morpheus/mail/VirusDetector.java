package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class VirusDetector implements UserMail {

    private final UserMail userMail;

    public VirusDetector(UserMail userMail) {
        this.userMail = userMail;
    }

    public void sendEmail(Message message) {
        scanAttachments(message);
        userMail.sendEmail(message);
    }

    private void scanAttachments(Message message) {
        for (Attachment attachment : message.getAttachments()) {
            String result = scanAttachment(attachment);
            if (result != null) {
                throw new IllegalArgumentException("Virus found in attachment " + attachment + "\nDescription: " + result);
            }
        }
    }

    private String scanAttachment(Attachment attachment) {
        // todo
        return null;
    }

    public void validateEmail(Message message) {
        scanAttachments(message);
        userMail.validateEmail(message);
    }
}
