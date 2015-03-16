package com.penglecode.common.redis.jedis.test.example;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

import com.penglecode.common.redis.jedis.ms.ShardedMasterSlaveJedis;
import com.penglecode.common.redis.jedis.ms.ShardedMasterSlaveJedisSentinelPool;
import com.penglecode.common.redis.jedis.test.AbstractJedisExample;

public class ShardedMasterSlaveJedisSentinelPoolExample extends AbstractJedisExample<ShardedMasterSlaveJedis> {

	public Pool<ShardedMasterSlaveJedis> createJedisPool(JedisPoolConfig jedisPoolConfig) {
		Set<String> masterNames = new LinkedHashSet<String>();
		masterNames.add("master-1");
		masterNames.add("master-2");
		Set<String> sentinels = new LinkedHashSet<String>();
		sentinels.add("192.168.137.101:63791");
		sentinels.add("192.168.137.101:63792");
		return new ShardedMasterSlaveJedisSentinelPool(masterNames, sentinels, jedisPoolConfig);
	}

	@Test
	public void getRourceAndReturnResource(){
		Pool<ShardedMasterSlaveJedis> pool = this.getJedisPool();
		ShardedMasterSlaveJedis shardedMasterSlaveJedis = pool.getResource();
		System.out.println(">>> shardedMasterSlaveJedis = " + shardedMasterSlaveJedis);
		pool.returnResource(shardedMasterSlaveJedis);
		
		System.out.println("--------------------------------------");
		for(int i = 0; i < 10; i++){
			shardedMasterSlaveJedis = pool.getResource();
			System.out.println(">>> shardedMasterSlaveJedis = " + shardedMasterSlaveJedis);
			String key = "shard-" + i;
			shardedMasterSlaveJedis.set(key, System.currentTimeMillis() + "");
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
			System.out.println(key + " = " + shardedMasterSlaveJedis.getShard(key).opsForSlave().get(key));
			pool.returnResource(shardedMasterSlaveJedis);
		}
		System.out.println("--------------------------------------");
		
		System.out.println(">>> pool = " + pool);
		pool.destroy();
		System.out.println(">>> pool = " + pool);
		System.out.println(pool.getResource());// 如果pool已经被销毁,调用pool.getResource()会抛出"Can not get a resource from pool"异常
	}
	
	
	@Test
	public void masterSlaveFailover() throws Exception {
		Pool<ShardedMasterSlaveJedis> pool = this.getJedisPool();
		
		ShardedMasterSlaveJedis shardedMasterSlaveJedis = pool.getResource();
		System.out.println(">>> shardedMasterSlaveJedis = " + shardedMasterSlaveJedis);
		pool.returnResource(shardedMasterSlaveJedis);
		
		System.out.println("--------------------------------------");
		
		shardedMasterSlaveJedis = pool.getResource();
		System.out.println(">>> shardedMasterSlaveJedis = " + shardedMasterSlaveJedis);
		pool.returnResource(shardedMasterSlaveJedis);
		
		Thread.sleep(10L * 10);
		
		shardedMasterSlaveJedis = pool.getResource();
		System.out.println(">>> shardedMasterSlaveJedis = " + shardedMasterSlaveJedis);
		pool.returnResource(shardedMasterSlaveJedis);
		
		System.out.println("--------------------------------------");
		pool.destroy();
	}
	
}
