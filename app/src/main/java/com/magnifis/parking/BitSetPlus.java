package com.magnifis.parking;

import java.util.BitSet;

public class BitSetPlus extends BitSet {

	public BitSetPlus(int nbits) {
		super(nbits);
	}


	public int prevClearBit(int pos) throws IndexOutOfBoundsException {
		if (pos<0) throw new IndexOutOfBoundsException();
		for (;pos>=0;pos--)
		  if (!get(pos)) return pos;
		return -1;
	}


	public int prevSetBit(int pos) throws IndexOutOfBoundsException  {
		if (pos<0) throw new IndexOutOfBoundsException();
		for (;pos>=0;pos--)
			  if (get(pos)) return pos;
		return -1;
	}

}
