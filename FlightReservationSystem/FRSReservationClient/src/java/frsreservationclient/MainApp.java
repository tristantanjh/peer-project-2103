/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frsreservationclient;

import ejb.session.stateless.CustomerSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
import ejb.session.stateless.ReservationSessionBeanRemote;
import entity.CabinClass;
import entity.Customer;
import entity.Flight;
import entity.FlightRoute;
import ejb.session.stateless.FlightScheduleSessionBeanRemote;
import ejb.session.stateless.ReservationSessionBeanRemote;
import entity.CabinClass;
import entity.Customer;
import entity.Fare;
import entity.FlightSchedule;
import entity.Passenger;
import entity.Reservation;
import entity.Seat;
import enumeration.CabinClassEnum;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import util.exception.AirportNotFoundException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.InvalidLoginCredentialException;
import util.exception.NoAvailableSeatsException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author jonang
 */
public class MainApp {
    private Customer customer;
    private CustomerSessionBeanRemote customerSessionBeanRemote;
    private ReservationSessionBeanRemote reservationSessionBeanRemote;

    private FlightSessionBeanRemote flightSessionBeanRemote;

    private FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote;

    
    public MainApp() {
       
    }
    
    public MainApp(CustomerSessionBeanRemote customerSessionBeanRemote, ReservationSessionBeanRemote reservationSessionBeanRemote, FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote, FlightSessionBeanRemote flightSessionBeanRemote) {
       this();
       this.customerSessionBeanRemote = customerSessionBeanRemote;
       this.reservationSessionBeanRemote = reservationSessionBeanRemote;
       this.flightScheduleSessionBeanRemote = flightScheduleSessionBeanRemote;
       this.flightSessionBeanRemote = flightSessionBeanRemote;
    }
    
