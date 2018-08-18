package main.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

/**
 * Representing each chromosome as a String made up of genes equal to the number
 * of items available Each gene can either be present or absent in a chromosome
 * which is represented by 1 or 0 respectively. An Individual has a chromosome
 * and a weight and value as its traits
 * 
 * @author Prashant K
 *
 */
public class Knapsack {

	private static final Logger LOGGER = Logger.getLogger(Knapsack.class.getName());
	public double accuracy;
	private List<Item> items;
	public List<Individual> currentPopulation;
	public List<Individual> oldPopulation;
	public List<Individual> bestOfEachGeneration;
	private double knapsackCapacity;
	private int generationCounter;
	private int populationCapacity;
	private Random rm;
	private List<Double> meanValueOfEachGeneration;
	private int numberOfItems;
	private boolean parallel;

	private Knapsack() {
	}

	/**
	 * 
	 * @param knapsackCapacity maximum capacity of the knapsack
	 * @param maxGenerations maximum generations to create
	 * @param populationCapacity number of individual in each generation
	 * @param numberOfItems number of items available to put in the sack
	 */
	public Knapsack(double knapsackCapacity, int populationCapacity, int numberOfItems, boolean parallel) {
		LOGGER.info("-------------------------------------");
		LOGGER.info("Knapsack Capacity: " + knapsackCapacity);
		LOGGER.info("Population Capacity: " + populationCapacity);
		LOGGER.info("Number of Items available: " + numberOfItems);
		LOGGER.info("-------------------------------------");
		currentPopulation = new ArrayList<>();
		oldPopulation = new ArrayList<>();
		bestOfEachGeneration = new ArrayList<>();
		items = Individual.items;
		meanValueOfEachGeneration = new ArrayList<>();
		this.knapsackCapacity = knapsackCapacity;
		this.populationCapacity = populationCapacity;// populationCapacity;
		this.numberOfItems = numberOfItems;
		this.generationCounter = 0;
		this.parallel = parallel;
		accuracy = 0.001;
		rm = new Random(populationCapacity);
		makeItems();
		generateInitialPopulation(currentPopulation);

	}

	private void makeItems() {
		items.clear();
		for (int i = 0; i < numberOfItems; i++) {
			addItem(rm.nextInt((int) (knapsackCapacity / numberOfItems * 4)),
					rm.nextInt((int) (knapsackCapacity / numberOfItems * 4)));
		}
	}

	/**
	 * generates initial population for the problem with random traits
	 */
	public void generateInitialPopulation(List<Individual> list) {

		LOGGER.info("------Generations------");
		StringBuilder zeros = new StringBuilder(numberOfItems);

		for (int j = 0; j < numberOfItems; j++) {
			zeros.append("0");
		}
		list.add(new Individual(zeros));
		while (list.size() < populationCapacity) {
			StringBuilder chromosome = new StringBuilder(numberOfItems);

			for (int j = 0; j < numberOfItems; j++) {

				if (rm.nextBoolean())
					chromosome.append("1");
				else
					chromosome.append("0");
			}
			list.add(new Individual(chromosome));
		}
	}

	/**
	 * makes new generation and keeps tracks of the progress. Stops making new
	 * generation if there is no progress since last three generations
	 */
	public void makeGenerations() {

		do {
			LOGGER.info("==== Generation " + generationCounter++ + " ====");
			evaluateAndBreed();
		} while (!checkGenerationProgress());

		sortByValue(bestOfEachGeneration);
		LOGGER.info("========Best========");
		LOGGER.info(bestOfEachGeneration.get(0).getChromosome() + " " + bestOfEachGeneration.get(0).getTotalValue()
				+ " " + bestOfEachGeneration.get(0).getTotalWeight());
	}

