package com.volmit.fulcrum.event;

import org.bukkit.event.Event;

import com.volmit.fulcrum.world.scm.IMappedVolume;
import com.volmit.fulcrum.world.scm.IVolume;

public class VolumeConstructEvent extends FulcrumEvent
{
	private IMappedVolume mappedVolume;
	private IVolume volume;
	private String volumeName;
	private Event cause;

	public VolumeConstructEvent(Event cause, IVolume volume, IMappedVolume mappedVolume, String volumeName)
	{
		this.mappedVolume = mappedVolume;
		this.cause = cause;
		this.volume = volume;
		this.volumeName = volumeName;
	}

	public IMappedVolume getMappedVolume()
	{
		return mappedVolume;
	}

	public IVolume getVolume()
	{
		return volume;
	}

	public String getVolumeName()
	{
		return volumeName;
	}

	public Event getCause()
	{
		return cause;
	}
}
