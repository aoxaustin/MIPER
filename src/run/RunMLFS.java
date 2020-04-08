package run;

import java.io.FileNotFoundException;

import algorithm.MINELFS;


public class RunMLFS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 0) {
			MINELFS moop;
			try {
				moop = new MINELFS("./data/alginput/synthetic/toy2sequence.dat", 2, 4, 0.6, 6, 1, 12);
				moop.runAlg();
				moop.printStats("./data/experiments/performance.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			MINELFS moop;
			try {
				moop = new MINELFS(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]));
				moop.runAlg();
//				oarSpan.writeRule2File("./data/experiments/rules-oarspan.txt");
				moop.printStats("./data/experiments/performance-batch.csv");
			} catch (NumberFormatException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ogs.writeRule2File(args[7]);
		}
	}
}
