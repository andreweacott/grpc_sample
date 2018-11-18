package org.marmoset.service.client;

import java.util.concurrent.CountDownLatch;

import org.marmoset.service.ChatServerGrpc;
import org.marmoset.service.ChatServerGrpc.ChatServerStub;
import org.marmoset.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class ServiceClientImpl {

   private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClientImpl.class);
   private final ChatServerStub serverStub;
   private final StreamObserver<Message> outgoingStream;
   private final CountDownLatch finishLatch = new CountDownLatch(1);
   
   public ServiceClientImpl(ManagedChannel channel) {
      this.serverStub = ChatServerGrpc.newStub(channel);
      this.outgoingStream = 
       serverStub.openSession(new StreamObserver<Message>() {

         @Override
         public void onNext(Message value) {
            LOGGER.info("received message : {}", value.getMessage());
         }

         @Override
         public void onError(Throwable t) {
            LOGGER.error("Caught server error", t);
            finishLatch.countDown();
         }

         @Override
         public void onCompleted() {
            LOGGER.info("Server closed connection");
            finishLatch.countDown();
         }
      });
   }
   
   public void sendMessage(String message) {
      LOGGER.info("Client sending message: {}", message);
      Message msg = Message.newBuilder().setTarget("Server").setMessage(message).build();
      outgoingStream.onNext(msg);
   }
   
   
   public void awaitTermination() {
      try {
         finishLatch.await();
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }
   
}
