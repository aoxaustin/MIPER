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
import structures.AERule;

import structures.GNode;
import structures.ARPara;

public class MIPERDFS {
	private String inputFile;
	private long startTime;
	private long endTime;

	private List<ArrayList<GNode>> GraphList = new ArrayList<ArrayList<GNode>>();
	private Set<String> moList = new HashSet<String>();
	private ARPara parameter = null;

	private static int eventUpperBound = 50;
	private long deltaTime = 0L;
//	private long deltaT4MiningAnt = 0L;
//	private long delta2 = 0L;
//	private long deltaT3 = 0L;

	private int begin = 0;
	private int end = 0;
	private HashMap<String, Integer> eventMap = new HashMap<String, Integer>();
	private int ESize = 0;
	private Map<Integer, ArrayList<String>> ALLS = new TreeMap<Integer, ArrayList<String>>();
	private Map<Integer, ArrayList<String>> FreS = new TreeMap<Integer, ArrayList<String>>();

	private Map<String, HashSet<Integer>> candidates = new HashMap<String, HashSet<Integer>>();
	private Map<String, HashSet<Integer>> frequentEpisode = new HashMap<String, HashSet<Integer>>();

	private Set<AERule> validRules = new HashSet<AERule>();

	public MIPERDFS(String inputFile, int min_sup, int delta, double conf,
			int span, int begin, int end) throws FileNotFoundException {
		this.inputFile = inputFile;
		this.parameter = new ARPara(min_sup, delta, conf, span);
		this.begin = begin;
		this.end = end;

	}

	public void runAlg() {
		this.loadFrequentSequence(this.inputFile);
		// this.startTime = System.currentTimeMillis();
		this.algCore();
		// this.endTime = System.currentTimeMillis();
		// this.deltaTime = this.endTime - this.startTime;
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
				}
				if (eSet.size() > 0 && eSet.size() <= eventUpperBound) {
					this.ALLS.put(timestamp, eSet);
					this.FreS.put(timestamp, eSet2);
					this.begin = (timestamp > this.begin) ? this.begin
							: timestamp;
					this.end = (timestamp < this.end) ? this.end : timestamp;
				}
				timestamp++;
			}
			this.FreS = RepairSequence(this.FreS,
					this.parameter.getMin_support(), eventSet);
			reader.close();
			int offset = 0;
			for (Entry<String, Integer> entry : eventSet.entrySet()) {
				if (entry.getValue() >= this.parameter.getMin_support()) {
					this.ESize++;
					this.eventMap.put(entry.getKey(), offset++);
				}
			}
			System.out.println("Number of Frequent Events: " + this.ESize);
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
			ExecutorService es = Executors.newFixedThreadPool(3);
			this.startTime = System.currentTimeMillis();
//			long start1 = System.currentTimeMillis();
			for (int i = this.begin; i <= this.end; i++) {

				// if (i % 1000 == 0) {
				// System.out.println("time stamp: " + i);
				// }
				ArrayList<String> EkplusOne = this.FreS.get(i);
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
							updateCandidateSet(this.moList, i);
						}
						EkplusOne = this.FreS.remove(i);
						EkplusOne = null;
					}
				}
				cutGraphList(i);
			}
			getFrequent();
