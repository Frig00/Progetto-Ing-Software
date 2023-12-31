package it.unipv.po.aioobe.trenissimo.model.viaggio.ricerca;

import it.unipv.po.aioobe.trenissimo.model.Utils;
import it.unipv.po.aioobe.trenissimo.model.persistence.entity.RoutesEntity;
import it.unipv.po.aioobe.trenissimo.model.persistence.entity.StopTimesEntity;
import it.unipv.po.aioobe.trenissimo.model.persistence.entity.StopsEntity;
import it.unipv.po.aioobe.trenissimo.model.persistence.entity.TripsEntity;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.CachedRoutesService;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.CachedStopTimesService;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.CachedStopsService;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.CachedTripsService;
import it.unipv.po.aioobe.trenissimo.model.viaggio.Viaggio;
import it.unipv.po.aioobe.trenissimo.model.viaggio.ricerca.utils.Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * See <a href="https://arxiv.org/pdf/1703.05997.pdf">Connection Scan Algorithm</a>
 *
 * @author ArrayIndexOutOfBoundsException
 */
public class CSASearch {
    public static final int MAX_STATIONS = 100000;

    /**
     * See <a href="https://arxiv.org/pdf/1703.05997.pdf">Connection Scan Algorithm</a>
     * Metodo che genera una lista di connessioni di stazioni ordinate cronologicamente
     *
     * @param routesList    lista di tratte
     * @param stopsList     lista di fermate
     * @param stopTimesList lista di orari
     * @param tripsList     lista di viaggi
     * @return una lista di Connection di stazioni ordinate cronologicamente
     */
    private List<Connection> generaTimetable(List<RoutesEntity> routesList, List<StopsEntity> stopsList, List<StopTimesEntity> stopTimesList, List<TripsEntity> tripsList) {
        List<Connection> timetable = new ArrayList<Connection>();

        // Per ogni tratta... (Routes)
        for (RoutesEntity route : routesList) {
            var thisTrip = tripsList.stream().filter(x -> x.getRouteId().equals(route.getRouteId())).toList();

            // Per ogni orario possibile di tale tratta...
            for (TripsEntity trip : thisTrip) {
                // Prendo tutte le fermate che fa in ordine cronologico...
                var tripStopTimes = stopTimesList.stream().filter(x -> x.getTripId() == trip.getTripId()).sorted((Comparator.comparing(o -> o.getArrivalTime()))).toList();

                // Creo connessioni ordinate cronologicamente delle stazioni
                for (int i = 0; i < tripStopTimes.size() - 1; i++) {
                    var thisNode = tripStopTimes.get(i);
                    var nextNode = tripStopTimes.get(i + 1);
                    timetable.add(
                            new Connection(
                                    thisNode.getStopId(),
                                    nextNode.getStopId(),
                                    Utils.timeToSeconds(thisNode.getDepartureTime()),
                                    Utils.timeToSeconds(nextNode.getArrivalTime()),
                                    thisNode.getTripId(),
                                    nextNode.getTripId()
                            ));
                }
            }
        }

        // Per permettere i cambi prendo ogni stazione...
        for (StopsEntity stop : stopsList) {
            // di ogni stazione trovo tutti i possibili treni che passano durante la giornata
            var trainStopTimes = stopTimesList.stream().filter(x -> x.getStopId() == stop.getStopId()).toList();
            for (StopTimesEntity nodeA : trainStopTimes) {
                for (StopTimesEntity nodeB : trainStopTimes) {
                    // Collego tutte le fermate trovate con fermate successive
                    if (nodeA.getStopId() != nodeB.getStopId() // non collego fermate uguali
                            && Utils.timeToSeconds(nodeA.getArrivalTime()) < Utils.timeToSeconds(nodeB.getArrivalTime()) // non collego fermate con tempo di percorrenza negativo
                            && (Utils.timeToSeconds(nodeB.getArrivalTime()) - Utils.timeToSeconds(nodeA.getArrivalTime())) < 3600 // non collego fermate con tempo di percorrenza superiore a un'ora
                    ) {
                        timetable.add(
                                new Connection(
                                        nodeA.getStopId(),
                                        nodeB.getStopId(),
                                        Utils.timeToSeconds(nodeA.getDepartureTime()),
                                        Utils.timeToSeconds(nodeB.getArrivalTime()),
                                        nodeA.getTripId(),
                                        nodeB.getTripId()
                                ));
                    }
                }
            }
        }

        return timetable;
    }

