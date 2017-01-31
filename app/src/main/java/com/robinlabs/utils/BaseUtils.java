package com.robinlabs.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import android.database.Cursor;


public class BaseUtils {
	
	
	public static <T> ArrayList<T> toArrayList(Collection<T> cc) {
	   if (cc!=null) {
		 if (cc instanceof ArrayList) return (ArrayList<T>)cc;
		 ArrayList<T> al=new ArrayList<T>();
		 al.addAll(cc);
		 return al;
	   }
	   return null;
	}
	
	
	public static URL url(String cs) {
		try {
			return new URL(cs);
		} catch (MalformedURLException e) {
		    return null;
		}
	}
	
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
	
	public static String toString(CharSequence cs) {
	  return cs==null?null:cs.toString();
	}
	
	public static String extractFileNameFromUrl(String url) {
		if (isEmpty(url)) return null;
		int slashIndex = url.lastIndexOf('/');
		return slashIndex>=0?url.substring(slashIndex + 1):url;
	}
	
	public static <T> T car(Collection<T> c) {
	  if (c!=null) for (T t:c) return t; 
	  return null;
	}
	
	public static <T> T car(T c[]) {
	  if (c!=null) for (T t:c) return t; 
	  return null;
	}
	
	public static  Integer car(int c[]) {
	  if (c!=null) for (int t:c) return t; 
	  return null;
	}
	
	public static boolean isNotSpace(char c) {
		return c>' ';
	}
	
	public static boolean isSpace(char c) {
		return c<=' ';
	}	
	
	public static CharSequence trim(CharSequence cs) {
		if (!isEmpty(cs)) {
		   int i=0;
		   for (;i<cs.length();i++) if (isNotSpace(cs.charAt(i))) break;
		   if (i>0) cs=cs.subSequence(i, cs.length());
		   if (cs.length()>0) {
			 int li=cs.length()-1;
			 i=li;
			 for (;i>=0;i--) if (isNotSpace(cs.charAt(i))) break;
			 if (i<li) cs=cs.subSequence(0, i+1);
		   }
		}
		return cs;
	}
	
	public static int indexOf(CharSequence cs, char c) {
		if (cs!=null) for (int i=0;i<cs.length();i++) if (cs.charAt(i)==c) return i;
		return -1;
	}
	
	public static CharSequence [] splitBy(CharSequence cs,char c) {
		if (!isEmpty(cs)) {
		   int k=0;
		   for (int i=0;i<cs.length();i++) if (cs.charAt(i)==c) ++k;
		   if (k>0) {
			  CharSequence res[]=new CharSequence[k+1];
			  int j=0;
			  for (;(k=indexOf(cs,c))>=0;j++) {
				  res[j]=cs.subSequence(0, k);
				  cs=cs.subSequence(k+1, cs.length());
			  }
			  if (!isEmpty(cs)) res[res.length-1]=cs;
			  return res;
		   }
		}
		return new CharSequence [] {cs};
	}
	
	public static boolean isNotEng(char c) {
		return c>0xff;
	}
	
	public static boolean isRus(int c) {
		return ((c >  0x0400) &&  (c < 0x04ff));
	}
	
	private static Random r=new Random((int)(System.currentTimeMillis()%1000000));
	
	public static boolean in1per3times() {
	   int x=r.nextInt(2);
	   return x==0;
	}
	
	public static void copy(URL from, File to, boolean renew) throws IOException {
		if (renew&&to.exists()) to.delete();
		FileOutputStream fos=new FileOutputStream(to);
		try {
			byte buf[]=new byte[4096];
			InputStream is=from.openStream();
			for (;;) {
			  //int avl=is.available();
			  //if (avl<=0) break;
			  int sz=is.read(buf);
			  if (sz<=0) break;
			  fos.write(buf, 0, sz);
			}
		} finally {
			fos.close();
		}
	}
	
	public static  int [] cons(int  aa[],int bb[]) {
		if (isEmpty(aa)) return isEmpty(bb)?null:bb.clone();
		if (isEmpty(bb)) return aa.clone();
		int xx[]= new int [aa.length+bb.length];
		for (int i=0;i<aa.length;i++) xx[i]=aa[i];
		for (int i=0;i<bb.length;i++) xx[i+aa.length]=bb[i];
		return xx;
	}
	
