package com.magnifis.parking.traffic;

import com.magnifis.parking.utils.Utils;

public class RouteSummary {
	
	String routeName = "an unkosherNamed route"; 
	float routeLength = -1; 
	String lengthUnits = "miles"; 
	int durationMinutes = -1; 
	int trafficDuration = -1; 

	public RouteSummary(String kosherName, float length, String units, 
						int normalDuration, int trafficDuration) {
		this.routeName = kosherName; 
		this.routeLength = length; 
		this.lengthUnits = units; 
		this.durationMinutes = normalDuration; 
		this.trafficDuration = trafficDuration; 
	}
	
	public RouteSummary(String name) { 
		setRouteName(name); 
	}
	
	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String name) {
		
		String[] components = name.split("\\/");
		if (Utils.isEmpty(components))
			return; // NOP
		
		// format highway names
		String kosherName = components[0]; 
		kosherName = kosherName.replaceAll("(^| )(N|NB)( |$)", "$1North$3"); 
		kosherName = kosherName.replaceAll("(^| )(S|SB)( |$)", "$1South$3"); 
		kosherName = kosherName.replaceAll("(^| )(E|EB)( |$)", "$1East$3"); 
		kosherName = kosherName.replaceAll("(^| )(W|WB)( |$)", "$1West$3"); 
		kosherName = kosherName.replaceAll("([0-9]+)(N|NB)($| )", "$1 North ");  // e.g. 101 N ==> 101 North  
		kosherName = kosherName.replaceAll("([0-9]+)(S|SB)($| )", "$1 South ");  
		kosherName = kosherName.replaceAll("([0-9]+)(E|EB)($| )", "$1 East"); 
		kosherName = kosherName.replaceAll("([0-9]+)(W|WB)($| )", "$1 West"); 
		
		this.routeName = kosherName; 
	}
	public float getRouteLength() {
		return routeLength;
	}

	public String getLengthUnits() {
		return lengthUnits;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public int getTrafficDuration() {
		return trafficDuration;
	}
}
