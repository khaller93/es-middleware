package at.ac.tuwien.ifs.es.middleware.service.exploration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@EnableAspectJAutoProxy(proxyTargetClass=true)
@Configuration
@Profile("aop")
public class AspectConfiguration {

}
