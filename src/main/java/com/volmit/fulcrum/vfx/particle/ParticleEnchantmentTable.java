package com.volmit.fulcrum.vfx.particle;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.fulcrum.bukkit.ParticleEffect;
import com.volmit.fulcrum.vfx.DirectionalEffect;
import com.volmit.fulcrum.vfx.ParticleBase;
import com.volmit.fulcrum.vfx.SpreadEffect;

public class ParticleEnchantmentTable extends ParticleBase implements SpreadEffect, DirectionalEffect
{
	private double spread;
	private Vector direction;

	public ParticleEnchantmentTable()
	{
		this.spread = 0;
		direction = new Vector(0, 0, 0);
	}

	@Override
	public ParticleEnchantmentTable setSpread(double s)
	{
		this.spread = s;
		return this;
	}

	@Override
	public double getSpread()
	{
		return spread;
	}

	@Override
	public void play(Location l, double range)
	{
		ParticleEffect.ENCHANTMENT_TABLE.display(getDirection(), (float) getSpread(), l, range);
	}

	@Override
	public void play(Location l, Player p)
	{
		ParticleEffect.ENCHANTMENT_TABLE.display(getDirection(), (float) getSpread(), l, p);
	}

	@Override
	public ParticleEnchantmentTable setDirection(Vector v)
	{
		this.direction = v;
		return this;
	}

	@Override
	public Vector getDirection()
	{
		return direction;
	}
}
