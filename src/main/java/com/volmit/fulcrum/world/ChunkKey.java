package com.volmit.fulcrum.world;

import org.bukkit.Chunk;

public class ChunkKey
{
	private String world;
	private int x;
	private int z;

	public ChunkKey(Chunk c)
	{
		this(c.getWorld().getName(), c.getX(), c.getZ());
	}

	public ChunkKey(String world, int x, int z)
	{
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public String getWorld()
	{
		return world;
	}

	public void setWorld(String world)
	{
		this.world = world;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z)
	{
		this.z = z;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((world == null) ? 0 : world.hashCode());
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		ChunkKey other = (ChunkKey) obj;
		if(world == null)
		{
			if(other.world != null)
			{
				return false;
			}
		}
		else if(!world.equals(other.world))
		{
			return false;
		}
		if(x != other.x)
		{
			return false;
		}
		if(z != other.z)
		{
			return false;
		}
		return true;
	}

}
