package com.magnifis.parking.messaging;

import  com.magnifis.parking.messaging.Message;

oneway interface IListener {
  void onNewMessage(in Message msg);
}  