package com.belchingjalapeno.androidchip8.assembly.assembler;

import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidJumpLocationException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidLabelNameException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidNumberException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.InvalidRegisterException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.UnsupportedOpCodeException;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.UnsupportedOperandException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AssemblerTest {

    @Rule
    public ExpectedException rule = ExpectedException.none();
    private Assembler assembler = new Assembler();

    @Test
    public void invalidJumpException() throws Exception {
        rule.expect(InvalidJumpLocationException.class);
        assembler.assemble("jmp .lab");
    }

    @Test
    public void testUnsupportedOpcodeException1() throws Exception {
        rule.expect(UnsupportedOpCodeException.class);
        assembler.assemble("a mov v0 v1");
    }

    @Test
    public void testUnsupportedOpcodeException2() throws Exception {
        rule.expect(UnsupportedOpCodeException.class);
        assembler.assemble("mova v0 v1");
    }

    @Test
    public void testUnsupportedOpcodeException3() throws Exception {
        rule.expect(UnsupportedOpCodeException.class);
        assembler.assemble("aMOV v0 v1");
    }

    @Test
    public void test12BitMaxRange() throws Exception {
        assertArrayEquals(new short[]{0x1F, 0xFF}, assembler.assemble("jmp 4095"));

        rule.expect(InvalidNumberException.class);
        assembler.assemble("jmp 4096");
    }

    @Test
    public void test12BitMinRange() throws Exception {
        assertArrayEquals(new short[]{0x10, 0x00}, assembler.assemble("jmp 000"));

        rule.expect(InvalidNumberException.class);
        assembler.assemble("jmp -1");
    }

    @Test
    public void labelTest() throws Exception {
        assembler.assemble(" .lab\njmp .lab");
        rule.expect(InvalidLabelNameException.class);
        assembler.assemble(".lab\n jmp .lab\n.lab");
    }

    @Test
    public void testUnsupportedOperandException1() throws Exception {
        rule.expect(UnsupportedOperandException.class);
        assembler.assemble("mov a v0 v1");
    }

    @Test
    public void testUnsupportedOperandException2() throws Exception {
        rule.expect(UnsupportedOperandException.class);
        assembler.assemble("mov v0 a v1");
    }

    @Test
    public void testUnsupportedOperandException3() throws Exception {
        rule.expect(UnsupportedOperandException.class);
        assembler.assemble("mov v0 v1 a");
    }

    @Test
    public void testDecimalRange() throws Exception {
        rule.expect(InvalidNumberException.class);
        assembler.assemble("mov v0 256");
        rule.expect(InvalidNumberException.class);
        assembler.assemble("mov v0 -1");
        rule.expect(InvalidNumberException.class);
        assembler.assemble("mov v0 300");
    }

    @Test
    public void testDrawNumberRange() throws Exception {
        assertArrayEquals(new short[]{0xD0, 0x10}, assembler.assemble("drw v0 v1 0"));
        assertArrayEquals(new short[]{0xD0, 0x11}, assembler.assemble("drw v0 v1 1"));
        assertArrayEquals(new short[]{0xD0, 0x1E}, assembler.assemble("drw v0 v1 14"));
        assertArrayEquals(new short[]{0xD0, 0x1F}, assembler.assemble("drw v0 v1 15"));
        rule.expect(InvalidNumberException.class);
        assembler.assemble("drw v0 v1 256");
        rule.expect(InvalidNumberException.class);
        assembler.assemble("drw v0 v1 16");
        rule.expect(InvalidNumberException.class);
        assembler.assemble("drw v0 v1 17");
        rule.expect(InvalidNumberException.class);
        assembler.assemble("drw v0 v1 -1");
    }

    @Test
    public void jumpNumberPadding() throws Exception {
        assertArrayEquals(new short[]{0x12, 0x04, 0xF0, 0x00, 0x12, 0x04}, assembler.assemble(
                "jmp .start\n" +
                        ".data\n" +
                        "0xF0\n" +
                        ".start\n" +
                        "jmp .start"));
        assertArrayEquals(new short[]{0x12, 0x04, 0xF0, 0x00, 0x12, 0x04}, assembler.assemble(
                "jmp .start\n" +
                        ";broken" +
                        ".data\n" +
                        "0xF0\n" +
                        ".start\n" +
                        "jmp .start"));
        assertArrayEquals(new short[]{0x12, 0x04, 0xF0, 0xF0, 0x12, 0x04}, assembler.assemble(
                "jmp .start\n" +
                        ".data\n" +
                        "0xF0\n" +
                        ".data2\n" +
                        "0xF0\n" +
                        ".start\n" +
                        "jmp .start"));
        assertArrayEquals(new short[]{0x12, 0x04, 0xF0, 0xF0, 0x12, 0x04}, assembler.assemble(
                "jmp .start\n" +
                        ".data\n" +
                        "0xF0\n" +
                        ".data2\n" +
                        "\n" +
                        "0xF0\n" +
                        ".start\n" +
                        "jmp .start"));
        assertArrayEquals(new short[]{0x12, 0x06, 0xF0, 0xF0, 0xF0, 0x00, 0x12, 0x06}, assembler.assemble(
                "jmp .start\n" +
                        ".data\n" +
                        "0xF0\n" +
                        ".data2\n" +
                        "\n" +
                        "0xF0\n" +
                        ".data3\n" +
                        "\n" +
                        "\n" +
                        "0xF0\n" +
                        ".start\n" +
                        "jmp .start"));
    }

    @Test
    public void testInvalidJumpLocationLineNumber() throws Exception {
        try {
            assertArrayEquals(new short[]{0x12, 0x04, 0xF0, 0x00, 0x12, 0x04}, assembler.assemble(
                    "jmp .start\n" +
                            "\n" +
                            ".data\n" +
                            "\n" +
                            "0xF0\n" +
                            "\n" +
                            ".start\n" +
                            "jmp .start\n" +
                            "jmp .end"));
        } catch (InvalidJumpLocationException e) {
            assertEquals(9, e.getLineNumber());
        }
    }

    @Test
    public void invalidRegisterException() throws Exception {
        assertArrayEquals(new short[]{0x80, 0x10}, assembler.assemble("mov v0 v1"));
        rule.expect(InvalidRegisterException.class);
        assembler.assemble("mov v0 vG");
        rule.expect(InvalidRegisterException.class);
        assembler.assemble("mov vG v1");
    }

    @Test
    public void numberPadding() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x00}, assembler.assemble("0xF0"));
        assertArrayEquals(new short[]{0x03, 0x02, 0x01, 0x00}, assembler.assemble("0x03\n0x02\n0x01"));
    }

    @Test
    public void clearScreen() throws Exception {
        assertArrayEquals(new short[]{0x00, 0xE0}, assembler.assemble("cls"));
    }

    @Test
    public void returnSub() throws Exception {
        assertArrayEquals(new short[]{0x00, 0xEE}, assembler.assemble("ret"));
    }

    @Test
    public void jump() throws Exception {
        assertArrayEquals(new short[]{0x12, 0x02, 0x12, 0x02}, assembler.assemble(
                "jmp .start\n" +
                        ".start\n" +
                        "jmp .start"));
    }

    @Test
    public void callSub() throws Exception {
        assertArrayEquals(new short[]{0x22, 0x04, 0x11, 0x11}, assembler.assemble(
                "cal .func\n" +
                        ".data\n" +
                        "0x11\n" +
                        "0x11\n" +
                        ".func"));
    }

    @Test
    public void skipNextIfEquals() throws Exception {
        assertArrayEquals(new short[]{0x30, 0x31}, assembler.assemble("eq V0 0x31"));
        assertArrayEquals(new short[]{0x31, 31}, assembler.assemble("eq V1 31"));
        assertArrayEquals(new short[]{0x3f, 0b001011}, assembler.assemble("eq Vf 0b001011"));
        assertArrayEquals(new short[]{0x3f, 1}, assembler.assemble("eq Vf 1"));
    }

    @Test
    public void skipNextIfNotEquals() throws Exception {
        assertArrayEquals(new short[]{0x40, 0x00}, assembler.assemble("neq V0 0x00"));
        assertArrayEquals(new short[]{0x41, 255}, assembler.assemble("neq V1 255"));
        assertArrayEquals(new short[]{0x4f, 0b001011}, assembler.assemble("neq Vf 0b001011"));
        assertArrayEquals(new short[]{0x4f, 1}, assembler.assemble("neq Vf 1"));
    }

    @Test
    public void skipNextIfRegsEqual() throws Exception {
        assertArrayEquals(new short[]{0x50, 0x30}, assembler.assemble("eq V0 V3"));
        assertArrayEquals(new short[]{0x51, 0x00}, assembler.assemble("eq V1 V0"));
        assertArrayEquals(new short[]{0x5e, 0xf0}, assembler.assemble("eq Ve Vf"));
        assertArrayEquals(new short[]{0x5f, 0x00}, assembler.assemble("eq Vf V0"));
    }

    @Test
    public void movNumbToReg() throws Exception {
        assertArrayEquals(new short[]{0x62, 11}, assembler.assemble("MOV v2 11"));
        assertArrayEquals(new short[]{0x60, 0}, assembler.assemble("MOV v0 0"));
        assertArrayEquals(new short[]{0x6F, 255}, assembler.assemble("MOV vf 255"));
        assertArrayEquals(new short[]{0x6E, 00}, assembler.assemble("MOV vE 000"));

        rule.expect(InvalidNumberException.class);
        String text = "MOV vf 0000";
        assertArrayEquals(new short[]{0x6F, 0}, assembler.assemble(text));
        text = "MOV vf -1";
        assertArrayEquals(new short[]{0x6F, 255}, assembler.assemble(text));
        text = "MOV vf 256";
        assertArrayEquals(new short[]{0x6F, 0}, assembler.assemble(text));
    }

    @Test
    public void add() throws Exception {
        assertArrayEquals(new short[]{0x72, 11}, assembler.assemble("ADD v2 11"));
        assertArrayEquals(new short[]{0x70, 0}, assembler.assemble("aDd v0 0"));
        assertArrayEquals(new short[]{0x7F, 255}, assembler.assemble("Add vf 255"));
        assertArrayEquals(new short[]{0x7E, 00}, assembler.assemble("Add vE 000"));
    }

    @Test
    public void movRegToReg() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x00}, assembler.assemble("MOV v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x00}, assembler.assemble("MOV v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe0}, assembler.assemble("MOV vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x10}, assembler.assemble("MOV vE v1"));

        rule.expect(UnsupportedOperandException.class);
        String text = "MOV vf vh";
        rule.expectMessage(text);
        assertArrayEquals(new short[]{0x8F, 0}, assembler.assemble(text));
        text = "MOV vf vg";
        rule.expectMessage(text);
        assertArrayEquals(new short[]{0x8F, 255}, assembler.assemble(text));
        text = "MOV vf v10";
        rule.expectMessage(text);
        assertArrayEquals(new short[]{0x8F, 0}, assembler.assemble(text));
    }

    @Test
    public void or() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x01}, assembler.assemble("or v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x01}, assembler.assemble("oR v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe1}, assembler.assemble("Or vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x11}, assembler.assemble("OR vE v1"));
    }

    @Test
    public void and() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x02}, assembler.assemble("and v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x02}, assembler.assemble("aNd v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe2}, assembler.assemble("and vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x12}, assembler.assemble("anD vE v1"));
    }

    @Test
    public void xor() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x03}, assembler.assemble("xor v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x03}, assembler.assemble("xoR v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe3}, assembler.assemble("XOr vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x13}, assembler.assemble("xOR vE v1"));
    }

    @Test
    public void addRegReg() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x04}, assembler.assemble("add v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x04}, assembler.assemble("aDD v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe4}, assembler.assemble("AdD vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x14}, assembler.assemble("ADd vE v1"));
    }

    @Test
    public void subRegReg() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x05}, assembler.assemble("sub v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x05}, assembler.assemble("sub v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe5}, assembler.assemble("sUB vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x15}, assembler.assemble("SUB vE v1"));
    }

    @Test
    public void shiftRightOne() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x06}, assembler.assemble("shr v2"));
        assertArrayEquals(new short[]{0x80, 0x06}, assembler.assemble("shr v0"));
        assertArrayEquals(new short[]{0x8F, 0x06}, assembler.assemble("shr vf"));
        assertArrayEquals(new short[]{0x8E, 0x06}, assembler.assemble("shr vE"));
    }

    @Test
    public void subRegRegOpposite() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x07}, assembler.assemble("suby v2 v0"));
        assertArrayEquals(new short[]{0x80, 0x07}, assembler.assemble("SubY v0 v0"));
        assertArrayEquals(new short[]{0x8F, 0xe7}, assembler.assemble("SuBY vf vE"));
        assertArrayEquals(new short[]{0x8E, 0x17}, assembler.assemble("sUby vE v1"));
    }

    @Test
    public void shiftLeftOne() throws Exception {
        assertArrayEquals(new short[]{0x82, 0x0E}, assembler.assemble("shl v2"));
        assertArrayEquals(new short[]{0x80, 0x0E}, assembler.assemble("shl v0"));
        assertArrayEquals(new short[]{0x8F, 0x0E}, assembler.assemble("shl vf"));
        assertArrayEquals(new short[]{0x8E, 0x0E}, assembler.assemble("shl vE"));
    }

    @Test
    public void skipNextIfRegsNotEqual() throws Exception {
        assertArrayEquals(new short[]{0x90, 0x30}, assembler.assemble("neq V0 V3"));
        assertArrayEquals(new short[]{0x91, 0x00}, assembler.assemble("neq V1 V0"));
        assertArrayEquals(new short[]{0x9e, 0xf0}, assembler.assemble("neq Ve Vf"));
        assertArrayEquals(new short[]{0x9f, 0x00}, assembler.assemble("neq Vf V0"));
    }

    @Test
    public void setI() throws Exception {
        assertArrayEquals(new short[]{0xA2, 0x02}, assembler.assemble("mov I .store\n.store"));
        assertArrayEquals(new short[]{0xA2, 0x04, 0xFF, 0xFF}, assembler.assemble("mov I .store\n0xFF\n0xFF\n.store"));
    }

    @Test
    public void jmpIPlusV0() throws Exception {
        assertArrayEquals(new short[]{0xB2, 0x02}, assembler.assemble("jmp0 .store\n.store"));
        assertArrayEquals(new short[]{0xB2, 0x04, 0xFF, 0xFF}, assembler.assemble("jmp0 .store\n0xFF\n0xFF\n.store"));
    }

    @Test
    public void rnd() throws Exception {
        assertArrayEquals(new short[]{0xC0, 0x30}, assembler.assemble("rnd V0 0x30"));
        assertArrayEquals(new short[]{0xC1, 00}, assembler.assemble("rnd V1 00"));
        assertArrayEquals(new short[]{0xCe, 255}, assembler.assemble("rnd Ve 255"));
        assertArrayEquals(new short[]{0xCf, 0b00101}, assembler.assemble("rnd Vf 0b00101"));
    }

    @Test
    public void drw() throws Exception {
        assertArrayEquals(new short[]{0xD0, 0x30}, assembler.assemble("drw V0 V3 0"));
        assertArrayEquals(new short[]{0xD1, 0x01}, assembler.assemble("drw V1 V0 1"));
        assertArrayEquals(new short[]{0xDe, 0xfE}, assembler.assemble("drw Ve Vf 0xE"));
        assertArrayEquals(new short[]{0xDf, 0x0F}, assembler.assemble("drw Vf V0 15"));
    }

    @Test
    public void kp() throws Exception {
        assertArrayEquals(new short[]{0xE0, 0x9E}, assembler.assemble("kp V0"));
        assertArrayEquals(new short[]{0xE1, 0x9E}, assembler.assemble("kp V1"));
        assertArrayEquals(new short[]{0xEe, 0x9E}, assembler.assemble("kp Ve"));
        assertArrayEquals(new short[]{0xEf, 0x9E}, assembler.assemble("kp Vf"));
    }

    @Test
    public void knp() throws Exception {
        assertArrayEquals(new short[]{0xE0, 0xA1}, assembler.assemble("knp V0"));
        assertArrayEquals(new short[]{0xE1, 0xA1}, assembler.assemble("knp V1"));
        assertArrayEquals(new short[]{0xEe, 0xA1}, assembler.assemble("knp Ve"));
        assertArrayEquals(new short[]{0xEf, 0xA1}, assembler.assemble("knp Vf"));
    }


    @Test
    public void movDelayTimerToReg() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x07}, assembler.assemble("MOV v0 dt"));
    }

    @Test
    public void kw() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x0A}, assembler.assemble("kw V0"));
        assertArrayEquals(new short[]{0xF1, 0x0A}, assembler.assemble("kw V1"));
        assertArrayEquals(new short[]{0xFe, 0x0A}, assembler.assemble("kw Ve"));
        assertArrayEquals(new short[]{0xFf, 0x0A}, assembler.assemble("kw Vf"));
    }

    @Test
    public void movRegToDelayTimer() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x15}, assembler.assemble("MOV dt v0"));
        assertArrayEquals(new short[]{0xF1, 0x15}, assembler.assemble("MOV dt v1"));
        assertArrayEquals(new short[]{0xFF, 0x15}, assembler.assemble("MOV dt vF"));

        rule.expect(InvalidRegisterException.class);
        rule.expectMessage("G");
        assertArrayEquals(new short[]{0xF0, 0x15}, assembler.assemble("MOV dt vG"));
        rule.expectMessage("g");
        assertArrayEquals(new short[]{0xF0, 0x15}, assembler.assemble("MOV dt vg"));
        rule.expectMessage("z");
        assertArrayEquals(new short[]{0xF0, 0x15}, assembler.assemble("MOV dt vz"));
        rule.expectMessage("O");
        assertArrayEquals(new short[]{0xF0, 0x15}, assembler.assemble("MOV dt VO"));
    }

    @Test
    public void movRegToSoundTimer() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x18}, assembler.assemble("MOV st v0"));
        assertArrayEquals(new short[]{0xF1, 0x18}, assembler.assemble("MOV st v1"));
        assertArrayEquals(new short[]{0xFe, 0x18}, assembler.assemble("MOV st vE"));
        assertArrayEquals(new short[]{0xFF, 0x18}, assembler.assemble("MOV st vF"));

        rule.expect(InvalidRegisterException.class);
        rule.expectMessage("G");
        assertArrayEquals(new short[]{0xF0, 0x18}, assembler.assemble("MOV st vG"));
        rule.expectMessage("g");
        assertArrayEquals(new short[]{0xF0, 0x18}, assembler.assemble("MOV st vg"));
        rule.expectMessage("z");
        assertArrayEquals(new short[]{0xF0, 0x18}, assembler.assemble("MOV st vz"));
        rule.expectMessage("O");
        assertArrayEquals(new short[]{0xF0, 0x18}, assembler.assemble("MOV st VO"));
    }

    @Test
    public void addVxToI() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x1E}, assembler.assemble("add I   V0"));
        assertArrayEquals(new short[]{0xF1, 0x1E}, assembler.assemble("ADD I  V1"));
        assertArrayEquals(new short[]{0xFe, 0x1E}, assembler.assemble("add i  Ve"));
        assertArrayEquals(new short[]{0xFf, 0x1E}, assembler.assemble("aDD I  Vf"));
    }

    @Test
    public void setFontSprite() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x29}, assembler.assemble("mov I   V0"));
        assertArrayEquals(new short[]{0xF1, 0x29}, assembler.assemble("MOV I  V1"));
        assertArrayEquals(new short[]{0xFe, 0x29}, assembler.assemble("MoV i  Ve"));
        assertArrayEquals(new short[]{0xFf, 0x29}, assembler.assemble("mOV I  Vf"));
    }

    @Test
    public void bcd() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x33}, assembler.assemble("bcd V0"));
        assertArrayEquals(new short[]{0xF1, 0x33}, assembler.assemble("bcd V1"));
        assertArrayEquals(new short[]{0xFe, 0x33}, assembler.assemble("bcd Ve"));
        assertArrayEquals(new short[]{0xFf, 0x33}, assembler.assemble("bcd  Vf"));
    }

    @Test
    public void str() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x55}, assembler.assemble("str V0"));
        assertArrayEquals(new short[]{0xF1, 0x55}, assembler.assemble("str V1"));
        assertArrayEquals(new short[]{0xFe, 0x55}, assembler.assemble("str Ve"));
        assertArrayEquals(new short[]{0xFf, 0x55}, assembler.assemble("str  Vf"));
    }

    @Test
    public void lod() throws Exception {
        assertArrayEquals(new short[]{0xF0, 0x65}, assembler.assemble("lod V0"));
        assertArrayEquals(new short[]{0xF1, 0x65}, assembler.assemble("lod V1"));
        assertArrayEquals(new short[]{0xFe, 0x65}, assembler.assemble("lod Ve"));
        assertArrayEquals(new short[]{0xFf, 0x65}, assembler.assemble("lod  Vf"));
    }
}