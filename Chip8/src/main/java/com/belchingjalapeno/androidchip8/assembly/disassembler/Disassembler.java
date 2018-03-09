package com.belchingjalapeno.androidchip8.assembly.disassembler;

import com.belchingjalapeno.androidchip8.assembly.Assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Turns machine readable code into human readable assembly.
 */
public class Disassembler {

    private Map<Short, String> jumpMap = new HashMap<Short, String>();
    private Map<Short, String> subMap = new HashMap<Short, String>();
    private Set<Short> unReachable = new HashSet<>();

    public String disAssemble(short[] opCodes) {
        jumpMap = new HashMap<>();
        subMap = new HashMap<>();
        unReachable = new HashSet<>();
        buildJumpMap(opCodes);
        String st = "";
        for (int i = 0; i < opCodes.length; i += 2) {
            short s = opCodes[i];
            if (i + 1 >= opCodes.length) {
                break;
            }
            s = (short) ((s << 8) + (opCodes[i + 1]));
            short key = (short) (i + 0x200);
            if (jumpMap.containsKey(key)) {
                String s1 = jumpMap.get(key);
                st += "\n.LABEL:" + s1 + "\n";
            }
            if (subMap.containsKey(key)) {
                String s1 = subMap.get(key);
                st += "\n.SUB:" + s1 + "\n";
            }
            if (unReachable.contains(key)) {
                st += "0b" + Integer.toBinaryString(opCodes[i]) + "\n";
                st += "0b" + Integer.toBinaryString(opCodes[i + 1]);
            } else {
                String str = disAssemble(s);
                st += str;
            }
            st += ("\t\t;0x" + Integer.toHexString(i + 0x200) + "\n");
        }
        return st.trim();
    }

    private void buildJumpMap(short[] opCodes) {
        for (int i = 0; i < opCodes.length; i += 2) {
            if (i + 1 >= opCodes.length) {
                break;
            }
            short opCode = (short) ((opCodes[i] << 8) + opCodes[i + 1]);
            short key = (short) (opCode & 0x0FFF);
            if (key % 2 == 0) {
                switch ((opCode & 0xF000) >> 12) {
                    case 0x1:
                        if (!jumpMap.containsKey(key)) {
                            jumpMap.put(key, "" + jumpMap.size());
                        }
                        break;
                    case 0x2:
                        if (!subMap.containsKey(key)) {
                            subMap.put(key, "" + subMap.size());
                        }
                        break;
                    case 0xA:
                        if (!jumpMap.containsKey(key) && key >= 0x200) {
                            jumpMap.put(key, "" + jumpMap.size());
                            this.unReachable.add(key);
                        }
                        break;
                    case 0xB:
                        if (!jumpMap.containsKey(key)) {
                            jumpMap.put(key, "" + jumpMap.size());
                        }
                        break;
                }
            }
        }
        boolean unreachable = false;
        short line = 0x200;
        for (int i = 0; i < opCodes.length; i += 2) {
            line = (short) ((short) 0x200 + (short) i);

            if (this.unReachable.contains(line)) {
                unreachable = true;
            } else if (unreachable) {
                if ((jumpMap.containsKey(line) || subMap.containsKey(line))) {
                    unreachable = false;
                } else {
                    this.unReachable.add(line);
                }
            }
        }
    }

    private String disAssemble(int opCode) {
        short key = (short) (opCode & 0x0FFF);
        switch ((opCode & 0xF000) >> 12) {
            case 0x0:
                switch (opCode & 0x0FFF) {
                    case 0x00E0:
                        return Assembly.CLS;
                    case 0x00EE:
                        return Assembly.RET;
                    default:
                }
            case 0x1:
                return getJumpOperand(Assembly.JMP, key);
            case 0x2:
                return getSubroutineOperand(Assembly.CAL, key);
            case 0x3:
                return Assembly.EQ + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " 0x" + Integer.toHexString(0x00FF & opCode).toUpperCase();
            case 0x4:
                return Assembly.NEQ + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " 0x" + Integer.toHexString(0x00FF & opCode).toUpperCase();
            case 0x5:
                return Assembly.EQ + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " " + " V" + Integer.toHexString((0x00F0 & opCode) >> 4).toUpperCase();
            case 0x6:
                return Assembly.MOV + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " 0x" + Integer.toHexString((short) (0x00FF & opCode)).toUpperCase();
            case 0x7:
                return Assembly.ADD + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " 0x" + Integer.toHexString(0x00FF & opCode).toUpperCase();
            case 0x8:
                return Ox8(opCode);
            case 0x9:
                return Assembly.NEQ + " V" + Integer.toHexString((opCode & 0x0F00) >> 8).toUpperCase() + " V" + Integer.toHexString((opCode & 0x00F0) >> 4).toUpperCase();
            case 0xA:
                return getMovIOperand(Assembly.MOV, key);
            case 0xB:
                return getJumpOperand(Assembly.JMP0, key);
            case 0xC:
                return Assembly.RND + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + " 0x" + Integer.toHexString((short) (0x00FF & opCode)).toUpperCase();
            case 0xD:
                int X = (0x0F00 & opCode) >> 8;
                int Y = (0x00F0 & opCode) >> 4;
                int height = 0x000F & opCode;
                return Assembly.DRW + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase() + " " + height;
            case 0xE:
                return OxE(opCode);
            case 0xF:
                return OxF(opCode);
            default:
        }
        throw new UnsupportedOperationException();
    }