	/**
	 * checks if there is no progress in results between last three generation
	 * 
	 * @return true if no progress has been made false if some progress has been
	 *         made
	 */
	private boolean checkGenerationProgress() {
		if (meanValueOfEachGeneration.size() > 3) {
			double x = meanValueOfEachGeneration.get(meanValueOfEachGeneration.size() - 1);
			double y = meanValueOfEachGeneration.get(meanValueOfEachGeneration.size() - 2);
			double z = meanValueOfEachGeneration.get(meanValueOfEachGeneration.size() - 3);
			double x_y = Math.abs(Math.round(x) - Math.round(y));
			double y_z = Math.abs(Math.round(z) - Math.round(y));
			double x_z = Math.abs(Math.round(z) - Math.round(x));
			double delta = knapsackCapacity * accuracy;
			if (x_y < delta && y_z < delta && x_z < delta) {
				LOGGER.info("means " + x + " " + y + " " + z);
				return true;
			}
		}
		return false;
	}

	/**
	 * Breeds new Individual with unique genes for next generation from the fittest
	 * Individuals in present generation either by mutation or sexual crossover
	 * which is decided between randomly on every iteration until the population
	 * size is equal to or greater the populationCapacity It first tries to keep the
	 * current generation diverse by adding only unique individuals to the
	 * population. If no new unique Individual are generated after oldPopulation/4
	 * tries, it fills up the population by doing crossover between random
	 * Individuals until the current population size is equal to or greater than the
	 * populationCapacity
	 */
	private void evaluateAndBreed() {

		if (parallel)
			evaluateParallel();
		else
			evaluatePopulation();

		int crossoverCounter = -1;
		while (currentPopulation.size() <= populationCapacity) {
			// Decide if to do either a mutation or a crossover

			if (crossoverCounter >= oldPopulation.size() / 4) {
				fillUp();
			} else if (crossoverCounter < oldPopulation.size() / 4 && oldPopulation.size() > 2 && rm.nextBoolean()) {
				// do crossover

				Individual i2;
				Individual i1;
				// find two unique individuals for crossover
				// Do crossover of i1 and i2

				do {
					crossoverCounter++;
					i1 = oldPopulation.get(rm.nextInt(oldPopulation.size()));
					i2 = oldPopulation.get(rm.nextInt(oldPopulation.size()));
					if (crossover(i1.getChromosome(), i2.getChromosome())) {
						crossoverCounter = -1;
						break;
					}

					if (crossoverCounter >= oldPopulation.size() / 4)
						break;

				} while (true);

			} else {
				// do mutation

				Individual i1 = oldPopulation.get(rm.nextInt(oldPopulation.size()));
				StringBuilder toMutate = i1.getChromosome();

				i1 = mutate(toMutate);
				toMutate = i1.getChromosome();
				currentPopulation.add(i1);
			}
		}

	}

	/**
	 * evaluates the present population based on their totalweight and removes then
	 * from currentPopulation list If they are fit they are added to the
	 * oldPopulation list. The old population, which now consists of only the
	 * fittest Individual, is then sorted based on their TotalValue Also, the best
	 * of each generation is added to bestOfEachGeneration. 
	 */
	private void evaluatePopulation() {
		sortByWeight(currentPopulation);
		oldPopulation.clear();
		double meanValueOfThisGeneration = 0;
		Iterator<Individual> iterator = currentPopulation.iterator();
		while (iterator.hasNext()) {
			Individual individual = iterator.next();
			if (isFit(individual)) {
				oldPopulation.add(individual);
				meanValueOfThisGeneration += individual.getTotalValue();
			}
			iterator.remove();
		}

		sortByValue(oldPopulation);
		cloneFromOld();
		if (oldPopulation.isEmpty()) {
			generateInitialPopulation(oldPopulation);
		} else {
			bestOfEachGeneration.add(oldPopulation.get(0));
			LOGGER.info("Best Individual:");
			LOGGER.info(oldPopulation.get(0).getChromosome() + " " + oldPopulation.get(0).getTotalValue() + " "
					+ oldPopulation.get(0).getTotalWeight());
		}
		meanValueOfEachGeneration.add(meanValueOfThisGeneration / populationCapacity);

	}
	
	/**
	 * Divides the present population into four parts and each part is evaluated by different Future simultaneously. 
	 * Each future returns the a filtered list of fit Individuals.  
	 * These results are then merged into the oldPopulation list.
	 * Evaluation is based on their totalweight and the unfit are removed from the list.
	 * The old population, which now consists of only the
	 * fittest Individual, is then sorted based on their TotalValue Also, the best
	 * of each generation is added to bestOfEachGeneration
	 */

