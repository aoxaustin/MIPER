package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import structures.GNode;
import structures.AERuleParas;

public class MINELFS {
	private String inputFile;
	private long startTime;
	private long endTime;

	private List<ArrayList<GNode>> GraphList = new ArrayList<ArrayList<GNode>>();
	private Set<String> moList = new HashSet<String>();
	private AERuleParas parameter = null;

	private static int eventUpperBound = 50;
	private long deltaTime = 0L;

	private int begin = 0;
	private int end = 0;
	private HashMap<String, Integer> eventMap = new HashMap<String, Integer>();
	private int ESize = 0;
	private Map<Integer, ArrayList<String>> ALLS = new TreeMap<Integer, ArrayList<String>>();

	private Map<String, Integer> candidates = new HashMap<String, Integer>();
	private Map<String, Integer> frequentEpisode = new HashMap<String, Integer>();

	public MINELFS(String inputFile, int min_sup, int delta, double conf,
			int span, int begin, int end) throws FileNotFoundException {
		this.inputFile = inputFile;
		this.parameter = new AERuleParas(min_sup, delta, conf, span);
		this.begin = begin;
		this.end = end;

	}

	public void runAlg() {
		this.loadFrequentSequence(this.inputFile);
		this.algCore();
		System.err.println("Execution time: " + deltaTime);
	}

	private void loadFrequentSequence(String input) {
		// TODO Auto-generated method stub
		try {
			TreeMap<String, Integer> eventSet = new TreeMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(input), "UTF-8"));
			String line = null;
			int timestamp = 1;

