package com.penglecode.common.redis.jedis;

import redis.clients.util.Pool;

public class JedisTemplate<I> {

	private final Pool<I> jedisPool;
	
	public JedisTemplate(Pool<I> jedisPool){
		this.jedisPool = jedisPool;
	}
	
	protected I getResource(){
		return jedisPool.getResource();
	}
	
	protected void returnResource(I jedis){
		if(jedis != null){
			jedisPool.returnResource(jedis);
		}
	}
	
	public <O> O execute(JedisCallback<I,O> callback){
		if(callback != null){
			I jedis = null;
			try {
				jedis = getResource();
				return callback.doInJedis(jedis);
			} finally {
				returnResource(jedis);
			}
		}
		return null;
	}
	
}
