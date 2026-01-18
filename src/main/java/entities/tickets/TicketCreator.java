package entities.tickets;

import fileio.TicketInput;

public interface TicketCreator {
    /**
     * Creates a Ticket object based on the provided TicketInput
     *
     * @param id the unique identifier for the ticket
     * @param ticketInput the input data for the ticket
     * @return the created Ticket object
     */
    Ticket create(int id, TicketInput ticketInput);
}
