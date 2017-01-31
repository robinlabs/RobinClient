/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/vladgets/development/AndroidStudioProjects/RobinClient/app/src/main/aidl/.Trash/RobinClient/app/src/main/aidl/development/AndroidStudioProjects/RobinClient/app/src/main/aidl/com/magnifis/parking/messaging/IMessaging.aidl
 */
package com.magnifis.parking.messaging;
public interface IMessaging extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.magnifis.parking.messaging.IMessaging
{
private static final java.lang.String DESCRIPTOR = "com.magnifis.parking.messaging.IMessaging";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.magnifis.parking.messaging.IMessaging interface,
 * generating a proxy if needed.
 */
public static com.magnifis.parking.messaging.IMessaging asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.magnifis.parking.messaging.IMessaging))) {
return ((com.magnifis.parking.messaging.IMessaging)iin);
}
return new com.magnifis.parking.messaging.IMessaging.Stub.Proxy(obj);
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
case TRANSACTION_get:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
android.os.Message _result = this.get(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_countUnread:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.countUnread();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_countUnreadOfType:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.countUnreadOfType(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_read:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.Message[] _result = this.read(_arg0);
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_readOfType:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
android.os.Message[] _result = this.readOfType(_arg0, _arg1);
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_readAll:
{
data.enforceInterface(DESCRIPTOR);
android.os.Message[] _result = this.readAll();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_readAllOfType:
{
data.enforceInterface(DESCRIPTOR);
android.os.Message[] _result = this.readAllOfType();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_startNotification:
{
data.enforceInterface(DESCRIPTOR);
com.magnifis.parking.messaging.IListener _arg0;
_arg0 = com.magnifis.parking.messaging.IListener.Stub.asInterface(data.readStrongBinder());
boolean _arg1;
_arg1 = (0!=data.readInt());
this.startNotification(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_stopNotification:
{
data.enforceInterface(DESCRIPTOR);
this.stopNotification();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.magnifis.parking.messaging.IMessaging
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
/*
   in a returned Message "statusBarNotificationId" field should be set
   if a notification regarding corresponding message would has been sent  
  */
@Override public android.os.Message get(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Message _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_get, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Message.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// can return null if the message is not cached

@Override public int countUnread() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_countUnread, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int countUnreadOfType(int type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
mRemote.transact(Stub.TRANSACTION_countUnreadOfType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public android.os.Message[] read(int firstN) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Message[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(firstN);
mRemote.transact(Stub.TRANSACTION_read, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.os.Message.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public android.os.Message[] readOfType(int type, int firstN) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Message[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeInt(firstN);
mRemote.transact(Stub.TRANSACTION_readOfType, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.os.Message.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public android.os.Message[] readAll() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Message[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_readAll, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.os.Message.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public android.os.Message[] readAllOfType() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Message[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_readAllOfType, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.os.Message.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// this callback notification methods do not affect
// on sending intents/ s/bar notifications 

@Override public void startNotification(com.magnifis.parking.messaging.IListener listener, boolean withBody) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeInt(((withBody)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_startNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopNotification() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_get = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_countUnread = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_countUnreadOfType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_read = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_readOfType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_readAll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_readAllOfType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_startNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_stopNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
}
/*
   in a returned Message "statusBarNotificationId" field should be set
   if a notification regarding corresponding message would has been sent  
  */
public android.os.Message get(java.lang.String id) throws android.os.RemoteException;
// can return null if the message is not cached

public int countUnread() throws android.os.RemoteException;
public int countUnreadOfType(int type) throws android.os.RemoteException;
public android.os.Message[] read(int firstN) throws android.os.RemoteException;
public android.os.Message[] readOfType(int type, int firstN) throws android.os.RemoteException;
public android.os.Message[] readAll() throws android.os.RemoteException;
public android.os.Message[] readAllOfType() throws android.os.RemoteException;
// this callback notification methods do not affect
// on sending intents/ s/bar notifications 

public void startNotification(com.magnifis.parking.messaging.IListener listener, boolean withBody) throws android.os.RemoteException;
public void stopNotification() throws android.os.RemoteException;
}