    public void runApp() {
        Scanner scanner = new Scanner(System.in);
        int response;

        while (true) {
            System.out.println("*** Welcome to the FRS reservation client system ***\n");
            System.out.println("1: Login");
            System.out.println("2: Register");
            System.out.println("3: Search Flight");
            System.out.println("4: Exit\n");

            response = 0;

            while (response < 1 || response > 4) {
                System.out.print("> ");

                if (scanner.hasNextInt()) {
                    response = scanner.nextInt();
                    scanner.nextLine();

                    switch (response) {
                        case 1:
                            try {
                                this.customer = this.doLogin(scanner);
                                break; // Exiting the switch after handling login
                            } catch (InvalidLoginCredentialException ex) {
                                System.out.println("Invalid login: " + ex.getMessage() + " Please try again!");
                                break;
                            }
                        case 2:
                            this.doRegister(scanner);
                            break; 
                        case 3:
                            this.searchFlight();
                            break;
                        case 4: 
                            System.out.println("Exiting the application.");
                            return;
                        default:
                            System.out.println("Invalid option, please try again!");
                            break; // Exiting the switch after an invalid option
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next(); // Clear the invalid input
                }
            }

            if (customer != null) {
                loggedInView();
            }
//            if (response == 2) {
//                break;
//            }
        }
    }

    public void loggedInView() {
        //1 Login --> runApp()
        //2 Register --> runApp()
        //3 Search Flight --> in runApp() as well --> ie in both 
        //4 Reserve Flight --> inside search flight ie search then do you want reserve?
        //5 View my Flight
        //6 View my flight reservation detail
        //7 customer log out --> set customer to null
        
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("*** Welcome to the FRS reservation client system ***\n");
            System.out.println("You are login as " + customer.getFirstName() + " " + customer.getLastName() + "\n");
            System.out.println("1: Search Flight");
            System.out.println("2: View Flight");
            System.out.println("3: View Flight Reservation Detail");
            System.out.println("4: logout\n"); 

            int response;

            System.out.print("> ");
            response = scanner.nextInt();

            switch (response) {
                case 1:
                    searchFlight();
                    break;
                case 2:
                    viewAllFlightReservations();
                    break;
                case 3:
                    viewFlightReservationDetails();
                    break;
                case 4:
                    System.out.println("Exiting the application.");
                    doLogOut();
                    return;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        }

    }
    
    
    public void viewAllFlightReservations() {        
        System.out.println("*** FRS Reservation :: View All Flight Reservations ***\n");
        List<Reservation> flightReservations = customer.getReservations();
        
        if (flightReservations.isEmpty()) {
            System.out.println("No available reservations!");
            return;
        }
        System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s\n", "Index", "Flight Reservation ID", "Departure Date", "Departure Airport", "Destination Airport", "Return Date");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
        Integer num = 0;

        for (Reservation res : flightReservations) {
            num++;
            System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s\n", num, res.getReservationId(), res.getDepartureDate(), res.getDepartureAirport(), res.getDestinationAirport(), res.getReturnDate());
        }

        System.out.println();
    }
    
        public void reserveFlight(Integer tripType, Integer numOfPassengers) {
        //try {
            Scanner scanner = new Scanner(System.in);
            DateFormat inputDateFormat = new SimpleDateFormat("dd MMM yy");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            System.out.println("*** FRS Reservation :: Search Flight :: Reserve Flight***\n");

            List<Long> flightScheduleIds = new ArrayList<>();
            List<Long> returnFlightScheduleIds = new ArrayList<>();

            System.out.print("Enter Departure Airport IATA Code> ");
            String departureAirportiATACode = scanner.nextLine().trim();
            System.out.print("Enter Destination Airport IATA Code> ");
            String destinationAirportiATACode = scanner.nextLine().trim();
            System.out.print("Enter Departure Date (dd Mmm yyyy ie  '31 Dec 23') > ");
            String departureDateString = scanner.nextLine().trim();
                    
             Date departureDate = null;
            try {
                departureDate = inputDateFormat.parse(departureDateString);
            } catch (ParseException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            
            Date returnDate = null;
            if (tripType == 2) {
                System.out.print("Enter Return Date (dd Mmm yyyy ie  '31 Dec 23') > ");
                try {
                    returnDate = inputDateFormat.parse(scanner.nextLine().trim());
                } catch (ParseException ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }

            boolean next = true;
            while (next) {
                System.out.print("Enter Flight Schedule ID to Reserve> ");
                flightScheduleIds.add(scanner.nextLong());

                System.out.print("More Flights to Reserve? Y/N> ");
                String option = scanner.nextLine().trim();

                if (option.charAt(0) == 'N') {
                    next = false;
                }

            }

            next = true;
            if (tripType == 2) {
                while (next) {
                    System.out.print("Enter Return Flight Schedule ID to Reserve> ");
                    returnFlightScheduleIds.add(scanner.nextLong());

                    System.out.print("More Flights to Reserve? Y/N> ");
                    String option = scanner.nextLine().trim();

                    if (option.charAt(0) == 'N') {
                        next = false;
                    }
                }
            }

            List<String> creditCard = new ArrayList<>();
            System.out.print("Enter Credit Card Number> ");
            creditCard.add(scanner.nextLine().trim());
            System.out.print("Enter Name On the Card> ");
            creditCard.add(scanner.nextLine().trim());
            System.out.print("Enter Expiry Date> ");
            creditCard.add(scanner.nextLine().trim());
            System.out.print("Enter CVV Number> ");
            creditCard.add(scanner.nextLine().trim());
            
            BigDecimal temp = new BigDecimal(1.00);
            
            List<Passenger> passengers = new ArrayList<>();
           
            for (int i = 1; i <= numOfPassengers; ++i) {
                
                try {
                System.out.println("Enter First Name of Passenger " + i + "> ");
                String firstName  = scanner.nextLine().trim();
                
                System.out.println("Enter Last Name of Passenger " + i + "> ");
                String lastName = scanner.nextLine().trim();
                
                System.out.println("Enter Passport Number of Passenger " + i + "> ");
                String passportNumber = scanner.nextLine().trim();
                
                System.out.println("Select Cabin Class For Passenger " + i + ": 1: First Class, 2: Business Class, 3: Premium Economy Class, 4: Economy Class> ");
                String cabinClass = scanner.nextLine().trim();
                /*passenger must have reseration tagged to it --> handle during session bean --> pass in reservation id as well
                for the creation of the passenger*/
                Passenger passenger = new Passenger(firstName, lastName, passportNumber, cabinClass); //need sessionbean for this
                
                passengers.add(passenger);
                
                } catch(Exception ex) { // for compile
                    System.out.println("Error: " + ex.getMessage());
                }
           
            Reservation reservation = new Reservation(temp, numOfPassengers, creditCard);
            try {
                Long newFlightReservationId = reservationSessionBeanRemote.reserveFlight(numOfPassengers,passengers, creditCard,
                        flightScheduleIds, returnFlightScheduleIds, departureAirportiATACode, destinationAirportiATACode, departureDate, returnDate, customer);
                System.out.println("Reserved Successfully! Flight Reservation ID: " + newFlightReservationId + "\n");

            } catch (NoAvailableSeatsException ex) {
                System.out.println("Error: " + ex.getMessage());
            }


                
                reservation.setPassengerList(passengers);
                //set thelist of passenger into reservation
                //reservation.set(passengers)
                
                
            }
        }



    
    
    public void viewFlightReservationDetails() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("*** FRS Reservation :: View Flight Reservation Details ***\n");
            System.out.print("Enter Reservation ID> ");
            Long reservationId = scanner.nextLong();
            Reservation res = reservationSessionBeanRemote.retrieveReservationById(reservationId);

            List<Passenger> passengers = res.getPassengers();

            System.out.println("Overall Reservation Details: ");
            System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s\n", "Index", "Flight Reservation ID", "Departure Date", "Departure Airport", "Destination Airport", "Return Date");
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-20s%-20s%-20s%-20s%-20s\n", res.getReservationId(), res.getDepartureDate(), res.getDepartureAirport(), res.getDestinationAirport(), res.getReturnDate());

            System.out.println("Flights in Reservations:");
            System.out.printf("%-20s%-20s%-20s%-20s%-20s\n", "Flight Number", "Departure Date", "Arrival Date", "Origin", "Destination Airport");
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
            
            List<FlightSchedule> flightSchedules = res.getFlightSchedules();
            for (FlightSchedule fs : flightSchedules) {
                String flightNum = fs.getFlightNumber();
                Flight flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNum); 
                Date departure = fs.getDepartureDateTime();
                Date arrival = fs.getArrivalDateTime();
                FlightRoute fr = flight.getFlightRoute();
                String origin = fr.getOrigin().getAirportName();
                String destination = fr.getDestination().getAirportName();
                System.out.printf("%-20s%-20s%-20s%-20s%-20s\n", flightNum, departure, arrival, origin, destination);
            }

            System.out.print("Reservation Details for Passenger: ");
            for (Passenger p : passengers) {
                System.out.print(p.getFirstName() + " " + p.getLastName());
                System.out.println();
                List<Seat> seats = p.getSeats();

                for (Seat s : seats) {
                    System.out.printf("%-20s%-20s%-20s\n", "Seat Number", "Cabin Class Type", "Flight Number");
                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
                    String seatNum = s.getSeatNumber();
                    CabinClassEnum ccType= s.getCabinClass().getCabinClassType();
                    FlightSchedule fs = s.getCabinClass().getFlightSchedule();
                    String flightNumber = fs.getFlightNumber();
                    System.out.printf("%-20s%-20s%-20s\n", seatNum, ccType, flightNumber);
                }
                System.out.println();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public Customer doLogin(Scanner sc) throws InvalidLoginCredentialException { //should return custiomer imo --> atm void just to have no errors
        //this.loggedInView();
        String username = "";
        String password = "";
    
        System.out.println("Please enter your login details.");

        System.out.println("Enter Username:");
        System.out.print("> ");
        username = sc.nextLine().trim();

        System.out.println("Enter Password:");
        System.out.print("> ");

        password = sc.nextLine().trim();
        
        try {
            Customer customer = customerSessionBeanRemote.login(username, password);
            return customer;
        } catch (InvalidLoginCredentialException ex) {
            throw ex;
        }
    }
    
    public void doRegister(Scanner sc) { //can decide whether auto login after register or not 
        String firstName = "";
        String lastName = "";
        String email = "";
        String contactNumber = "";
        String address = "";
        String username = "";
        String password = "";

        
        System.out.println("Enter First Name:");
        System.out.print("> ");
        firstName = sc.nextLine().trim();

        System.out.println("Enter Last Name:");
        System.out.print("> ");
        lastName = sc.nextLine().trim();

        System.out.println("Enter Email:");
        System.out.print("> ");
        email = sc.nextLine().trim();

        System.out.println("Enter Contact Number:");
        System.out.print("> ");
        contactNumber = sc.nextLine().trim();

        System.out.println("Enter Address:");
        System.out.print("> ");
        address = sc.nextLine().trim();

        System.out.println("Enter Username:");
        System.out.print("> ");
        username = sc.nextLine().trim();

        System.out.println("Enter Password:");
        System.out.print("> ");
        password = sc.nextLine().trim();
        
        if (firstName.length() > 0 && lastName.length() > 0 && email.length() > 0 && contactNumber.length() > 0 && address.length() > 0 && username.length() > 0 && password.length() > 0) {
            Customer newCustomer = new Customer(firstName, lastName, email, contactNumber, address, username, password);
            try {
            Long newCustId = customerSessionBeanRemote.registerCustomer(new Customer(firstName, lastName, email, contactNumber, address, username, password));
                System.out.println("Registered succefully! Customer ID: " + newCustId + "\n");
                customer = newCustomer;
            } catch (UnknownPersistenceException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println("Incomplete information, please try again!\n");
        }
    }
    
    
    public void doLogOut() {
        this.customer = null;
    }
    
       public void searchFlight() {

        try {
            Scanner scanner = new Scanner(System.in);
            //SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd MMM", Locale.US);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMM", Locale.US);
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd MMM yyyy");
            Integer tripType;
            String departureAirportiATACode = "";
            String destinationAirportiATACode = "";
            Date departureDate;
            Date returnDate = null;
            Integer numOfPassengers;
            Integer flightTypePreference;
            Integer cabinClass;

            System.out.println("*** FRS Reservation :: Search Flight ***\n");
            System.out.print("Enter Trip Type:  1: One-way, 2: Round-trip > ");
            tripType = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter Departure Airport IATA Code> ");
            departureAirportiATACode = scanner.nextLine().trim();
            System.out.print("Enter Destination Airport IATA Code> ");
            destinationAirportiATACode = scanner.nextLine().trim();
            System.out.print("Enter Departure Date (dd MMM yyyy  '01 JAN 2023')> ");
            departureDate = inputDateFormat.parse(scanner.nextLine().trim());
            
            if (tripType == 2) {
                System.out.println("Enter Return Date (dd MMM yyyy '01 JAN 2023')> ");
                returnDate = inputDateFormat.parse(scanner.nextLine().trim());
            }
            
            System.out.print("Enter Number Of Passengers> ");
            numOfPassengers = scanner.nextInt();
            System.out.print("Enter Flight Type Preference:  1: Direct Flight, 2: Connecting Flight, 3: No Preference > ");
            flightTypePreference = scanner.nextInt();
            System.out.print("Enter Cabin Class Preference:  1: First Class, 2: Business Class, 3: Premium Economy Class, 4: Economy Class, 5: No Preference > ");
            cabinClass = scanner.nextInt();

            CabinClassEnum cabinClassType = null;
            
            switch (cabinClass) {
                case 1:
                    cabinClassType = CabinClassEnum.FIRST;
                    break;
                case 2:
                    cabinClassType = CabinClassEnum.BUSINESS;
                    break;
                case 3:
                    cabinClassType = CabinClassEnum.PREMIUMECONOMY;
                    break;
                case 4:
                    cabinClassType = CabinClassEnum.ECONOMY;
                    break;
                default:
                    break;
            }

            switch (flightTypePreference) {
                case 1:
                    searchDirectFlight(departureAirportiATACode, destinationAirportiATACode, departureDate, returnDate, numOfPassengers, tripType, cabinClassType);
                    break;
                case 2:
                    searchConnectingFlights(departureAirportiATACode, destinationAirportiATACode, departureDate, returnDate, numOfPassengers, tripType, cabinClassType);
                    break;
                default:
                    searchDirectFlight(departureAirportiATACode, destinationAirportiATACode, departureDate, returnDate, numOfPassengers, tripType, cabinClassType);
                    searchConnectingFlights(departureAirportiATACode, destinationAirportiATACode, departureDate, returnDate, numOfPassengers, tripType, cabinClassType);
                    break;
            }

            scanner.nextLine();
            System.out.print("Do you wish to reserve a flight? (Y/N) > ");
            String reserve = scanner.nextLine().trim();

            if (reserve.equals("Y")) {
                reserveFlight(tripType, numOfPassengers);
            }
        } catch (ParseException ex) {
            System.out.println("Invalid date input!\n");
        } catch (AirportNotFoundException | FlightScheduleNotFoundException ex) {
            System.out.println("Error: " + ex.getMessage());
            System.out.println();
        }
    }
       
 
    public void searchDirectFlight(String departureAirportiATACode, String destinationAirportiATACode, Date departureDate, Date returnDate, Integer numOfPassengers,
            Integer tripType, CabinClassEnum cabinClassType) throws AirportNotFoundException, FlightScheduleNotFoundException {
        try {
            System.out.println("Departure Flight Information :: Direct Flight\n");
            //on required departure date
            //System.out.println("sdf debug1  : "  + departureDate);
            
            List<FlightSchedule> flightSchedules =new ArrayList<>();
           
            try {
                flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, departureDate, cabinClassType);
            } catch(FlightScheduleNotFoundException ex) {
                flightSchedules = new ArrayList<>();
            }
            
            //System.out.println("sdf debug2");
            if (flightSchedules.isEmpty()) {
                System.out.println("No available flights departing on " + departureDate + " !\n");
            } else {
                System.out.println("----- Departure On " + departureDate + "\n");
                printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
            }

            //System.out.println("sdf debug3");
            //3 days before
            for (int i = 3; i > 0; --i) {
                Date newDepartureDate = new Date(departureDate.getTime() - i * 24 * 60 * 60 * 1000);
                System.out.println("sdf debug2 :" + newDepartureDate);
            try {
                flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, newDepartureDate, cabinClassType);
            } catch(FlightScheduleNotFoundException ex) {
                flightSchedules = new ArrayList<>();
            }
            
                if (flightSchedules.isEmpty()) {
                    System.out.println("No available flights departing on " + newDepartureDate + " !\n");
                } else {
                    System.out.println("----- Departure On " + departureDate + "\n");
                    printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                }
            }

            //3 days after
            for (int i = 1; i < 4; ++i) {
                Date newDepartureDate = new Date(departureDate.getTime() + i * 24 * 60 * 60 * 1000);
                System.out.println("sdf debug3 :" + newDepartureDate);
                try {
                    flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, newDepartureDate, cabinClassType);
                } catch(FlightScheduleNotFoundException ex) {
                    flightSchedules = new ArrayList<>();
                }
            
                if (flightSchedules.isEmpty()) {
                    System.out.println("No available flights departing on " + newDepartureDate + " !\n");
                } else {
                    System.out.println("----- Departure On " + departureDate + "\n");
                    printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                }
            }

            if (tripType == 2) {
                System.out.println("\nReturn Flight Information :: Direct Flights\n");
                //on required departure date
                
                try {
                    flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, departureDate, cabinClassType);
                } catch(FlightScheduleNotFoundException ex) {
                    flightSchedules = new ArrayList<>();
                }
                
                if (flightSchedules.isEmpty()) {
                    System.out.println("No available flights departing on " + returnDate + " !\n");
                } else {
                    System.out.println("----- Return On " + returnDate + "\n");
                    printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                }

                //3 days before
                for (int i = 3; i > 0; --i) {
                    ;
                    Date newReturnDate = new Date(returnDate.getTime() - i * 24 * 60 * 60 * 1000);
                    
                    try {
                        flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, newReturnDate, cabinClassType);
                    } catch(FlightScheduleNotFoundException ex) {
                        flightSchedules = new ArrayList<>();
                    }
                    
                    if (flightSchedules.isEmpty()) {
                        System.out.println("No available flights departing on " + newReturnDate + " !\n");
                    } else {
                        System.out.println("----- Return On " + newReturnDate + "\n");
                        printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                    }
                }

                //3 days after
                for (int i = 1; i < 4; ++i) {
                    
                    Date newReturnDate = new Date(departureDate.getTime() + i * 24 * 60 * 60 * 1000);
                   
                    try {
                        flightSchedules = flightScheduleSessionBeanRemote.searchDirectFlightSchedules(departureAirportiATACode, destinationAirportiATACode, newReturnDate, cabinClassType);
                    } catch(FlightScheduleNotFoundException ex) {
                        flightSchedules = new ArrayList<>();
                    }

                    if (flightSchedules.isEmpty()) {
                        System.out.println("No available flights departing on " + newReturnDate + " !\n");
                    } else {
                        System.out.println("----- Return On " + newReturnDate + "\n");
                        printDirectFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);

                    }
                }
            }
        } catch (AirportNotFoundException /*| FlightScheduleNotFoundException*/ ex) {
            throw ex;
        }
    }
    
       public void printDirectFlightSchedulesTable(List<FlightSchedule> flightSchedules, CabinClassEnum cabinClassType, Integer numOfPassengers) {

        System.out.printf("%3s%15s%15s%35s%30s%18s%35s%30s%30s%37s%37s%37s%37s%8s\n", "   ", "Flight Schdule ID", "Flight Number", "Departure Airport", "Departure Time (Local Time)",
                "Flight Duration Hours","Flight Duration minutes" , "Destination Airport", "Arrival Time (Local Time)", "First Class Available Seats", "First Class Price", "Business Class Available Seats",
                "Business Class Price", "Premium Economy Class Available Seats", "Premium Economy Class Price", "Economy Class Available Seats", "Economy Class Price");

        String firstClassAvailableSeats = "/";
        String businessClassAvailableSeats = "/";
        String premiumEcoClassAvailableSeats = "/";
        String economyClassAvailableSeats = "/";

        Double lowestFareFirstClass = Double.MAX_VALUE;
        Double lowestFareBusinessClass = Double.MAX_VALUE;
        Double lowestFarePremiumEconomyClass = Double.MAX_VALUE;
        Double lowestFareEconomyClass = Double.MAX_VALUE;

        Integer num = 1;

        System.out.println(cabinClassType.toString().equals("FIRST"));
        
        for (FlightSchedule flightSchedule : flightSchedules) {
            
            System.out.println(flightSchedule.getCabinClasses().size());
 
            
            for (CabinClass cabinClass : flightSchedule.getCabinClasses()) {
                
                 System.out.println("dawwen wins 1" );
                if (cabinClassType.toString().equals("FIRST")) {
                    System.out.println("dawwen wins 10" );
                    firstClassAvailableSeats = cabinClass.getAvailableSeats().toString();
                    System.out.println("dawwen wins 10" + cabinClass.getFares().size());
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareFirstClass = Math.min(lowestFareFirstClass, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.toString().equals("BUSINESS")) {
                    businessClassAvailableSeats = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareBusinessClass = Math.min(lowestFareBusinessClass, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.toString().equals("PREMIUMECONOMY")) {
                    premiumEcoClassAvailableSeats = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFarePremiumEconomyClass = Math.min(lowestFarePremiumEconomyClass, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.toString().equals("ECONOMY")) {
                    economyClassAvailableSeats = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareEconomyClass = Math.min(lowestFareEconomyClass, fare.getFareAmount().doubleValue());
                    }
                }
            }

            String firstClassFare;
            String businessClassFare;
            String premiumEconomyClassFare;
            String economyClassFare;

            if (lowestFareFirstClass == Double.MAX_VALUE) {
                firstClassFare = "/";
            } else {
                firstClassFare = lowestFareFirstClass.toString();
            }
            if (lowestFareBusinessClass == Double.MAX_VALUE) {
                businessClassFare = "/";
            } else {
                businessClassFare = lowestFareBusinessClass.toString();
            }
            if (lowestFarePremiumEconomyClass == Double.MAX_VALUE) {
                premiumEconomyClassFare = "/";
            } else {
                premiumEconomyClassFare = lowestFarePremiumEconomyClass.toString();
            }
            if (lowestFareEconomyClass == Double.MAX_VALUE) {
                economyClassFare = "/";
            } else {
                economyClassFare = lowestFareEconomyClass.toString();
            }

            System.out.printf("%5s%15s%15s%35s%30s%18s%35s%30s%30s%37s%37s%37s%37s%8s\n", num.toString() + ": " , flightSchedule.getFlightScheduleId(), flightSchedule.getFlightNumber(), flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOrigin().getIataAirportcode(), flightSchedule.getDepartureDateTime(), flightSchedule.getEstimatedFlightDurationHours(), flightSchedule.getEstimatedFlightDurationMinutes(),
        flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getIataAirportcode(), flightSchedule.getArrivalDateTime(), firstClassAvailableSeats, firstClassFare, businessClassAvailableSeats, businessClassFare, premiumEcoClassAvailableSeats, premiumEconomyClassFare, economyClassAvailableSeats, economyClassFare);

            num++;
        }
    }
       
    public void searchConnectingFlights(String departureAirportiATACode, String destinationAirportiATACode, Date departureDate, 
             Date returnDate, Integer numOfPassengers, Integer tripType, CabinClassEnum cabinClassType) throws AirportNotFoundException, FlightScheduleNotFoundException {
                try {
            System.out.println("\nDeparture Flight Information :: Connecting Flights\n");
            //on required departure date
            List<List<FlightSchedule>> flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(departureAirportiATACode, destinationAirportiATACode, departureDate, cabinClassType);
            System.out.println("----- Departure On " + departureDate + "\n");
            printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);

            //3 days before
            for (int i = 3; i > 0; --i) {
                Date newDepartureDate = new Date(departureDate.getTime() - i * 24 * 60 * 60 * 1000);
                flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(departureAirportiATACode, destinationAirportiATACode, newDepartureDate, cabinClassType);
                System.out.println("----- Departure On " + departureDate + "\n");
                printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
            }

            //3 days after
            for (int i = 1; i < 4; ++i) {
                Date newDepartureDate = new Date(departureDate.getTime() + i * 24 * 60 * 60 * 1000);
                flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(departureAirportiATACode, destinationAirportiATACode, departureDate, cabinClassType);
                System.out.println("----- Departure On " + departureDate + "\n");
                printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
            }

            if (tripType == 2) {
                System.out.println("\nReturn Flight Information: \n");
                //on required departure date
                flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(destinationAirportiATACode, departureAirportiATACode, returnDate, cabinClassType);
                System.out.println("----- Return On " + returnDate + "\n");
                printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);

                //3 days before
                for (int i = 3; i > 0; --i) {
                    Date newReturnDate = new Date(returnDate.getTime() - i * 24 * 60 * 60 * 1000);
                    flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(destinationAirportiATACode, departureAirportiATACode, newReturnDate, cabinClassType);
                    System.out.println("----- Return On " + returnDate + "\n");
                    printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                }

                //3 days after
                for (int i = 1; i < 4; ++i) {
                    Date newReturnDate = new Date(departureDate.getTime() + i * 24 * 60 * 60 * 1000);
                    flightSchedules = flightScheduleSessionBeanRemote.searchConnectingFlightScehdules(destinationAirportiATACode, departureAirportiATACode, newReturnDate, cabinClassType);
                    System.out.println("----- Return On " + returnDate + "\n");
                    printConnectingFlightSchedulesTable(flightSchedules, cabinClassType, numOfPassengers);
                }
            }
        } catch (AirportNotFoundException | FlightScheduleNotFoundException ex) {

            throw ex;

        } 
        
   }
    
       public void printConnectingFlightSchedulesTable(List<List<FlightSchedule>> flightSchedules, CabinClassEnum cabinClassType, Integer numOfPassengers) {

        System.out.printf("%3s%15s%15s%35s%30s%18s%35s%30s%30s%37s%37s%37s%37s%8s\n", "   ", "Flight Schdule ID", "Flight Number", "Departure Airport", "Departure Time (Local Time)",
                "Flight Duration Hours", "Flight Duration minutes", "Destination Airport", "Arrival Time (Local Time)", "First Class Available Seats", "First Class Price", "Business Class Available Seats",
                "Business Class Price", "Premium Economy Class Available Seats", "Premium Economy Class Price", "Economy Class Available Seats", "Economy Class Price");

        String firstClassAvailableSeats1 = "/";
        String businessClassAvailableSeats1 = "/";
        String premiumEcoClassAvailableSeats1 = "/";
        String economyClassAvailableSeats1 = "/";
        String firstClassAvailableSeats2 = "/";
        String businessClassAvailableSeats2 = "/";
        String premiumEcoClassAvailableSeats2 = "/";
        String economyClassAvailableSeats2 = "/";

        Double lowestFareFirstClass1 = Double.MAX_VALUE;
        Double lowestFareBusinessClass1 = Double.MAX_VALUE;
        Double lowestFarePremiumEconomyClass1 = Double.MAX_VALUE;
        Double lowestFareEconomyClass1 = Double.MAX_VALUE;
        Double lowestFareFirstClass2 = Double.MAX_VALUE;
        Double lowestFareBusinessClass2 = Double.MAX_VALUE;
        Double lowestFarePremiumEconomyClass2 = Double.MAX_VALUE;
        Double lowestFareEconomyClass2 = Double.MAX_VALUE;

        Integer num = 1;
        for (List<FlightSchedule> firstFlightSchedules : flightSchedules) {
            FlightSchedule firstFlightSchedule = firstFlightSchedules.remove(0);
            for (CabinClass cabinClass : firstFlightSchedule.getCabinClasses()) {
                if (cabinClassType.equals(CabinClassEnum.FIRST)) {
                    firstClassAvailableSeats1 = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareFirstClass1 = Math.min(lowestFareFirstClass1, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.equals(CabinClassEnum.BUSINESS)) {
                    businessClassAvailableSeats1 = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareBusinessClass1 = Math.min(lowestFareBusinessClass1, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.equals(CabinClassEnum.PREMIUMECONOMY)) {
                    premiumEcoClassAvailableSeats1 = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFarePremiumEconomyClass1 = Math.min(lowestFarePremiumEconomyClass1, fare.getFareAmount().doubleValue());
                    }
                }
                if (cabinClassType.equals(CabinClassEnum.ECONOMY)) {
                    economyClassAvailableSeats1 = cabinClass.getBalanceSeats().toString();
                    for (Fare fare : cabinClass.getFares()) {
                        lowestFareEconomyClass1 = Math.min(lowestFareEconomyClass1, fare.getFareAmount().doubleValue());
                    }
                }
            }

            String firstClassFare1;
            String businessClassFare1;
            String premiumEconomyClassFare1;
            String economyClassFare1;

            if (lowestFareFirstClass1 == Double.MAX_VALUE) {
                firstClassFare1 = "/";
            } else {
                firstClassFare1 = lowestFareFirstClass1.toString();
            }
            if (lowestFareBusinessClass1 == Double.MAX_VALUE) {
                businessClassFare1 = "/";
            } else {
                businessClassFare1 = lowestFareBusinessClass1.toString();
            }
            if (lowestFarePremiumEconomyClass1 == Double.MAX_VALUE) {
                premiumEconomyClassFare1 = "/";
            } else {
                premiumEconomyClassFare1 = lowestFarePremiumEconomyClass1.toString();
            }
            if (lowestFareEconomyClass1 == Double.MAX_VALUE) {
                economyClassFare1 = "/";
            } else {
                economyClassFare1 = lowestFareEconomyClass1.toString();
            }

            for (FlightSchedule secondFlightSchedule : firstFlightSchedules) {
                for (CabinClass cabinClass : secondFlightSchedule.getCabinClasses()) {
                    if (cabinClassType.equals(CabinClassEnum.FIRST)) {
                        firstClassAvailableSeats2 = cabinClass.getBalanceSeats().toString();
                        for (Fare fare : cabinClass.getFares()) {
                            lowestFareFirstClass2 = Math.min(lowestFareFirstClass2, fare.getFareAmount().doubleValue());
                        }
                    }
                    if (cabinClassType.equals(CabinClassEnum.BUSINESS)) {
                        businessClassAvailableSeats2 = cabinClass.getBalanceSeats().toString();
                        for (Fare fare : cabinClass.getFares()) {
                            lowestFareBusinessClass2 = Math.min(lowestFareBusinessClass2, fare.getFareAmount().doubleValue());
                        }
                    }
                    if (cabinClassType.equals(CabinClassEnum.PREMIUMECONOMY)) {
                        premiumEcoClassAvailableSeats2 = cabinClass.getBalanceSeats().toString();
                        for (Fare fare : cabinClass.getFares()) {
                            lowestFarePremiumEconomyClass2 = Math.min(lowestFarePremiumEconomyClass2, fare.getFareAmount().doubleValue());
                        }
                    }
                    if (cabinClassType.equals(CabinClassEnum.ECONOMY)) {
                        economyClassAvailableSeats2 = cabinClass.getBalanceSeats().toString();
                        for (Fare fare : cabinClass.getFares()) {
                            lowestFareEconomyClass2 = Math.min(lowestFareEconomyClass2, fare.getFareAmount().doubleValue());
                        }
                    }
                }

                String firstClassFare2;
                String businessClassFare2;
                String premiumEconomyClassFare2;
                String economyClassFare2;

                if (lowestFareFirstClass2 == Double.MAX_VALUE) {
                    firstClassFare2 = "/";
                } else {
                    firstClassFare2 = lowestFareFirstClass2.toString();
                }
                if (lowestFareBusinessClass2 == Double.MAX_VALUE) {
                    businessClassFare2 = "/";
                } else {
                    businessClassFare2 = lowestFareBusinessClass2.toString();
                }
                if (lowestFarePremiumEconomyClass2 == Double.MAX_VALUE) {
                    premiumEconomyClassFare2 = "/";
                } else {
                    premiumEconomyClassFare2 = lowestFarePremiumEconomyClass2.toString();
                }
                if (lowestFareEconomyClass2 == Double.MAX_VALUE) {
                    economyClassFare2 = "/";
                } else {
                    economyClassFare2 = lowestFareEconomyClass2.toString();
                }

                System.out.printf("%3s%15s%15s%35s%30s%%30s18s%35s%30s%37s%37s%37s%37s%8s\n", num, firstFlightSchedule.getFlightScheduleId(), firstFlightSchedule.getFlightNumber(), firstFlightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getIataAirportcode(), firstFlightSchedule.getDepartureDateTime(), firstFlightSchedule.getDepartureDateTime().getTime(), firstFlightSchedule.getEstimatedFlightDurationHours(), firstFlightSchedule.getEstimatedFlightDurationMinutes(),
                        firstFlightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getIataAirportcode(), firstFlightSchedule.getArrivalDateTime(), firstClassAvailableSeats1, firstClassFare1, businessClassAvailableSeats1, businessClassFare1, premiumEcoClassAvailableSeats1, premiumEconomyClassFare1, economyClassAvailableSeats1, economyClassFare1);

                System.out.printf("%3s%15s%15s%35s%30s%30s%18s%35s%30s%37s%37s%37s%37s%8s\n", num, secondFlightSchedule.getFlightScheduleId(), secondFlightSchedule.getFlightNumber(), secondFlightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getIataAirportcode(), secondFlightSchedule.getDepartureDateTime(), secondFlightSchedule.getDepartureDateTime().getTime(), secondFlightSchedule.getEstimatedFlightDurationHours(), secondFlightSchedule.getEstimatedFlightDurationMinutes(),
                        secondFlightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getIataAirportcode(), secondFlightSchedule.getArrivalDateTime(), firstClassAvailableSeats2, firstClassFare2, businessClassAvailableSeats2, businessClassFare2, premiumEcoClassAvailableSeats2, premiumEconomyClassFare2, economyClassAvailableSeats2, economyClassFare2);

                num++;
            }

        }
    }
    
   

    
}
