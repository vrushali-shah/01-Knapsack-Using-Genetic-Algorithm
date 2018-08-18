# 01 Knapsack using Genetic Algorithm

## Problem Statement
In a Knapsack problem you are given a sack which can hold a maximum weight W along with N items each with a weight and a value. The goal is to select a set of items from given object such that their total weight is less than or equal to the knapsack weight W and at the same time the total value is maximized. There is only one quantity of each object.

## Implementation Summary
To solve the problem using Genetic Algorithm we have represented a chromosome with a String made up of 1s and 0s, where each character in the string is considered as a gene. 1/0 represents whether an item is present in the sack or not, respectively. Item class has a weight and value. Individual class holds a chromosome and the total weight and total value of the Individual. The total weight and total value of an Individual are the phenotypes which are depent upon the genes the Individual inherited. The fitness of each Individual is measured by comparing its total weight with the maximum weight(W) the knapsack can hold. An Individual is considered fit only if its total weight is less than or equal to W. At each generation, the fitness of each Individual is checked and unfit Individuals are culled. The fit Individuals are carried on to the next generation. The culling process can be performed by parallel processing or on a single thread, which is specified before the execution start via a parameterized constructor.  New Individual for next generation are then bred by mutating fit Individuals from current generation and/or by crossover between two fit Individual from current generation. The program first tries to keep the next generation diverse by adding new Individuals only if no Individual with same chromosome is already present in the population. If it fails to find unique Individual then it fills up the population with random crossover without checks. The fittest Individuals amongst the fit Individual from each generation is found and stored. The iteration of next generation stops when there is no progress detected between last three generations. This is being achieved by comparing the mean values of last the generations. If the difference between each pair of last three values is less than the delta then it is assumed no further progress can be made and the iteration of making next generation stops. At this point, the fittest Individuals from each generation are compared and the one with maximum total value is chosen as the solution.

## Execution Steps
- Step 1: Input Knapsack capacity, number of items along with their values and weights, and the	population size. 
- Step 2: Generate Individual with random chromosome
- Step 3: Calculate the fitness of each Individual and cull the unfit ones.
 	If no Individual is fit in current generation go to step 2
	else, sort the fit Individuals based on their value and make a note of the fittest amongst	them.
- Step 4: Check if any progress has been made since last three generations
  If yes, continue to Step 5
 	If no, go to step 7
- Step 5: Perform mutation and crossover between the fit Individuals for next generation
- Step 6: Go to step 3
- Step 7: Sort the best Individuals from each generation based on their value. 
 	The one with highest value is our solution 
  
## Observation
- With lower delta the number of generations are higher but the solution is even closer to the supplied knapsack capacity

## Conclusion
- The population size and the delta play a key role in finding the optimum solution but genetic algorithm does not guarantee to find the optimum solution. This can be seen from above cases. Thus, it is important not to have a small population size compared to the number of items. But at the same time increasing the population size after a threshold value did not make much difference.  

## Author
Vrushali Shah
