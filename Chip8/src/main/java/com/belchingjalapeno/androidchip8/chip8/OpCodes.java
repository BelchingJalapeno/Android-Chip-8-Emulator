package com.belchingjalapeno.androidchip8.chip8;


import com.belchingjalapeno.androidchip8.chip8.graphics.Display;
import com.belchingjalapeno.androidchip8.chip8.graphics.Font;
import com.belchingjalapeno.androidchip8.chip8.timers.CountdownTimer;

import java.util.Random;

/**
 *
 */
public class OpCodes {

    private final Memory memory;
    private final Stack stack;
    private final Input input;
    private final CountdownTimer delayTimer;
    private final CountdownTimer soundTimer;
    private final Registers registers;
    private final Display display;
    private Random rand = new Random();

    public OpCodes(Memory memory, Stack stack, Input input, CountdownTimer delayTimer, CountdownTimer soundTimer, Registers registers, Display display) {
        this.memory = memory;
        this.stack = stack;
        this.input = input;
        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
        this.registers = registers;
        this.display = display;
    }

    public void opCode(int opCode) {
        switch (opCode & 0xF000) {
            case 0x0000:
                OxO(opCode);
                break;
            case 0x1000:
                Ox1(opCode);
                break;
            case 0x2000:
                Ox2(opCode);
                break;
            case 0x3000:
                Ox3(opCode);
                break;
            case 0x4000:
                Ox4(opCode);
                break;
            case 0x5000:
                Ox5(opCode);
                break;
            case 0x6000:
                Ox6(opCode);
                break;
            case 0x7000:
                Ox7(opCode);
                break;
            case 0x8000:
                Ox8(opCode);
                break;
            case 0x9000:
                Ox9(opCode);
                break;
            case 0xA000:
                OxA(opCode);
                break;
            case 0xB000:
                OxB(opCode);
                break;
            case 0xC000:
                OxC(opCode);
                break;
            case 0xD000:
                OxD(opCode);
                break;
            case 0xE000:
                OxE(opCode);
                break;
            case 0xF000:
                OxF(opCode);
                break;
        }
    }

    private void OxO(int opCode) {
        switch (getNNN(opCode)) {
            case 0x00E0://clears the screen
                display.clear();
                break;
            case 0x00EE://returns from a subroutine
                stack.pop();
                break;
            default://0NNN : calls RCA 1802 program at address NNN
                throw new UnknownOpcodeException(opCode);
        }
    }

    private void Ox1(int opCode) {
        //0x1NNN : jumps to address NNN
        memory.setProgramCounter((getNNN(opCode)));
    }

    private void Ox2(int opCode) {
        //0x2NNN : calls subroutine at NNN
        stack.push((getNNN(opCode)));
    }

    private void Ox3(int opCode) {
        //0x3XNN : skips the next instruction if VX equals NN
        int X = getXReg(opCode);
        if (registers.getRegisterData(X) == (getNN(opCode))) {
//            System.out.println("skip");
            memory.next();
        }
    }

    private void Ox4(int opCode) {
        //0x4XNN : skips the next instruction if VX doesn't equals NN
        int X = getXReg(opCode);
        if (registers.getRegisterData(X) != (getNN(opCode))) {
            memory.next();
        }
    }

    private void Ox5(int opCode) {
        if ((opCode & 0x000F) != 0x0000) {
            throw new UnknownOpcodeException(opCode);
        }
        //0x5XY0 : skips the next instruction if VX equals VY
        int X = getXReg(opCode);
        int Y = getYReg(opCode);
        if (registers.getRegisterData(X) == registers.getRegisterData(Y)) {
            memory.next();
        }
    }

    private void Ox6(int opCode) {
        //0x6XNN : sets VX to NN
        int X = getXReg(opCode);
        registers.setRegister(X, (short) (getNN(opCode)));
    }

