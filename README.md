# Delegated Proof of Stake Voter Representation Algorithms

This repository holds algorithms that check whether the elected block producers of a delegated proof of stake consensus protocol are fairly representing the electorate.

The idea of fair representation is based on the justified and extended justified representation axioms introduced in [this peer reviewed research](https://arxiv.org/abs/1407.8269).

## Delegated Proof of Stake (DPoS) overview

In a DPoS blockchain, there is a `k` number of block producers who can produce blocks. These `k` block producers can change over time through elections. In DPoS, there will be a set `N = {1, 2, ..., n}` coinholders, where each coinholder `i` can cast a ballot `A_i` selecting an `l` number of candidate block producers. The weight of a ballot `w(A_i)` depends on the  

## Justified Representation Axiom

*Motivation of Justified Representation:* Every group of voters of size at least n/k, who agree on at least one candidate, should have at least one representative in the committee.


## Justified Representation Algorithm 
...
