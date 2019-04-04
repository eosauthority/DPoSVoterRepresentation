# Delegated Proof of Stake Voter Representation Algorithms

This repository holds algorithms that check whether the elected block producers of a delegated proof of stake consensus protocol are fairly representing the electorate.

The idea of fair representation is based on the justified and extended justified representation axioms introduced in [this peer reviewed research](https://arxiv.org/abs/1407.8269).

## Delegated Proof of Stake (DPoS) overview

In a DPoS blockchain, there is a `k` number of block producers who can produce blocks. These `k` block producers can change over time through elections. In DPoS, there will be a set `N = {1, 2, ..., n}` coinholders, where each coinholder `i` can cast a ballot `A_i` selecting a `p` number of candidate block producers. The weight of a ballot `w(A_i)` depends on the number of coins `i` has (usually 1 coin = 1 vote). Then all of the ballots are counted and the `k` candidate block producers with the most votes become the elected block producers.

## Justified Representation Axiom

*Motivation of Justified Representation:* If there exists a group of coinholders of size at least `n/k`, who agree on at least `1` candidate, then each coinholder should have at least `1` representative in the elected block producers.

## Justified Representation Algorithm 
...
