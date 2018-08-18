package main.java;

public class Item {

	private double weight;
	private double value;

	Item(double weight, double value) {
		this.weight = weight;
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}

	public double getValue() {
		return value;
	}

}