package org.marmoset.service.server;

import io.grpc.ServerBuilder;

public class Server {

   public static void main(String[] args) throws Exception  {
      io.grpc.Server server =
         ServerBuilder
            .forPort(8980)
            .addService(new ServiceServerImpl())
            .build();
      server.start();
      server.awaitTermination();
   }
   
}
