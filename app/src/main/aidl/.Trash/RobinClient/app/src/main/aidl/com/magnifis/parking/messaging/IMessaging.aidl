package com.magnifis.parking.messaging;

import  com.magnifis.parking.messaging.Message;
import  com.magnifis.parking.messaging.IListener;

interface IMessaging {
  /*
   in a returned Message "statusBarNotificationId" field should be set
   if a notification regarding corresponding message would has been sent  
  */

  Message get(String id); // can return null if the message is not cached
  int countUnread();
  int countUnreadOfType(int type);
  
  Message []read(int firstN);
  Message []readOfType(int type,int firstN);
  Message []readAll();
  Message []readAllOfType();

  // this callback notification methods do not affect
  // on sending intents/ s/bar notifications 
  void startNotification(IListener listener, boolean withBody); 
  void stopNotification();
}  