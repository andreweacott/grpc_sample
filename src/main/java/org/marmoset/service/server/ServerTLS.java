package org.marmoset.service.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

public class ServerTLS {

   private static final Logger LOGGER = LoggerFactory.getLogger(ServerTLS.class);
   private static final String certChainFilePath = "/Users/andrew/dev/code/projects/keys/server.crt";
   private static final String privateKeyFilePath = "/Users/andrew/dev/code/projects/keys/server.pem";
   private static final String trustCertCollectionFilePath = "/Users/andrew/dev/code/projects/keys/ca.crt";
   private io.grpc.Server server;
   
   public static void main(String[] args) throws Exception  {
      ServerTLS server = new ServerTLS();
      server.start();
      server.awaitTermination();
   }
   
   private static SslContextBuilder getSslContextBuilder() {
      SslContextBuilder sslClientContextBuilder =
         SslContextBuilder.forServer(
           new File(certChainFilePath),
           new File(privateKeyFilePath));
      
      if (trustCertCollectionFilePath != null) {
          sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
          sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
      }
      return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL);
  }

  private void start() throws IOException {
     this.server =
        NettyServerBuilder
           .forAddress(new InetSocketAddress(8443))
           .addService(new ServiceServerImpl())
           .sslContext(getSslContextBuilder().build())
           .build()
           .start();
      LOGGER.info("Server started, listening on " + 8443);
      Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
              // Use stderr here since the logger may have been reset by its JVM shutdown hook.
              System.err.println("*** shutting down gRPC server since JVM is shutting down");
              server.shutdown();
              System.err.println("*** server shut down");
          }
      });
  }
  
  private void awaitTermination() {
     try {
        server.awaitTermination();
     } catch (InterruptedException ex) {
        ex.printStackTrace();
     }
  }
}
