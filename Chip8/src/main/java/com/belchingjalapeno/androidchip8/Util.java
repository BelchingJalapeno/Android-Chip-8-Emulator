package com.belchingjalapeno.androidchip8;

import java.util.regex.Pattern;

/**
 * Miscellaneous functions used throughout the program ranging from number checks to string joins.
 */
public class Util {

    private static final String hexRegex = "-?0[xX][0-9a-fA-f].*";
    private static final String binaryRegex = "-?0[bB][0,1].*";
    private static final String decimalRegex = "-?[0-9].*";

    private static final Pattern hexPattern = Pattern.compile(hexRegex);
    private static final Pattern binaryPattern = Pattern.compile(binaryRegex);
    private static final Pattern decimalPattern = Pattern.compile(decimalRegex);

    /**
     * Matches {@code 0x} or {@code 0X} followed hexadecimal digits (0 to F).
     *
     * @param text
     * @return if it is a valid hex number.
     */
    public static boolean isHex(String text) {
        return hexPattern.matcher(text).matches();
    }

    /**
     * Matches {@code 0b} or {@code 0B} followed binary digits (0 or 1).
     *
     * @param text
     * @return if it is a valid binary number.
     */
    public static boolean isBinary(String text) {
        return binaryPattern.matcher(text).matches();
    }

    /**
     * Matches decimal digits (0 to 9).
     *
     * @param text
     * @return if it is a valid decimal number.
     */
    public static boolean isDecimal(String text) {
        return decimalPattern.matcher(text).matches();
    }

    /**
     * Checks if the text is a valid number, which just checks if {@link #isBinary(String)} || {@link #isHex(String)}
     * || {@link #isDecimal(String)}.
     *
     * @param text
     * @return if the text is a valid number
     */
    public static boolean isNumber(String text) {
        return isDecimal(text) || isHex(text) || isBinary(text);
    }

    /**
     * Takes an array of {@link String}s and combines them together seperated by the separator or nothing if
     * the separator is {@code null}.
     *
     * @param strings   if {@code null} returns null, if {@code strings.length == 0} returns
     *                  empty {@link String} otherwise, returns the string array combined into one String with
     *                  the separator between each String
     * @param separator the string to be inserted between all {@code strings}
     * @return the combined String
     */
    public static String joinStrings(String[] strings, String separator) {
        if (strings == null) {
            return null;
        }
        if (strings.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(strings.length);
        for (int i = 0; i < strings.length; i++) {
            builder.append(strings[i]);
            /* As long as the separator isn't null
             * and it is not last String in the Array
             */
            if (separator != null && i < strings.length - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    /**
     * Takes an array of {@link String}s and combines them together.
     *
     * @param strings if {@code null} returns null, if {@code strings.length == 0} returns
     *                empty {@link String} otherwise, returns the string array combined into one String
     * @return the combined String
     */
    public static String joinStrings(String[] strings) {
        return joinStrings(strings, null);
    }

    /**
     * Takes a number and makes it into a byte ranging from 0 to 255, overflowing or underflowing if it is
     * greater or less than.
     *
     * @param number the number to wrap
     * @return a number in the range of 0 - 255
     */
    public static short wrapToByte(short number) {
        // wrap between 0 - 255
        number %= 256;//now range is -255 to 255
        //add 256 to it to wrap the number if it was negative
        number += 256;// now range is 1 - 511
        //wrap back to 0 -255 if the number was not negative
        number %= 256;// now range is 0 to 255
        return number;
    }
}