    /**
     * Metodo che calcola il viaggio possibile partendo da departure_time tra la stazione di partenza e di arrivo passate come parametro
     *
     * @param departure_station id della stazione di partenza
     * @param arrival_station   id della stazione di arrivo
     * @param departure_time    ora di partenza
     * @param timetable         lista di connessioni
     * @return lista di Connection che identificano un viaggio dalla stazione di partenza a quella di arrivo
     */
    private List<Connection> compute(int departure_station, int arrival_station, int departure_time, List<Connection> timetable) {
        Connection[] in_connection = new Connection[MAX_STATIONS];
        int[] earliest_arrival = new int[MAX_STATIONS];
        for (int i = 0; i < MAX_STATIONS; ++i) {
            in_connection[i] = null;
            earliest_arrival[i] = Integer.MAX_VALUE;
        }
        earliest_arrival[departure_station] = departure_time;

        if (departure_station <= MAX_STATIONS && arrival_station <= MAX_STATIONS) {
            // CSA Main loop
            int earliest = Integer.MAX_VALUE;
            timetable = timetable.stream().sorted(Comparator.comparingInt(x -> x.getArrivalTimestamp())).toList();

            for (Connection connection : timetable) {
                if (connection.getDepartureTimestamp() >= earliest_arrival[connection.getDepartureStation()] &&
                        connection.getArrivalTimestamp() < earliest_arrival[connection.getArrivalStation()]) {
                    earliest_arrival[connection.getArrivalStation()] = connection.getArrivalTimestamp();
                    in_connection[connection.getArrivalStation()] = connection;

                    if (connection.getArrivalStation() == arrival_station) {
                        earliest = Math.min(earliest, connection.getArrivalTimestamp());
                    }
                } else if (connection.getArrivalTimestamp() > earliest) {
                    continue;
                }
            }
        }

        // Reprocess result
        if (in_connection[arrival_station] == null) {
            return null;
        } else {
            List<Connection> route = new ArrayList<Connection>();
            Connection last_connection = in_connection[arrival_station];
            while (last_connection != null) {
                route.add(last_connection);
                last_connection = in_connection[last_connection.getDepartureStation()];
            }
            Collections.reverse(route);
            return route;
        }
    }

    /**
     * Metodo che ritorna una lista di viaggi data una stazione di partenza e una stazione di arrivo riordinata per orario di partenza
     *
     * @param departureStopId id stazione di partenza
     * @param arrivalStopId   id stazione arrivo
     * @return lista di Viaggio
     */
    public List<Viaggio> eseguiRicerca(int departureStopId, int arrivalStopId) {


        // Database data load
        List<RoutesEntity> routesList = CachedRoutesService.getInstance().findAll();
        List<StopsEntity> stopsList = CachedStopsService.getInstance().findAll();
        List<StopTimesEntity> stopTimesList = CachedStopTimesService.getInstance().findAll();
        List<TripsEntity> tripsList = CachedTripsService.getInstance().findAll();


        var timetable = generaTimetable(routesList, stopsList, stopTimesList, tripsList);


        // compute(...) tira fuori solo il primo viaggio possibile a partire dal departure_time, bisogna ciclare finche ritorna null

        List<Viaggio> viaggi = new ArrayList<Viaggio>();

        var lastTime = 0;

        while (true) {
            var result = compute(departureStopId, arrivalStopId, lastTime, timetable);
            if (result == null) {
                break;
            }

            var viaggio = new Viaggio();
            viaggio.setCambi(result);
            viaggi.add(viaggio);

            lastTime = result.get(0).getDepartureTimestamp() + 1;
        }

        return viaggi
                .stream()
                .collect(Collectors.groupingBy(Viaggio::getOrarioArrivo)) //Raggruppo per orario di arrivo
                .values()
                .stream()
                .map(x -> x.stream().min(Comparator.comparing(Viaggio::getDurata)).get()) //Scelgo, per un dato orario di arrivo, il viaggio più breve
                .sorted(Comparator.comparingInt(Viaggio::getOrarioPartenza)) //Riordino per orario partenza
                .toList();
    }

}
