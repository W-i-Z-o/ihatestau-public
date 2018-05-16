package de.tinf15b4.ihatestau.ihatestau_androidapp.rest;

import java.util.List;

import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExitService {

    String ROUTE_EXITS = "exit";

    @GET(ROUTE_EXITS)
    Call<List<ExitSpotConfig>> getExits();
}
