package app.tickets;

import app.tickets.enums.BusinessValue;

public class UiFeedbackTicket extends Ticket {
    private String uiElementId; // optional
    private BusinessValue businessValue;
    private int usabilityScore;
    private String screenshotUrl; // optional
    private String suggestedFix; // optional


}