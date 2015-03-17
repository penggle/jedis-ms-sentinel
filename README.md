jedis-ms-sentinel
=================

This is a Redis Master-Slave system architecture based jedis client.
It can provide master-slave redundancy、failover by redis-sentinel、sharding and so on.

1、Master-Slaves no sharding
	
	
![image](https://raw.githubusercontent.com/penggle/jedis-ms-sentinel/master/architecture/MasterSlaveSetinelPool.jpg)

	Set<String> sentinels = new LinkedHashSet<String>();
	sentinels.add("192.168.137.101:63791");
	sentinels.add("192.168.137.101:63792");
	Pool<MasterSlaveJedis> masterSlaveJedisPool = new MasterSlaveJedisSentinelPool("master-1", sentinels,jedisPoolConfig);

	MasterSlaveJedis masterSlaveJedis = masterSlaveJedisPool.getResource();
	//>>> masterSlaveJedis = MasterSlaveJedis {master=192.168.137.101:6379, slaves=[192.168.137.101:6380, 192.168.137.101:6381]}
	System.out.println(">>> masterSlaveJedis = " + masterSlaveJedis);
	
	masterSlaveJedis.set("nowTime", "2015-03-16 15:34:55"); // The underlying actually call the master.set("nowTime", "2015-03-16 15:34:55");
	
	LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
	
	String slaveHolder1 = "myslave1";
	Jedis slave1 = masterSlaveJedis.opsForSlave(slaveHolder); // if no any slave found, opsForSlave() will return master as a slave to be use
	System.out.println(">>> nowTime = " + slave1.get("nowTime")); //>>> nowTime = 2015-03-16 15:34:55

	String slaveHolder2 = "myslave1";
	Jedis slave2 = masterSlaveJedis.opsForSlave(slaveHolder);
	System.out.println(">>> nowTime = " + slave2.get("nowTime")); //>>> nowTime = 2015-03-16 15:34:55

	System.out.println(slave1.equals(slave2)); // must be true if slaveHolder1 equals slaveHolder2

	masterSlaveJedisPool.returnResource(masterSlaveJedis);

2、Master-Slaves with sharding
	
![image](https://raw.githubusercontent.com/penggle/jedis-ms-sentinel/master/architecture/ShardedMasterSlaveSetinelPool.jpg)

	master-1 : master=192.168.137.101:6379 slaves=[192.168.137.101:6380, 192.168.137.101:6381]
	master-2 : master=192.168.137.101:6382 slaves=[192.168.137.101:6383, 192.168.137.101:6384]

	Set<String> masterNames = new LinkedHashSet<String>();
	masterNames.add("master-1");
	masterNames.add("master-2");
	Set<String> sentinels = new LinkedHashSet<String>();
	sentinels.add("192.168.137.101:63791");
	sentinels.add("192.168.137.101:63792");
	Pool<ShardedMasterSlaveJedis> shardedMasterSlaveJedisPool = new ShardedMasterSlaveJedisSentinelPool(masterNames, sentinels,jedisPoolConfig);

	ShardedMasterSlaveJedis shardedMasterSlaveJedis = shardedMasterSlaveJedisPool.getResource();
	for(int i = 0; i < 10; i++){
		String key = "shard-" + i;
		shardedMasterSlaveJedis.set(key, String.valueOf(i));
		// sharded in master-1[192.168.137.101:6379] for keys : shard-0, shard-2, shard-6, shard-8, shard-9, and sharded in master-2[192.168.137.101:6382] for keys : shard-1, shard-3, shard-4, shard-5, shard-7
		System.out.println(key + " = " + shardedMasterSlaveJedis.get(key));
		
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
		System.out.println(key + " = " + shardedMasterSlaveJedis.getShard(key).opsForSlave().get(key)); // Get from one master group's one slave
	}
	
	shardedMasterSlaveJedisPool.returnResource(shardedMasterSlaveJedis);
