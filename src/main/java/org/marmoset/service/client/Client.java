package org.marmoset.service.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {

   public static void main(String[] args) {
      ManagedChannel channel =
         ManagedChannelBuilder
            .forAddress("localhost", 8980)
            .usePlaintext()
            .build();

      ServiceClientImpl impl = new ServiceClientImpl(channel);
      try {
         impl.sendMessage("CMD START");
         Thread.sleep(20000);
         impl.sendMessage("CMD STOP");
         Thread.sleep(5000);
         impl.sendMessage("CMD EXIT");
         impl.awaitTermination();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

}
