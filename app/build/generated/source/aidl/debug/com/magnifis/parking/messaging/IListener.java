/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/vladgets/development/AndroidStudioProjects/RobinClient/app/src/main/aidl/com/magnifis/parking/messaging/IListener.aidl
 */
package com.magnifis.parking.messaging;
public interface IListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.magnifis.parking.messaging.IListener
{
private static final java.lang.String DESCRIPTOR = "com.magnifis.parking.messaging.IListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.magnifis.parking.messaging.IListener interface,
 * generating a proxy if needed.
 */
public static com.magnifis.parking.messaging.IListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.magnifis.parking.messaging.IListener))) {
return ((com.magnifis.parking.messaging.IListener)iin);
}
return new com.magnifis.parking.messaging.IListener.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onNewMessage:
{
data.enforceInterface(DESCRIPTOR);
android.os.Message _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Message.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onNewMessage(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.magnifis.parking.messaging.IListener
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onNewMessage(android.os.Message msg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((msg!=null)) {
_data.writeInt(1);
msg.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onNewMessage, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onNewMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onNewMessage(android.os.Message msg) throws android.os.RemoteException;
}
