package com.volmit.fulcrum.sfx;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.volmit.fulcrum.lang.GList;

public interface Audible extends Cloneable
{
	public Audible setDelay(int ticks);

	public int getDelay();

	public Audible d(int ticks);

	public void play(Location l, float v, float p);

	public void play(Player l, float v, float p);

	public void play(Location l);

	public void play(Player l);

	public void scalePitch(float p);

	public void scaleVolume(float p);

	public void play(Player l, Location pos);

	public Audible setVolume(float v);

	public Audible setPitch(float p);

	public Audible v(float v);

	public Audible p(float p);

	public Audible c(SoundCategory c);

	public Audible s(Sound s);

	public Audible vp(float v, float p);

	public float getVolume();

	public float getPitch();

	public GList<Audible> getChildren();

	public Audible addChild(Audible a);

	public SoundCategory getCategory();

	public Audible setCategory(SoundCategory c);

	public Sound getSound();

	public Audible setSound(Sound s);

	public Audible addChildren(GList<Audible> a);

	public boolean hasDelay();
}
