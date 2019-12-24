package net.devh.boot.grpc.server.autoconfigure;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import io.grpc.Server;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.serverfactory.GrpcServerRegisterProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({ConsulDiscoveryProperties.class, ConsulClient.class, GrpcServerProperties.class})
public class GrpcServerConsulRegisterProcessor implements GrpcServerRegisterProcessor {
    @Autowired
    private ConsulServiceRegistry consulServiceRegistry;

    @Autowired
    private AbstractApplicationContext applicationContext;

    private ConsulRegistration consulRegistration;

    @Override
    public void register(Server server) {
        ConsulDiscoveryProperties properties = applicationContext.getBean(ConsulDiscoveryProperties.class);

        NewService grpcService = new NewService();
        grpcService.setPort(server.getPort());
        if (!properties.isPreferAgentAddress()) {
            grpcService.setAddress(properties.getHostname());
        }

        String appName = "grpc-" + ConsulAutoRegistration.getAppName(properties, applicationContext.getEnvironment());
        grpcService.setName(ConsulAutoRegistration.normalizeForDns(appName));
        grpcService.setId("grpc-" + ConsulAutoRegistration.getInstanceId(properties, applicationContext));
        consulRegistration = new ConsulRegistration(grpcService, properties);
        // Registry grpc
        this.consulServiceRegistry.register(consulRegistration);
    }

    @Override
    public void deregister() {
        if (consulRegistration != null){
            consulServiceRegistry.deregister(consulRegistration);
            consulServiceRegistry.close();
            consulRegistration = null;
        }
    }
}
