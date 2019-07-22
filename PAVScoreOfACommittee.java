import java.io.*;  
import java.util.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;
public class PAVScoreOfACommittee {  

	//**variables for analysis
	static int votersNotVoting = 0; //the number of users that are not voting 
	static double coinsNotVoting = 0.0; //the number of coins who are not voting
	static int votersVoting = 0; //the number of users who are voting
	static double coinsVoting = 0.0; //the number of coins who are voting
	static int proxyVotersVoting = 0; //the number of users who are voting via a proxy
	static double proxyCoinsVoting = 0.0; //the number of coins who are voting via a proxy
	static int[] S = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 25, 21}; //the index of block producers elected at this time
	static int k = 21; //the number of block producers in the elected committee
	//ELECTORATE INCLUSION THRESHOLD VARIABLES:
	static double coinThreshold = 0.0; //a voter must have at greater than this many coins before he is counted as a valid member of the electorate
	static int numberOfBPsVotedForThreshold = 0; //a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate 
	static double coinsBelowThreshold = 0.0; //this is the running total of coins below the threshold
	static int votersBelowThreshold = 0; //this is the running total of voters below the threshold
	

	//**EXTENDED JUSTIFIED REPRESENTATION GLOBAL VARIABLES:
	
	static ArrayList<Candidate> C = new ArrayList<Candidate>(); //the candidates
	static HashMap<String,Integer> CandidateIds = new HashMap<String,Integer>(); //mapping candidate names to their index. This is required for the graph.

	static ArrayList<String[]> A = new ArrayList<String[]>(); //the ballots of these voters
	static ArrayList<Double> b = new ArrayList<Double>(); //the budget of these voters
	static ArrayList<Integer> r = new ArrayList<Integer>(); //the number of representatives in the selected committee
	//for decimal truncating to 4 decimal spaces
	static DecimalFormat df = new DecimalFormat("#.####");

	static class Candidate {
		boolean inS = false; //is the candidate currently in S?
		String name = ""; //the name of the candidate 
		double w = 0.0; //the total vote weight assigned to this candidate
		ArrayList<Integer> E = new ArrayList<Integer>(); //edges to voters who voted for this candidate.
	}


	//**LS-PAV variables
	static double PavSc = 0; //This is the current PAV-score of the committee

	
//args[0] is the voterlist, args[1] is the BPlist
    public static void main(String[] args) {  
  
        try {  
		whichBlockProducersWereElected(args[1]); //now scan the file that lists the block producers candidates for this election
        	csvReaderByLine(args[0]); //scan the voter list
		printResults(args[0]); //print the results into an analysed file
		//summerise results		
		System.out.println("\n");
        } catch (Exception e) {  
            // if any error occurs  
            e.printStackTrace();  
        }  
    } 

/**
*
* This function automates the search for the 21 elected block producers
*/
public static void whichBlockProducersWereElected(String BPfilename){

	//adjust the file name to csv type
	String csvFile = BPfilename + ".csv";
	int BPcount = 0;
        String line = "";
	String toSplit = ",";
	BufferedReader br = null;
        
	try{
		br = new BufferedReader(new FileReader(csvFile)); //read the file
		String[] BPRow = new String[0]; //we will be spliting each row into sections
		Integer register;

		//for each BP candidate elected:
	    	while ((BPcount < k)&&((line = br.readLine()) != null)) {
			BPRow = line.split(toSplit);
			System.out.println("" + BPRow[0] + " (ID)");
		    	System.out.println("" + BPRow[1] + " (NAME)");
		    	System.out.println("" + BPRow[2] + " (VOTER NUMBER)");
		    	System.out.println("" + BPRow[5] + " (COIN NUMBER)");		
			//The BP gets assigned its rank as an ID
			Candidate newElectedCandidate = new Candidate();
			newElectedCandidate.name = BPRow[1];
			newElectedCandidate.inS = true;
			C.add(newElectedCandidate);
			CandidateIds.put(newElectedCandidate.name,C.size()-1);
			//next BP
			BPcount++;
		}
			System.out.println("All elected BPs have been added.");
		//for each BPcandidate not elected
		while((line = br.readLine()) != null){
			BPRow = line.split(toSplit);
			System.out.println(BPRow[0] + " (ID)");
		    	System.out.println(BPRow[1] + " (NAME)");
		    	System.out.println(BPRow[2] + " (VOTER NUMBER)");
		    	System.out.println(BPRow[5] + " (COIN NUMBER)");		
			//Again the BP gets assigned its rank as an ID
			Candidate newUnelectedCandidate = new Candidate();
			newUnelectedCandidate.name = BPRow[1];
			newUnelectedCandidate.inS = false;
			C.add(newUnelectedCandidate);
			CandidateIds.put(newUnelectedCandidate.name,C.size()-1);
			BPcount++;
		}


 	} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

} 