//			long end1 = System.currentTimeMillis();
//			this.deltaT4MiningAnt = end1 - start1;
			// end of mining antecedent
			// begin to mine rule
			System.out.println("Mining frequent antecedent is over.");
			System.out.println("Number of frequent episodes: "
					+ this.frequentEpisode.size());
			for (Entry<String, HashSet<Integer>> antecedent : this.frequentEpisode
					.entrySet()) {

				DFSpan(antecedent.getKey(), antecedent.getValue(),
						new ArrayList<Integer>(), this.parameter.getEpsilon(),
						this.parameter.getMin_confidence());

			}

			this.endTime = System.currentTimeMillis();
			this.deltaTime += this.endTime - this.startTime;

			es.shutdown();
			es.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
			System.out.println("Number of Rules: " + this.validRules.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	private void DFSpan(String prefix, HashSet<Integer> plist,
			ArrayList<Integer> prefixSpan, int rightmost, double min_confidence) {
		// TODO Auto-generated method stub
		String antecedent = null;
		boolean isRoot = true;
		if (prefix.indexOf("->") == -1) {
			antecedent = prefix;
		} else {
			antecedent = prefix.substring(0, prefix.indexOf("->"));
			isRoot = false;
		}

		for (int offset = 1; offset <= rightmost; offset++) {
			Map<String, HashSet<Integer>> tmp = new HashMap<String, HashSet<Integer>>();
			for (Integer timestamp : plist) {
				int targetTimestamp = timestamp + offset;
				ArrayList<String> eventSet = this.ALLS.get(targetTimestamp);
				if (eventSet != null) {
					for (String event : eventSet) {
						if (tmp.containsKey(event)) {
							tmp.get(event).add(targetTimestamp);
						} else {
							HashSet<Integer> newTimeList = new HashSet<Integer>();
							newTimeList.add(targetTimestamp);
							tmp.put(event, newTimeList);
						}
					}
				}
			}
			if (tmp.size() > 0) {
				double bound = this.frequentEpisode.get(antecedent).size()
						* min_confidence;
				for (Entry<String, HashSet<Integer>> entry : tmp.entrySet()) {
					Integer support = entry.getValue().size();
					if (support >= bound) {
						// can get a new significant rule
						ArrayList<Integer> spans = new ArrayList<Integer>();
						String newRule = "";
						Integer upperbound = -1;
						if (isRoot) {
							newRule = antecedent + "->" + entry.getKey();
							spans.add(offset);
							upperbound = this.parameter.getEpsilon() - offset;
						} else {
							newRule = prefix + "@" + entry.getKey();
							spans.addAll(prefixSpan);
							spans.add(offset);
							int distance2Root = sum(spans);
							upperbound = this.parameter.getEpsilon() - distance2Root;

						}
						Double confidence = support.doubleValue()
								/ (double) this.frequentEpisode.get(antecedent)
										.size();
						AERule rule = new AERule(newRule, spans, support,
								confidence);
						this.validRules.add(rule);

						this.DFSpan(newRule, entry.getValue(), spans,
								upperbound, min_confidence);
					}
				}
			}
		}
	}

	private int sum(ArrayList<Integer> spans) {
		// TODO Auto-generated method stub
		int ret = 0;
		for (Integer dis : spans) {
			ret += dis;
		}
		return ret;
	}

	private void getFrequent() {
		// TODO Auto-generated method stub
		for (Entry<String, HashSet<Integer>> entry : this.candidates.entrySet()) {
			String episode = entry.getKey();
			HashSet<Integer> tmlist = entry.getValue();
			if (tmlist.size() >= this.parameter.getMin_support()) {
				this.frequentEpisode.put(episode, tmlist);
			}
		}
	}

	private void updateCandidateSet(Set<String> moList2, int endtime) {
		// TODO Auto-generated method stub
		for (String mo : moList2) {
			if (this.candidates.containsKey(mo)) {
				this.candidates.get(mo).add(endtime);
			} else {
				HashSet<Integer> tmp = new HashSet<Integer>();
				tmp.add(endtime);
				this.candidates.put(mo, tmp);
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

			// String outString = String.valueOf("BAR-Span\n" + deltaTime + "\t"
			// + this.deltaT4MiningAnt + "\t" + this.delta2 + "\t"
			// + this.deltaT3);
			// writer.write(outString + "\n");
			String outString = String.valueOf(deltaTime);
			writer.write(outString + ",");
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeRule2File(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename)));
			bw.write("Rule|Spans|Support|Confidence\n");
			for (AERule r : this.validRules) {

//				if (r.getConfidence() < 0.6) {
					bw.write(r.getName() + "|" + r.getSpans().toString() + "|"
							+ r.getSupport() + "|" + r.getConfidence() + "\n");
//				}

			}
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