	public static  int [] cons(int x,int aa[]) {
		if (isEmpty(aa)) return new int[] {x};
		int xx[]=new int[aa.length+1];
		xx[0]=x;
		for (int i=0;i<aa.length;i++) xx[i+1]=aa[i];
		return xx;
	}
	
	public static  Object [] cons(Object x,Object aa[]) {
		if (isEmpty(aa)) return new Object[] {x};
		Object xx[]=new Object[aa.length+1];
		xx[0]=x;
		for (int i=0;i<aa.length;i++) xx[i+1]=aa[i];
		return xx;
	}
	
	public static  <T> Set<T> or(Set<T> x, Set<T> y) {
		if (isEmpty(x)) return y;
		if (isEmpty(y)) return x;
		Set<T> r=new HashSet<T>();
		r.addAll(x);
		r.addAll(y);
		return r;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T cloneSerializable(T obj) {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
			ObjectOutputStream oos=new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois=new ObjectInputStream(bais);
			return (T)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int [] clone(int xx[]) {
	   return isEmpty(xx)?null:xx.clone();
	}
	
	public static <T> int sizeOf(Collection<T> c) {
	   return c==null?0:c.size();
	}
	
	
	public static boolean containsTerm(String s, String t) {
		s=trim(s); t=trim(t);
		if (!(isEmpty(s)||isEmpty(t))) {
			int k=0;
			while (k<s.length()&&(k=s.indexOf(t, k))>=0) {
			   if (k==0||!Character.isLetterOrDigit(s.charAt(k-1))) {
				  int after=k+=t.length();
				  if (after>=s.length()||!Character.isLetterOrDigit(s.charAt(after))) return true;
				  k=after;
			   } else
			     k+=t.length();
			}
		}
		return false;
	}
	
	public static String warrantyThatStartsWithPlus(String s) {
	   if (!isEmpty(s)&&s.charAt(0)!='+') return '+'+s;
	   return s;
	}
	
	public static boolean isGlobalPhoneNumber(String s) {
		return !isEmpty(s)&&
		  (s.charAt(0)=='+'||
		    (s.length()>10||s.charAt(0)=='1') // global American number
		  );
	}
	
	public static String removeLeadingZeros(String s, int ...zeros) {
		if (!isEmpty(s)&&s.charAt(0)=='0') {
		   int i=1;
		   while ((i<s.length())&&(s.charAt(i)=='0')) i++; 
		   if (zeros.length>0) zeros[0]=i;
		   return (i==s.length())?"":s.substring(i);
		}
		if (zeros.length>0) zeros[0]=0;
		return s;
	}
	
	public static boolean isNumberType(Field fl) {
		return isNumberType(fl.getType());
	}
	
	public static boolean isNumberType(Class c) {
	  return c==int.class||c==Integer.class||c==long.class||c==Long.class||
			 c==short.class||c==Short.class||c==Byte.class||c==byte.class;
	}
	
	public static String toLowerCase(String s) {
		return isEmpty(s)?s:s.toLowerCase();
	}
	
	public static String [] toLowerCase(String a[]) {
	  if (!isEmpty(a)) {
		String b[]=new String[a.length];
		for (int i=0;i<a.length;i++) b[i]=toLowerCase(a[i]);
		return b;
	  }
	  return a;
	}
	
	
	public static boolean containsIgnoreCase(Collection<String> cc, String s) {
		if (!(isEmpty(cc)||isEmpty(s))) for (String c:cc) 
			if (c.equalsIgnoreCase(s)) return true;
		return false;
	}
	
	public static <T> T getLast(List<T> lst) {
		return isEmpty(lst)?null:lst.get(lst.size()-1);
	}
	
	public static <T> LinkedHashSet<T> toSet(T val) {
		LinkedHashSet<T> s=new LinkedHashSet<T>();
		if (val!=null) s.add(val);
		return s;
	}
	
	public static boolean ge(boolean a, boolean b) {
		return a||!b;
	}
	
	public static <T> T tryToCloneEmpty(T o) {
	   if (o!=null)
			try {
				return (T)o.getClass().newInstance();
			} catch (Throwable e) {}
	   
	   return null;
	}
	
	public static <T> T tryToClone(T o) {
		if (o!=null&&(o instanceof Cloneable)) try {
			Class c=o.getClass();
			for (;c!=null;) {
				Method clone=null;
				try {
					clone=c.getDeclaredMethod("clone");
				} catch(Throwable t) {}
				if (clone==null) {
					c=c.getSuperclass();
				} else {
					clone.setAccessible(true);
					return (T)(clone.invoke(o));
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	} 
	
	public static String [] simpleSplit(String s, char c) {
	   if (!isEmpty(s)) {
		   int cnt=0;
		   {
			   int prevStart=0;
			   for (int i=0; i<s.length();i++) if (s.charAt(i)==c) {
				   if (prevStart<i&&i<s.length()-1) ++cnt;
				   prevStart=i+1;
			   } else
				   if (cnt==0) cnt=1;
		   }
		   
		   if (cnt>0) {
			   String a[]=new String[cnt];

			   int prevStart=0, j=0;
			   for (int i=0; i<s.length();i++) if (s.charAt(i)==c) {
				   if (prevStart<i) {
					   a[j++]=s.substring(prevStart,i); 
				   }
				   prevStart=i+1;
			   }
			   if (prevStart<s.length()) a[j]=s.substring(prevStart,s.length());

			   return a;
		   } else
			   return new String[] { s };
	   }
	   return null;
	}
	
	
	public static String simpleReplaceAll(String s,String ...terms) {
		if (terms!=null&&terms.length>=2) for (int i=0;i<terms.length;i+=2) {
			s=simpleReplaceAll(s,terms[i],terms[i+1]).toString();
		}
		return s;
	}
	
	public static boolean endsWith(CharSequence s, char  n) {
	   return isEmpty(s)?false:(s.charAt(s.length()-1)==n);
	}
	
	public static int countCharOccurrences(CharSequence s, char  n) {
		int cnt=0;
		for (int i=0;i<s.length();i++) if (s.charAt(i)==n) cnt++;
		return cnt;
	}
	
	
	public static boolean containsMacro(String  s, CharSequence key) {
		if (!(isEmpty(s)||isEmpty(key))) {
			return s.contains("${"+key+"}");
		}
		return false;
	}
	
	public static CharSequence expandMacro(String s, String key, CharSequence r) {
		return simpleReplaceAll(s,"${"+key+"}",r);
	}

	
	public static CharSequence simpleReplaceAll(String s, String n, CharSequence r) {
		if (!(isEmpty(s)||isEmpty(n)||r==null)) {
		  StringBuilder res=new StringBuilder();
		  int i=0, k;
		  while ((k=s.indexOf(n, i))>=0) {
			  if (k>0) res.append(s.substring(i, k));
			  res.append(r);
			  i=k+n.length();
			  if (i>=s.length()) break;
		  }
		  if (i<s.length()) res.append(s.substring(i));
		  return res;
		}
		return s;
	}
	

	
	public static List<Field> getAnnotatedFields(Class cls, Class ancls, boolean makeThemAccessible) {
		List<Class> clss=getSuperClasses(cls);
		ArrayList<Field> flds=new ArrayList<Field>();
		for (Class c:clss) for (Field f:c.getDeclaredFields()) 
		  if (f.getAnnotation(ancls)!=null)  {
			  if (makeThemAccessible) f.setAccessible(true);
			  flds.add(f);
		  }
		return flds;
	} 
	
	
	public static ArrayList<Class> getSuperClasses(Class cls) {
		ArrayList<Class> clss=new ArrayList<Class>();
		do {
			clss.add(cls);
			cls=cls.getSuperclass();
		} while (cls!=null);
		
		return  clss;
	}
	
	
	
	public static Class tryToLoad(String clsname) {
	  try {
	    return BaseUtils.class.forName(clsname);
	  } catch(Throwable t) {}
	  return null;
	}
	
	public static boolean isClassAvailable(String clsname) {
	   return tryToLoad(clsname)!=null;
	}
	
	public static Thread setTimeout(
	  final Runnable action,
	  final long timeout
	) {
	  Thread t=new Thread() {
		 @Override
		 public void run() {
			 try {
				sleep(timeout);
				action.run();
			 } catch (InterruptedException e) {
			}
		 }
	  };
	  t.start();
	  return t;
	}
	
	public static int countSubdirs(File f) {
		if (f!=null&&f.exists()) {
		  int cnt=0;
		  for (File h:f.listFiles())
			  if (h.isDirectory()&&!(h.getName().equals(".")||h.getName().equals(".."))) ++cnt;
		  return cnt;
		}
		return 0;
	}
	
	public static int countChildren(File f) {
		if (f!=null&&f.exists()) {
		  int cnt=0;
		  for (File h:f.listFiles())
			  if (h.isDirectory()) {
				  if (!(h.getName().equals(".")||h.getName().equals(".."))) ++cnt;
			  } else
				  ++cnt;
		  return cnt;
		}
		return 0;
	}
	
	public static void callMethodIfExists(Object obj,String mn) {
		Runnable r=getMethod(obj,mn);
		if (r!=null) r.run();
	}
	
	public static Runnable getMethod(final Object obj,String mn) {
		final Method m[]=new Method[] {null};
		try {
			m[0] = obj.getClass().getDeclaredMethod(mn);
			m[0].setAccessible(true);
		} catch (Throwable e) {}
		if (m[0]==null) return null;
		m[0].setAccessible(true);
	    return new Runnable() {
			@Override
			public void run() {
               try {
				m[0].invoke(obj);
			  } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }		
			}
		};
	}
	
	public static boolean equals(Object o1, Object o2) {
	  boolean n1=o1==null, n2=o2==null;
	  if (n1==n2) {
		if (n1) return true;
		return o1.equals(o2);
	  }
	  return false;
	}
	
	public static List<Integer> spacePositions(String s) {
	    ArrayList<Integer> al=new ArrayList<Integer>();
	    boolean sp=false; int nsp=0;
		for (int i=0;i<s.length();i++) {
		  char c=s.charAt(i);
		  if (Character.isSpace(c)) {
			 if (!sp) {
				 sp=true; al.add(i-nsp);
			 }
			 ++nsp;
		  } else
			 sp=false;
			 
		}
	   return al;
	} 
	
	public static CharSequence remove(CharSequence s, char ...chars) {
		if (isEmpty(s)||isEmpty(chars)) return s;
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<s.length();i++) {
		  char c=s.charAt(i);
		  if (indexOf(chars,c)<0) sb.append(c);
		}
		return sb;
	}
	
	public static String removeSpaces(String s) {
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<s.length();i++) {
		  char c=s.charAt(i);
		  if (!Character.isSpace(c)) sb.append(c);
		}
		return sb.toString();
	}
	
	
	public static int f2c(int f) {
		return ((f - 32) * 5) / 9;
	}

	
	
	public static boolean stringContainsPhrase(String string, String phrase) {
		int lan=phrase.length();
		int ix=string.indexOf(phrase);
		if (ix>=0) {
			boolean strt=ix==0,endd=ix+lan==string.length(),ok=strt&&endd;
			if (!ok) {
				if (strt&&!endd) 
					ok=Character.isSpace(string.charAt(ix+lan)); 
				else if (!strt&&!endd)
					ok=Character.isSpace(string.charAt(ix-1))&&
					Character.isSpace(string.charAt(ix+lan));
				else 
					ok=Character.isSpace(string.charAt(ix-1));
			}
			return ok;
		}

		return false;
	}
	
	public static String breakCamalCase(String s) {
	  if (isEmpty(s)) return s;
	  StringBuilder sb=new StringBuilder();
	  int sl1=s.length()-1;
	  for (int i=0;i<s.length();i++) {
		 char c=s.charAt(i);
		 sb.append(c);
		 if ((i<sl1)&&Character.isLowerCase(c)&&Character.isUpperCase(s.charAt(i+1))) 
			sb.append(' ');
	  }
	  return sb.toString();
	}

	public static File createFolderForFile(String f) {
		return f==null?null:createFolderForFile(new File(f));
	}
   
	
	public static File createFolderForFile(File f) {
		if (f!=null) {
		  File p=f.getParentFile();
		  if (p!=null) {
			  if (!p.exists()) p.mkdirs();
		      return p.exists()?p:null;
		  }
		}
		return null;
	}
	
	public static void smart_droid_rm(File d) {
		boolean ok=false;
		try {
		  File to = new File(d.getAbsolutePath() + System.currentTimeMillis());
		  d.renameTo(to);
		  ok=rmrf(to);
		} catch(Throwable t) {}
		if (!ok) try {
		  rmrf(d);
		} catch(Throwable t) {}
	}
	
	public static boolean rmrf(File d) {
		if (!d.exists()) return true;
		if (d.isDirectory()) {
			String dn=d.toString();
			if ((dn.length()<=2)&&dn.charAt(0)=='.') return true; 
			File ff[] = d.listFiles();
			if (ff!=null) for (File f : ff) rmrf(f);
		}
		return d.delete();
	}
	
	
	
	public static int countWords(CharSequence cs) {
	  if (isEmpty(cs)) return 0;
	  int cnt=0; boolean sp=false;
	  for (int i=0; i<cs.length(); i++) if (isSpace(cs.charAt(i))) {
		 if (!sp) { sp=true; ++cnt; }
	  } else
		 sp=false;
	  return cnt+1;
	}
	

	
	static public String hexDec(byte buf[]) {
		StringBuilder hexString = new StringBuilder();  
		for (int i=0; i<buf.length; i++)  {
		  int b=(0xFF & buf[i]);
		  if (b<0x10) hexString.append('0');
		  hexString.append(Integer.toHexString(b)); 
		}
		return hexString.toString();  		
	}
	

  public static boolean is(Boolean b) {
	 return b!=null&&b;
  }
  
  public static String trim(String s) {
	  return s==null?null:s.trim();
  }
  
  public static boolean isEmpty(char a[]) {
	 return a==null||a.length==0;
  }
  
  public static boolean isEmpty(Integer i) {
		 return i==null||i==0;
	  }
  
  public static boolean isEmpty(boolean a[]) {
	 return a==null||a.length==0;
  }
  
  public static boolean isEmpty(float a[]) {
	 return a==null||a.length==0;
  }
  
  public static boolean isEmpty(Map m) {
  	return m==null||m.isEmpty();
  }
	
  public static boolean isEmpty(CharSequence cs) {
	return cs==null||cs.length()==0;
  }
  
  public static boolean isEmptyOrBlank(CharSequence cs) {
	if (!isEmpty(cs)) for (int i=0;i<cs.length();i++)
		if (isNotSpace(cs.charAt(i))) return false;
	return true;
  }
  
  public static boolean isEmpty(ByteArrayOutputStream baos) {
	  return baos==null||baos.size()==0;
  }
  
  public static boolean isEmpty(Collection cs) {
	return cs==null||(cs.size()==0);	  
  } 
  
  public static  <T> boolean isEmpty(T a[]) {
	    return a==null||a.length==0;	  
  } 
  
  public static  boolean isEmpty(byte a[]) {
	    return a==null||a.length==0;	  
  } 
  
  public static  boolean isEmpty(int a[]) {
	    return a==null||a.length==0;	  
  } 
  
  public static  boolean isEmpty(long a[]) {
	    return a==null||a.length==0;	  
} 
  
  @SuppressWarnings("unchecked")
  public static <T,K> boolean contains(T ar[], K key, @SuppressWarnings("rawtypes") Comparator c) {
	  if (ar!=null) for (T t:ar) if (c.compare(t, key)==0) return true;
	  return false;
  }
  
  @SuppressWarnings("unchecked")
  public static <T,K> T find(T ar[], K key, @SuppressWarnings("rawtypes") Comparator c) {
	  if (ar!=null) for (T t:ar) if (c.compare(t, key)==0) return t;
	  return null;
  }
  
  public static boolean intersects(char a[], char b[]) {
	 for (char ac:a) for (char bc:b) if (bc==ac) return true;
	 return false;
  }
  
	public static boolean isOneFrom(int v, int ... a) {
		if (!isEmpty(a)) for (int x:a) if (x==v) return true;
		return false;
	}
	
	public static boolean isOneFrom(CharSequence v, CharSequence ... a) {
		if (!isEmpty(a)) for (CharSequence x:a) if (x!=null&&x.equals(v)) return true;
		return false;
	} 
  
    public static boolean contains(char ar[], char key) {
	   if (ar!=null) for (char t:ar) if (t==key) return true;
	   return false;
   }
  
    public static <T> boolean contains(T ar[], T key) {
		   if (ar!=null) for (T t:ar) if (t.equals(key)) return true;
		   return false;
    }
  /*
  public static  Collection linearize(Collection c) {
		 if (c!=null) {
			Set res=new HashSet();
			for (Object o:c)
			  if (o!=null) {
				  if (o instanceof Collection) res.addAll((Collection)o); else res.add(o);
			  } 
		 }
		 return null;
  }  
  */
  public static <T> Collection<T> linearize(Collection c) {
	 if (c!=null) {
		Set<T> res=new HashSet<T>();
		for (Object o:c)
		  if (o!=null) {
			  if (o instanceof Collection) res.addAll((Collection<T>)o); else res.add((T)o);
		  } 
		return res;
	 }
	 return null;
  }
  
  public static <T> List<T> asList(Collection<T> x) {
	  if (x==null) return null;
	  if (x instanceof List) return (List<T>)x;
	  return new ArrayList<T>(x);  
  }
  
  public static <X extends Collection<String>, Y extends Collection<String>> 
    Collection<String> mul(X a,  Y b, boolean insertSpace) {
	  if (isEmpty(a)) return b;
	  if (isEmpty(b)) return a;
	  List<String> res=new ArrayList<String>();
	  for (String as:a) for (String bs:b) {
		  StringBuilder sb=new StringBuilder(as);
		  if (insertSpace) sb.append(' ');
		  sb.append(bs);
		  res.add(sb.toString());
	  }
	  return res;
  }
  
  public static String[] mul(
		  String a[],  String b[], boolean insertSpace, boolean onlyNew,
		  boolean ... anyNew
  ) {
	  if (!isEmpty(anyNew)) anyNew[0]=false;
	  if (isEmpty(a)) return b;
	  if (isEmpty(b)) return a;
	  List<String> res=new ArrayList<String>();
	  for (String as:a) for (String bs:b) {
		  as=as.toLowerCase(); bs=bs.toLowerCase();
		  if (onlyNew) {
			  int ix=as.indexOf(bs);
			  if (ix>=0) {
				 if (ix==0||!Character.isLetter(as.charAt(ix-1))) {
					 ix+=bs.length();
					 if (ix>=as.length()||!Character.isLetter(as.charAt(ix))) {
						 res.add(as);
						 continue;
					 }
				 }  
			  }
		  }
		  StringBuilder sb=new StringBuilder(as);
		  if (insertSpace) sb.append(' ');
		  sb.append(bs);
		  res.add(sb.toString());
		  if (!isEmpty(anyNew)) anyNew[0]=true;
	  }
	  return res.toArray(new String[res.size()]);
  }
  
  public static boolean anyIntersection(Set<Integer> aa,int ...bb) {
	  if (!(isEmpty(aa)||isEmpty(bb)))
		  for (int y:bb) if (aa.contains(y)) return true;
	  return false;
  }
  
  public static boolean anyIntersection(Collection<Integer> aa,int ...bb) {
	  if (!(isEmpty(aa)||isEmpty(bb)))
		  for (int x:aa) for (int y:bb) if (x==y) return true;
	  return false;
  }
  
  public static boolean anyIntersection(int aa[],int ...bb) {
	  if (!(isEmpty(aa)||isEmpty(bb)))
		  for (int x:aa) for (int y:bb) if (x==y) return true;
	  return false;
  }
  
  public static int indexOf(int a[], int v) {
	  if (a!=null) for (int i=0;i<a.length;i++) if (v==a[i]) return i; 
	  return -1;
  }
  
  public static int indexOf(char a[], char v) {
	  if (a!=null) for (int i=0;i<a.length;i++) if (v==a[i]) return i; 
	  return -1;
  }
  
  public static <T> int indexOf(T a[], T v) {
	  if (a!=null) for (int i=0;i<a.length;i++) if (v.equals(a[i])) return i; 
	  return -1;
  } 
  

  
	
	public static String urldecode(String s) {
		if (!isEmpty(s)) try {
			return URLDecoder.decode(s.replace("+", "%20"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    return s;
	}
	
	public static String urlencode(String s) {
		if (!isEmpty(s)) try {
			return URLEncoder.encode(s.replace("%", " percent "),"UTF-8").replace("+", "%20");//.replace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return s;
	}
	
	
	
	public static Date addOffset(Date d, int days, int monthes, int years) {
	   Calendar cal=Calendar.getInstance();
	   cal.setTime(d);
	   cal.add(Calendar.DAY_OF_MONTH, days);
	   cal.add(Calendar.YEAR, years);
	   cal.add(Calendar.MONTH, monthes);
	   return cal.getTime();  	
	}
	

	
	public static boolean isAmong(Date d, Date from, Date to) {
	   return (from.before(d)||from.equals(d))&&(to.after(d)||to.equals(d));
	}
	
	public static long daysBetween(Date startDate, Date endDate) { 
	   Calendar cal1=Calendar.getInstance(),cal2=Calendar.getInstance();
	   cal1.setTime(startDate);
	   cal2.setTime(endDate);
	   return daysBetween(cal1,cal2);
	}
	

	
	
	// from http://tripoverit.blogspot.com/2007/07/java-calculate-difference-between-two.html
    /** Using Calendar - THE CORRECT (& Faster) WAY**/  
    //assert: startDate must be before endDate  
    public static long daysBetween(Calendar startDate, Calendar endDate) {  
        Calendar sDate = (Calendar) startDate.clone();  
        long daysBetween = 0;  
      
        int y1 = sDate.get(Calendar.YEAR);  
        int y2 = endDate.get(Calendar.YEAR);  
        int m1 = sDate.get(Calendar.MONTH);  
        int m2 = endDate.get(Calendar.MONTH);  
      
        //**year optimization**  
        while (((y2 - y1) * 12 + (m2 - m1)) > 12) {  
      
            //move to Jan 01  
            if ( sDate.get(Calendar.MONTH) == Calendar.JANUARY  
                 && sDate.get(Calendar.DAY_OF_MONTH) == sDate.getActualMinimum(Calendar.DAY_OF_MONTH)) {  
      
                daysBetween += sDate.getActualMaximum(Calendar.DAY_OF_YEAR);  
                sDate.add(Calendar.YEAR, 1);  
            } else {  
                int diff = 1 + sDate.getActualMaximum(Calendar.DAY_OF_YEAR) - sDate.get(Calendar.DAY_OF_YEAR);  
                sDate.add(Calendar.DAY_OF_YEAR, diff);  
                daysBetween += diff;  
            }  
            y1 = sDate.get(Calendar.YEAR);  
        }  
      
        //** optimize for month **  
        //while the difference is more than a month, add a month to start month  
        while ((m2 - m1) % 12 > 1) {  
            daysBetween += sDate.getActualMaximum(Calendar.DAY_OF_MONTH);  
            sDate.add(Calendar.MONTH, 1);  
            m1 = sDate.get(Calendar.MONTH);  
        }  
      
        // process remainder date  
        while (sDate.before(endDate)) {  
            sDate.add(Calendar.DAY_OF_MONTH, 1);  
            daysBetween++;  
        }  
      
        return daysBetween;  
    }
    
    public static long timeToGMT(long time) {
		Calendar cal=Calendar.getInstance();
		return time+(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET));
    }
    
    public static long timeFromGMT(long time) {
		Calendar cal=Calendar.getInstance();
		return time-(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET));
    }
    
    public static Date costructForTimezone(long msec, String tzId) {
    	if (tzId==null) 
    		return new Date(msec);
    	TimeZone tz=tzId==null?TimeZone.getDefault():TimeZone.getTimeZone(tzId);
    	Calendar cal=Calendar.getInstance(tz);
    	cal.setTimeInMillis(msec);
    	return cal.getTime();
    }
      

    
    public static CharSequence phoneNumberToSpeech(String s) {
    	if (!(isEmpty(s)||isEmpty(s=s.trim()))) {
    	   s=s.replaceAll("\\s+", "-");
    	   
    	   // TODO: experiment 
    	   int len = s.length(); 
    	   StringBuilder sb = null; 
    	   if (len >= 10 && len <= 12) { // US phone number?
    		   // convert e.g 1234567890 to 123-456-7890
    		   sb=new StringBuilder(s);
    		  
    		   if (s.startsWith("1") && s.length() == 11) {
    			   sb.delete(0, 1); 
    			  // sb.insert(1, '-');
    		   } else if (s.startsWith("+1") && s.length() == 12) {
    			   sb.delete(0, 2); 
    			   //sb.insert(2, '-');
    		   } else if (s.startsWith("001") && s.length() == 13) {
    			   sb.delete(0, 3); 
    			   //sb.insert(3, '-');
    		   }
    		   sb.reverse(); 
    		   sb.insert(4, '-');
    		   sb.insert(8, '-');
    		   sb.reverse();
    	   } else {
    	      int l1=len-1;
	    	   sb=new StringBuilder();
	    	   for (int i=0;i<l1; i++) {
	    		 char c=s.charAt(i);
	    		 sb.append(c);
	    		 if (!(Character.isSpace(c)||Character.isSpace( s.charAt(i+1) )))
	    			 sb.append(' ');
	    	   }
	    	   sb.append(s.charAt(l1));
    	   }   
    	   return sb;
    	}
    	
    	return s;
    }
 
    public static String formatDateToNiceString(String dateStr) {
    	  try {
    	   SimpleDateFormat basicFormat = new SimpleDateFormat("dd/MM/yyyy");
    	   Date date = basicFormat.parse(dateStr);
    	   SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, y");
    	   return sdf.format(date);
    	  }
    	  catch (Exception e) {
    	   e.printStackTrace();
    	   return null;
    	  }
    }    
    
    public static <T> int lastIndexOf(List<T> lst, Collection<T> s) {
    	if (!(isEmpty(lst)||isEmpty(s)))
    	  for (int i=lst.size();--i>=0;) 
    		  if (s.contains(lst.get(i))) return i;
    	return -1;
    }
    
    // count all elements in the collections hierarchy
    public static int count(Collection root) {
      int n=0;
      if (root!=null) for (Object o:root) if (o!=null) {
    	 if (o instanceof Collection) n+=count((Collection)o); else ++n;
      } 
      return n;
    }
    
    public static  CharSequence dump(float a[]) {
       if (!isEmpty(a)) {
    	 StringBuilder sb=new StringBuilder("{");
         boolean first=true;
         for (float o:a) {
       	    if (first) first=false; else sb.append(',');
       	    sb.append(o);
         }
         sb.append('}');
         return sb;
       }
       return "";
    }
    
    public static <T> CharSequence dump(T a[]) {
       return isEmpty(a)?"":dump(Arrays.asList(a));
    }
    
    public static <C extends Collection> CharSequence dump(C c) {
       StringBuilder sb=new StringBuilder("{");
       boolean first=true;
       for (Object o:c) {
    	  if (first) first=false; else sb.append(',');
    	  if (o==null) sb.append("null"); else
    	  if (o instanceof Collection) sb.append(dump((Collection)o)); else
    	  sb.append(o.toString());
       }
       sb.append('}');
       return sb;
    }
  
	
	public static boolean isPhoneNumber(String str) {
		boolean prevD=false;
		for(int i=0; i<str.length(); i++) {
			char c=str.charAt(i);
			boolean d=Character.isDigit(c), sp=Character.isSpaceChar(c);
			if (!(d||sp)) switch (c) {
			case '*': case '+': if ((prevD||i==0)&&(i!=str.length()-1)) break;
			return false;
			case '-': if ((prevD)&&(i!=str.length()-1)) break;
			default: return false;
			}		
			if (!sp) prevD=d;
		}
		
		return true;
	}

    public static double milesToKilometers(double miles) {
    	return miles / 1.609344;
    }

    /*
    void dump(Cursor cr) {
        StringBuilder sb=new StringBuilder();
        int count=cr.getCount();
        if (count>0&&cr.moveToFirst()) {
     	   sb.append("----------------\ncount="+count);
     	   sb.append('\n');
     	   do {
     		  for (int i=0;i<cr.getColumnCount();i++) {
     			  sb.append(cr.getColumnName(i));
     			  sb.append('=');
     			  sb.append(cr.isNull(i)?"null":cr.getString(i));
     			  sb.append('\n');
     		  }
     		  sb.append(">>>end record--------------------\n");
            } while (cr.moveToNext());
        };
        sb.append(">>>end dump--------------------\n");
        Log.d(TAG,"DUMP==\n"+sb);
     }*/
    
	public interface TextInputDialogResult {
		public void onDialogOK(String result);
		public void onDialogClose();
	};    	
}
