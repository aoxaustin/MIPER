package run;

import java.io.FileNotFoundException;

import algorithm.MIPERBFS;


public class RunBFS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 0) {
			MIPERBFS barSpan;
			try {
				barSpan = new MIPERBFS("./data/alginput/synthetic/toy2sequence.dat", 2, 5, 0.5, 7, 1, 12);
				barSpan.runAlg();
				barSpan.writeRule2File("./data/experiments/rules-barspan.txt");
				barSpan.printStats("./data/experiments/performance.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			MIPERBFS barSpan;
			try {
				barSpan = new MIPERBFS(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]));
				barSpan.runAlg();
				barSpan.writeRule2File(args[7]);
//				barSpan.printStats("./data/experiments/performance.csv");
			} catch (NumberFormatException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ogs.writeRule2File(args[7]);
		}
	}
}
