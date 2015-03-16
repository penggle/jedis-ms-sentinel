package com.penglecode.common.redis.jedis.test.example;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

import com.penglecode.common.redis.jedis.ms.MasterSlaveJedis;
import com.penglecode.common.redis.jedis.ms.MasterSlaveJedisSentinelPool;
import com.penglecode.common.redis.jedis.test.AbstractJedisExample;

public class MasterSlaveJedisSentinelPoolExample extends AbstractJedisExample<MasterSlaveJedis> {

	public Pool<MasterSlaveJedis> createJedisPool(JedisPoolConfig jedisPoolConfig) {
		Set<String> sentinels = new LinkedHashSet<String>();
		sentinels.add("192.168.137.101:63791");
		sentinels.add("192.168.137.101:63792");
		return new MasterSlaveJedisSentinelPool("master-1", sentinels, jedisPoolConfig);
	}

	@Test
	public void getRourceAndReturnResource(){
		Pool<MasterSlaveJedis> pool = this.getJedisPool();
		MasterSlaveJedis masterSlaveJedis = pool.getResource();
		System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis);
		pool.returnResource(masterSlaveJedis);
		
		System.out.println("--------------------------------------");
		for(int i = 0; i < 10; i++){
			masterSlaveJedis = pool.getResource();
			Jedis slaveJedis = masterSlaveJedis.opsForSlave(String.valueOf(i));
			System.out.println(">>> slaveJedis = " + slaveJedis.getClient().getHost() + ":" + slaveJedis.getClient().getPort());
			pool.returnResource(masterSlaveJedis);
		}
		System.out.println("--------------------------------------");
		
		System.out.println(">>> pool = " + pool);
		pool.destroy();
		System.out.println(">>> pool = " + pool);
		System.out.println(pool.getResource());// 如果pool已经被销毁,调用pool.getResource()会抛出"Can not get a resource from pool"异常
	}
	
	@Test
	public void masterSetSlaveGet(){
		Date now = new Date();
		String nowTime = String.format("%tF %tT", now, now);
		Pool<MasterSlaveJedis> pool = this.getJedisPool();
		MasterSlaveJedis masterSlaveJedis = pool.getResource();
		
		System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis.getClient().getHost() + ":" + masterSlaveJedis.getClient().getPort());
		System.out.println(">>> nowTime = " + nowTime);
		
		masterSlaveJedis.set("current_time", nowTime);
		
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
		
		System.out.println("--------------------------------------");
		Jedis slaveJedis = masterSlaveJedis.opsForSlave();
		System.out.println(">>> slaveJedis = " + slaveJedis.getClient().getHost() + ":" + slaveJedis.getClient().getPort());
		System.out.println(">>> nowTime = " + slaveJedis.get("current_time"));
		
		//slaveJedis.set("current_time", nowTime + ".000"); // slave节点默认是只读的,如果在只读的slave节点上进行写操作会抛出异常
		pool.returnResource(masterSlaveJedis);
		
		System.out.println("--------------------------------------");
		pool.destroy();
	}
	
	@Test
	public void masterSlaveFailover() throws Exception {
		Pool<MasterSlaveJedis> pool = this.getJedisPool();
		
		MasterSlaveJedis masterSlaveJedis = pool.getResource();
		System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis.getClient().getHost() + ":" + masterSlaveJedis.getClient().getPort());
		pool.returnResource(masterSlaveJedis);
		
		System.out.println("--------------------------------------");
		
		masterSlaveJedis = pool.getResource();
		System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis.getClient().getHost() + ":" + masterSlaveJedis.getClient().getPort());
		pool.returnResource(masterSlaveJedis);
		
		Thread.sleep(120000L * 10);
		
		masterSlaveJedis = pool.getResource();
		System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis.getClient().getHost() + ":" + masterSlaveJedis.getClient().getPort());
		pool.returnResource(masterSlaveJedis);
		
		System.out.println("--------------------------------------");
		pool.destroy();
	}
	
}
