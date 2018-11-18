package org.marmoset.service.server;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.marmoset.service.ChatServerGrpc.ChatServerImplBase;
import org.marmoset.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;

public class ServiceServerImpl extends ChatServerImplBase {

   private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServerImpl.class);
   
   public StreamObserver<Message> openSession(StreamObserver<Message> responseObserver) {
      return new StreamObserver<Message>() {
         
         private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
         private Future<?> future = null;
         
         @Override
         public void onNext(Message value) {
            if (value.getMessage().equals("CMD START")) {
               if (future == null) {
                  Message startMsg = Message.newBuilder().setTarget("client").setMessage("Starting").build();
                  responseObserver.onNext(startMsg);
                  future = executor.scheduleAtFixedRate(() -> {
                     LOGGER.info("Sending server tick");
                     Message tick = Message.newBuilder().setTarget("client").setMessage("Tick").build();
                     responseObserver.onNext(tick);
                  }, 0, 1, TimeUnit.SECONDS);
               }
            } else if (value.getMessage().equals("CMD STOP")) {
               future.cancel(true);
               future = null;
               Message finished = Message.newBuilder().setTarget("client").setMessage("Stopped").build();
               responseObserver.onNext(finished);
            } else if (value.getMessage().equals("CMD EXIT")) {
               if (future != null) {
                  future.cancel(true);
                  future = null;
               }
               responseObserver.onCompleted();
            }
         }

         @Override
         public void onError(Throwable t) {
            LOGGER.warn("Caught exception from client", t);
         }

         @Override
         public void onCompleted() {
            LOGGER.warn("Client closed session");
            if (future != null) {
               future.cancel(true);
               future = null;
            }
            responseObserver.onCompleted();
         }
      };
   }
   
}
