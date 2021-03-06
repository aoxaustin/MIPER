package run;

import java.io.FileNotFoundException;

import algorithm.MIPERDFS;


public class RunDFS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 0) {
			MIPERDFS darSpan;
			try {
				darSpan = new MIPERDFS("./data/alginput/synthetic/toy2sequence.dat", 2, 4, 0.6, 7, 1, 12);
				darSpan.runAlg();
				darSpan.writeRule2File("./data/experiments/rules-darspan.txt");
				darSpan.printStats("./data/experiments/performance.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			MIPERDFS darSpan;
			try {
				darSpan = new MIPERDFS(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]));
				darSpan.runAlg();
//				barSpan.writeRule2File("./data/experiments/rules-barspan.txt");
				darSpan.printStats("./data/experiments/performance.csv");
			} catch (NumberFormatException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ogs.writeRule2File(args[7]);
		}
	}
}
