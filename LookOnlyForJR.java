import java.io.*;  
import java.util.*;
public class LookOnlyForJR {  

	static double[] coinCount = new double[22]; //to keep track of the number of satisfied coins. I.e. if coinCount[i] = 5, then 5 coins are satisfied with (i-1) elected block producers.  
	static long[] userCount = new long[22]; //to keep track of the number of satisfied users. I.e. if userCount[i] = 10, then 10 users are satisfied with (i-1) elected block producers.
	static long votersNotVoting = 0; //the number of users that are not voting 
	static double coinsNotVoting = 0.0; //the number of coins who are not voting
	static long votersVoting = 0; //the number of users who are voting
	static double coinsVoting = 0.0; //the number of coins who are voting
	static long proxyVotersVoting = 0; //the number of users who are voting via a proxy
	static double proxyCoinsVoting = 0.0; //the number of coins who are voting via a proxy
	static String[] BPs; //the block producers elected at this time
	static int BPnumber = 21; //the number of block producers
	
	//JUSTIFIED REPRESENTATION VARIABLES:
	static boolean justifiedRepCoinPass; //have we satisfied justified representation according to the coins?
	static boolean justifiedRepVoterPass; //have we satisfied justified representation according to the users?
	static Vector<Integer> IdsOfVotersNotRepresented = new Vector<Integer>(); //this is the row ids for the users who have no elected block producer representing them. Recording this detail helps us speed up searches later.
	static int justifiedUsersCount;  //this is the number of users who voted for the block producer under consideration and have no elected block producer representing them.
	static double justifiedCoinCount; //this is the number of coins who voted for the block producer under consideration and have no elected block producer representing them.
	//ELECTORATE INCLUSION THRESHOLD VARIABLES:
	static double coinThreshold = 0.0; //a voter must have at greater than this many coins before he is counted as a valid member of the electorate
	static int numberOfBPsVotedForThreshold = 0; //a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate 
	static double coinsBelowThreshold = 0.0; //this is the running total of coins below the threshold
	static int votersBelowThreshold = 0; //this is the running total of voters below the threshold

