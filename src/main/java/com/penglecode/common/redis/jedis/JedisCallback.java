package com.penglecode.common.redis.jedis;

public interface JedisCallback<I,O> {

	public O doInJedis(I jedis);
	
}
