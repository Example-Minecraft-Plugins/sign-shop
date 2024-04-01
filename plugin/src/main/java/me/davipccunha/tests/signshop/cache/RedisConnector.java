package me.davipccunha.tests.signshop.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnector {
    private final JedisPool pool;

    public RedisConnector(String host, int port, String password) {
        flush();
        this.pool = new JedisPool(defaultPoolConfig(), host, port, 0, password);
    }

    public Jedis getJedis() {
        return this.pool.getResource();
    }

    public boolean isConnected() {
        return (this.pool != null && !this.pool.isClosed());
    }

    public void flush(Runnable... actions) {
        if (actions != null) {
            for (Runnable action : actions) {
                action.run();
            }
        }

        if (this.pool != null) {
            pool.close();
        }
    }

    private JedisPoolConfig defaultPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}
