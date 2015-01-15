package de.lmu.ifi.dbs.mediaqpoi.entity;

public class Distance {

	public static double getDistanceInMeters(Location loc1, Location loc2) {
		
		// radius of the earth in km
		double r = 6371;
		
		double dLat = deg2rad(loc2.latitude - loc1.latitude);
		double dLon = deg2rad(loc2.longitude - loc1.longitude);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(loc1.latitude)) * Math.cos(deg2rad(loc2.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		// distance in meters
		double d = r * c * 1000;
		return d;
		
	}

	private static double deg2rad(double deg) {
		return deg * (Math.PI / 180);
	}

	public static Location getMidPoint(Location loc1, Location loc2) {

		double dLon = Math.toRadians(loc2.longitude - loc1.longitude);

		// convert to radians
		loc1.latitude = Math.toRadians(loc1.latitude);
		loc2.latitude = Math.toRadians(loc2.latitude);
		loc1.longitude = Math.toRadians(loc1.longitude);

		double Bx = Math.cos(loc2.latitude) * Math.cos(dLon);
		double By = Math.cos(loc2.latitude) * Math.sin(dLon);
		double lat3 = Math.atan2(Math.sin(loc1.latitude) + Math.sin(loc2.latitude), Math.sqrt((Math.cos(loc1.latitude) + Bx) * (Math.cos(loc1.latitude) + Bx) + By * By));
		double lon3 = loc1.longitude + Math.atan2(By, Math.cos(loc1.latitude) + Bx);
		
		return new Location(lat3, lon3);
	}

	/**
	 * just for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		double lat1 = 48.507063;
		double lon1 = 11.688703;
		double lat2 = 48.136012;
		double lon2 = 11.580288;
		
		Location loc1 = new Location(lat1, lon1);
		Location loc2 = new Location(lat2, lon2);
		
		double d = Distance.getDistanceInMeters(loc1, loc2);
		System.out.println("Distance in meters: " + d);
		
		Location mid = Distance.getMidPoint(loc1, loc2);
		System.out.println("lat: " + Math.toDegrees(mid.latitude) + " lon: " + Math.toDegrees(mid.longitude));
		
	}

}
