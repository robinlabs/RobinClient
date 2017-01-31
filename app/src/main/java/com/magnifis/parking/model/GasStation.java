package com.magnifis.parking.model;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Phrases;
import com.magnifis.parking.R;
import com.magnifis.parking.Xml.ML;

public class GasStation extends PoiLike {
	public GasPrice getRegularPrice() {
		return regularPrice;
	}
	public void setRegularPrice(GasPrice regularPrice) {
		this.regularPrice = regularPrice;
	}
	public GasPrice getPlusPrice() {
		return plusPrice;
	}
	public void setPlusPrice(GasPrice plusPrice) {
		this.plusPrice = plusPrice;
	}
	public GasPrice getPremiumPrice() {
		return premiumPrice;
	}
	public void setPremiumPrice(GasPrice premiumPrice) {
		this.premiumPrice = premiumPrice;
	}
	public GasPrice getDieselPrice() {
		return dieselPrice;
	}
	public void setDieselPrice(GasPrice dieselPrice) {
		this.dieselPrice = dieselPrice;
	}
	@ML("regular_price")
	protected GasPrice regularPrice=null; 
	@ML("plus_price")
	protected GasPrice plusPrice=null; 
	@ML("premium_price")
	protected GasPrice premiumPrice=null; 
	@ML("diesel_price")
	protected GasPrice dieselPrice=null; 
	
	final static public int 
	  FT_REGULAR=0, FT_PLUS=1, FT_PREMIUM=2, FT_DIESEL=3;
	
	public static boolean isEmpty(GasPrice p) {
	   return p==null||p.getPrice()==null;
	}
	
	private static WeakReference<HashMap<String,Integer>> gtt=null;
	
	static HashMap<String,Integer> getGasTypeTable() {
		synchronized(GasStation.class) {
			if (gtt!=null) {
				HashMap<String,Integer> t=gtt.get();
				if (t!=null) return t;
			}
			HashMap<String,Integer> t=new HashMap<String,Integer>() {
				{
					put(App.self.getString(R.string.FtRegular),GasStation.FT_REGULAR);
					put(App.self.getString(R.string.FtPlus),GasStation.FT_PLUS);
					put(App.self.getString(R.string.FtPremium),GasStation.FT_PREMIUM);
					put(App.self.getString(R.string.FtDiesel),GasStation.FT_DIESEL);
				}
			};

			gtt=new WeakReference<HashMap<String,Integer>>(t);
			return t;
		}
	}
	
	public static int resolveGasType(String gt) {
		return  getGasTypeTable().get(gt.toLowerCase());
	}
	
	public boolean hasFuelPrice(int ft) {
		switch (ft) {
		case FT_REGULAR:
			return  !isEmpty(regularPrice);
		case FT_PLUS: 
			return  !isEmpty(plusPrice);
		case FT_PREMIUM: 
			return  !isEmpty(premiumPrice);
		case FT_DIESEL: 
			return  !isEmpty(dieselPrice);
		}
		return false;
	}
	static class GasRegularPriceComparator implements Comparator<GasStation> {

		@Override
		public int compare(GasStation f0, GasStation f1) {
			GasPrice gr0=f0.getRegularPrice(), gr1=f1.getRegularPrice();
		    Double r0=gr0==null?null:gr0.getPrice(), r1=gr1==null?null:gr1.getPrice();
		    if (r0==null) r0=Double.MAX_VALUE;
		    if (r1==null) r1=Double.MAX_VALUE;
			return r0.compareTo(r1);
		}
		
	}
	
	static class GasPlusPriceComparator implements Comparator<GasStation> {

		@Override
		public int compare(GasStation f0, GasStation f1) {
			GasPrice gr0=f0.getPlusPrice(), gr1=f1.getPlusPrice();
		    Double r0=gr0==null?null:gr0.getPrice(), r1=gr1==null?null:gr1.getPrice();
		    if (r0==null) r0=Double.MAX_VALUE;
		    if (r1==null) r1=Double.MAX_VALUE;
			return r0.compareTo(r1);
		}
		
	}
	
	static class GasPremiumPriceComparator implements Comparator<GasStation> {

		@Override
		public int compare(GasStation f0, GasStation f1) {
			GasPrice gr0=f0.getPremiumPrice(), gr1=f1.getPremiumPrice();
		    Double r0=gr0==null?null:gr0.getPrice(), r1=gr1==null?null:gr1.getPrice();
		    if (r0==null) r0=Double.MAX_VALUE;
		    if (r1==null) r1=Double.MAX_VALUE;
			return r0.compareTo(r1);
		}
		
	}
	
	static class GasDieselPriceComparator implements Comparator<GasStation> {

		@Override
		public int compare(GasStation f0, GasStation f1) {
			GasPrice gr0=f0.getDieselPrice(), gr1=f1.getDieselPrice();
		    Double r0=gr0==null?null:gr0.getPrice(), r1=gr1==null?null:gr1.getPrice();
		    if (r0==null) r0=Double.MAX_VALUE;
		    if (r1==null) r1=Double.MAX_VALUE;
			return r0.compareTo(r1);
		}
		
	}

	public static Comparator<GasStation> getPriceComparator(int ft) {
		switch (ft) {
		case FT_REGULAR:
			return  new GasRegularPriceComparator();
		case FT_PLUS: 
			return  new GasPlusPriceComparator();
		case FT_PREMIUM: 
			return  new GasPremiumPriceComparator();
		case FT_DIESEL: 
			return  new GasDieselPriceComparator();
		}		
		return null;
	}
	
	public static String getActiveGasTypeName() {
		return App.self.getPrefs().getString(
			App.self.getString(R.string.PfFuelType), 
			App.self.getString(R.string.FtRegular)
		);
	}
	
	public static int getActiveGasType() {
		return resolveGasType(getActiveGasTypeName());
	}
	
	public void roundThePrices() {
		if (regularPrice!=null) regularPrice.roundThePrice();
		if (plusPrice!=null) plusPrice.roundThePrice();
		if (premiumPrice!=null) premiumPrice.roundThePrice();
		if (dieselPrice!=null) dieselPrice.roundThePrice();
	}
	
	public boolean hasPrice() {
		return hasFuelPrice(getActiveGasType());
	}
	
	public GasPrice getPrice() {
		switch (getActiveGasType()) {
		case FT_REGULAR:
			return  this.regularPrice;
		case FT_PLUS: 
			return  this.plusPrice;
		case FT_PREMIUM: 
			return  this.premiumPrice;
		case FT_DIESEL: 
			return  this.dieselPrice;
		}
		return null;		
	}
	
	@Override
	public String getFormattedPrice() {
		GasPrice gpr=getPrice();
		Double pr=gpr==null?null:gpr.getPrice();
		if (pr==null) return "n/a";
		return "$"+  String.format("%1$.2f", pr.doubleValue()); 
	}
	
	@Override
	public String getPriceInfoToSpeak() {
	  StringBuilder sb=new StringBuilder();
	  Phrases.formCostText(sb, this, false);
	  return sb.toString();
	}
}
