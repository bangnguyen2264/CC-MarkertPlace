package com.example.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class LoadBalancerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final LoadBalancerClientFactory clientFactory;
    private final DiscoveryClient discoveryClient;
    private final ApplicationContext applicationContext;
    private boolean loadBalancersRegistered = false; // Flag to prevent multiple registrations

    public LoadBalancerConfig(LoadBalancerClientFactory clientFactory, DiscoveryClient discoveryClient, ApplicationContext applicationContext) {
        this.clientFactory = clientFactory;
        this.discoveryClient = discoveryClient;
        this.applicationContext = applicationContext;
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (loadBalancersRegistered) {
            log.debug("Load balancers already registered, skipping");
            return;
        }

        if (discoveryClient == null) {
            log.error("DiscoveryClient is not available, skipping load balancer registration");
            return;
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
        discoveryClient.getServices().forEach(serviceId -> {
            String beanName = "loadBalancer-" + serviceId;
            if (!registry.containsBeanDefinition(beanName)) {
                log.info("Registering load balancer for service: {}", serviceId);
                BeanDefinition beanDefinition = BeanDefinitionBuilder
                        .genericBeanDefinition(ReactorLoadBalancer.class, () -> new RoundRobinLoadBalancer(
                                clientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                                serviceId))
                        .getBeanDefinition();
                registry.registerBeanDefinition(beanName, beanDefinition);
            } else {
                log.debug("Load balancer for service {} already registered", serviceId);
            }
        });

        loadBalancersRegistered = true; // Set flag to prevent re-registration
    }
}