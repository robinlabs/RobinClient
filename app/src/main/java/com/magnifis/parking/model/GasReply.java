package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.Xml.ML;
import static com.magnifis.parking.utils.Utils.*;

public class GasReply extends PoiLikeGeoSpannable<GasStation> 
   implements Filterable, Sortable , SortableByPrice, FacilitiesSetter<GasStation>
{
	
	private int fuelType=-1;
	
	private void classifyPrices() {
		Double 
		     minRegular=null, maxRegular=null,
		     minPlus=null, maxPlus=null,
		     minPremium=null, maxPremium=null,
		     minDiesel=null, maxDiesel=null
			;
		if (!isEmpty(pois)) {
			for (GasStation gs : pois) {
				minRegular = GasPrice.min(minRegular, gs.getRegularPrice());
				maxRegular = GasPrice.max(maxRegular, gs.getRegularPrice());

				minPlus = GasPrice.min(minPlus, gs.getPlusPrice());
				maxPlus = GasPrice.max(maxPlus, gs.getPlusPrice());

				minPremium = GasPrice.min(minPremium, gs.getPremiumPrice());
				maxPremium = GasPrice.max(maxPremium, gs.getPremiumPrice());

				minDiesel = GasPrice.min(minDiesel, gs.getDieselPrice());
				maxDiesel = GasPrice.max(maxDiesel, gs.getDieselPrice());
			}
			for (GasStation gs : pois) {
				gs.getRegularPrice().markMinMax(minRegular, maxRegular);
				gs.getPlusPrice().markMinMax(minPlus, maxPlus);
				gs.getPremiumPrice().markMinMax(minPremium, maxPremium);
				gs.getDieselPrice().markMinMax(minDiesel, maxDiesel);
			}
		}
		
	}
	
	private void resetFuelType() {
		fuelType=fuelTypeTable.get(
		   App.self.getPrefs().getString(
			   App.self.getString(R.string.PfFuelType),
			   App.self.getString(R.string.FtRegular)
		   ).toLowerCase()
		);				
	}
	
	@Override
	public void calculate(DoublePoint orgLoc, String orderBy) {
		
		if (!isEmpty(pois))
			for (GasStation gs : pois) gs.roundThePrices();
		
		classifyPrices();
		
		resetFuelType();		
	    super.calculate(orgLoc, orderBy);
	    
	}

    @ML("poi")
    protected GasStation pois[]=null;
	
	public GasStation[] getPois() {
		return pois;
	}

	public void setPois(GasStation[] pois) {
		this.pois = pois;
	}

	@Override
	public GasStation[] getFacilities() {
		return getPois();
	}
	
	private final HashMap<String,Integer> fuelTypeTable=GasStation.getGasTypeTable();
	
	private int _countAvailable() {
		int counter=0;
		if (pois!=null) for (GasStation fas:pois)
			if (fas.hasFuelPrice(fuelType)) ++counter;
		return counter;
	}

	@Override
	public int filterOutFacilites() {
          int cnt=_countAvailable();
          /*
          if (cnt>0) {
        	   GasStation ar[]=new GasStation[cnt];
			   int i=0;
			   for (GasStation fas:pois) 
				   if (fas.hasFuelPrice(fuelType)) ar[i++]=fas;
			   setPois(ar);
          } else
        	  setPois(null);
        	  */
		  return cnt;	
    }
	
	
	@Override
	public boolean orderByPrice() {
		  if (Understanding.orderByPrice(orderBy)) return false;
		  orderBy=Understanding.ORDER_PRICE;
		  
	      if (pois!=null) {
				Arrays.sort(
				  pois, 
				  GasStation.getPriceComparator(fuelType)
			    );
		   }
	       return true;
		}

	@Override
	public void orderThem(String orderBy) {
		if (Understanding.orderByDistance(orderBy))
			  orderByDistance();
			else if (Understanding.orderByPrice(orderBy))
			  orderByPrice();	
	}

	@Override
	public void setFacilities(GasStation[] fass) {
		setPois(fass);
	}

}
