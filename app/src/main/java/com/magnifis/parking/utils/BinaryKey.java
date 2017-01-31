package com.magnifis.parking.utils;

import java.util.Arrays;

public class BinaryKey implements Comparable<BinaryKey> {
	
	private byte a[];
	
	public BinaryKey(byte a[]) { this.a=a; }
	
	public BinaryKey(String s) { this(Utils.rawMd5(s)); }

	@Override
	public boolean equals(Object o) {
		if (o instanceof BinaryKey) 
		  return Arrays.equals(a, ((BinaryKey)o).a);
		return false;
	}

	@Override
	public int compareTo(BinaryKey bk) {
		// TODO Auto-generated method stub
		int sz=Math.min(a.length, bk.a.length);
		for (int i=0;i<bk.a.length;i++ ) {
		   int diff=a[i]-bk.a[i];
		   if (diff!=0) return diff;
		}
		return a.length-bk.a.length;
	}

}