/**
*
* This function searches all voters to see who they have voted for and how satisfied they are by the elected block producers
*/
public static void csvReaderByLine(String filename){

	//mades sure file is csv
	String csvFile = filename + ".csv";
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",\"";
	String toSplit = ",";
	Integer rowNumber = 0;
	//info we need:
	Double coins;
	String BPsVotedFor = "";
	String proxy = "";

        try {

		//start the reading of the file
            br = new BufferedReader(new FileReader(csvFile));
		rowNumber++;
		
		//first line will be the titles so skip
	    String firstLine = br.readLine();
		rowNumber++;
	   //voter_name, staked, producers, proxy (is what the first line should say)
	    System.out.println("The firstLine is \n" + firstLine);

	   //continue the following loop for every voter
	    while ((line = br.readLine()) != null) {
		//reset the variables we are looking for, for this particular voter		
		BPsVotedFor = ""; 
		proxy = "";
		//increase the row count
		rowNumber++;
		//printouts to show the current row		
		System.out.println("Analysing rowNumber= " + rowNumber);
		//System.out.println("line is= " + line);
                //split row by separator
		String[] row = line.split(csvSplitBy);
		//find the number of coins this voter has
		coins = Double.valueOf(row[0].split(toSplit)[1].trim());
		System.out.println("coins: " + coins);
		try{
			//find the number of block producers this voter voted for
			//this is in a try/catch as a voter may have voted for 0 block producers
			BPsVotedFor = row[1].split("\",")[0];
		}catch (ArrayIndexOutOfBoundsException e){BPsVotedFor = "";}
		System.out.println("BPsVotedFor.length: " + BPsVotedFor.split(",").length);		
		try{
			//find the proxy this voter is using
			//this is in a try/catch as a voter may not have used a proxy
			proxy = row[1].split("\",")[1];
			System.out.println("proxy: " + proxy);
		}catch (ArrayIndexOutOfBoundsException e){}
		if (BPsVotedFor.equals("")){
			//the voter has made no vote, so record this:
			votersNotVoting++;
			coinsNotVoting = coinsNotVoting + coins;
			System.out.println("!!NOT VOTING!!");

		} else if ((BPsVotedFor.split(",").length <= numberOfBPsVotedForThreshold)||(coins <= coinThreshold)) {
			//voter is under the threshold and so eliminated from the electorate, so record this:
			votersBelowThreshold++;
			coinsBelowThreshold = coinsBelowThreshold + coins;
			System.out.println("!!UNDERTHRESHOLD!!");

		} else {
			coinsVoting = coinsVoting + coins;
			//lets see if the voter voted through a proxy...
	    		if (proxy.length() > 1){
				//Yes!
				System.out.println("!!PROXYING!!");
				proxyVotersVoting++;
				proxyCoinsVoting = proxyCoinsVoting + coins;
			} else {
				//No!
				System.out.println("!!DIRECTLY_VOTING!!");
			}		
			//ok so we have a voter casting a ballot. Lets find out calculate how satisfied this voter is with the elected block producers:
			int representatives = checkHowManyBPsRepresentVoter(BPsVotedFor, coins,votersVoting);
			//lets correct the PAV score
			//df.setRoundingMode(RoundingMode.FLOOR);
			double toAddPav = 0.0;
			for (int q = 1; q <= representatives; q++){
				toAddPav = toAddPav + (((1.0)/q) * coins);
			}
			PavSc = PavSc + toAddPav;
			A.add(BPsVotedFor.split(","));
			b.add(coins);
			r.add(representatives);
			//record the voter as voting:
			votersVoting++;
		
		}
		System.out.println();	

            }

	System.out.println("First PAV: " + PavSc);
	try {
		Thread.sleep(3000);
	} catch (Exception e){
	}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
 

/**
*
* This function is called by the previous one, specifically looking at a single voter and seeing how satisified that single voter is with the elected block producers
*/
public static int checkHowManyBPsRepresentVoter(String BPsVotedFor, double coins,int voterID){

	int count = 0;
	int countBP = 0;
	String toSplitBPs = ",";
	//get the individual BPs voted for
	String[] BPsVoted = BPsVotedFor.split(toSplitBPs);
	int howSatisfiedIsThisVoter = 0;
	Integer id;
	//loop through the block producers voted for...
	while (count < BPsVoted.length){
		countBP = 0;
		id = CandidateIds.get(BPsVoted[count]);
		if (id == null){
			//voters can vote for old block producer candidates, so this if statement filters old candidates out
		} else {
			//see if each voted for block producer has been elected...
			while (countBP < S.length){
				if (id == S[countBP]){
					howSatisfiedIsThisVoter++; //if it has then increase the satisfication of this voter
					//System.out.println("Voter satisfied with candidate = " + BPsVoted[count]);
					countBP = S.length;
				}
				countBP++;
			}
		}		
		count++;
	}

	//for each voted for candidate, we need to add this voter and this voter's coins to their count
	count = 0;
	while (count < BPsVoted.length){
		//get the ID of the BPcandidate
		//System.out.println("candidate name = " + BPsVoted[count]);
		id = CandidateIds.get(BPsVoted[count]);
		//System.out.println("Update candidate ID = " + id);		
		if (id == null){
			//voters can vote for old block producer candidates, so this if statement filters old candidates out
		} else {
			Candidate currentCandidate = C.get(id);
			currentCandidate.w = currentCandidate.w + coins;
			currentCandidate.E.add(voterID); 
			//record this voter and coins under the correct array section
			C.set(id, currentCandidate);

		}	
		count++;

	}
	return howSatisfiedIsThisVoter;
}






/**
*
* Now 
*
**/
public static void printResults(String originalFileName){

	try (PrintWriter writer = new PrintWriter(new File("Analysed_"+originalFileName+".csv"))) {
	StringBuilder sb = new StringBuilder();
	//create output string
	String forOutput = "The results after analysing " + originalFileName + " are:\n\n";
	//add main result
	forOutput = forOutput + "Final Committee Ids = ," + Arrays.toString(S) + "\n";
	forOutput = forOutput + "PAV score = ," + PavSc + "\n";	
	//PRINT OUT VOTING ANALYTICS

	forOutput = forOutput + "\n" + "Total Voters, Total Voting Coins" + "\n";
	forOutput = forOutput + votersVoting + ", " + coinsVoting + "\n\n";

	forOutput = forOutput + "\n" + "Voters Using Proxy, Total Proxied Coins" + "\n";
	forOutput = forOutput + proxyVotersVoting + ", " + proxyCoinsVoting + "\n\n";
	forOutput = forOutput + "\n" + "Voters Directly Voting, Total Direct Voted Coins" + "\n";
	forOutput = forOutput + (votersVoting-proxyVotersVoting) + ", " + (coinsVoting-proxyCoinsVoting) + "\n\n";

	forOutput = forOutput + "\n" + "Non Voters, Non Voting Coins" + "\n";
	forOutput = forOutput + votersNotVoting + ", " + coinsNotVoting + "\n\n";
	forOutput = forOutput + "\n" + "Voters Under Threshold, Voting Coins Under Threshold" + "\n";
	forOutput = forOutput + votersBelowThreshold + ", " + coinsBelowThreshold + "\n\n";
	forOutput = forOutput + "\n" + "Total Users, Total User Coins" + "\n";
	forOutput = forOutput + (votersVoting+votersNotVoting) + ", " + (coinsVoting+coinsNotVoting) + "\n\n";
	System.out.println("forOutput: " + forOutput);
	sb.append(forOutput);
        writer.write(sb.toString());
        System.out.println("File Written!");

        } catch (FileNotFoundException e) {
	     System.out.println(e.getMessage());
	}



    }


}  
