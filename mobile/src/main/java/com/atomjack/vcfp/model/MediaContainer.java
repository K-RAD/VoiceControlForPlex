package com.atomjack.vcfp.model;

import com.atomjack.shared.model.Timeline;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(strict=false)
public class MediaContainer {
	@Attribute(required=false)
	public String machineIdentifier;
	
	@Attribute(required=false)
	public String friendlyName;
	
	@Attribute(required=false)
	public String title1;
	
	@Attribute(required=false)
	public String grandparentTitle;
	
	@ElementList(required=false, inline=true, name="Server")
	public List<PlexClient> clients = new ArrayList<PlexClient>();
	
	@ElementList(required=false, inline=true, entry="Directory")
	public List<PlexDirectory> directories = new ArrayList<PlexDirectory>();
	
	@ElementList(required=false, inline=true, entry="Video")
	public List<PlexVideo> videos = new ArrayList<PlexVideo>();
	
	@ElementList(required=false, inline=true, entry="Track")
	public List<PlexTrack> tracks = new ArrayList<PlexTrack>();

	@ElementList(required=false, inline=true, entry="Device")
	public List<Device> devices = new ArrayList<Device>();

	@Attribute(required=false)
	public String token;

	@Attribute(required=false)
	public String location;

	@Attribute(required=false)
	public String commandID;

	@ElementList(required=false, inline=true, entry="Timeline")
	public List<Timeline> timelines;

  @Attribute(required=false)
  public String art;

  @Attribute(required=false)
  public String playQueueID;

	public Timeline getTimeline(String type) {
		if(timelines != null) {
			for (Timeline t : timelines) {
				if (t.type.equals(type))
					return t;
			}
		}
		return null;
	}

  public Timeline getActiveTimeline() {
    for(Timeline t : timelines) {
      if(t.state != null && !t.state.equals("stopped"))
        return t;
    }
    return null;
  }
}
