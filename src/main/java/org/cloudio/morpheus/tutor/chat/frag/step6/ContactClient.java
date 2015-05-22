package org.cloudio.morpheus.tutor.chat.frag.step6;

import org.cloudio.morpheus.tutor.chat.frag.step1.Contact;
import org.cloudio.morpheus.tutor.chat.frag.step4.ContactPrinter;

/**
 *
 * Created by zslajchrt on 04/05/15.
 */
public class ContactClient {

    public static final Boolean printMode = Boolean.getBoolean("printMode");

    public static <T extends Contact & ContactPrinter & PrinterControl> void useContact(T contactPrinter, boolean mode) {
        if (mode) {
            contactPrinter.prettyPrint();
        } else {
            contactPrinter.rawPrint();
        }
        contactPrinter.printContact();
    }

    public static <T extends Contact & ContactPrinter & PrinterControl> void useContact(T contactPrinter) {
        useContact(contactPrinter, printMode);
    }

}
