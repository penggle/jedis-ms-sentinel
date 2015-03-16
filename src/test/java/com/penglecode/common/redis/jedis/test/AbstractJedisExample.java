package com.penglecode.common.redis.jedis.test;

import org.junit.Before;

import com.penglecode.common.redis.jedis.JedisTemplate;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

public abstract class AbstractJedisExample<I> {

	private JedisPoolConfig jedisPoolConfig;
	
	private Pool<I> jedisPool;
	
	private JedisTemplate<I> jedisTemplate;

	@Before
	public final void setUp(){
		jedisPoolConfig = createJedisPoolConfig();
		jedisPool = createJedisPool(jedisPoolConfig);
		jedisTemplate = new JedisTemplate<I>(jedisPool);
	}
	
	public JedisPoolConfig createJedisPoolConfig(){
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(8);
		jedisPoolConfig.setMaxIdle(8);
		jedisPoolConfig.setMinIdle(0);
		jedisPoolConfig.setMaxWaitMillis(15000);
		return jedisPoolConfig;
	}
	
	public abstract Pool<I> createJedisPool(JedisPoolConfig jedisPoolConfig);
	
	public JedisPoolConfig getJedisPoolConfig() {
		return jedisPoolConfig;
	}

	public Pool<I> getJedisPool() {
		return jedisPool;
	}

	public JedisTemplate<I> getJedisTemplate() {
		return jedisTemplate;
	}
	
}
