package org.cloudio.morpheus.mail;

/**
 * Created by zslajchrt on 24/08/15.
 */
public class EmployeeUserMail extends DefaultUserMail {
    public EmployeeUserMail(Employee employee) {
        super(new EmployeeAdapter(employee));
    }

    @Override
    public void sendEmail(Message message) {
        Employee employee = ((EmployeeAdapter) getMailOwner()).getEmployee();
        String signature = "\n\n" + employee.getFirstName() + " " + employee.getLastName() + "\n" + employee.getDepartment();

        Message newMsg = new Message(message);
        newMsg.setBody(message.getBody() + signature);

        super.sendEmail(newMsg);
    }

}
