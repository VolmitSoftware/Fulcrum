package com.volmit.fulcrum.vfx.particle;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.fulcrum.bukkit.ParticleEffect;
import com.volmit.fulcrum.vfx.DirectionalEffect;
import com.volmit.fulcrum.vfx.MotionEffect;
import com.volmit.fulcrum.vfx.ParticleBase;

public class ParticleWaterBubble extends ParticleBase implements MotionEffect, DirectionalEffect
{
	private double speed;
	private Vector direction;

	public ParticleWaterBubble()
	{
		this.speed = 0;
		direction = new Vector();
	}

	@Override
	public ParticleWaterBubble setSpeed(double s)
	{
		this.speed = s;
		return this;
	}

	@Override
	public double getSpeed()
	{
		return speed;
	}

	@Override
	public ParticleWaterBubble setDirection(Vector v)
	{
		this.direction = v;
		return this;
	}

	@Override
	public Vector getDirection()
	{
		return direction;
	}

	@Override
	public void play(Location l, double range)
	{
		ParticleEffect.WATER_BUBBLE.display(getDirection(), (float) getSpeed(), l, range);
	}

	@Override
	public void play(Location l, Player p)
	{
		ParticleEffect.WATER_BUBBLE.display(getDirection(), (float) getSpeed(), l, p);
	}
}
