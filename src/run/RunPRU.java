package run;

import java.io.FileNotFoundException;

import algorithm.MIPERPRU;

public class RunPRU {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 0) {
			MIPERPRU oarSpan;
			try {
				oarSpan = new MIPERPRU("./data/alginput/real/traffic/1-eventSequence-timeinterval-15.txt", 2, 1, 0.01, 4, 1, 19390);
				oarSpan.runAlg();
				oarSpan.rankRuleByConfidence();
				oarSpan.writeRule2File("./data/experiments/actionable-episodes-traffic-data-1-timeinterval=15-20170831.csv");
				oarSpan.printStats("./data/experiments/performance.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			MIPERPRU oarSpan;
			try {
				oarSpan = new MIPERPRU(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]));
				oarSpan.runAlg();
//				oarSpan.rankRuleByConfidence();
				oarSpan.writeRule2File(args[7]);
//				oarSpan.printStats("./data/experiments/performance.csv");
			} catch (NumberFormatException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ogs.writeRule2File(args[7]);
		}
	}
}
