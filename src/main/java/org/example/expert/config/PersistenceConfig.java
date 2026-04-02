package org.example.expert.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        // EntityManager란 JPA 가 DB와 통신할 때 사용하는 객체로 JPAQueryFactory가 내부적으로 사용
        return new JPAQueryFactory(em); // EntityManager를 넘겨서 JPAQueryFactory 생성
    }
}