	private void evaluateParallel() {
		oldPopulation.clear();
		CompletableFuture<List<Individual>> part1 = part(copy(0, currentPopulation.size() / 4));
		CompletableFuture<List<Individual>> part2 = part(
				copy(currentPopulation.size() / 4, currentPopulation.size() / 2));
		CompletableFuture<List<Individual>> part3 = part(
				copy(currentPopulation.size() / 2, 3 * currentPopulation.size() / 4));
		CompletableFuture<List<Individual>> part4 = part(
				copy(3 * currentPopulation.size() / 4, currentPopulation.size()));
		CompletableFuture<List<Individual>> part = part1.thenCombine(part2, (xs1, xs2) -> {

			List<Individual> list = new ArrayList<>();
			list.addAll(xs1);
			list.addAll(xs2);
			return list;
		});
		part = part.thenCombine(part3, (xs1, xs2) -> {

			List<Individual> list = new ArrayList<>();
			list.addAll(xs1);
			list.addAll(xs2);
			return list;
		});
		part = part.thenCombine(part4, (xs1, xs2) -> {

			List<Individual> list = new ArrayList<>();
			list.addAll(xs1);
			list.addAll(xs2);
			return list;
		});
		oldPopulation = part.join();

		double meanValueOfThisGeneration = 0;

		for (Individual i : oldPopulation) {
			meanValueOfThisGeneration += i.getTotalValue();
		}
		sortByValue(oldPopulation);

		currentPopulation.clear();
		cloneFromOld();
		meanValueOfEachGeneration.add(meanValueOfThisGeneration / populationCapacity);

		if (oldPopulation.isEmpty()) {
			generateInitialPopulation(oldPopulation);
		} else {
			bestOfEachGeneration.add(oldPopulation.get(0));
			LOGGER.info("Best Individual:");
			LOGGER.info(oldPopulation.get(0).getChromosome() + " " + oldPopulation.get(0).getTotalValue() + " "
					+ oldPopulation.get(0).getTotalWeight());
		}
	}

	/**
	 * 
	 * @param list the list to be filtered based on their total weight
	 * @return the list which contains only the fit Individuals
	 */
	private CompletableFuture<List<Individual>> part(List<Individual> list) {
		return CompletableFuture.supplyAsync(() -> {
			sortByWeight(list);
			Iterator<Individual> iterator = list.iterator();
			while (iterator.hasNext()) {
				Individual individual = iterator.next();
				if (!isFit(individual))
					iterator.remove();
			}
			return list;
		});
	}

	private synchronized List<Individual> copy(int to, int from) {
		List<Individual> copy = new ArrayList<>();
		copy.addAll(currentPopulation.subList(to, from));
		return copy;
	}

	/**
	 * do a crossover between two chromosomes, c1 and c2 and add them to
	 * currentPopulation
	 */
	private void fillUp() {
		int oldSize = oldPopulation.size();
		while (currentPopulation.size() <= populationCapacity) {

			StringBuilder c1 = oldPopulation.get(rm.nextInt(oldSize / 2)).getChromosome();
			StringBuilder c2 = oldPopulation.get(rm.nextInt(oldSize - oldSize / 2)).getChromosome();
			int crossoverPoint = rm.nextInt(numberOfItems);
			StringBuilder c3 = new StringBuilder(c1.substring(0, crossoverPoint) + c2.substring(crossoverPoint));
			StringBuilder c4 = new StringBuilder(c2.substring(0, crossoverPoint) + c1.substring(crossoverPoint));
			currentPopulation.add(new Individual(c3));
			currentPopulation.add(new Individual(c4));
		}
	}

	/**
	 * Performs crossover between c1 and c2 until a new chromosome is generated and
	 * then randomly decides whether to performs mutation on the new chromosomes or
	 * not. It then adds them to the currentPopulatin list
	 * 
	 * @param c1 chromosome of Individual 1
	 * @param c2 chromosome of Individual 2
	 * @return true if the new chromosome is not present in the current population,
	 *         false otherwise
	 */

