/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/StatelessEjbClass.java to edit this template
 */
package ejb.session.stateless;

import entity.Fare;
import entity.Flight;
import entity.FlightSchedule;
import entity.FlightSchedulePlan;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import util.exception.FlightSchedulePlanExistException;
import util.exception.GeneralException;

/**
 *
 * @author tristan
 */
@Stateless
public class FlightSchedulePlanSessionBean implements FlightSchedulePlanSessionBeanRemote, FlightSchedulePlanSessionBeanLocal {
    
    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    public Long createNewSingleFlightSchedulePlan(FlightSchedulePlan newFlightSchedulePlan, Long flightId, Date departureDateTime, Date estimatedFlightDuration) throws FlightSchedulePlanExistException, GeneralException {
        try {
            em.persist(newFlightSchedulePlan);

            // link flight and flightscheduleplan
            Flight flight = em.find(Flight.class, flightId);
            newFlightSchedulePlan.setFlight(flight);
            flight.getFlightSchedulePlans().add(newFlightSchedulePlan);

            // link fare and flightscheduleplan
            for (Fare fare : newFlightSchedulePlan.getFares()) {
                fare.setFlightSchedulePlan(newFlightSchedulePlan);
            }

            Date arrivalDateTime = this.findArrivalDateTime(departureDateTime, estimatedFlightDuration);

            FlightSchedule newFlightSchedule = new FlightSchedule(departureDateTime, estimatedFlightDuration, arrivalDateTime, flight.getFlightNumber(), flight.getAirCraftConfig().getCabinClasses(), newFlightSchedulePlan);
            em.persist(newFlightSchedule);
            newFlightSchedule.setFlightSchedulePlan(newFlightSchedulePlan);
            newFlightSchedulePlan.getFlightSchedules().add(newFlightSchedule);

            em.flush();
            return newFlightSchedulePlan.getFlightSchedulePlanId();
        } catch (PersistenceException | ParseException ex) {
            if(ex.getCause() != null && 
                    ex.getCause().getCause() != null &&
                    ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException"))
            {
                throw new FlightSchedulePlanExistException("This flight schedule plan already exists!");
            }
            else {
                throw new GeneralException("An unexpected error has occurred: " + ex.getMessage());
            }
        }
    }
    
    private Date findArrivalDateTime(Date departureDateTime, Date estimatedFlightDuration) throws ParseException {
        try {
            // Set the departureDateTime and estimatedFlightDuration to appropriate values
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy hh:mm aa");
            SimpleDateFormat durationFormat = new SimpleDateFormat("hh 'Hours' mm 'Minute'");

            departureDateTime = sdf.parse("01 Nov 23 09:30 AM");
            estimatedFlightDuration = durationFormat.parse("03 Hours 30 Minute");

            // Create a Calendar instance for departure date and time
            Calendar departureCalendar = Calendar.getInstance();
            departureCalendar.setTime(departureDateTime);

            // Create a Calendar instance for estimated flight duration
            Calendar durationCalendar = Calendar.getInstance();
            durationCalendar.setTime(estimatedFlightDuration);

            // Add the estimated flight duration to the departure date and time
            departureCalendar.add(Calendar.HOUR, durationCalendar.get(Calendar.HOUR));
            departureCalendar.add(Calendar.MINUTE, durationCalendar.get(Calendar.MINUTE));

            Date arrivalDateTime = departureCalendar.getTime();
            return arrivalDateTime;
        } catch (ParseException ex) {
            throw ex;
        }
    }
}