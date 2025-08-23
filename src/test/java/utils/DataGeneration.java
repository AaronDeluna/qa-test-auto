package utils;

import java.util.Random;

public class DataGeneration {

    public static String generateEmail() {
        return String.format("test%s@gmail.com", System.currentTimeMillis());
    }

//    public static String generatePassword(int passwordSize) {
//
//    }

    public static String generateName() {
        return String.format("Test name %s", System.currentTimeMillis());
    }

    public static String generatePhone() {
        int min = 1000000;
        int max = 9999999;
        return String.format("+7988%s", new Random().nextInt(min, max));
    }

}
