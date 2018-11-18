package org.marmoset.service.client;

import java.io.File;

import javax.net.ssl.SSLException;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class ClientTLS {

   private static final String caCertFile = "/Users/andrew/dev/code/projects/keys/ca.crt";
   private static final String clientCertFile = "/Users/andrew/dev/code/projects/keys/client.crt";
   private static final String clientKeyFile = "/Users/andrew/dev/code/projects/keys/client.pem";
   
   public static void main(String[] args) throws Exception {
      ManagedChannel channel =
         NettyChannelBuilder
            .forAddress("localhost", 8443)
            .negotiationType(NegotiationType.TLS)
            .sslContext(buildSslContext(
               caCertFile,
               clientCertFile,
               clientKeyFile)) 
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
   
   private static SslContext buildSslContext(
      String trustCertCollectionFilePath,
      String clientCertChainFilePath,
      String clientPrivateKeyFilePath) throws SSLException {
      SslContextBuilder builder = GrpcSslContexts.forClient();
      if (trustCertCollectionFilePath != null) {
         builder.trustManager(new File(trustCertCollectionFilePath));
      }

      if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
         builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
      }
      return builder.build();
   }
}
