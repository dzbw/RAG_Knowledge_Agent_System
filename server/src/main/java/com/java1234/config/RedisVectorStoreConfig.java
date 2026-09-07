package com.java1234.config;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

/**
 * Redis 向量库：需在索引中注册可过滤的 metadata 字段，否则 delete / filter 会报
 * {@code Not allowed filter identifier name}。
 */
@Configuration
public class RedisVectorStoreConfig {

    @Bean
    public RedisVectorStore redisVectorStore(
            JedisConnectionFactory jedisConnectionFactory,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.redis.index-name:spring-ai-index}") String indexName,
            @Value("${spring.ai.vectorstore.redis.prefix:embedding:}") String prefix,
            @Value("${spring.ai.vectorstore.redis.initialize-schema:false}") boolean initializeSchema,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<VectorStoreObservationConvention> observationConvention,
            ObjectProvider<BatchingStrategy> batchingStrategy) {

        JedisPooled jedis = jedisPooled(jedisConnectionFactory);
        RedisVectorStore.Builder builder = RedisVectorStore.builder(jedis, embeddingModel)
                .initializeSchema(initializeSchema)
                .indexName(indexName)
                .prefix(prefix)
                .metadataFields(
                        RedisVectorStore.MetadataField.tag("docId"),
                        RedisVectorStore.MetadataField.tag("categoryId"),
                        RedisVectorStore.MetadataField.tag("title"))
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(observationConvention.getIfAvailable(() -> null));

        batchingStrategy.ifUnique(builder::batchingStrategy);

        return builder.build();
    }

    private static JedisPooled jedisPooled(JedisConnectionFactory factory) {
        RedisStandaloneConfiguration standalone = factory.getStandaloneConfiguration();
        String host = standalone.getHostName();
        int port = standalone.getPort();
        int database = standalone.getDatabase();

        DefaultJedisClientConfig.Builder cfg = DefaultJedisClientConfig.builder().database(database);
        String username = standalone.getUsername();
        if (username != null && !username.isEmpty()) {
            cfg.user(username);
        }
        RedisPassword pw = standalone.getPassword();
        if (pw.isPresent()) {
            cfg.password(new String(pw.get()));
        }
        return new JedisPooled(new HostAndPort(host, port), cfg.build());
    }
}
