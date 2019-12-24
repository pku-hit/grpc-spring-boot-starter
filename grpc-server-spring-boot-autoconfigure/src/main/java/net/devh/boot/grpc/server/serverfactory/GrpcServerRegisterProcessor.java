package net.devh.boot.grpc.server.serverfactory;

import io.grpc.Server;

/**
 * register Grpc Service
 */
public interface GrpcServerRegisterProcessor {

    void register(Server server);

    void deregister();
}
