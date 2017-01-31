/*
 * Xml.java
 *
 * Created on April 3, 2007, 11:38 AM
 *

 *     Copyright(C) 2007 Ze'ev (Vladimir) Belkin. All rights reserved.
 *
   <h1>The Artistic License</h1>

<tt>

<p>Preamble</p>

<p>The intent of this document is to state the conditions under which a
Package may be copied, such that the Copyright Holder maintains some
semblance of artistic control over the development of the package,
while giving the users of the package the right to use and distribute
the Package in a more-or-less customary fashion, plus the right to make
reasonable modifications.</p>

<p>Definitions:</p>

<ul>

<li>	"Package" refers to the collection of files distributed by the
	Copyright Holder, and derivatives of that collection of files
	created through textual modification.</li>

<li>	"Standard Version" refers to such a Package if it has not been
	modified, or has been modified in accordance with the wishes
	of the Copyright Holder.</li>

<li>	"Copyright Holder" is whoever is named in the copyright or
	copyrights for the package.</li>

<li>	"You" is you, if you're thinking about copying or distributing
	this Package.</li>

<li>	"Reasonable copying fee" is whatever you can justify on the
	basis of media cost, duplication charges, time of people involved,
	and so on.  (You will not be required to justify it to the
	Copyright Holder, but only to the computing community at large
	as a market that must bear the fee.)</li>

<li>	"Freely Available" means that no fee is charged for the item
	itself, though there may be fees involved in handling the item.
	It also means that recipients of the item may redistribute it
	under the same conditions they received it.</li>
	
</ul>

<p>1. You may make and give away verbatim copies of the source form of the
Standard Version of this Package without restriction, provided that you
duplicate all of the original copyright notices and associated disclaimers.</p>

<p>2. You may apply bug fixes, portability fixes and other modifications
derived from the Public Domain or from the Copyright Holder.  A Package
modified in such a way shall still be considered the Standard Version.</p>

<p>3. You may otherwise modify your copy of this Package in any way, provided
that you insert a prominent notice in each changed file stating how and
when you changed that file, and provided that you do at least ONE of the
following:</p>

<blockquote>

    <p>a) place your modifications in the Public Domain or otherwise make them
    Freely Available, such as by posting said modifications to Usenet or
    an equivalent medium, or placing the modifications on a major archive
    site such as ftp.uu.net, or by allowing the Copyright Holder to include
    your modifications in the Standard Version of the Package.</p>

    <p>b) use the modified Package only within your corporation or organization.</p>

    <p>c) rename any non-standard executables so the names do not conflict
    with standard executables, which must also be provided, and provide
    a separate manual page for each non-standard executable that clearly
    documents how it differs from the Standard Version.</p>

    <p>d) make other distribution arrangements with the Copyright Holder.</p>
	
</blockquote>

<p>4. You may distribute the programs of this Package in object code or
executable form, provided that you do at least ONE of the following:</p>

<blockquote>

    <p>a) distribute a Standard Version of the executables and library files,
    together with instructions (in the manual page or equivalent) on where
    to get the Standard Version.</p>

    <p>b) accompany the distribution with the machine-readable source of
    the Package with your modifications.</p>

    <p>c) accompany any non-standard executables with their corresponding
    Standard Version executables, giving the non-standard executables
    non-standard names, and clearly documenting the differences in manual
    pages (or equivalent), together with instructions on where to get
    the Standard Version.</p>

    <p>d) make other distribution arrangements with the Copyright Holder.</p>

</blockquote>

<p>5. You may charge a reasonable copying fee for any distribution of this
Package.  You may charge any fee you choose for support of this Package.
You may not charge a fee for this Package itself.  However,
you may distribute this Package in aggregate with other (possibly
commercial) programs as part of a larger (possibly commercial) software
distribution provided that you do not advertise this Package as a
product of your own.</p>

<p>6. The scripts and library files supplied as input to or produced as
output from the programs of this Package do not automatically fall
under the copyright of this Package, but belong to whomever generated
them, and may be sold commercially, and may be aggregated with this
Package.</p>


<p>7. The name of the Copyright Holder may not be used to endorse or promote
products derived from this software without specific prior written permission.</p>

<p>8. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.</p>

<p>The End</p>
 */
/*
 * This file is borrowed from Yaacfi open source project 
 * of Ze'ev Belkin http://zeevbelkin.com/yaacfi/ 
 * 
 * */

