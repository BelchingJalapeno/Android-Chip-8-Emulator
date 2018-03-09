package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

import com.belchingjalapeno.androidchip8.Util;

/**
 * Thrown by the {@link com.belchingjalapeno.androidchip8.assembly.assembler.Assembler} when an invalid number is found, such as
 * things that are not numbers or numbers that are outside the valid range( {@code < 0} or {@code > 256} when looking
 * for a byte for example).
 **/
public class InvalidNumberException extends AssemblerException {

    public InvalidNumberException(String number, int lineNumber, int start, int min, int max) {
        super("Invalid number  : " +
                        (!Util.isNumber(number) ? "NaN : " + number
                                : Util.isDecimal(number) ? number + " : expected between " + min + " - " + max
                                : Util.isHex(number) ? number + " : expected between 0x" + Integer.toHexString(min) + " - 0x" + Integer.toHexString(max)
                                : number + " : expected between 0b" + Integer.toBinaryString(min) + " - 0b" + Integer.toBinaryString(max)
                        )
                , lineNumber, start
        );
    }
}
