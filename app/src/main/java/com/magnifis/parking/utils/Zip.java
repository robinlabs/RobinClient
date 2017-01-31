package com.magnifis.parking.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Zip {
	private File archive;
	
	public Zip(File archive) throws ReadException  {
		this.archive=archive;
	}
	
	public List<String> dirlist() throws ReadException {
		return dirlist(null);
	}
	
	public List<String> dirlist(Integer level) throws ReadException {
		ZipFile zip;
		try {
		  zip = new ZipFile(archive);
	    } catch(IOException e) {
		  throw new ReadException(e);
	    }
		
		try {
			Enumeration<? extends ZipEntry> ens=zip.entries();
			ArrayList<String> dirs=new ArrayList<String>();
			while (ens.hasMoreElements()) {
				ZipEntry ze;
				try {
				   ze=ens.nextElement();
				} catch(Throwable e) {
				   throw new ReadException(e);
			    }
				if (ze.isDirectory()) {
					String name=ze.getName();
					if (level==null) dirs.add(name); else {
					   int co=Utils.countCharOccurrences(name,'/');
					   if (Utils.endsWith(name,'/')) --co;
					   if (co==level) dirs.add(name);
					}
				}
			}
			return dirs;
		} finally {
		   try {
		     zip.close();
		   } catch(Throwable t) {}
		}		
	}
	
	public void unzip(File to) throws IOException {
		ZipFile zip;
		try {
		  zip = new ZipFile(archive);
	    } catch(IOException e) {
		  throw new ReadException(e);
	    }
		
		try {
			Enumeration<? extends ZipEntry> ens=zip.entries();
			while (ens.hasMoreElements()) {
				ZipEntry ze;
				try {
				   ze=ens.nextElement();
				} catch(Throwable e) {
				   throw new ReadException(e);
			    }
				if (!ze.isDirectory()) {
					File dir=to;
					String name=ze.getName();
					int px=name.lastIndexOf('/');
					if (px>=0) {
						dir=new File(to,name.substring(0,px));
						if (!dir.exists()) dir.mkdirs();
						name=name.substring(px+1);
					}
					InputStream is;
					try {
					  is=zip.getInputStream(ze);
					} catch(Throwable e) {
					  throw new ReadException(e);
				    }
					try {
						FileOutputStream fos;
						try {
						  fos=new FileOutputStream(new File(dir,name));
						} catch(IOException e) {
							throw new WriteException(e);
						}
						try {
							byte buf[]=new byte[4096];
							for (;;) {
								int sz;
								try {
								  sz=is.read(buf);
								} catch(IOException e) {
									throw new ReadException(e);
								}								 
								if (sz<=0) break;
								try {
								  fos.write(buf,0,sz);
								} catch(IOException e) {
								  throw new WriteException(e);
								}  
							}
						} finally {
							fos.close();
						}
					} finally {
						is.close();
					}
				}
			}
		} finally {
			try {
				zip.close();
			} catch(Throwable t) {}
		}
	}	

}
