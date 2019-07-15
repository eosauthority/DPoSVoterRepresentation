# Delegated Proof of Stake Voter Representation Algorithms

This repository holds algorithms that check whether the elected block producers of a delegated proof of stake consensus protocol are fairly representing the electorate. In voting theory, we call these elected block producers the elected committee.

The idea of fair representation is based on the justified and extended justified representation axioms introduced in [this peer reviewed research](https://arxiv.org/abs/1407.8269).

## Delegated Proof of Stake (DPoS) overview

In a DPoS blockchain, there is a `k` number of block producers who can produce blocks. These `k` block producers (the committee) can change over time through elections. In DPoS, there will be a set `N = {1, 2, ..., n}` coinholders, where each coinholder `i` can cast a ballot `A_i` selecting a `p` number of candidate block producers. The weight of a ballot `w(A_i)` depends on the number of coins `i \in N` has (usually 1 coin = 1 vote). We can describe the total amount of voting coins as the budget `b`. Then all of the ballots are counted and (usually) the `k` candidate block producers with the most votes become the elected block producers.

## Justified Representation

There are multiple versions of the Justified Representation definition of committee that fairly represents the population, which we present below. Note that [this paper](https://arxiv.org/abs/1407.8269) shows that there exist some elections that cannot provide a committee guaranteeing the elected block producers satisfy the Strong and Semi-Strong Justified Representation fair representation definition.

### Strong Justified Representation Axiom

*Motivation of Strong Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then this common candidate should be in the elected block producers__.

### Semi-Strong Justified Representation Axiom

*Motivation of Semi-Strong Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then each coinholder should have at least `1` representative in the elected block producers__.

### Justified Representation Axiom

*Motivation of Strong Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then one coinholder in this group should have at least `1` representative in the elected block producers__.


## Justified Representation Algorithm 
The algorithm to evaluate if a DPoS election satisfies justified representation can be found [here](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/LookOnlyForJR.java). This algorithm checks every elected block producer and seeks to find a group coinholders with a budget of at least `b/k` where all members of the group has no representative in the committee. This algorithm therefore checks if the election satisfies the Justified Representation Axiom, and by inference the Semi-Strong Justified Representation Axiom, and Strong Justified Representation Axiom (as the final two are stronger versions of the more basic axiom).

This algorithm operates using the following command:

```
java LookOnlyForJR <your_voting_data_csv_file> <your_block_producer_list_csv_file>
```
Note that:
- `<your_voting_data_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/eos_voting_data.csv). 
- `<your_block_producer_list_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/AllBPs.csv), where the column headings are `Producer Name, Number of Voters, Average Vote Size, Largest Vote Size, Total Vote Size, Median Vote Size, Avg BPs per Vote` 

In this algorithm's code, you can:
- adjust the `coinThreshold` to indicate that a voter must have at greater than this many coins before he is counted as a valid member of the electorate. 
- adjust the `numberOfBPsVotedForThreshold` to indicate that a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate.

Finally, if the algorithm runs correctly, you will see a new file generated like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/Analysed_eos_voting_data.csv) indicating whether the election has passed or failed the justified representation axiom, analysed from the perspective of individual voters or coinholders.
