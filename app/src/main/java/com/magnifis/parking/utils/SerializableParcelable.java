package com.magnifis.parking.utils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.lang.reflect.Array;

import com.magnifis.parking.messaging.Message;

public class SerializableParcelable implements Serializable, Parcelable {

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		 out.writeSerializable(this);
	}
	
	public static class SPCreator<T extends SerializableParcelable> implements Parcelable.Creator<T> {
		
		public SPCreator(Class<T> cls) {
		  this.cls=cls;
		}
		
		Class<T> cls;
		
		@Override
		public T createFromParcel(Parcel in) {
			// TODO Auto-generated method stub
			return (T) in.readSerializable();
		}

		@Override
		@SuppressWarnings({"unchecked"})

		public T[] newArray(int size) {
			// TODO Auto-generated method stub
			//return new T[size];
			//ArrayList<T> al=new ArrayList<T>(size);
			//al.to
			return (T[])Array.newInstance(cls, size);
		}	
	}

	public static final Creator<SerializableParcelable> CREATOR= new SPCreator<SerializableParcelable>(SerializableParcelable.class);
}
