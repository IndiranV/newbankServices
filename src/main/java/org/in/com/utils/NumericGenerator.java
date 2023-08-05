package org.in.com.utils;

import java.util.Random;

public class NumericGenerator {

	public static int randInt() {
		Random rand = new Random();
		int randomNum = 100000 + rand.nextInt(900000);
		return randomNum;
	}
 
}
