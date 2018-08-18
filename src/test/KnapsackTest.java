
package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import main.java.Individual;
import main.java.Knapsack;

public class KnapsackTest {

	@Test
	public void test1() {
		Knapsack ks = new Knapsack(50, 10, 10,true);
		assertTrue(ks.isFit(ks.currentPopulation.get(1)));

		StringBuilder[] oldC = { ks.currentPopulation.get(0).getChromosome(), ks.currentPopulation.get(1).getChromosome() };
		StringBuilder[] newC = ks.performCrossover(oldC[0], oldC[1]);

		assertNotEquals(oldC[0], newC[0]);
		assertNotEquals(oldC[1], newC[1]);
		assertNotNull(ks.mutate(oldC[0]));
		ks.makeGenerations();
	}

	@Test
	public void test2() {
		Knapsack ks = new Knapsack(50, 20, 10,false);
		assertFalse(ks.isFit(ks.currentPopulation.get(1)));
		assertNotNull(ks.mutate(ks.currentPopulation.get(2).getChromosome()));
		ks.makeGenerations();
	}

	@Test
	public void test3() {
		Knapsack ks = new Knapsack(99, 10, 20,false);
		ks.makeGenerations();
		assertTrue(ks.isFit(ks.currentPopulation.get(1)));

		StringBuilder[] oldC = { ks.currentPopulation.get(0).getChromosome(), ks.currentPopulation.get(0).getChromosome() };
		StringBuilder[] newC = ks.performCrossover(oldC[0], oldC[1]);
		assertEquals(oldC[0].toString(), newC[0].toString());
		assertEquals(oldC[1].toString(), newC[1].toString());
		assertNotNull(ks.mutate(oldC[1]));
	}

	@Test
	public void test4() {
		Knapsack ks = new Knapsack(10, 5, 5,true);
		
		ks.sortByWeight(ks.currentPopulation);
		assertTrue(10== ks.currentPopulation.get(0).getTotalValue());
		ks.makeGenerations();
		ks.sortByValue(ks.oldPopulation);
		assertTrue(9==ks.currentPopulation.get(0).getTotalWeight());
	}

	
	@Test
	public void test5() {
		
		Knapsack ks = new Knapsack(500, 5, 10,true);
		Individual individual =new Individual(new StringBuilder("1100010001"));
		assertTrue(209==individual.getTotalWeight());
		ks.makeGenerations();
	}
	@Test
	public void test6() {
		
		Knapsack ks = new Knapsack(500, 5, 10,false);
		Individual individual =new Individual(new StringBuilder("1100010001"));
		assertNotEquals(15,individual.getTotalValue());
		ks.makeGenerations();
	}
	
	@Test
	public void test7() {
		
		Knapsack ks = new Knapsack(1000, 500, 100,true);
		ks.accuracy=0.005;
		ks.makeGenerations();
		assertTrue(1525==ks.bestOfEachGeneration.get(0).getTotalValue());
	}
	
	@Test
	public void test8() {
		
		Knapsack ks = new Knapsack(10000, 2000, 500,true);
		ks.accuracy=0.01;
		ks.makeGenerations();
		assertTrue(9987==ks.bestOfEachGeneration.get(0).getTotalWeight());
	}
	
	
}
