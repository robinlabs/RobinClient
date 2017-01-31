package com.magnifis.parking;

import java.io.Serializable;

import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.PkResponse;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.model.Understanding;

public class MAStatus implements Serializable, Cloneable {
	
	protected String lastCommand=null, lastCommandDomain=null;
	
	protected DoublePoint prevLocation=null, prevMapCenter=null;

	public DoublePoint getPrevMapCenter() {
		return prevMapCenter;
	}

	public void setPrevMapCenter(DoublePoint prevMapCenter) {
		this.prevMapCenter = prevMapCenter;
	}

	public DoublePoint getPrevLocation() {
		return prevLocation;
	}

	public void setPrevLocation(DoublePoint prevLocation) {
		this.prevLocation = prevLocation;
	}

	public String getLastCommand() {
		return lastCommand;
	}

	public void setLastCommand(String prevCommand) {
		this.lastCommand = prevCommand;
	}

	public String getLastCommandDomain() {
		return lastCommandDomain;
	}

	public void setLastCommandDomain(String prevDomain) {
		this.lastCommandDomain = prevDomain;
	}

	protected MapItemIterator<Poi> poiIterator=null;

	public MapItemIterator<Poi> getPoiIterator() {
		return poiIterator;
	}

	public void setPoiIterator(MapItemIterator<Poi> poiIterator) {
		this.poiIterator = poiIterator;
	}
	
	public MapItemIterator<GasStation> getGasStationIterator() {
		return gasStationIterator;
	}

	public void setGasStationIterator(MapItemIterator<GasStation> gasStationIterator) {
		this.gasStationIterator = gasStationIterator;
	}

	protected MapItemIterator<GasStation> gasStationIterator=null;
	

	protected GeoSpannable<Poi> pois=null;
	
	protected GasStation selectedGasStation=null;
	public GasStation getSelectedGasStation() {
		return selectedGasStation;
	}

	public void setSelectedGasStation(GasStation selectedGasStation) {
		this.selectedGasStation = selectedGasStation;
	}

	public GeoSpannable<GasStation> getGasStations() {
		return gasStations;
	}

	public void setGasStations(GeoSpannable<GasStation> gasStations) {
		this.gasStations = gasStations;
		setGasStationIterator(gasStations==null?null:gasStations.facilities());
	}

	protected GeoSpannable<GasStation> gasStations=null;

	public Poi getSelectedPoi() {
		return selectedPoi;
	}

	public void setSelectedPoi(Poi currentPoi) {
		this.selectedPoi = currentPoi;
	}

	public GeoSpannable<Poi> getPois() {
		return pois;
	}

	public void setPois(GeoSpannable<Poi> pois) {
		this.pois = pois;
		setPoiIterator(pois==null?null:pois.facilities());
	}
	
	protected Poi selectedPoi=null;

	final public static int MODE_OTHER = 0, MODE_PARKING = 1;
	final public static int MODE_POI = 2, MODE_GAS=3, MODE_TRAFFIC=4;

	protected int mode = MODE_OTHER;

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public MAStatus clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (MAStatus) super.clone();
	}

	protected PkResponse parkingResponse = null;
	protected PkFacility selectedParking = null;
	protected MapItemIterator<PkFacility> parkings = null;
	protected int lastCmdCode = Understanding.CMD_UNKNOWN;
	protected Long timestamp = null;
	protected boolean confirmationAsked = false;
	protected DoublePoint originLocation = null, destinationLocation = null;

	/**
	 * @return the destinationLocation
	 */
	public DoublePoint getDestinationLocation() {
		return destinationLocation;
	}

	/**
	 * @param destinationLocation
	 *            the destinationLocation to set
	 */
	public void setDestinationLocation(DoublePoint destinationLocation) {
		this.destinationLocation = destinationLocation;
	}

	public boolean isFresh() {
		if (timestamp == null)
			return false;
		return (System.currentTimeMillis() - timestamp) < 60l * 1000l; // one
																		// minute
	}

	public int indexOfTheLastSelected() {
		return parkings.indexOf(selectedParking);
	}

	public int indexOf(PkFacility fas) {
		return parkings.indexOf(fas);
	}

	public PkFacility next(PkFacility fas) {
		return parkings.next(fas);
	}

	public PkFacility prev(PkFacility fas) {
		return parkings.prev(fas);
	}

	public PkFacility next() {
		return parkings.next(selectedParking);
	}

	public PkFacility prev() {
		return parkings.prev(selectedParking);
	}

	public boolean isSelected(PkFacility fas) {
		return fas == selectedParking;
	}
	
	public int getNumberOfAvailableOptions() {
		switch (mode) {
		case MODE_PARKING: return getNumberOfAvailableParkings();
		case MODE_POI: return  getNumberOfAvailablePois();
		}
		return 0;
	}
	
	public int getNumberOfAvailablePois() {
		return (getPoiIterator()==null)?0:getPoiIterator().size();
	}

	public int getNumberOfAvailableParkings() {
		return (getParkingResponse() == null) ? 0 : getParkingResponse()
				.countAvailable();
	}

	public DoublePoint[] getSpanOfTheParkings() {
		return parkingResponse == null ? null : parkingResponse.getBoundBox();
	}

	/**
	 * @return the originLocation
	 */
	public DoublePoint getOriginLocation() {
		return originLocation;
	}

	/**
	 * @param originLocation
	 *            the originLocation to set
	 */
	public void setOriginLocation(DoublePoint originLocation) {
		this.originLocation = originLocation;
	}

	/**
	 * @return the confirmationAsked
	 */
	public boolean isConfirmationAsked() {
		return confirmationAsked;
	}

	/**
	 * @param confirmationAsked
	 *            the confirmationAsked to set
	 */
	public void setConfirmationAsked(boolean confirmationAsked) {
		this.confirmationAsked = confirmationAsked;
	}

	public void updateTime() {
		timestamp = System.currentTimeMillis();
	}

	/**
	 * @return the parkingResponse
	 */
	public PkResponse getParkingResponse() {
		return parkingResponse;
	}

	/**
	 * @param parkingResponse
	 *            the parkingResponse to set
	 */
	public void setParkingResponse(PkResponse parkingResponse) {
		this.parkingResponse = parkingResponse;
	}

	/**
	 * @return the selectedParking
	 */
	public PkFacility getSelectedParking() {
		return selectedParking;
	}

	/**
	 * @param selectedParking
	 *            the selectedParking to set
	 */
	public void setSelectedParking(PkFacility selectedParking) {
		this.selectedParking = selectedParking;
	}

	/**
	 * @return the parkings
	 */
	public MapItemIterator<PkFacility> getParkings() {
		return parkings;
	}

	/**
	 * @param parkings
	 *            the parkings to set
	 */
	public void setParkings(MapItemIterator<PkFacility> parkings) {
		this.parkings = parkings;
	}

	/**
	 * @return the lastCmdCode
	 */
	public int getLastCmdCode() {
		return lastCmdCode;
	}

	/**
	 * @param lastCmdCode
	 *            the lastCmdCode to set
	 */
	public void setLastCmdCode(int lastCmdCode) {
		this.lastCmdCode = lastCmdCode;
	}

	/**
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