package com.magnifis.parking;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import javax.xml.parsers.DocumentBuilderFactory;        
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import java.net.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A class that provides utilizes to deal with XML-files
 * @author Zeev Belkin
 * @email koyaanisqatsi@narod.ru
 * @www http://zeevbelkin.com
 */
public class Xml {
  static private final String TAG="Xml";
	
/**
 * loads an XML-document from an URL
 * @return the loaded document, or <b>null</b> if any error
 */
  public  static org.w3c.dom.Document loadXmlFile(URL url) {
     try {
       InputStream is=url.openStream();
       try {
         return loadXmlFile(is);
       } finally {
         is.close();
       }
     } catch (Throwable t) {}
     return null;
  }
/**
 * loads an XML-document from a file
 * @return the loaded document, or <b>null</b> if any error
 */
  public  static org.w3c.dom.Document loadXmlFile(File  src) {
     try {
       InputStream is=new FileInputStream(src);
       try {
         return loadXmlFile(is);
       } finally {
         is.close();
       }
     } catch (Throwable t) {}
     return null;
  }
  
  /**
   * loads an XML-document from a file
   * @return the loaded document, or <b>null</b> if any error
   */
   public  static org.w3c.dom.Document loadXmlData(String data) {
     return loadXmlFile(new ByteArrayInputStream(data.getBytes()));
   } 
  
  /**
  * loads an XML-document from a file
  * @return the loaded document, or <b>null</b> if any error
  */
  public  static org.w3c.dom.Document loadXmlFile(InputStream is) {
     org.w3c.dom.Document doc=null;
     try {
        DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
        if (is!=null) try {
          doc=builder.parse(is);
        } finally {
          is.close();
        }
     } catch (Throwable t) { t.printStackTrace(); }
     return doc;
  }
  
  @Retention(value=RetentionPolicy.RUNTIME)
  @Target(value=ElementType.FIELD)
  public static @interface ML {
    public String value() default "";
    public String attr() default "";
    public String tag() default "";
    public String format() default "";
    public boolean indirect() default false; 
    public boolean ifpresents() default true;
  };
  
  @Retention(value=RetentionPolicy.RUNTIME)
  @Target(value=ElementType.FIELD) 
  public static @interface ML_alternatives {
    public ML[] value();
  }

  
  
  static boolean isEmpty(String s) {
	return (s==null)||s.length()==0;
  }
  
  private static volatile DateFormat simpleDateFormat=null;
  
  @SuppressWarnings({ "rawtypes", "serial" })
  private static HashSet<Class> simpleClass= new HashSet<Class>() {
	  {Class __simpleClass[]={
				 String.class,Integer.class,Long.class,java.util.Date.class ,
				 Double.class, Boolean.class,
				 boolean.class, int.class, long.class,
				 double.class
	    };
	    for (Class c:__simpleClass) this.add(c); 
	  }  
  };
  
  @SuppressWarnings("rawtypes")
  public static boolean isSimpleClass(Class c) {
	return simpleClass.contains(c);
  }
  
