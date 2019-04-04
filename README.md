# Delegated Proof of Stake Voter Representation Algorithms

This repository holds algorithms that check whether the elected block producers of a delegated proof of stake consensus protocol are fairly representing the electorate.

The idea of fair representation is based on the justified and extended justified representation axioms introduced in [this peer reviewed research](https://arxiv.org/abs/1407.8269).

## Delegated Proof of Stake (DPoS) overview

In a DPoS blockchain, there is a `k` number of block producers who can produce blocks. These `k` block producers can change over time through elections. In DPoS, there will be a set `N = {1, 2, ..., n}` coinholders, where each coinholder `i` can cast a ballot `A_i` selecting a `p` number of candidate block producers. The weight of a ballot `w(A_i)` depends on the number of coins `i` has (usually 1 coin = 1 vote). Then all of the ballots are counted and the `k` candidate block producers with the most votes become the elected block producers.

## Justified Representation Axiom

*Motivation of Justified Representation:* If there exists a group of coinholders of size at least `n/k`, who agree on at least `1` candidate, then each coinholder should have at least `1` representative in the elected block producers.

## Justified Representation Algorithm 
The algorithm to evaluate if a DPoS election satisfies justified representation can be found [here](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/LookOnlyForJR.java). This algorithm operates using the following command:

```
java LookOnlyForJR.java <your_voting_data_csv_file> <your_block_producer_list_csv_file>
```
Note that:
- `<your_voting_data_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/eos_voting_data.csv). 
- `<your_block_producer_list_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/AllBPs.csv), where the column headings are `Producer Name, Number of Voters, Average Vote Size, Largest Vote Size, Total Vote Size, Median Vote Size, Avg BPs per Vote` 

In this algorithm's code, you can:
- adjust the `coinThreshold` to indicate that a voter must have at greater than this many coins before he is counted as a valid member of the electorate. 
- adjust the `numberOfBPsVotedForThreshold` to indicate that a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate.

Finally, if the algorithm runs correctly, you will see a new file generated like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/Analysed_eos_voting_data.csv) indicating whether the election has passed or failed the justified representation axiom, analysed from the perspective of individual voters or coinholders.
