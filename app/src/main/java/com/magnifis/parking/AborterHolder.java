package com.magnifis.parking;

public interface AborterHolder {
	void setAborter(Runnable abo);
	boolean abortOperation(int flags);
}
