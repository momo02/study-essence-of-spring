package moviebuddy;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.*;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.concurrent.TimeUnit;

/**
 * @Configuration 어노테이션을 붙임으로,
 * 스프링의 빈 구성정보 => Configuration Metadata로 사용이 될 수 있음을 선언.
 */
@Configuration
@PropertySource("/application.properties")
// 스프링은 자동 클래스 탐지 기법으로 스프링 컨테이너에 빈을 자동으로 등록해 주는 기능을 제공.
// @ComponentScan 을 통해 활성화. -> 지정된 패키지 경로에서 @Component 와 같이 스테레오 타입으로 선언된 클래스를 찾아 빈으로 동록하고 관리.
// 패키지를 지정하지 않으면 @ComponentScan 이 선언된 클래스의 패키지 경로를 기준으로 탐색.
@ComponentScan(basePackages = { "moviebuddy" })
@Import({ MovieBuddyFactory.DomainModuleConfig.class, MovieBuddyFactory.DataSourceModuleConfig.class })
@EnableCaching
// CachingConfigurer interface is to be implemented by @Configuration classes annotated with @EnableCaching.
// It provides various methods to configure or customize caching abstraction.
// CachingConfigurer 인터페이스는 @EnableCaching 주석이 달린 @Configuration 클래스에 의해 구현된다. 캐싱 추상화를 구성하거나 사용자 정의하는 다양한 방법을 제공한다.
public class MovieBuddyFactory implements CachingConfigurer {

    @Bean  // Jaxb2Marshaller : JAXB를 이용해 마샬링,언마샬링을 해주는 스프링의 OXM 모듈 구현체
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // 스프링의 자동 클래스 탐지와 유사하게 지정된 패키지에서 XML을 자바 객체로 변환 시 사용할 클래스를 찾아서 사용을 할 수 있음.
        marshaller.setPackagesToScan("moviebuddy");
        return marshaller;
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS));

        return cacheManager;
    }

    @Override
    public CacheManager cacheManager() {
        return caffeineCacheManager();
    }

    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(caffeineCacheManager());
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

    @Configuration
    static class DomainModuleConfig {
    }

    @Configuration
    static class DataSourceModuleConfig {
    }
}
