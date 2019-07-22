# Delegated Proof of Stake Voter Representation Algorithms

This repository holds algorithms that check whether the elected block producers of a delegated proof of stake consensus protocol are fairly representing the electorate. In voting theory, we call these elected block producers the elected committee.

The idea of fair representation is based on the justified and extended justified representation axioms introduced in [this peer reviewed research](https://arxiv.org/abs/1407.8269).

## Delegated Proof of Stake (DPoS) overview

In a DPoS blockchain, there is a `k` number of block producers who can produce blocks. These `k` block producers (the committee) can change over time through elections. In DPoS, there will be a set `N = {1, 2, ..., n}` coinholders, where each coinholder `i` can cast a ballot `A_i` selecting a `p` number of candidate block producers. The weight of a ballot `w(A_i)` depends on the number of coins `i \in N` and the voting weight rule. We can describe the total amount of voting coins as the budget `b`. Then all of the ballots are counted and (usually) the `k` candidate block producers with the most votes become the elected block producers.

## Justified Representation

There are multiple versions of the Justified Representation definition of a committee that fairly represents the population, which we present below for completeness. Our algorithm concentrates on the Justified Representation Axiom as [this paper](https://arxiv.org/abs/1407.8269) shows that there exist some elections that cannot provide a committee guaranteeing the elected block producers satisfy the Strong and Semi-Strong Justified Representation Axioms.

### Strong Justified Representation Axiom

*Motivation of Strong Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then this common candidate should be in the elected block producers__.

### Semi-Strong Justified Representation Axiom

*Motivation of Semi-Strong Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then each coinholder should have at least `1` representative in the elected block producers__.

### Justified Representation Axiom

*Motivation of Justified Representation:* If there exists a group of coinholders who hold a budget of at least `b/k` (total voting coins divided by the number elected block producers), who agree on at least `1` candidate, __then one coinholder in this group should have at least `1` representative in the elected block producers__.


## Justified Representation Algorithm 
The algorithm to evaluate if a DPoS election satisfies the Justified Representation Axiom is based on Theorem 1 of [this paper](https://arxiv.org/pdf/1407.8269.pdf). This paper says that, even though it may seem that we need to consider every subset of voters with a budget of at least `b/k`, in fact it is sufficient to consider the block producer candidates one by one, and for each candidate count the coins that have voted for this candidate and do not have any representative in the elected committee. If this count is greater than `b/k` then the elected committee does not satisfy any of the Justified Representation Axioms.

The code of our Java implementation of this algorithm can be found [here](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/LookOnlyForJR.java). This algorithm checks every elected block producer candidate and seeks to find a group of coinholders with a budget of at least `b/k` where all members of the group have no representative in the committee. This algorithm therefore checks if the election satisfies the Justified Representation Axiom, and by inference the Semi-Strong Justified Representation Axiom, and Strong Justified Representation Axiom (as the final two are stronger versions of the more basic axiom).

This algorithm operates using the following command:

```
java LookOnlyForJR <your_voting_data_csv_file_name> <your_block_producer_list_csv_file_name>
```
Note that:
- `<your_voting_data_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/eos_voting_data.csv). 
- `<your_block_producer_list_csv_file>` should be formatted like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/AllBPs.csv), where the column headings are `Producer Name, Number of Voters, Average Vote Size, Largest Vote Size, Total Vote Size, Median Vote Size, Avg BPs per Vote` 

For instance, to run the algorithm for the two linked example files above, you would type (as long as all files are in the same folder):

```
java LookOnlyForJR eos_voting_data AllBPs
``` 

In this algorithm's code, you can:
- adjust the `coinThreshold` to indicate that a voter must have at greater than this many coins before he is counted as a valid member of the electorate. 
- adjust the `numberOfBPsVotedForThreshold` to indicate that a voter must vote for greater than this many block producers before he is counted as a valid member of the electorate.

Finally, if the algorithm runs correctly, you will see a new file generated like [so](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/Example%20Data/Analysed_eos_voting_data.csv) indicating whether the election has passed or failed the justified representation axiom, analysed from the perspective of individual voters or coinholders.

## Extended Justified Representation

There is only one version of the Extended Justified Representation definition of committee that fairly represents the population, as it extends the Justified Representation Axiom. This is so that there is always at least one committee available that satisfies the Extended Justified Representation axiom.

### Extended Justified Representation Axiom

*Motivation of Extended Justified Representation:* For all `1 <= l <= k`, if there exists a group of coinholders who hold a budget of at least `l*(b/k)` (`l` times the total voting coins divided by the number elected block producers), who agree on at least `l` candidates, __then one coinholder in this group should have at least `l` representatives in the elected block producers__.

## Extended Justified Representation Algorithm

Checking an elected committee satisfies Extended Justified Representation is a computationally hard problem (see Theorem 14 of [this paper](https://arxiv.org/pdf/1407.8269.pdf)). Therefore we have modified the approximation algorithm presented in Algorithm 1 of [this paper](https://fpt.akt.tu-berlin.de/publications/skowron_ejr_poly.pdf). We have modified it, firstly to adapt the theory allow a voter to have multiple votes (or coins) and secondly we have performed modifications to computationally optimise it. Our theoretical modifications and formal description of the algorithm can be seen [here](https://github.com/Luker501/DPoSVoterRepresentation/blob/master/EJRTheoryAndAlgorithm.pdf). But basically, this algorithm starts with the committee of the most popular block producer candidates, then attempts to swap in/out candidates to increase the overall committee score. The algorithm stops when no more possible improving swaps are available.

This algorithm operates using the following command:

```
java LookForEJR <your_voting_data_csv_file_name> <your_block_producer_list_csv_file_name>
```
The presentation describing the ideas behind this algorithm can be found [here](https://docs.google.com/presentation/d/1rTHXWrsVhZQLD3_vixjb7qRhoj7InFbZr1pnLa_bqsk/edit?usp=sharing)
