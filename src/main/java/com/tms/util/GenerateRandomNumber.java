package com.tms.util;

import java.util.Random;

public class GenerateRandomNumber {

   public static String generateNum(){
       Random randomNum = new Random();
       int number = randomNum.nextInt(9999);
       String str = "TID-" + number;
       return String.format(str);

   }
}
