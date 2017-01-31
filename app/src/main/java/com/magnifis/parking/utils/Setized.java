package com.magnifis.parking.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

public class Setized extends AbstractSet<CharSequence> {
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (o!=null&&(o instanceof Setized)) {
			Setized sz=(Setized)o;
			if (seqs==sz.seqs) return true;
			if (hash==sz.hash&&seqs!=null&&sz.seqs!=null&&(seqs.length==sz.seqs.length)) {
				for (int i=0;i<seqs.length;i++) 
					if (!seqs[i].equals(sz.seqs[i])) return false;
				return true;
			}
		}
		return false;
	}

	private CharSequence seqs[]=null;
	private int hash=0;
	
	public Setized(CharSequence cs) {
		if (!Utils.isEmpty(cs)) {
		  String s=cs.toString().trim();
		  if (!Utils.isEmpty(s)) {
			  ArrayList<CharSequence> lst=new ArrayList<CharSequence>();
			  int i=0,j=0, csl=cs.length()-1;
			  x:for (;i<=csl;i++) {
				 char c=cs.charAt(i);
				 if (!Character.isJavaIdentifierPart(c)) {
					 if (i>j) {
					   CharSequence xx=cs.subSequence(j, i);
					   lst.add(xx);
					   hash+=xx.hashCode();
					 }
					 while (++i<csl) {
						 c=cs.charAt(i);
						 if (Character.isJavaIdentifierPart(c)) {
							 j=i++; continue x; 
						 }
					 }
					 break;
				 } else if (i==csl) {
					 CharSequence xx=cs.subSequence(j, csl+1);
					 lst.add(xx); 
					 hash+=xx.hashCode();
				 }
			  }
			  seqs=lst.toArray(new CharSequence[lst.size()]);
		  }
		}
	}

	@Override
	public Iterator<CharSequence> iterator() {
		return new Iterator<CharSequence>() {
			
			int index=0;

			@Override
			public boolean hasNext() {
				return seqs!=null&&index<seqs.length;
			}

			@Override
			public CharSequence next() {
				if (hasNext()) return seqs[index++];
				return null;
			}

			@Override
			public void remove() {
				System.out.println(index);
				if (seqs!=null) {
					int z=index-1;
					hash-=seqs[z].hashCode();
					if (seqs.length==1) seqs=null; else {
					   CharSequence a[]=new CharSequence[seqs.length-1];
					   int i=0;
					   if (z>0) for (;i<z;i++) a[i]=seqs[i];
					   for (;i<a.length;) a[i]=seqs[++i];
					   seqs=a;
					}
					--index;
				}
			}
			
		};
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return seqs==null?0:(seqs.length);
	}
}