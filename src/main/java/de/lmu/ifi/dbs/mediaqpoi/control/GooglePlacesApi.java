package de.lmu.ifi.dbs.mediaqpoi.control;

import java.util.logging.Logger;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Place;
import de.lmu.ifi.dbs.mediaqpoi.entity.PlacesList;

public class GooglePlacesApi {

  private static final Logger LOGGER = Logger.getLogger(GooglePlacesApi.class.getName());
  private static final String API_KEY = "AIzaSyDIZgzM1EkYHEhOJfcjUvm0ovOUczk7v8s";
  private static final String PLACES_SEARCH_URL =
      "https://maps.googleapis.com/maps/api/place/search/json?";
  private static final String PLACES_AUTOCOMPLETE_URL =
      "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
  private static final String PLACES_DETAILS_URL =
      "https://maps.googleapis.com/maps/api/place/details/json?";
  private static final JacksonFactory jacksonFactory = new JacksonFactory();
  private static final HttpTransport transport = new UrlFetchTransport();

  public static PlacesList searchPlaces(Location location, long radius) {
    try {

      HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
      HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));

      request.getUrl().put("key", API_KEY);
      request.getUrl().put("location", location.getLatitude() + "," + location.getLongitude());
      request.getUrl().put("radius", radius);
      request.getUrl().put("sensor", "false");

      PlacesList placesList = request.execute().parseAs(PlacesList.class);
      return placesList;

    } catch (Exception e) {
      LOGGER.severe("Exception while searching places via Google API: " + e);
    }

    return null;
  }

  public static Place getDetails(String placeId) {
    try {

      HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
      HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));

      request.getUrl().put("key", API_KEY);
      request.getUrl().put("placeid", placeId);

      Place place = request.execute().parseAs(Place.class);
      return place;

    } catch (Exception e) {
      LOGGER.severe("Exception while getting place details via Google API: " + e);
    }
    return null;
  }

  public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {

    return transport.createRequestFactory(new HttpRequestInitializer() {
      public void initialize(HttpRequest request) {
        JsonObjectParser parser = new JsonObjectParser(jacksonFactory);
        request.setParser(parser);
      }
    });
  }
}
