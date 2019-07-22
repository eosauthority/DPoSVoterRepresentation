import java.io.*;  
import java.util.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class LookForEJR {  

	//**variables for analysis
	static int votersNotVoting = 0; //the number of users that are not voting 
	static double coinsNotVoting = 0.0; //the number of coins who are not voting
	static int votersVoting = 0; //the number of users who are voting
	static double coinsVoting = 0.0; //the number of coins who are voting
	static int proxyVotersVoting = 0; //the number of users who are voting via a proxy
	static double proxyCoinsVoting = 0.0; //the number of coins who are voting via a proxy
	static int[] S; //the index of block producers elected at this time
	static int k = 21; //the number of block producers in the elected committee
	//ELECTORATE INCLUSION THRESHOLD VARIABLES:
	static double coinThreshold = 0.0; //a voter must have at greater than this many coins before he is counted as a valid member of the electorate
	static int numberOfBPsVotedForThreshold = 0; //a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate 
	static double coinsBelowThreshold = 0.0; //this is the running total of coins below the threshold
	static int votersBelowThreshold = 0; //this is the running total of voters below the threshold
	

	//**EXTENDED JUSTIFIED REPRESENTATION GLOBAL VARIABLES:
	static boolean EJRRepCoinPass; //have we satisfied extended justified representation according to the coins? 
	static int swapNumber = 0;

	static ArrayList<Candidate> C = new ArrayList<Candidate>(); //the maximum vote weight of each candidate
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
		EJRRepCoinPass = true; //start the algorithm by assuming that justified representation will pass for the coins
		whichBlockProducersWereElected(args[1]); //now scan the file that lists the block producers candidates for this election
        	csvReaderByLine(args[0]); //scan the voter list
		LSPAVMain(); //attempt to improve the committee
		printResults(args[0]); //print the results into an analysed file
		//summerise results		
		System.out.println("\n");
		System.out.println("\nEJRRepCoinPass= " + EJRRepCoinPass);
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
	//instantiate the block producer array
	S = new int[k];
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
			S[BPcount] = BPcount; //this block producer has been elected, so add it to the elected committee list.
			//The BP gets assigned its rank as an ID
			Candidate newElectedCandidate = new Candidate();
			newElectedCandidate.name = BPRow[1].trim();
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
			newUnelectedCandidate.name = BPRow[1].trim();
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
			double toAddPav = 0.0;
			//df.setRoundingMode(RoundingMode.FLOOR); //we are truncating
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
		System.out.println("S is: " + Arrays.toString(S));
	try {
		Thread.sleep(3000);
	} catch (Exception e){
	}
	

        }catch (IOException e) {
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
		id = CandidateIds.get(BPsVoted[count].trim());
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
		id = CandidateIds.get(BPsVoted[count].trim());
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
* We now evaluate if a group of block producers fail extended justified representation
*
**/
   public static void LSPAVMain(){

	//declare the variables needed...
	int SCount = k-1; //the search counter that we need to run down
	int candidateMinus; //the candidate we are removing from the optimal committee
	int candidatePlus; //the candidate we are adding to the optimal committee
	double lostPavSc; //how much PAV score we lost by subtracking this candidate from the selected committee
	double addedPavSc; //how much PAV score we gain by adding this candidate to the selected committee
	ArrayList<Integer> rStar = new ArrayList<Integer>(); //copy of the vector holding the representatives
	double tempAddedPavSc;
	double tempLostPavSc;
	int tempVoterReps;

	while (SCount >= 0){
	
		System.out.println("SCount = " + SCount);
		candidateMinus = S[SCount];
		//System.out.println("candidateMinus = " + candidateMinus);
		lostPavSc = 0.0;
		tempLostPavSc = 0.0;
		tempVoterReps = 0;		
		rStar = new ArrayList<>(r);

		//get this candidate from the hashtable
		Candidate currentCandidate = C.get(candidateMinus);
		//for all voters pointing to this candidate, we need to find the lost PAV score.
		int voter;
		int voter2;
		double voterBudget = 0.0;
		int numberOfEdges = currentCandidate.E.size();
		//System.out.println("CandidateMinus edges = " + numberOfEdges);
		//df.setRoundingMode(RoundingMode.FLOOR); //we are truncating
		for (int edgeCount = 0; edgeCount < numberOfEdges; edgeCount++){
			voter = currentCandidate.E.get(edgeCount);
			//System.out.println("voterID is : " + voter);
			//get voter's weight
			voterBudget = b.get(voter);
			//System.out.println("voterBudget is : " + voterBudget);
			//get the lost pav of each voter (times by the voter budget)
			tempLostPavSc = ((1.0)/(r.get(voter)));
			tempLostPavSc = tempLostPavSc * voterBudget;
			lostPavSc = lostPavSc + (tempLostPavSc);
			tempVoterReps = rStar.get(voter);			
			rStar.set(voter,(tempVoterReps-1));
		}

		try {
			System.out.println("numberOfEdges: " + numberOfEdges);
			System.out.println("sleep time");
			System.out.println("currentCandidate: " + currentCandidate.name);
			Thread.sleep(5000);
		} catch (Exception e){}
		
		System.out.println("lostPavSc = " + lostPavSc);
		double maxScore = lostPavSc + (b.size()/Math.pow(k,2));
		System.out.println("maxScore = " + maxScore);
		System.out.println("*****************NEXT***********************");
		//for loop
		for (int p = 0; p < C.size(); p++){
			tempAddedPavSc = 0.0;
			addedPavSc = 0.0; //PAV score gained by adding this candidate
			Candidate thisCandidate = C.get(p);
			if (thisCandidate.inS == true){
				//System.out.println("This candidate is in the selected committee = " + thisCandidate.name);
				//System.out.println("*****************NEXT***********************");		

				continue; //do not replace with candidates already in the elected committee
			} else if (p == candidateMinus){
				//System.out.println("This is the candidate being minused (do not replace him with the same candidate) = " + thisCandidate.name);	
				//System.out.println("*****************NEXT***********************");	
				continue; //do not replace with candidates already in the elected committee

			}
				
			candidatePlus = p;
			//System.out.println("candidatePlus = " + candidatePlus);
			numberOfEdges = thisCandidate.E.size();
			double candidateW = thisCandidate.w;
			//System.out.println("candidatePlus edges = " + numberOfEdges);
			if (candidateW < maxScore) {
					
				//System.out.println("candidate maximum weight is below threshold (" + maxScore + ") so skipping analysis of swap");
				continue;
			}

			//df.setRoundingMode(RoundingMode.FLOOR); //we are truncating
			for (int edgeCount2 = 0; edgeCount2 < numberOfEdges; edgeCount2++){
				voter2 = thisCandidate.E.get(edgeCount2);
				//get voter's weight
				voterBudget = b.get(voter2);
				//get the added pav of each voter (times by the voter budget)
				tempAddedPavSc = ((1.0)/(rStar.get(voter2)+(1.0)));
				tempAddedPavSc = tempAddedPavSc * voterBudget;
				addedPavSc = addedPavSc + tempAddedPavSc;
				tempVoterReps = rStar.get(voter2);				
				rStar.set(voter2,tempVoterReps+1);
			}
			System.out.println("PavSc = " + PavSc);	
			System.out.println("AddedPavSc = " + addedPavSc);
			System.out.println("[Adjusted Score]: PavSc - lostPavSc + addedPavSc = " + (PavSc - lostPavSc + addedPavSc));
			System.out.println("[Threshold]: PavSc + (b.size()/math.pow(k,2)     = " + (PavSc + (b.size()/Math.pow(k,2))));
			//did we improve the committee by the threshold amount?
			if (PavSc - lostPavSc + addedPavSc >= PavSc + (b.size()/Math.pow(k,2))){
				System.out.println("************WE HAVE FOUND A BETTER COMMITTEE!!!***************");		
				EJRRepCoinPass = false; //failed as we can improve the committee
				//update the selected committee. Take candidateMinus out and put candidatePlus in
				swapNumber++;
				System.out.println("swapNumber: " + swapNumber);
				System.out.println();
				System.out.println("SCount: " + SCount);
				//System.out.println("CandidateMinus: " + candidateMinus);
				//System.out.println("CandidatePlus: " + candidatePlus);
				System.out.println("AddedPavSc = " + addedPavSc);
				System.out.println("lostPavSc = " + lostPavSc);
				System.out.println("Adjusted PAV Score = " + (PavSc - lostPavSc + addedPavSc));
				System.out.println("Previous PAV Score = " + PavSc);
				System.out.println("threshold PAV      = " + (PavSc + b.size()/Math.pow(k,2)));
				Candidate CandidateOut = C.get(candidateMinus);
				CandidateOut.inS = false;
				C.set(candidateMinus, CandidateOut);
				Candidate CandidateIn = C.get(candidatePlus);
				CandidateIn.inS = true;
				C.set(candidatePlus, CandidateIn);
				S[SCount] = candidatePlus;
				System.out.println("S array = " + Arrays.toString(S));
				//update representative vector
				r = new ArrayList<>(rStar);
				PavSc = PavSc - lostPavSc + addedPavSc;
				try {
					Thread.sleep(2000);

				} catch (Exception e){

				}
				//start analysis again				
				SCount = k;
System.out.println("************END OF BETTER COMMITTEE SECTION!!!***************");
				break;
				
			} else {
				System.out.println("*****************NEXT***********************");
				rStar = new ArrayList<>(r);  //reset rStar array	
			}


		}
		
		SCount--;

	}

	
	
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
	forOutput = forOutput + "Extended Justified Representation Coin Pass = ," + EJRRepCoinPass + "\n";
	forOutput = forOutput + "Number of swaps = ," + swapNumber + "\n";
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
