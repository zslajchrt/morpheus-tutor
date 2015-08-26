package org.cloudio.morpheus.mail;

/**
 *
 * Created by zslajchrt on 24/08/15.
 */
public class VirusDetector implements UserMail {

    private final UserMail userMail;

    public VirusDetector(UserMail userMail) {
        this.userMail = userMail;
    }

    public void sendEmail(Message message) {
        // It would not have to be called here explicitly, if there were not the delegation
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

    /**
     * This implementation is used by this class and can be used by users of this class. However, it does not
     * play its role completely, since, because of the delegation, it is not called from within userMail.sendMail().
     */
    public void validateEmail(Message message) {
        scanAttachments(message);
        userMail.validateEmail(message);
    }
}
