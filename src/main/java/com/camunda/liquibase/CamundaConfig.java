package com.camunda.liquibase;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("liquibase")
public class CamundaConfig {
}
