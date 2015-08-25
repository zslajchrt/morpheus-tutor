package org.cloudio.morpheus.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zslajchrt on 25/08/15.
 */
public class Message {
    private List<String> recipients;
    private String subject;
    private String body;
    private List<Attachment> attachments = Collections.<Attachment>emptyList();

    public Message() {
    }

    public Message(Message source) {
        recipients = new ArrayList<String>(source.recipients);
        subject = source.subject;
        body = source.body;
        attachments = new ArrayList<Attachment>(source.attachments);
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
