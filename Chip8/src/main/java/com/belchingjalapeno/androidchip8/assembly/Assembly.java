package com.belchingjalapeno.androidchip8.assembly;

/**
 * Holds all the {@link String} representations
 * of all the valid opcodes as well as the special registers.
 */
public class Assembly {
    //all valid opcodes
    public static final String MOV = "MOV";
    public static final String ADD = "ADD";
    public static final String JMP = "JMP";
    public static final String DRW = "DRW";
    public static final String CLS = "CLS";
    public static final String CAL = "CAL";
    public static final String RET = "RET";
    public static final String EQ = "EQ";
    public static final String NEQ = "NEQ";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String XOR = "XOR";
    public static final String SHL = "SHL";
    public static final String SHR = "SHR";
    public static final String RND = "RND";
    public static final String KP = "KP";
    public static final String KNP = "KNP";
    public static final String KW = "KW";
    public static final String BCD = "BCD";
    public static final String STR = "STR";
    public static final String LOD = "LOD";
    public static final String SUB = "SUB";
    public static final String SUBY = "SUBY";
    public static final String JMP0 = "JMP0";

    //special registers
    public static final String DELAY_TIMER = "DT";
    public static final String SOUND_TIMER = "ST";
    public static final String I = "I";
}
