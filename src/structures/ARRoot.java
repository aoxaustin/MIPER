package structures;

import java.util.HashSet;

public class ARRoot extends ARNode{
	private String episode;
	private HashSet<Integer> tmlist; //time list
	
	public ARRoot(String episode, HashSet<Integer> timeList){
		this.episode = episode;
		this.tmlist = timeList;
	}

	public String getEpisode() {
		return episode;
	}

	public void setEpisode(String episode) {
		this.episode = episode;
	}

	public HashSet<Integer> getTmlist() {
		return tmlist;
	}

	public void setTmlist(HashSet<Integer> tmlist) {
		this.tmlist = tmlist;
	}
	
	
}