    private int getXReg(int opCode) {
        return (0x0F00 & opCode) >> 8;
    }

    private void Ox7(int opCode) {
        //0x7XNN : adds NN to VX
        int X = getXReg(opCode);
        registers.setRegister(X,
                (short) (registers.getRegisterData(X)
                        + getNN(opCode)));
    }

    private int getNN(int opCode) {
        return 0x00FF & opCode;
    }

    private void Ox8(int opCode) {
        int X = getXReg(opCode);
        int Y = getYReg(opCode);
        switch (opCode & 0x000F) {
            case 0x0000://0x8XY0 : sets VX to the value at the address of VY
                registers.setRegister(X, registers.getRegisterData(Y));
                break;
            case 0x0001://0x8XY1 : Sets VX to VX or VY.
                registers.setRegister(X,
                        (short) (registers.getRegisterData(X) | registers.getRegisterData(Y)));
                break;
            case 0x0002://0x8XY2 : Sets VX to VX and VY.
                registers.setRegister(X,
                        (short) (registers.getRegisterData(X) & registers.getRegisterData(Y)));
                break;
            case 0x0003://0x8XY3 : Sets VX to VX xor VY.
                registers.setRegister(X,
                        (short) (registers.getRegisterData(X) ^ registers.getRegisterData(Y)));
                break;
            case 0x0004://0x8XY4 : Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                if (((int) registers.getRegisterData(X) + (int) registers.getRegisterData(Y)) > 0x00FF) {
                    registers.setRegister(0xF, (short) 0x01);
                } else {
                    registers.setRegister(0xF, (short) 0x00);
                }
                registers.setRegister(X, (short) (registers.getRegisterData(X) + registers.getRegisterData(Y)));
                break;
            case 0x0005://0x8XY5 : VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                if (((int) registers.getRegisterData(X) - (int) registers.getRegisterData(Y)) < 0x0000) {
                    registers.setRegister(0xF, (short) 0x00);
                } else {
                    registers.setRegister(0xF, (short) 0x01);
                }
                registers.setRegister(X,
                        (short) (registers.getRegisterData(X) - registers.getRegisterData(Y)));
                break;
            case 0x0006://0x8XY6 : Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.
                registers.setRegister(0xF, (short) (registers.getRegisterData(X) & 0b0000_0000_0000_0000_0000_0000_0000_0001));
                registers.setRegister(X, (short) ((((registers.getRegisterData(X) & 0xFFFE) >>> 1))));
                break;
            case 0x0007://0x8XY7 : Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                if (((int) registers.getRegisterData(Y) - (int) registers.getRegisterData(X)) < 0x0000) {
                    registers.setRegister(0xF, (short) 0x00);
                } else {
                    registers.setRegister(0xF, (short) 0x01);
                }
                registers.setRegister(X, (short) (registers.getRegisterData(Y) - registers.getRegisterData(X)));
                break;
            case 0x000E:/*0x8XYE : Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.*/
                registers.setRegister(0xF, (short) ((registers.getRegisterData(X) & 0b0000_0000_0000_0000_0000_0000_1000_0000) >> 7));
                registers.setRegister(X, (short) (((registers.getRegisterData(X) & 0x7F) << 1)));
                break;
            default:
                throw new UnknownOpcodeException(opCode);
        }
    }

    private void Ox9(int opCode) {
        if ((opCode & 0x000F) != 0x0000) {
            throw new UnknownOpcodeException(opCode);
        }
        //9XY0 : Skips the next instruction if VX doesn't equal VY.
        int X = (opCode & 0x0F00) >> 8;
        int Y = (opCode & 0x00F0) >> 4;
        if (registers.getRegisterData(X) != registers.getRegisterData(Y)) {
            memory.next();
        }
    }

    private void OxA(int opCode) {
        //0xANNN : Sets I to the address NNN.
        registers.setI(getNNN(opCode));
    }

    private void OxB(int opCode) {
        //0xBNNN : Jumps to the address NNN plus V0.
        memory.setProgramCounter(getNNN(opCode) + registers.getRegisterData(0));
    }

    private int getNNN(int opCode) {
        return opCode & 0x0FFF;
    }

    private void OxC(int opCode) {
        //0xCXNN : Sets VX to a random number and NN.
        int X = getXReg(opCode);
        registers.setRegister(X, (short) (rand.nextInt(256) & (short) (getNN(opCode))));
    }

    private void OxD(int opCode) {
        /* 0xDXYN : Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. Each row of 8 pixels is read as bit-coded (with the most significant bit of each short displayed on the left) starting from memory location I; I value doesn't change after the execution of this instruction. As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn't happen.*/
        int X = getXReg(opCode);
        int Y = getYReg(opCode);
        display.drawSprite(registers.getRegisterData(X),
                registers.getRegisterData(Y),
                getN(opCode));
    }

    private void OxE(int opCode) {
        int X = getXReg(opCode);
        switch (opCode & 0x00FF) {
            case 0x009E: //0xEX9E : Skips the next instruction if the key stored in VX is pressed.
                if (input.get(registers.getRegisterData(X))) {
                    memory.next();
                }
                break;
            case 0x00A1:// 0xEXA1 :  	Skips the next instruction if the key stored in VX isn't pressed.
                if (!input.get(registers.getRegisterData(X))) {
                    memory.next();
                }
                break;
            default:
                throw new UnknownOpcodeException(opCode);
        }
    }

    private void OxF(int opCode) {
        int X = getXReg(opCode);
        switch (opCode & 0x00FF) {
            case 0x0007://0xFX07 : Sets VX to the value of the delay timer.
                registers.setRegister(X, delayTimer.getCurrentCount());
                break;
            case 0x000A://0xFX0A : A key press is awaited, and then stored in VX.
                registers.setRegister(X, input.waitForKey());
                break;
            case 0x0015:// 0xFX15 : Sets the delay timer to VX.
                delayTimer.set(registers.getRegisterData(X));
                break;
            case 0x0018:// 0xFX18 : Sets the sound timer to VX.
                soundTimer.set(registers.getRegisterData(X));
                break;
            case 0x001E:// 0xFX1E : Adds VX to I.
                registers.setI(registers.getI() + registers.getRegisterData(X));
                break;
            case 0x0029:// 0xFX29 : Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                registers.setI(Font.get(registers.getRegisterData(X)));
                break;
            case 0x0033:// 0xFX33 : Stores the Binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                short value = registers.getRegisterData(X);
                short hundreds = 0;
                short tens = 0;
                short ones;
                if (value > 99) {
                    hundreds = (short) (value / 100);
                    value -= hundreds * 100;
                }
                if (value > 9) {
                    tens = (short) ((value) / 10);
                    value -= tens * 10;
                }
                ones = value;

                memory.setByte(registers.getI(), hundreds);
                memory.setByte(registers.getI() + 1, tens);
                memory.setByte(registers.getI() + 2, ones);
                break;
            case 0x0055:// 0xFX55 : Stores V0 to VX in memory starting at address I.
                for (int i = 0; i < X + 1; i++) {
                    memory.setByte(registers.getI() + i, registers.getRegisterData(i));
                }
                break;
            case 0x0065:// 0xFX65 : Fills V0 to VX with values from memory starting at address I.
                for (int i = 0; i < X + 1; i++) {
                    registers.setRegister(i, memory.getByte(registers.getI() + i));
                }
                break;
            default:
                throw new UnknownOpcodeException(opCode);
        }
    }

    /**
     * @param random
     */
    public void setRandom(Random random) {
        this.rand = random;
    }

    private int getN(int opCode) {
        return 0x000F & opCode;
    }

    private int getYReg(int opCode) {
        return (0x00F0 & opCode) >> 4;
    }

}
