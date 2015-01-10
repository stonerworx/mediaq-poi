package de.lmu.ifi.dbs.mediaqpoi;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

public class GooglePlacesApi {

  private static final String API_KEY = "AIzaSyDIZgzM1EkYHEhOJfcjUvm0ovOUczk7v8s";

  private static final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
  private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
  private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

  private static final JacksonFactory jacksonFactory = new JacksonFactory();
  private static final HttpTransport transport = new UrlFetchTransport();

  public static PlacesList searchPlaces(SearchRadius searchRadius) {
    try {

      HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
      HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?"));

      request.getUrl().put("key", "AIzaSyDIZgzM1EkYHEhOJfcjUvm0ovOUczk7v8s");
      request.getUrl().put("location", searchRadius.getLatitude() + "," +
                                       searchRadius.getLongitude());
      request.getUrl().put("radius", searchRadius.getRadius());
      request.getUrl().put("sensor", "false");

      PlacesList placesList = request.execute().parseAs(PlacesList.class);

      return placesList;

    } catch (HttpResponseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
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
