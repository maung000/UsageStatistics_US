/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Project\\DA\\UsageStatistics_US\\src\\main\\aidl\\ca\\mimic\\usagestatistics\\IWatchfulService.aidl
 */
package ca.mimic.usagestatistics;
public interface IWatchfulService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements ca.mimic.usagestatistics.IWatchfulService
{
private static final java.lang.String DESCRIPTOR = "ca.mimic.usagestatistics.IWatchfulService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ca.mimic.usagestatistics.IWatchfulService interface,
 * generating a proxy if needed.
 */
public static ca.mimic.usagestatistics.IWatchfulService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof ca.mimic.usagestatistics.IWatchfulService))) {
return ((ca.mimic.usagestatistics.IWatchfulService)iin);
}
return new ca.mimic.usagestatistics.IWatchfulService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
java.lang.String descriptor = DESCRIPTOR;
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(descriptor);
return true;
}
case TRANSACTION_createNotification:
{
data.enforceInterface(descriptor);
this.createNotification();
reply.writeNoException();
return true;
}
case TRANSACTION_destroyNotification:
{
data.enforceInterface(descriptor);
this.destroyNotification();
reply.writeNoException();
return true;
}
case TRANSACTION_buildTasks:
{
data.enforceInterface(descriptor);
this.buildTasks();
reply.writeNoException();
return true;
}
case TRANSACTION_buildReorderAndLaunch:
{
data.enforceInterface(descriptor);
this.buildReorderAndLaunch();
reply.writeNoException();
return true;
}
default:
{
return super.onTransact(code, data, reply, flags);
}
}
}
private static class Proxy implements ca.mimic.usagestatistics.IWatchfulService
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
@Override public void createNotification() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_createNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void destroyNotification() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_destroyNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void buildTasks() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_buildTasks, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void buildReorderAndLaunch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_buildReorderAndLaunch, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_createNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_destroyNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_buildTasks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_buildReorderAndLaunch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void createNotification() throws android.os.RemoteException;
public void destroyNotification() throws android.os.RemoteException;
public void buildTasks() throws android.os.RemoteException;
public void buildReorderAndLaunch() throws android.os.RemoteException;
}
