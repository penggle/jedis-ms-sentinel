package com.penglecode.common.redis.jedis.test.example;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import com.penglecode.common.redis.jedis.test.AbstractJedisExample;

public class JedisSentinelExample extends AbstractJedisExample<Jedis> {

	public Pool<Jedis> createJedisPool(JedisPoolConfig jedisPoolConfig) {
		Set<String> sentinels = new LinkedHashSet<String>();
		sentinels.add("192.168.137.101:63791");
		sentinels.add("192.168.137.101:63792");
		return new JedisSentinelPool("master-1", sentinels, jedisPoolConfig);
	}

	@Test
	public void getResource() throws Exception {
		Jedis master = null;
		try {
			master = this.getJedisPool().getResource();
			System.out.println(String.format(">>> jedis = %s", master));
			System.out.println(master.getClient().getHost() + ":" + master.getClient().getPort());
			//System.out.println(jedis.info()); //看以看出此处的jedis实例指向的是master
			
			Date now = new Date();
			master.set("current_time", String.format("%tF %tT", now, now));
			master.incr("COUNT");
		} finally {
			if(master != null){
				this.getJedisPool().returnResource(master);
			}
		}
	}
	
	@Test
	public void sentinelMasters() throws Exception {
		Jedis sentinel = null;
		try {
			sentinel = new Jedis("192.168.137.101", 63791);
			System.out.println(String.format(">>> sentinel = %s", sentinel));
			List<Map<String, String>> masters = sentinel.sentinelMasters();
			for(Map<String,String> master : masters){
				System.out.println("----------------------------------------------");
				for(Map.Entry<String,String> entry : master.entrySet()){
					System.out.println(entry.getKey() + " : " + entry.getValue());
				}
			}
		} finally {
			if(sentinel != null){
				sentinel.close();
			}
		}
	}
	
	@Test
	public void sentinelSlaves() throws Exception {
		Jedis sentinel = null;
		try {
			sentinel = new Jedis("192.168.137.101", 63791);
			System.out.println(String.format(">>> sentinel = %s", sentinel));
			List<Map<String, String>> slaves = sentinel.sentinelSlaves("master-1");
			for(Map<String,String> slave : slaves){
				System.out.println("----------------------------------------------");
				for(Map.Entry<String,String> entry : slave.entrySet()){
					System.out.println(entry.getKey() + " : " + entry.getValue());
				}
			}
		} finally {
			if(sentinel != null){
				sentinel.close();
			}
		}
	}
	
	@Test
	public void sentinelPubSubListener() throws Exception {
		final String masterName = "master-1";
		final String host = "192.168.137.101";
		final int port = 63791;
		final Jedis sentinelJedis = new Jedis(host, port);
		System.out.println(String.format(">>> sentinel = %s", sentinelJedis));
		
		try {
			sentinelJedis.subscribe(new JedisPubSub() {
				public void onMessage(String channel, String message) {
					System.out.println("Sentinel " + host + ":" + port + " published: " + message);
					if("+sdown".equals(channel)){
						System.err.println("+sdown " + message);
						String[] messages = message.split(" ");
						System.out.println(Arrays.toString(messages));
						if(messages.length == 8 && "slave".equals(messages[0])){
							if(masterName.equals(messages[5])){
								String slaveIp = messages[2];
								String slavePort = messages[3];
								String masterIp = messages[6];
								String masterPort = messages[7];
								System.err.println("Found unavailable redis slave[" + slaveIp + ":" + slavePort + "] for master[" + masterName + "@" + masterIp + ":" + masterPort + "]");
							}else{
								System.err.println("Ignoring message on +sdown for master name " + messages[5] + ", but our master name is " + masterName);
							}
						}else{
							System.err.println("Invalid message received on Sentinel " + host + ":" + port + " on channel +sdown: " + message);
						}
					}
					if("-sdown".equals(channel)){
						System.err.println("-sdown " + message);
						String[] messages = message.split(" ");
						System.out.println(Arrays.toString(messages));
						if(messages.length == 8 && "slave".equals(messages[0])){
							if(masterName.equals(messages[5])){
								String slaveIp = messages[2];
								String slavePort = messages[3];
								String masterIp = messages[6];
								String masterPort = messages[7];
								System.err.println("Found available redis slave[" + slaveIp + ":" + slavePort + "] for master[" + masterName + "@" + masterIp + ":" + masterPort + "]");
							}else{
								System.err.println("Ignoring message on +sdown for master name " + messages[5] + ", but our master name is " + masterName);
							}
						}else{
							System.err.println("Invalid message received on Sentinel " + host + ":" + port + " on channel -sdown: " + message);
						}
					}
					if("+switch-master".equals(channel)){
						System.err.println("+switch-master " + message);
						String[] messages = message.split(" ");
						System.out.println(Arrays.toString(messages));
						if(messages.length == 5){
							if(masterName.equals(messages[0])){
								String oldMasterIp = messages[1];
								String oldMasterPort = messages[2];
								String newMasterIp = messages[3];
								String newMasterPort = messages[4];
								System.err.println("Switch master " + masterName + " from [" + oldMasterIp + ":" + oldMasterPort + "] to [" + newMasterIp + ":" + newMasterPort + "]");
							}else{
								System.err.println("Ignoring message on +switch-master for master name " + messages[5] + ", but our master name is " + masterName);
							}
						}else{
							System.err.println("Invalid message received on Sentinel " + host + ":" + port + " on channel +switch-master: " + message);
						}
					}
				}
			}, "+switch-master", "+sdown", "-sdown");
		} catch(JedisConnectionException e) {
			System.err.println(e);
		}
		Thread.sleep(3600);
		//sentinelJedis.close();
	}
	
	protected HostAndPort toHostAndPort(List<String> hostAndPort){
		return new HostAndPort(hostAndPort.get(0), Integer.parseInt(hostAndPort.get(1)));
	}
	
}
