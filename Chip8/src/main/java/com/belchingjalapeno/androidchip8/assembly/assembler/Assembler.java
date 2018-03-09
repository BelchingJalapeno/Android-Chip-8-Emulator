package com.belchingjalapeno.androidchip8.assembly.assembler;

import com.belchingjalapeno.androidchip8.Util;
import com.belchingjalapeno.androidchip8.assembly.Assembly;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.AssemblerException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidJumpLocationException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidLabelNameException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidNumberException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidRegisterException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.UnsupportedOpCodeException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.UnsupportedOperandException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Turns human readable assembly into machine readable code.
 */
public class Assembler {

    private static final String hexDigitRegex = "[0-9a-fA-f]";
    private static final String binaryRegex = "0[bB](0|1)";
    private final HashMap<String, Integer> labelMap = new HashMap<>();
    private final ArrayList<Short> assembled = new ArrayList<>();
    private int isNumberCount = 0;
    private int lineNumber = 0;
    private int charCount = 0;

    public short[] assemble(String text) throws AssemblerException {
        resetState();
        String[] lines = splitAndClean(text);

        int lineCount = 0x200;
        //first pass, run through all lines and build a map of labels to memory locations
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            //if the line starts with . it must be a label, so add it to the jump map
            if (line.startsWith(".")) {
                String label = line.split(" ")[0];
                if (labelMap.containsKey(label)) {
                    throw new InvalidLabelNameException("Label already defined : " + label, lineNumber, charCount);
                }
                if (lineCount % 2 != 0 && nextLineData(lines, i)) {
                    lineCount++;
                }
                labelMap.put(label, lineCount);
            } else if (isNumb(line.trim())) {
                lineCount++;
            } else {
                final String trim = line.replace('\n', ' ').replace('\r', ' ').trim();
                if (!(line.startsWith(".") || trim.isEmpty() || trim.startsWith(";"))) {
                    lineCount += 2;
                }
            }
        }
        //2nd pass
        for (String line : lines) {
            lineNumber++;
            //remove comments
            if (!line.startsWith(";")) {
                line = line.split(";")[0];
            } else {
                line = "";
            }
            line = line.trim();
            //if it starts with a . it is a label, so we can ignore it
            if (line.startsWith(".") || line.replace('\n', ' ').replace('\r', ' ').trim().isEmpty()) {

            } else if (isNumb(line.trim())) {
                assembled.add((short) getNumb8Bits(line.trim()));
                isNumberCount++;
            } else {
                checkNumberCount();
                String[] spaceSplit = line.split(" ");
                List<String> sp = new ArrayList<>(spaceSplit.length);
                for (int i = 0; i < spaceSplit.length; i++) {
                    spaceSplit[i] = spaceSplit[i].trim();
                    if (!spaceSplit[i].isEmpty()) {
                        sp.add(spaceSplit[i]);
                    }
                }
                spaceSplit = sp.toArray(new String[0]);
                int split = split(spaceSplit);
                assembled.add((short) ((split >> 8) & 0xFF));
                assembled.add((short) ((split) & 0xFF));
            }
            charCount += line.length() + 1;// add this line plus the new line that was taken out when we split the string into a string[]
        }
        checkNumberCount();
        short[] shorts = new short[assembled.size()];
        for (int i = 0; i < assembled.size(); i++) {
            shorts[i] = assembled.get(i);
        }
        return shorts;
    }

    private void resetState() {
        assembled.clear();
        labelMap.clear();

        isNumberCount = 0;
        lineNumber = 0;
        charCount = 0;
    }

    /**
     * Splits the text by \n, and {@link String#trim()}s all of those splits.
     *
     * @param text the text to split and clean up
     * @return the text, split by {@code '\n'}, and {@link String#trim()}ed.
     */
    private String[] splitAndClean(String text) {
        String[] split = text.split("\n");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    /**
     * Searches to see if the next line in the text is a data line, used to see if we need to add an extra byte to
     * make it an even number.
     *
     * @param lines the place to search
     * @param start the starting place
     * @return if the next line is a data line instead of
     */
    private boolean nextLineData(String[] lines, int start) {
        for (int j = start + 1; j < lines.length; j++) {
            String line = lines[j];
            if (line.startsWith(".")) {
                continue;
            } else if (isNumb(line.trim())) {
                return false;
            } else {
                final String trim = line.replace('\n', ' ').replace('\r', ' ').trim();
                if (!(line.startsWith(".") || trim.isEmpty() || trim.startsWith(";"))) {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if we need to add an extra byte to the end of the assembled data
     */
    private void checkNumberCount() {
        if (isNumberCount > 0) {
            //if its odd, add another byte to make it even
            if (isNumberCount % 2 != 0) {
                assembled.add((short) 0x00);
            }
            isNumberCount = 0;
        }
    }

    private int split(String[] spaceSplit) throws AssemblerException {
        final String opcode = spaceSplit[0];
        if (isOpcode(opcode, Assembly.MOV)) {
            return mov(spaceSplit);
        } else if (isOpcode(opcode, Assembly.ADD)) {
            return add(spaceSplit);
        } else if (isOpcode(opcode, Assembly.JMP)) {
            return jumps(0x1000, spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.DRW)) {
            return 0xD000 + getVxAndVyDraw(spaceSplit) + getNumb4Bits(spaceSplit[3]);
        } else if (isOpcode(opcode, Assembly.CLS)) {
            return 0x00E0;
        } else if (isOpcode(opcode, Assembly.CAL)) {
            return jumps(0x2000, spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.RET)) {
            return 0x00EE;
        } else if (isOpcode(opcode, Assembly.EQ)) {
            return eq(spaceSplit);
        } else if (isOpcode(opcode, Assembly.NEQ)) {
            return neq(spaceSplit);
        } else if (isOpcode(opcode, Assembly.AND)) {
            return 0x8002 + getVxAndVy(spaceSplit);
        } else if (isOpcode(opcode, Assembly.OR)) {
            return 0x8001 + getVxAndVy(spaceSplit);
        } else if (isOpcode(opcode, Assembly.XOR)) {
            return 0x8003 + getVxAndVy(spaceSplit);
        } else if (isOpcode(opcode, Assembly.SHL)) {
            return 0x800E + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.SHR)) {
            return 0x8006 + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.RND)) {
            return 0xC000 + getVx(spaceSplit[1]) + getNumb8Bits(spaceSplit[2]);
        } else if (isOpcode(opcode, Assembly.KP)) {
            return 0xE09E + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.KNP)) {
            return 0xE0A1 + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.KW)) {
            return 0xF00A + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.BCD)) {
            return 0xF033 + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.STR)) {
            return 0xF055 + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.LOD)) {
            return 0xF065 + getVx(spaceSplit[1]);
        } else if (isOpcode(opcode, Assembly.SUB)) {
            return 0x8005 + getVxAndVy(spaceSplit);
        } else if (isOpcode(opcode, Assembly.SUBY)) {
            return 0x8007 + getVxAndVy(spaceSplit);
        } else if (isOpcode(opcode, Assembly.JMP0)) {
            return jumps(0xB000, spaceSplit[1]);
        }
        throw new UnsupportedOpCodeException(lineNumber, charCount, opcode, Util.joinStrings(spaceSplit, " "));
    }

    private boolean isOpcode(String opcode, String add) {
        return opcode.equalsIgnoreCase(add);
    }

    private int jumps(int opcode, String key) throws InvalidJumpLocationException, InvalidNumberException {
        final Integer jumpMapLocation = labelMap.get(key);
        if (jumpMapLocation == null) {
            if (isNumb(key)) {
                return opcode + getNumb12Bits(key);
            }
            throw new InvalidJumpLocationException("No jump found for : " + key, lineNumber, charCount);
        }
        return opcode + jumpMapLocation;
    }

    private int eq(String[] spaceSplit) throws InvalidRegisterException, InvalidNumberException, UnsupportedOperandException {
        if (isValidVReg(spaceSplit[1])) {
            if (isNumb(spaceSplit[2])) {
                return 0x3000 + getVx(spaceSplit[1]) + getNumb8Bits(spaceSplit[2]);
            } else if (isValidVReg(spaceSplit[2])) {
                return 0x5000 + getVxAndVy(spaceSplit);
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        }
        throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 1);
    }

    private int neq(String[] spaceSplit) throws InvalidRegisterException, InvalidNumberException, UnsupportedOperandException {
        if (isNumb(spaceSplit[2])) {
            return 0x4000 + getVx(spaceSplit[1]) + getNumb8Bits(spaceSplit[2]);
        } else if (isValidVReg(spaceSplit[2])) {
            return 0x9000 + getVxAndVy(spaceSplit);
        }
        throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
    }

    private int getVxAndVyDraw(String[] spaceSplit) throws AssemblerException {
        return getVxAndVy(spaceSplit[1].replace('[', ' ').replace(']', ' ').trim(), spaceSplit[2].replace('[', ' ').replace(']', ' ').trim());
    }

    private int getVxAndVy(String[] spaceSplit) throws InvalidRegisterException {
        return getVxAndVy(spaceSplit[1], spaceSplit[2]);
    }

    private int add(String[] spaceSplit) throws AssemblerException {
        if (isValidVReg(spaceSplit[1])) {
            if (isValidVReg(spaceSplit[2])) {
                return 0x8004 + getVxAndVy(spaceSplit[1], spaceSplit[2]);
            } else if (isNumb(spaceSplit[2])) {
                return 0x7000 + getVx(spaceSplit[1]) + getNumb8Bits(spaceSplit[2]);
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        } else if (isValidIReg(spaceSplit[1])) {
            return 0xF01E + getVx(spaceSplit[2]);
        }
        throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 1);
    }

    private int getVxAndVy(String vx, String vy) throws InvalidRegisterException {
        return (getVx(vx)) + (getVReg(vy) << 4);
    }

    private int mov(String[] spaceSplit) throws AssemblerException {
        if (spaceSplit.length > 3) {
            throw new UnsupportedOperandException(spaceSplit.length - 3 + " extra parameters for MOV", lineNumber, charCount, "MOV", spaceSplit, 4);
        }
        if (isValidVReg(spaceSplit[1])) {
            if (isNumb(spaceSplit[2])) {
                return 0x6000 + getVx(spaceSplit[1]) + getNumb8Bits(spaceSplit[2]);
            } else if (isValidVReg(spaceSplit[2])) {
                return 0x8000 + getVxAndVy(spaceSplit[1], spaceSplit[2]);
            } else if (isValidDelayTimer(spaceSplit[2])) {
                return 0xF007 + (getVx(spaceSplit[1]));
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        } else if (isValidIReg(spaceSplit[1])) {
            if (labelMap.containsKey(spaceSplit[2])) {
                return 0xA000 + labelMap.get(spaceSplit[2]);
            } else if (isNumb(spaceSplit[2])) {
                return 0xA000 + getNumb12Bits(spaceSplit[2]);
            } else if (isValidVReg(spaceSplit[2])) {
                return 0xF029 + (getVx(spaceSplit[2]));
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        } else if (isValidDelayTimer(spaceSplit[1])) {
            if (isValidVReg(spaceSplit[2])) {
                return 0xF015 + getVx(spaceSplit[2]);
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        } else if (isSoundTimer(spaceSplit[1])) {
            if (isValidVReg(spaceSplit[2])) {
                return 0xF018 + getVx(spaceSplit[2]);
            }
            throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 2);
        }
        throw new UnsupportedOperandException(lineNumber, charCount, spaceSplit[0], spaceSplit, 1);
    }

    private boolean isSoundTimer(String s) {
        return s.equalsIgnoreCase(Assembly.SOUND_TIMER);
    }

    private int getVx(String text) throws InvalidRegisterException {
        return getVReg(text) << 8;
    }

    private int getNumb8Bits(String text) throws InvalidNumberException {
        if (isValidHex8Bits(text)) {
            return getHex(text);
        } else if (isValidDecimal8Bits(text)) {
            return getDecimal(text);
        } else if (isValidBinary8Bits(text)) {
            return getBinary(text);
        }
        throw new InvalidNumberException(text, lineNumber, charCount, 0, 0xFF);
    }

    private int getNumb12Bits(String text) throws InvalidNumberException {
        if (isValidHex12Bits(text)) {
            return getHex(text);
        } else if (isValidDecimal12Bits(text)) {
            return getDecimal(text);
        } else if (isValidBinary12Bits(text)) {
            return getBinary(text);
        }
        throw new InvalidNumberException(text, lineNumber, charCount, 0, 0xFFF);
    }

    private int getNumb4Bits(String text) throws InvalidNumberException {
        if (isValidHex4Bits(text)) {
            return getHex(text);
        } else if (isValidDecimal4Bits(text)) {
            return getDecimal(text);
        } else if (isValidBinary4Bits(text)) {
            return getBinary(text);
        }
        throw new InvalidNumberException(text, lineNumber, charCount, 0, 0xF);
    }

    private boolean isValidDelayTimer(String text) {
        return text.equalsIgnoreCase(Assembly.DELAY_TIMER);
    }

    private boolean isValidIReg(String text) {
        return text.equalsIgnoreCase("I");
    }

    private boolean isValidVReg(String text) {
        return text.matches("[vV]" + hexDigitRegex);
    }

    private boolean isNumb(String text) {
        return Util.isNumber(text);
    }

    private boolean isValidHex8Bits(String text) {
        return text.matches("0[xX]" + hexDigitRegex + "{1,2}");
    }

    private boolean isValidBinary8Bits(String text) {
        return text.matches(binaryRegex + "{1,8}");
    }

    /**
     * if its a valid decimal number going from 0-255
     *
     * @param text
     * @return
     */
    private boolean isValidDecimal8Bits(String text) {
        return text.matches("[2][0-4][0-9]" +//matches 200-249
                "|[2][5][0-5]" + //matches 250-255
                "|[0-1]{0,1}\\d{1,2}");//matches 0-199
    }

    private boolean isValidHex12Bits(String text) {
        return text.matches("0[xX]" + hexDigitRegex + "{1,3}");
    }

    private boolean isValidBinary12Bits(String text) {
        return text.matches(binaryRegex + "{1,12}");
    }

    /**
     * if its a valid decimal number going from 0-255
     *
     * @param text
     * @return
     */
    private boolean isValidDecimal12Bits(String text) {
        return text.matches("[4][0][9][0-5]" +//matches 4090-4095
                "|[4][0][0-8][0-9]" + //matches 4000-4089
                "|[0-3]{0,1}\\d{1,3}");//matches 0-3999
    }

    private boolean isValidHex4Bits(String text) {
        return text.matches("0[xX]" + hexDigitRegex + "{1}");
    }

    private boolean isValidBinary4Bits(String text) {
        return text.matches(binaryRegex + "{1,4}");
    }

    /**
     * if its a valid decimal number going from 0-255
     *
     * @param text
     * @return
     */
    private boolean isValidDecimal4Bits(String text) {
        return text.matches("[1][0-5]" +//matches 10-15
                "|[0-9]");//matches 0-9
    }

    private int getVReg(String text) throws InvalidRegisterException {
        try {
            return Integer.valueOf(text.substring(1), 16);
        } catch (NumberFormatException e) {
            throw new InvalidRegisterException(text, lineNumber, charCount);
        }
    }

    private int getHex(String text) {
        //chops off the first 2 characters because they should be 0x
        return Integer.valueOf(text.substring(2), 16);
    }

    private int getBinary(String text) {
        //chops off the first 2 characters because they should be 0b
        return Integer.valueOf(text.substring(2), 2);
    }

    private int getDecimal(String text) {
        return Integer.valueOf(text);
    }

}
