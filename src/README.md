Copyright 2026 Ana-Cristina Calota 323CA

Tema #2 Programare Orientata pe Obiecte

Bug Tracker Pro

Aplicatia Bug Tracker Pro reprezinta un sistem de gestionare a tichetelor,
avand un ciclu de viata iterativ, care se intampla in 3 faze: Testare,
Dezvoltare si Verificare.

Arhitectura

Sistemul este organizat structural pentru a asigura a o separare clara a
responsabilitatilor:

1. Pachetul fileio: Utilizeaza biblioteca Jackson pentru maparea datelor JSON direct in
obiecte Java, eliminand parsing-ul manual. (De asemenea, am eliminat TicketInput
ca sa nu fie cazul de reflection in convertValue()).
2. Pachetul commands: Reprezinta o interfata intre utilizator si sistem. Fiecare cerere
este transformata intr-un obiect de tip comanda care contine toate datele necesare executiei.
3. Pachetul entities: Contine modelele de baza (tichete, milestone-uri, utilizatori) care
isi gestioneaza starea interna si comportamentul specific.
4. Pachetul app:
  - Contine serviciile care incapsuleaza logica pentru gestionarea comenzilor,
    in functie de entitatea vizata.
  - Contine un singleton AppContext care are datele globale ale aplicatiei.

Design Patterns implementate:
1. Singleton
- Implementare: AppContext
- Rol: gestioneaza starea globala a aplicatiei (baza de date de utilizatori,
tichete si milestone-uri). Asigura un punct unic de acces la resursele sistemului si include
o metoda de reset() pentru a garanta izolarea testelor in timpul executiei consecutive a
mai multor fisiere de input.

2. Factory
- Implementare: TicketFactory, UserFactory si CommandFactory
- Rol: centralizeaza instantierea obiectelor complexe.
  - TicketFactory foloseste o ierarhie de creatori (BugTicketCreator, etc.) pentru a
    mapa nodurile JSON in tipuri concrete de tichete.
  - CommandFactory decupleaza clasa App de implementarile specifice ale comenzilor,
    instantiind obiectul corect pe baza numelui primit din JSON.
  - UserFactory e esential pentru initializarea bazei de date. Acesta creeaza instante
    de Developer, Manager sau Reporter pe baza rolului din JSON. Acest Factory garanteaza ca
    obiectele create implementeaza interfata Observer, permitandu-le sa fie "abonati"
    ulterior la tichete sau milestone-uri.

3. Observer
- Implementare: clasele Subject si interfata Observer
- Rol: automatizeaza sistemul de notificari fara a cupla direct componentele. 
  - Clasele Ticket si Milestone sunt Subiecte. Orice schimbare semnificativa
    (schimbare status, atingere deadline) declanseaza automat notificarea
    "abonatilor" (observers).
  - Utilizatorii (User) sunt Observatori. Cand primesc un update, acestia
    decid cum sa gestioneze mesajul (salvare in log-ul de notificari din AppContext).
  
4. Strategy
- Implementare: interfata SearchStrategy si clasa SearchEngine
- Rol: permite cautarea avansata prin aplicarea unor filtre.
  - Fiecare criteriu (Prioritate, Tip, Senioritate, Keywords) este implementat
    ca o strategie separata.
  - SearchEngine este generic si poate filtra liste de orice tip (Ticket sau Developer)
    pe baza unei liste de strategii primite.
  
5. Command:
- Asta este o incercare de implementare, dar nu cred ca e corecta. Am incerat
sa introduc undo in invoker si sa implementez logica pentru comenzile cu undo,
dar nu am reusit sa o implementez cu tot cu cazurile limita, asa ca invokerul are
acum doar metoda de execute.
- Implementare: interfata Command, clasele BaseCommand si CommandInvoker
- Rol: incapsuleaza fiecare actiune a utilizatorului intr-un obiect independent
  - Invoker-ul actioneaza ca un dispecer care declanseaza executia comenzilor
  - BaseCommand elimina duplicarea codului prin centralizarea logicilor de
    verificare a permisiunilor si formatarea erorilor JSON.

Fluxul de executie al unei comenzi
1. Citire: App preia o comanda din input.
2. Inainte de executie, MilestoneService proceseaza trecerea timpului
   pentru toate milestone-urile active (actualizeaza prioritatile la
   fiecare 3 zile si verifica regulile de deadline).
3. CommandFactory analizeaza tipul comenzii si creeaza un obiect Command concret.
4. CommandInvoker primeste comanda si apeleaza metoda execute().
5. Comanda deleaga logica catre un serviciu specializat (ex: TicketService),
   care valideaza regulile pentru comanda.
6. Se poate modifica starea interna a entitatii vizate (ex: un tichet devine IN_PROGRESS).
7. Schimbarea de stare declanseaza mecanismul de Observer. Entitatea anunta observatorii,
   iar acestia isi actualizeaza listele de notificari.
8. Rezultatul executiei (succes sau eroare) este returnat sub forma de ObjectNode si
   adaugat in lista finala de iesire.
