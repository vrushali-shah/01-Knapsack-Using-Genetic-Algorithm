package main.java;


import java.util.ArrayList;
import java.util.List;

public class Individual {

	/**
	 * items the list stores the list of all items available to be put in the knapsack
	 * chromosome is a binary String, that is, its made up 1s and 0s which represent if an item in the list items is present in the bag or not respectively
	 * total_weight is the sum of the weights of all the items present in the bag
	 * total_value is the sum of the values of all the items present in the bag
	 */
	private StringBuilder chromosome;
	private double total_weight;
	private double total_value;
	public static List<Item> items=new ArrayList<>();
	public Individual(StringBuilder chromosome) {
		this.chromosome = chromosome;
		this.total_weight = 0;
		this.total_value = 0;
		calculateFitness();
		
	}
	
	/**
	 * calculates the total weight and total value
	 */
	void calculateFitness() {
		for (int i = 0; i < chromosome.length(); i++) {
			
			if (chromosome.charAt(i) == '1') {
				total_weight += items.get(i).getWeight();
				total_value += items.get(i).getValue();
			}
		}
	}

	void setChromosome(StringBuilder chromosome) {
		this.chromosome = chromosome;
	}

	public StringBuilder getChromosome() {
		return chromosome;
	}

	public double getTotalValue() {
		return total_value;
	}

	public double getTotalWeight() {
		return total_weight;
	}


}