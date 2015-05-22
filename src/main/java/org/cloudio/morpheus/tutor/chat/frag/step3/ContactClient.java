package org.cloudio.morpheus.tutor.chat.frag.step3;

import org.cloudio.morpheus.tutor.chat.frag.step1.Contact;

/**
 * Created by zslajchrt on 04/05/15.
 */
public class ContactClient {

    public static <T extends Contact & ContactPrinter> void useContact(T contactPrinter) {
        contactPrinter.printContact();
        String fn = contactPrinter.firstName();
        String ln = contactPrinter.lastName();
    }

}