    public static void main(String[] args) {  
  
        try {  
		justifiedRepCoinPass = true; //start the algorithm by assuming that justified representation will pass for the coins
		justifiedRepVoterPass = true; //start the algorithm by assuming that justified representation will pass for the users
		whichBlockProducersWereElected(args[1]); //now scan the file that lists the block producers candidates for this election
        	csvReaderByLine(args[0]); //scan the voter list
		lookForJustifiedRepresentation(args[1],args[0]); //look for justified representation
		printResults(args[0]); //print the results into an analysed file
		//summerise results		
		System.out.println("\n");
		System.out.println("\njustifiedRepCoinPass= " + justifiedRepCoinPass);
		System.out.println("justifiedRepVoterPass= " + justifiedRepVoterPass);
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
	BPs = new String[BPnumber];
	int BPcount = 0;
        String line = "";
	String toSplit = ",";
	BufferedReader br = null;
        
	try{
		br = new BufferedReader(new FileReader(csvFile)); //read the file
		String[] BPRow; //we will be spliting each row into sections
		Integer register;

		//for each BP candidate not elected,
	    	while (((line = br.readLine()) != null)&&(BPcount < BPnumber)) {
			BPRow = line.split(toSplit);
			System.out.println("row[0] " + BPRow[0] + " (ID)");
		    	System.out.println("row[1] " + BPRow[1] + " (NAME)");
		    	System.out.println("row[2] " + BPRow[2] + " (VOTER NUMBER)");
		    	System.out.println("row[5] " + BPRow[5] + " (COIN NUMBER)");		
			BPs[BPcount] = BPRow[1]; //add the block producer name to the array
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

		int countBP = 0;
		//reprint the elected block producers and initialise the arrays
		while (countBP < BPnumber){
			System.out.println("BP " + (countBP) + " = " + BPs[countBP] + "\n");
			//initialise counters as well
			coinCount[countBP] = 0; 
			userCount[countBP] = 0;
			countBP++;
		}

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
		System.out.println("line is= " + line);
                //split row by separator
		String[] row = line.split(csvSplitBy);
		//find the number of coins this voter has
		coins = Double.valueOf(row[0].split(toSplit)[1].trim());
		System.out.println("coins: " + coins);
		try{
			//find the number of block producers this voter voted for
			//this is in a try/catch as a voter may have voted for 0 block producers
			BPsVotedFor = row[1].split("\",")[0];
			System.out.println("BPsVotedFor: " + BPsVotedFor);
		}catch (ArrayIndexOutOfBoundsException e){BPsVotedFor = "";}
		try{
			//find the proxy this voter is using
			//this is in a try/catch as a voter may not have used a proxy
			proxy = row[1].split("\",")[1];
			System.out.println("proxy: " + proxy);
		}catch (ArrayIndexOutOfBoundsException e){}
		System.out.println("BPsVotedFor.length: " + BPsVotedFor.split(",").length);
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
			//record the voter as voting:
			votersVoting++;
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
			//ok so we have a voter casting a ballot, therefore we need to calculate how satisfied this voter is with the elected block producers:
			checkHowManyBPsRepresentVoter(BPs,BPsVotedFor, coins,rowNumber);

		}	

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
public static void checkHowManyBPsRepresentVoter(String[] BPsElected,String BPsVotedFor, double coins, Integer rowNumber){

	int count = 0;
	int countBP = 0;
	String toSplitBPs = ",";
	//get the individual BPs voted for
	String[] BPsVoted = BPsVotedFor.split(toSplitBPs);
	System.out.println("");
	System.out.println("BPsVotedFor: " + BPsVoted.length);
	int howSatisfiedIsThisVoter = 0;
	//loop through the block producers voted for...
	while (count < BPsVoted.length){
		countBP = 0;
		//and see if each voted for block producer has been elected...
		while (countBP < BPsElected.length){
			if (BPsVoted[count].equals(BPsElected[countBP])){
				howSatisfiedIsThisVoter++; //if it has then increase the satisfication of this voter
				countBP = BPsElected.length;
			}
			countBP++;
		}
		count++;
	}
	//record this voter and coins under the correct array section.
	userCount[howSatisfiedIsThisVoter] = userCount[howSatisfiedIsThisVoter]+1;
	coinCount[howSatisfiedIsThisVoter] = coinCount[howSatisfiedIsThisVoter]+coins;

	if (howSatisfiedIsThisVoter == 0){
		//if the voter is not satisfied by any candidates then we record the row number for easier searching later
		IdsOfVotersNotRepresented.add(rowNumber);

	}

}


/**
* Now we search to see if the elected block producers are justified
*/
public static void lookForJustifiedRepresentation(String BPfilename, String VoterFilename){

//We will now see if we can find a block producer that has not been elected, but has a large enough number of voters who vote for him and do not have any elected block producer representative.

	String csvFile = BPfilename + ".csv";
        BufferedReader br = null;
        String line = "";
	String toSplit = ",";

	try{
		br = new BufferedReader(new FileReader(csvFile));
		String[] row;
	
		//create ratios justified representation must pass (n/k)
		//users
		double requiredVoters = Double.valueOf(votersVoting)/BPnumber;
		//coins
		double requiredCoins = Double.valueOf(coinsVoting)/BPnumber;
		//print out ratios and related info:		
		System.out.println("votersVoting " + votersVoting);
		System.out.println("coinsVoting " + coinsVoting);
		System.out.println("requiredVoters " + requiredVoters);	//correct!
		System.out.println("requiredCoins " + requiredCoins);	//correct!

		//loop through every block producer in the csv file
	    	while ((line = br.readLine()) != null) {
			row = line.split(toSplit);
			//print out the related information
			System.out.println("row[0] " + row[0] + " (ID)");
		    	System.out.println("row[1] " + row[1] + " (NAME)");
		    	System.out.println("row[2] " + row[2] + " (VOTER NUMBER)");
		    	System.out.println("row[5] " + row[5] + " (COIN NUMBER)");		
			if (Integer.parseInt(row[0])<22){
				//if this block producer was elected, then ignore
				System.out.println("Block producer has been elected, so we will not evaluate it");

			} else {

				//reset the counters for the number of users and coins who have voted for this block producer and do not have an elected representative:
				justifiedUsersCount = 0;
				justifiedCoinCount = 0.0;


				//Our first pruning of the search space is to only check only block producer candidates who have a number of users or coins over n/k voting for them. To do so, we first find the total number of users and coins that block producer has:
				//users
				double votersInFavour = Double.valueOf(row[2]);
				//coins
				double coinsInFavour = Double.valueOf(row[5]);
				//then we see if the users or coins is greater than n/k
				if ((votersInFavour < requiredVoters)&&(coinsInFavour<requiredCoins)){
					System.out.println("Justified Representation PASS: Block producer not been elected but has less voters and coins then the n/k limit to disatisfy justified representation");
			System.out.println("votersInFavour " + votersInFavour);
		    	System.out.println("coinsInFavour " + coinsInFavour);
			return;
				} else {
				//now we have passed the pruning stage, so this block producer candidate needs a full evaluation...
					System.out.println("Block producer has NOT been elected, so we check for justified representation");
	//count all users that have no representative and voted for this BP candidate
					lookForJustifiedRepresentationForaBP(VoterFilename, row[1]);
					//print out the main info
					System.out.println(justifiedUsersCount + "=justifiedUsersCount");
					System.out.println(requiredVoters + "=requiredVoters");				
				    	System.out.println(justifiedCoinCount + "=justifiedCoinCount");
					System.out.println(requiredCoins + "=requiredCoins");

					//Decide whether justified representation has passed for users or coins
					if (justifiedUsersCount > requiredVoters){

						System.out.println("JUSTIFIED REPRESENTATIVE FAIL for voter number");
						justifiedRepVoterPass = false;
					}
					if (justifiedCoinCount > requiredCoins) {

						System.out.println("JUSTIFIED REPRESENTATIVE FAIL for coin number");
						justifiedRepCoinPass = false;
					}
					if ((justifiedUsersCount <= requiredVoters)&&(justifiedCoinCount <= requiredCoins)) {

						System.out.println("Justified Representation PASS: not greater than size n/k");

					}
				} 
			}			
		
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
* We now evaluate if a single block producer fails justified representation
*
**/
   public static void lookForJustifiedRepresentationForaBP(String filename, String candidateBP){

	//declare the variables needed...
	Integer nonRepVoterCount = 0;
	Integer nextRow;
	Integer BPCount;
	Integer rowNumber = 0;
	String csvFile = filename + ".csv";
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",\"";
	String toSplit = ",";
	String[] row;
	Double coins;
	String BPsVotedFor = "";
	String proxy = "";
	//make sure the key counting variables have been set	
	justifiedUsersCount = 0;
	justifiedCoinCount = 0;

	try {
		//open the file listing all of the voters
		br = new BufferedReader(new FileReader(csvFile));
		//loop through for every voter who has no elected representative
		while (nonRepVoterCount < IdsOfVotersNotRepresented.size()){
			//find the next row with a voter with no elected representative:
			nextRow = IdsOfVotersNotRepresented.elementAt(nonRepVoterCount);
			//loop until we get to that row:
			while (rowNumber.compareTo(nextRow) != 0){
				line = br.readLine();
				rowNumber++;
			}
			BPCount = 0;
			row = line.split(csvSplitBy);
			//get this voters number of coins:			
			coins = Double.valueOf(row[0].split(toSplit)[1].trim());
			try{
				//get which block producers this voter has voted for:
				BPsVotedFor = row[1].split("\",")[0];
			}catch (ArrayIndexOutOfBoundsException e){}
			//split the voted for block producers into an array
			String[] BPsVoted = BPsVotedFor.split(toSplit);
			try{
				//see if the voter elected a proxy:
				proxy = row[1].split("\",")[1];
			}catch (ArrayIndexOutOfBoundsException e){}		
			//now we have found the row so we need to investigate if the voter's voted for this specific block producer candidate			
			while (BPCount < BPsVoted.length){
				if (BPsVoted[BPCount].equals(candidateBP)){
					//if the voter did vote for this candidate then we need to record this					
					justifiedUsersCount++;
					justifiedCoinCount = justifiedCoinCount + coins;
					BPCount = BPsVoted.length;	
				}
		
				BPCount++;
			}
			nonRepVoterCount++;

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
* Now 
*
**/
public static void printResults(String originalFileName){

	try (PrintWriter writer = new PrintWriter(new File("Analysed_"+originalFileName+".csv"))) {
	StringBuilder sb = new StringBuilder();
	//create output string
	String forOutput = "The results after analysing " + originalFileName + " are:\n\n";
	//add main result
	forOutput = forOutput + "Justified Representation Voter Pass = ," + justifiedRepVoterPass +"\n";
	forOutput = forOutput + "Justified Representation Coin Pass = ," + justifiedRepCoinPass + "\n";

	//add heading
	forOutput = forOutput + "BP Number, UserCount, CoinCount\n";
	int count = 0;	
	while (count < 22){
		forOutput = forOutput + count + ", " + userCount[count] + ", " + coinCount[count] + "\n";
		count++;
	}

	forOutput = forOutput + "\n" + "Total Voters, Total Voting Coins" + "\n";
	forOutput = forOutput + votersVoting + ", " + coinsVoting + "\n\n";

	forOutput = forOutput + "\n" + "Voters Using Proxy, Total Proxied Coins" + "\n";
	forOutput = forOutput + proxyVotersVoting + ", " + proxyCoinsVoting + "\n\n";
	forOutput = forOutput + "\n" + "Voters Directly Voting, Total Direct Voted Coins" + "\n";
	forOutput = forOutput + (votersVoting-proxyVotersVoting) + ", " + (coinsVoting-proxyCoinsVoting) + "\n\n";

	forOutput = forOutput + "\n" + "Voters with a rep, Coins with a rep, Perc of voters with no rep, Perc of coins with no rep" + "\n";
	forOutput = forOutput + (votersVoting-userCount[0]) + ", " + (coinsVoting-coinCount[0]) + ", " + (Double.valueOf(userCount[0])/Double.valueOf(votersVoting)) + ", " + (coinCount[0]/coinsVoting) + "\n\n";

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