    private String getJumpOperand(String opCode, short key) {
        if (jumpMap.containsKey(key)) {
            return opCode + " .LABEL:" + jumpMap.get(key);
        } else {
            return opCode + " 0x" + Integer.toHexString(key);
        }
    }

    private String getSubroutineOperand(String opCode, short key) {
        if (subMap.containsKey(key)) {
            return opCode + " .SUB:" + subMap.get(key);
        } else {
            return opCode + " 0x" + Integer.toHexString(key);
        }
    }

    private String getMovIOperand(String opCode, short key) {
        if (jumpMap.containsKey(key)) {
            return opCode + " " + Assembly.I + " .LABEL:" + jumpMap.get(key);
        } else {
            return opCode + " " + Assembly.I + " 0x" + Integer.toHexString(key);
        }
    }

    private String Ox8(int opCode) {
        int X = (0x0F00 & opCode) >> 8;
        int Y = (0x00F0 & opCode) >> 4;
        switch (opCode & 0x000F) {
            case 0x0000://0x8XY0 : sets VX to the value at the address of VY
                return Assembly.MOV + " V" + Integer.toHexString((0x0F00 & opCode) >> 8).toUpperCase() + "  V" + Integer.toHexString((0x00F0 & opCode) >> 4).toUpperCase() + "";
            case 0x0001://0x8XY1 : Sets VX to VX or VY.
                return Assembly.OR + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x0002://0x8XY2 : Sets VX to VX and VY.
                return Assembly.AND + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x0003://0x8XY3 : Sets VX to VX xor VY.
                return Assembly.XOR + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x0004://0x8XY4 : Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                return Assembly.ADD + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x0005://0x8XY5 : VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                return Assembly.SUB + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
//                return "SYC V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x0006://0x8XY6 : Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift. //VY is ignored as it used to be used in the original interpreter but not in modern day implementations
                return Assembly.SHR + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0007://0x8XY7 : Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't
                return Assembly.SUBY + " V" + Integer.toHexString(X).toUpperCase() + " V" + Integer.toHexString(Y).toUpperCase();
            case 0x000E:/*0x8XYE : Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.*/ //VY is ignored as it used to be used in the original interpreter but not in modern day implementations
                return Assembly.SHL + " V" + Integer.toHexString(X).toUpperCase();
        }
        return "0x" + Integer.toHexString(opCode);
    }

    private String OxE(int opCode) {
        int X = (0x0F00 & opCode) >> 8;
        switch (opCode & 0x00FF) {
            case 0x009E: //0xEX9E : Skips the next instruction if the key stored in VX is pressed.
                return Assembly.KP + " V" + Integer.toHexString(X).toUpperCase();
            case 0x00A1:// 0xEXA1 :  	Skips the next instruction if the key stored in VX isn't pressed.
                return Assembly.KNP + " V" + Integer.toHexString(X).toUpperCase();
        }
        return "0x" + Integer.toHexString(opCode).toUpperCase();
    }

    private String OxF(int opCode) {
        int X = (0x0F00 & opCode) >> 8;
        switch (opCode & 0x00FF) {
            case 0x0007://0xFX07 : Sets VX to the value of the delay timer.
                return Assembly.MOV + " V" + Integer.toHexString(X).toUpperCase() + " " + Assembly.DELAY_TIMER;
            case 0x000A://0xFX0A : A key press is awaited, and then stored in VX.
                return Assembly.KW + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0015:// 0xFX15 : Sets the delay timer to VX.
                return Assembly.MOV + " " + Assembly.DELAY_TIMER + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0018:// 0xFX18 : Sets the sound timer to VX.
                return Assembly.MOV + " " + Assembly.SOUND_TIMER + " V" + Integer.toHexString(X).toUpperCase();
            case 0x001E:// 0xFX1E : Adds VX to I.
                return Assembly.ADD + " " + Assembly.I + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0029:// 0xFX29 : Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                return Assembly.MOV + " " + Assembly.I + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0033:// 0xFX33 : Stores the Binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                return Assembly.BCD + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0055:// 0xFX55 : Stores V0 to VX in memory starting at address I.
                return Assembly.STR + " V" + Integer.toHexString(X).toUpperCase();
            case 0x0065:// 0xFX65 : Fills V0 to VX with values from memory starting at address I.
                return Assembly.LOD + " V" + Integer.toHexString(X).toUpperCase();
        }
        return "0x" + Integer.toHexString(opCode).toUpperCase();
    }
}
