Copyright 2026 Ana-Cristina Calota

# Bug Tracker Pro

## Overview
The project represents the implementation of a ticket management system with an iterative lifecycle made of three phases: Testing, Development and Verification. The application reads users and commands from JSON files, processes the commands in chronological order and writes the results to a JSON output file.

## Project Details
I created a global database using the Singleton pattern in order to store all the registered users, tickets, milestones and notifications. The AppContext also keeps the global state of the application (testing phase active, investors lost) and owns a reset method that guarantees the isolation of tests when multiple input files are run one after another.

The project is split into packages with clearly separated responsibilities:
- fileio: uses the Jackson library to map the JSON input directly into Java objects, without manual parsing.
- commands: represents the interface between the user and the system. Every request is turned into a command object that holds all the data needed for its execution.
- entities: contains the base models (users, tickets, milestones, comments), which manage their own internal state and specific behavior.
- app: contains the services that encapsulate the logic of each command, depending on the entity it targets, and the AppContext singleton.
- search: contains the search strategies and the generic search engine.
- metrics: contains the metrics used to compute the reports.
- notifications: contains the Subject and Observer classes.

The users are created as subclasses of the User class (Reporter, Developer and Manager) using the Factory design pattern, based on the role given in the JSON. A Developer can only handle tickets according to his seniority (Junior, Mid or Senior) and expertise area.

The tickets are also created using the Factory design pattern. TicketFactory uses a hierarchy of creators (BugTicketCreator, FeatureRequestTicketCreator and UiFeedbackTicketCreator) to map the JSON nodes to concrete types of tickets.

The commands are created by the CommandFactory, which decouples the App class from the concrete implementations. It contains a switch case based on the name of the command and returns the matching command object, or null in case of an unknown command. The command is then given to the CommandInvoker, which calls its execute method. BaseCommand centralizes the permission checks and the formatting of the JSON errors.

The search is implemented using the Strategy design pattern. Every criterion is a separate class that implements SearchStrategy, and the generic SearchEngine keeps only the items that match all the strategies it receives. It can filter lists of any type (Ticket or Developer).

The reports are computed using metrics that extend AbstractMetric and are stored in the MetricEngine, which returns them by name.

## Execution flow
1. App reads the next command from the input file.
2. Before the execution, MilestoneService processes the passing of time for all active milestones (it updates the priorities every 3 days and applies the deadline rules).
3. CommandFactory creates the concrete command.
4. CommandInvoker receives the command and calls its execute method.
5. The command delegates the logic to a specialized service (e.g. TicketService), which validates the rules of the command.
6. The internal state of the targeted entity may change (e.g. a ticket becomes IN_PROGRESS).
7. The change triggers the Observer mechanism: the entity notifies its observers, which update their notification lists.
8. The result (success or error) is returned as an ObjectNode and added to the final output list.

The execution stops as soon as the investors are lost.

## Available commands
reportTicket: creates a new ticket (Bug, Feature Request or UI Feedback) in the database. It can only be done during a testing phase.

viewTickets: shows the tickets the user is allowed to see.

createMilestone: creates a new milestone that groups a list of tickets, has a due date and can block other milestones.

viewMilestones: shows the milestones the user is allowed to see.

assignTicket: assigns a ticket to a developer, only if his seniority and expertise area allow it.

undoAssignTicket: de-assigns the ticket from the developer and puts it back to the OPEN status.

viewAssignedTickets: shows the tickets assigned to the current developer.

addComment: adds a comment to a ticket.

undoAddComment: removes the last comment added by the user on a ticket.

changeStatus: moves a ticket to the next status: OPEN, IN_PROGRESS, RESOLVED, CLOSED.

undoChangeStatus: moves a ticket back to its previous status.

viewTicketHistory: shows the history of the actions performed on a ticket (assigned, de-assigned, status changed, added to milestone, removed from developer).

search: filters tickets or developers using criteria such as business priority, type, keywords, creation date, seniority, expertise, performance score or availability for assignment.

viewNotifications: shows and then clears all the notifications received by the user.

startTestingPhase: starts a new testing phase, which lasts for 12 days.

generateCustomerImpactReport: computes the customer impact of the tickets.

generateTicketRiskReport: computes the risk of the tickets.

generateResolutionEfficiencyReport: computes how efficiently the tickets were resolved.

appStabilityReport: gives an overview of the stability of the application, based on the risk and the impact of the tickets.

generatePerformanceReport: computes the performance score of the developers, depending on their seniority.

lostInvestors: marks the investors as lost, which stops the processing of the remaining commands.

## Design Patterns
### Singleton
- Implementation: AppContext
- Role: manages the global state of the application (users, tickets and milestones database). It provides a single access point to the resources of the system.

### Factory
- Implementation: TicketFactory, UserFactory and CommandFactory
- Role: centralizes the instantiation of complex objects.
    - TicketFactory maps the JSON nodes to concrete types of tickets.
    - UserFactory creates Developers, Managers or Reporters based on the role. It guarantees that the created objects implement the Observer interface, so they can be subscribed to tickets or milestones.
    - CommandFactory creates the right command based on the name received from the JSON.

### Observer
- Implementation: the Subject class and the Observer interface
- Role: automates the notification system without directly coupling the components.
    - Ticket and Milestone are Subjects. Every important change (status change, deadline reached) automatically notifies their subscribers.
    - The users are Observers. When they receive an update, they save the message in the notification log from AppContext.

### Strategy
- Implementation: the SearchStrategy interface and the SearchEngine class
- Role: allows advanced searching by applying a list of filters.
    - Every criterion (Priority, Type, Seniority, Keywords etc.) is implemented as a separate strategy.
    - SearchEngine is generic and can filter lists of any type, based on the list of strategies it receives.

### Command
- Implementation: the Command interface, the BaseCommand class and the CommandInvoker class
- Role: encapsulates every action of the user in an independent object.
    - The invoker acts as a dispatcher that triggers the execution of the commands.
    - BaseCommand removes the code duplication by centralizing the permission checks and the formatting of the JSON errors.
- Note: I tried to implement the undo logic inside the invoker, but I could not cover all the edge cases, so the invoker only has the execute method. The undo commands are handled by dedicated commands and services instead.