  public static <T> T setPropertiesFrom(
		    org.w3c.dom.Element node,
		    Class<T> cls
  ) {
	T v=null;
    try {
		v=setPropertiesFrom(node,(T)cls.newInstance());
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (InstantiationException e) {
		e.printStackTrace();
	}	
    return v;
  }
  
  public static <T> T setPropertiesFrom(
    org.w3c.dom.Element node,
    T obj
  ) {
	if (node==null)  Log.d(TAG,"node==null");
	boolean ok=true;
	
	Class cls=obj.getClass();
	ArrayList<Class> clss=new ArrayList<Class>(10);
	do {
		clss.add(cls);
		cls=cls.getSuperclass();
	} while (cls!=null);
	
	for (Class cl:clss) for (Field fl:cl.getDeclaredFields()) {
      fl.setAccessible(true);
      
      ML anns[]=null;
      
      if (fl.isAnnotationPresent(ML.class)) 
    	anns=new ML[] { fl.getAnnotation(ML.class) };
      else if (fl.isAnnotationPresent(ML_alternatives.class)) 
    	anns=fl.getAnnotation(ML_alternatives.class).value();
    	  
      
      if (anns!=null) {
    	Class flType=fl.getType();
    	  
        for (ML an:anns) try {
          String sVal=null;

          boolean useAttr=an.attr().length()>0,indirect=an.indirect();
          
          String anv=an.value();
          String tag=useAttr?an.tag():(anv.length()==0?an.tag():anv);
          if ("".equals(tag)) tag=null;
          
          //Log.d(TAG,"@@ "+fl.getName()+" tag:"+tag+" node:"+node);
          
          if (flType.isArray()) {
            NodeList nl=node.getElementsByTagName(tag);
            if (nl==null) {
               if (useAttr&&an.value().length()>0) {
                 nl=node.getElementsByTagName(an.value());
               }    
               if (nl==null) continue;
               useAttr=false;
            } 
            if (nl!=null){
            	int nll=nl.getLength();
            	if (!indirect) {
            	   for (int i=0;i<nl.getLength();i++) 
            		  if (node!=nl.item(i).getParentNode()) nll--;
            		  
            	}
            	if (nll>0) {
            		Class cc=flType.getComponentType();
            		Object ar=java.lang.reflect.Array.newInstance(cc, nll);
            		for (int i=0,j=0;i<nl.getLength();i++) {
            		  Node nli=nl.item(i);
            		  if (indirect||(node==nli.getParentNode())) {
            			Object ob=null;
            			if (isSimpleClass(cc)) {
            			   String txt=getInnerText(nli);
            			   if (cc==String.class) {
                              ob=txt;
            			   } else if (cc==Double.class||cc==double.class) { 
            				  ob=Double.parseDouble(txt);
            			   } else if (cc==Integer.class||cc==int.class) { 
         				      ob=Integer.parseInt(txt);
         			       }
            			} else {
            			  ob=cc.newInstance();
            			  setPropertiesFrom((org.w3c.dom.Element)nli,ob);
            			}
            			Array.set(ar, j++, ob);
            		  }
            		}
            		fl.set(obj,ar);
            	}
            	//Log.d(TAG,"!XXX! "+Array.get(ar, 0));
            }
            //Log.d(TAG,"!! "+fl.getGenericType());
          } else {
        	 // Log.d(TAG,"@55@ "+fl.getName()+" tag:"+tag+" node:"+node);
        	  
              org.w3c.dom.Element 
                 el=(tag==null)?((org.w3c.dom.Element)node):getTag(node,tag,indirect);  
        	  
              if (el==null) {
            	 // Log.d(TAG,"el==null "+domToText(node));
                if (useAttr&&an.value().length()>0) {
                  el=getTag(node,an.value(),indirect);
                }
                if (el==null) continue;
                useAttr=false;
              }
              
              if (useAttr) {
            	 // Log.d(TAG,"useAttr ");
                if (el.hasAttribute( an.attr())) 
                	sVal=el.getAttribute(an.attr());
                else
                	continue;
              } else {
            	if (!simpleClass.contains(flType)) {
            		  Object fo=flType.newInstance();
            		 //if (fo!=null) {
            		//  Log.d(TAG,"%%%% !simpleClass");
            		  fl.set(obj,setPropertiesFrom(el,fo)); 
            		 // Log.d(TAG,"%%%% is set "+fl.getName()+" "+fl.get(obj));
            		 //}
            	   continue;
            	}
            	CharSequence val=domToText(el,false, true);
            	if (val!=null) sVal= val.toString();//getInnerText(el);

              }
              
              if (sVal==null) {
            	  //Log.d(TAG,an.tag());
            	  if (an.ifpresents()&&(flType==Boolean.class||flType==boolean.class))
            		  fl.set(obj, true);
            	  
            	  continue;
              }
              
              // Log.i(TAG,"@sv@ "+sVal);

              if (flType==String.class) 
                fl.set(obj,sVal); 
              else if (flType==Integer.class||flType==int.class) {
            	if (sVal.length()>0) fl.set(obj, new Integer(sVal));  
              } 
              else if (flType==Long.class||flType==long.class) {
                if (sVal.length()>0) fl.set(obj, new Long(sVal));  
              } 
              else if (flType==Double.class||flType==double.class) {
                if (sVal.length()>0) fl.set(obj, new Double(sVal));  
              } else if (flType==Boolean.class||flType==boolean.class) {
              	if (sVal.length()>0) fl.set(obj, new Boolean(sVal));  
              } 
              else 
              if (flType==java.util.Date.class) try {
                if (simpleDateFormat==null) synchronized(Xml.class) {
                  if (simpleDateFormat==null) {
                    simpleDateFormat=new java.text.SimpleDateFormat("yyyy-MM-dd");
                  }
                }
                DateFormat df=isEmpty(an.format())?simpleDateFormat:new java.text.SimpleDateFormat(an.format());
                fl.set(obj,df.parse(sVal));
              } catch(Throwable t) {
            	  t.printStackTrace();
              }
          }
        } catch (Throwable ex) {
          ok=false;
          Log.e(TAG, ex.getMessage(),ex);
          //ex.printStackTrace();
        }
        //setPropertyFrom(node,an.value(),fl.getName(),obj);
      }
    }
    return ok?obj:null;
  }
  
  public static void setPropertyFrom(
    org.w3c.dom.Node node,
    String name,String propName,
    Object obj
  ) {
    String 
      val=getTagContent(node,name,false),
      setterName="set"+Character.toUpperCase(propName.charAt(0))+propName.substring(1);
      try {
        obj.getClass().getMethod(setterName,String.class).invoke(obj,val);
      } catch (Throwable ex) {
    	  //Log.e(TAG, ex.getMessage(),ex);
        //ex.printStackTrace();
      }
  }
  
  public static CharSequence domToText(Element el) {
	return domToText(el,true, false);
  }
  
  public static CharSequence domToText(Element el, boolean withEnvelope, boolean topLevel) {
	StringBuilder sb=new StringBuilder();
    if (withEnvelope) {
      sb.append("\n<");
      sb.append(el.getTagName());
      NamedNodeMap attrs=el.getAttributes();
      if (attrs!=null) for (int i=0;i<attrs.getLength();i++) {
    	 Attr a=(Attr)attrs.item(i);
    	 sb.append(' ');
    	 sb.append(a.getName());
    	 sb.append("=\"");
    	 sb.append(TextUtils.htmlEncode(a.getValue()));
    	 sb.append('"');
      }
    }
    NodeList cn=el.getChildNodes();
    if (cn!=null&&cn.getLength()>0) {
    	if (withEnvelope) sb.append('>');
    	///
    	for (int i=0;i<cn.getLength();i++) {
    	  Node node=cn.item(i);
    	  if (node instanceof Element) sb.append(domToText((Element)node)); else 
    	  switch (node.getNodeType()) {
    	  case Node.CDATA_SECTION_NODE:
    	  case Node.TEXT_NODE:
    		 String t=node.getNodeValue();
    		 sb.append(withEnvelope?TextUtils.htmlEncode(t):t);
    	  }
    	}
    	///
    	if (withEnvelope) {
    		sb.append("</");
    		sb.append(el.getTagName());
    		sb.append('>');   
    	}
    } else {
      if (withEnvelope) sb.append("/>"); else if (topLevel) return null;
    }    	
	return sb;
  }
  
  public static String getInnerText(Node node) {
	if (node.getNodeType()==Node.TEXT_NODE) return node.getNodeValue();
    NodeList nl=node.getChildNodes();
    String s="";
    if (nl!=null) for (int i=0;i<nl.getLength();i++) {
       String ss=getInnerText(nl.item(i));
       if (ss!=null) s+=ss;
    }
	return s==""?null:s;
  }
  
  public static org.w3c.dom.Element getTag(
    org.w3c.dom.Node node,String name,
    boolean idirect
  ) {
	 if (idirect&&(node instanceof org.w3c.dom.Element)) {
		 org.w3c.dom.Element top=(org.w3c.dom.Element)node;
		 org.w3c.dom.NodeList nl= top.getElementsByTagName(name);
		 if (nl!=null&&nl.getLength()>0) {
			 return (org.w3c.dom.Element)nl.item(0);
		 }
	 } 
	  
      org.w3c.dom.NodeList nl=node.getChildNodes();
      if (nl!=null) for (int i=0;i<nl.getLength();i++) {
        org.w3c.dom.Node n=nl.item(i);
        if (n instanceof org.w3c.dom.Element) {
          org.w3c.dom.Element el=(org.w3c.dom.Element)n;
          if (el.getTagName().equals(name)) return el;
        }
      }
      return null;
  }
  public static String getTagContent(org.w3c.dom.Node node,String name, boolean indirect) {
      org.w3c.dom.Node n=getTag(node,name,indirect);
      if (n!=null) return getInnerText(n);
      return null;
  }
  
}