			while ((line = reader.readLine()) != null) {
				String[] array = StringUtils.split(line.trim(), ' ');
				ArrayList<String> eSet = new ArrayList<String>();
				ArrayList<String> eSet2 = new ArrayList<String>();
				for (String event : array) {
					eSet.add(event);
					eSet2.add(event);
					if (eventSet.containsKey(event)) {
						eventSet.put(event, eventSet.get(event) + 1);
					} else {
						eventSet.put(event, 1);
					}
					if (eSet.size() <= eventUpperBound) {
						this.ALLS.put(timestamp, eSet);
						this.begin = (timestamp > this.begin) ? this.begin
								: timestamp;
						this.end = (timestamp < this.end) ? this.end
								: timestamp;
					}
				}
				timestamp++;
			}
			this.ALLS = RepairSequence(this.ALLS,
					this.parameter.getMin_support(), eventSet);
			reader.close();
			int offset = 0;
			for (Entry<String, Integer> entry : eventSet.entrySet()) {
				if (entry.getValue() >= this.parameter.getMin_support()) {
					this.eventMap.put(entry.getKey(), offset++);
				}
			}
			this.ESize = this.eventMap.size();
			System.out.println("Frequent Event:" + this.ESize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<Integer, ArrayList<String>> RepairSequence(
			Map<Integer, ArrayList<String>> S, double bound,
			TreeMap<String, Integer> eventSet) {
		// TODO Auto-generated method stub
		ArrayList<String> delList = new ArrayList<String>();
		for (Entry<Integer, ArrayList<String>> entry : S.entrySet()) {
			ArrayList<String> eSet = entry.getValue();
			for (String e : eSet) {
				int support = eventSet.get(e);
				if (support < bound) {
					delList.add(e);
				}
			}
			if (delList.size() > 0) {
				eSet.removeAll(delList);
				delList.clear();
			}
			S.put(entry.getKey(), eSet);
		}
		return S;
	}

	public void algCore() {
		try {
			ExecutorService es = Executors.newFixedThreadPool(5);
			this.startTime = System.currentTimeMillis();
			for (int i = this.begin; i <= this.end; i++) {
//				if (i % 10000 == 0) {
//					System.out.println("time stamp: " + i);
//				}
				ArrayList<String> EkplusOne = this.ALLS.get(i);
				if (EkplusOne != null) {
					if (EkplusOne.size() > 0) {
						final Set<String> tmpS = this.moList;
						this.moList = new HashSet<String>();
						es.submit(new Runnable() {
							@Override
							public void run() {
								tmpS.clear();
							}
						});

						ArrayList<GNode> gkplusOne = BuildGraph(EkplusOne, i);
						if (gkplusOne != null)
							this.GraphList.add(gkplusOne);
						if (this.GraphList.size() > 1) {
							UpdateGraphs(this.GraphList, EkplusOne, i);
						}
						if (this.moList.size() > 0) {
							// updateFPTree(this.moList);
							updateCandidateSet(this.moList);
						}
						EkplusOne = this.ALLS.remove(i);
						EkplusOne = null;
					}
				}
				cutGraphList(i);
			}
			getFrequent();
			this.endTime = System.currentTimeMillis();
			this.deltaTime += this.endTime - this.startTime;
			es.shutdown();
			es.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
			// System.out.println("Number of Rules: " + this.validRules.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	private void getFrequent() {
		// TODO Auto-generated method stub

		for (Entry<String, Integer> entry : this.candidates.entrySet()) {
			String episode = entry.getKey();
			Integer support = entry.getValue();
			if (support >= this.parameter.getMin_support()) {
				this.frequentEpisode.put(episode, support);
			}
		}
	}

	private void updateCandidateSet(Set<String> moList2) {
		// TODO Auto-generated method stub
		for (String mo : moList2) {
			if (this.candidates.containsKey(mo)) {
				int count = this.candidates.get(mo) + 1;
				this.candidates.put(mo, count);
			} else {
				this.candidates.put(mo, 1);
			}
		}
	}

	private void cutGraphList(int timestamp) {
		// TODO Auto-generated method stub
		ArrayList<ArrayList<GNode>> delList = new ArrayList<ArrayList<GNode>>();
		for (ArrayList<GNode> g : this.GraphList) {
			if (timestamp - g.get(0).getTe() >= this.parameter.getDelta() - 1) {
				delList.add(g);
			} else {
				break;
			}
		}
		this.GraphList.removeAll(delList);
		// delList = null;
	}

	private void UpdateGraphs(List<ArrayList<GNode>> graphList2,
			ArrayList<String> ekplusOne, int timestamp) {
		// TODO Auto-generated method stub
		if (ekplusOne.size() > 0) {
			ArrayList<GNode> leafNodes = new ArrayList<GNode>();
			for (int i = graphList2.size() - 2; i >= 0; i--) {//
				// vertex set
				ArrayList<GNode> gnSet = graphList2.get(i);
				if (gnSet.size() == 0)
					continue;
				leafNodes.clear();

				for (String event : ekplusOne) {
					GNode gn = new GNode(event, timestamp, this.ESize);
					leafNodes.add(gn);
				}
				for (GNode prefix : gnSet) {
					Set<String> lmoSet = prefix.getLmoSet();
					for (GNode gn : leafNodes) {
						String event = gn.getName();
						if (!prefix.isDead()) {
							if (prefix.getChildren()[this.eventMap.get(event)] == false) {
								prefix.getChildren()[this.eventMap.get(event)] = true;
								event = "@" + event;
								String oc = "";
								for (String lmo : lmoSet) {
									oc = lmo + event;
									gn.getLmoSet().add(oc);
									this.moList.add(oc);
								}
							}
						}
					}
					lmoSet.removeAll(this.moList);
					if (prefix.getLmoSet().size() == 0) {
						prefix.setDead(true);
					}
				}
				gnSet.addAll(leafNodes);
			}
			leafNodes = null;
		}
	}

	private ArrayList<GNode> BuildGraph(ArrayList<String> ekplusOne,
			int timestamp) {
		// TODO Auto-generated method stub
		ArrayList<GNode> g = new ArrayList<GNode>();
		if (ekplusOne.size() == 0)
			return g;
		for (String e : ekplusOne) {
			GNode node = new GNode(e, timestamp, this.ESize);
			node.getLmoSet().add(e);
			g.add(node);
			this.moList.add(e);
		}
		return g;
	}

	public void printStats(String filename) {
		// TODO Auto-generated method stub
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename, true), "UTF-8"));
			String outString = String.valueOf(deltaTime);
			writer.write(outString + "\n");
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write2File(String filename){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
			
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
