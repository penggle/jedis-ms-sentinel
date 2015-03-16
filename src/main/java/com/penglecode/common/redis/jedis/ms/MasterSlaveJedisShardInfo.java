package com.penglecode.common.redis.jedis.ms;

import java.util.List;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.util.Hashing;
import redis.clients.util.ShardInfo;
import redis.clients.util.Sharded;

public class MasterSlaveJedisShardInfo extends ShardInfo<MasterSlaveJedis> {

	private final String masterName;
	
	private final JedisShardInfo masterShard;
	
	private final List<JedisShardInfo> slaveShards;
	
	private final String name;
	
	public MasterSlaveJedisShardInfo(String masterName, JedisShardInfo masterShard, List<JedisShardInfo> slaveShards) {
		this(masterName, masterShard, slaveShards, Sharded.DEFAULT_WEIGHT);
	}
	
	public MasterSlaveJedisShardInfo(String masterName, JedisShardInfo masterShard, List<JedisShardInfo> slaveShards, int weight) {
		this(masterName, masterShard, slaveShards, Sharded.DEFAULT_WEIGHT, null);
	}
	
	public MasterSlaveJedisShardInfo(String masterName, JedisShardInfo masterShard, List<JedisShardInfo> slaveShards, int weight, String name) {
		super(weight);
		this.masterName = masterName;
		this.masterShard = masterShard;
		this.slaveShards = slaveShards;
		this.name = name;
	}

	protected MasterSlaveJedis createResource() {
		return new MasterSlaveJedis(masterShard, slaveShards, Hashing.MURMUR_HASH, null);
	}

	public String getName() {
		return name;
	}

	public String getMasterName() {
		return masterName;
	}

	public JedisShardInfo getMasterShard() {
		return masterShard;
	}

	public List<JedisShardInfo> getSlaveShards() {
		return slaveShards;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{masterName=" + masterName + ", master=" + masterShard.getHost() + ":" + masterShard.getPort() + ", slaves=");
		sb.append("[");
		for(int i = 0, len = slaveShards.size(); i < len; i++){
			sb.append(slaveShards.get(i).getHost() + ":" + slaveShards.get(i).getPort());
			if(i != len - 1){
				sb.append(", ");
			}
		}
		sb.append("]}");
		return sb.toString();
	}

}