	private boolean crossover(StringBuilder c1, StringBuilder c2) {
		StringBuilder[] newChromosome;
		int x = 0;
		if (c1.equals(c2))
			return false;
		do {
			x++;
			newChromosome = performCrossover(c1, c2);
			if (x == numberOfItems / 2)
				break;
		} while (chromosomeExists(newChromosome[0]) && chromosomeExists(newChromosome[1]));

		if (x == numberOfItems / 2)
			return false;
		// mutate newChromosome1
		if (rm.nextBoolean())
			currentPopulation.add(mutate(newChromosome[0]));
		else
			currentPopulation.add(new Individual(newChromosome[0]));
		// mutate newChromosome2
		if (rm.nextBoolean())
			mutate(newChromosome[1]);
		else
			currentPopulation.add(new Individual(newChromosome[1]));

		return true;

	}

	/**
	 * Randomly selects a crossover point and interchanges the selected genes of c1
	 * and c1
	 * 
	 * @param c1 c1 chromosome of Individual 1
	 * @param c2 chromosome of Individual 2
	 * @return a String array with two new chromosomes
	 */
	public StringBuilder[] performCrossover(StringBuilder c1, StringBuilder c2) {

		int crossoverPoint = rm.nextInt(numberOfItems);
		StringBuilder c3 = new StringBuilder(c1.substring(0, crossoverPoint) + c2.substring(crossoverPoint));
		StringBuilder c4 = new StringBuilder(c2.substring(0, crossoverPoint) + c1.substring(crossoverPoint));
		StringBuilder[] newChromosome = { c3, c4 };
		return newChromosome;
	}

	/**
	 * Randomly selects genes to mutate in oldChromosome
	 * 
	 * @param oldChromosome the chromosome of the Individual to mutate
	 * @return a new Individual with the mutated chromosome
	 */
	public Individual mutate(StringBuilder oldChromosome) {

		StringBuilder newChromosome = new StringBuilder(oldChromosome);
		while (rm.nextBoolean()) {
			int gene = rm.nextInt(numberOfItems);
			newChromosome.setCharAt(gene, newChromosome.charAt(gene) == '1' ? '0' : '1');
			if (chromosomeExists(newChromosome)) {
				newChromosome.setCharAt(gene, newChromosome.charAt(gene) == '1' ? '0' : '1');
			}
		}

		return new Individual(newChromosome);
	}

	/**
	 * checks if the individual is fit or not by comparing its Total Weight with the
	 * knapsack capacity
	 * 
	 * @param individual The Individual who's fitness is to be checked
	 * @return true if Individual is fit false if not
	 */
	public boolean isFit(Individual individual) {
		if (individual.getTotalWeight() <= knapsackCapacity)
			return true;
		return false;
	}

	/**
	 * Checks if the an Individual with same chromosome is already present in the
	 * current population or not
	 * 
	 * @param c the chromosome to check
	 * @return true if present, false if not present
	 */
	private boolean chromosomeExists(StringBuilder c) {
		for (Individual i : currentPopulation) {
			if (i.getChromosome().toString().equals(c.toString()))
				return true;
		}
		return false;
	}

	private void cloneFromOld() {
		for (int i = 0; i < oldPopulation.size() / 2; i++) {
			currentPopulation.add(oldPopulation.get(i));
		}
	}

	/**
	 * Sorts the list based on their value
	 * @param individualList the list of Individual to be sorted
	 */
	public void sortByValue(List<Individual> individualList) {
		Collections.sort(individualList, (i1, i2) -> new Double((i2.getTotalValue() - i1.getTotalValue())).intValue());
	}

	/**
	 * Sorts the list based on their weight
	 * @param individualList  the list of Individual to be sorted
	 */
	public void sortByWeight(List<Individual> individualList) {
		Collections.sort(individualList,
				(i1, i2) -> new Double((i2.getTotalWeight() - i1.getTotalWeight())).intValue());
	}

	public void printList(List<Individual> list) {
		for (Individual i : list) {
			System.out.println(i.getChromosome() + " " + i.getTotalValue() + " " + i.getTotalWeight());
		}
	}

	public void addItem(int weight, int value) {
		items.add(new Item(weight, value));
		LOGGER.info("Value: " + value + " Weight: " + weight);
	}
}
