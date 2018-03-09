package com.belchingjalapeno.androidchip8.assembly.assembler;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.belchingjalapeno.androidchip8.assembly.Assembly;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colors assembly text to make it more readable.
 */
public class AssemblyTextColorer {

    private static final ArrayList<String> validOpcodes = new ArrayList<>();

    //all of the colors used by this Colorer
    private static final int opcodeColor = Color.parseColor("#6897BB");
    private static final int registerColor = Color.parseColor("#9876AA");
    private static final int labelColor = Color.parseColor("#CC7832");
    private static final int commentsColor = Color.parseColor("#AFAFAF");
    private static final int othersColor = Color.parseColor("#FFFFFF");

    //the patterns used to find text to color
    private static final Pattern commentPattern = Pattern.compile(".*?(;.*)");
    private static final Pattern labelPattern = Pattern.compile(".*?(\\.\\w+)");
    private static final Pattern registerPattern = Pattern.compile(".*?([vV][0-9a-fA-F]|[iI]|[DdSs][tT])");
    private static final Pattern opcodesPattern;

    static {
        validOpcodes.add(Assembly.MOV);
        validOpcodes.add(Assembly.ADD);
        validOpcodes.add(Assembly.JMP);
        validOpcodes.add(Assembly.DRW);
        validOpcodes.add(Assembly.CLS);
        validOpcodes.add(Assembly.CAL);
        validOpcodes.add(Assembly.RET);
        validOpcodes.add(Assembly.EQ);
        validOpcodes.add(Assembly.NEQ);
        validOpcodes.add(Assembly.AND);
        validOpcodes.add(Assembly.OR);
        validOpcodes.add(Assembly.XOR);
        validOpcodes.add(Assembly.SHL);
        validOpcodes.add(Assembly.SHR);
        validOpcodes.add(Assembly.RND);
        validOpcodes.add(Assembly.KP);
        validOpcodes.add(Assembly.KNP);
        validOpcodes.add(Assembly.KW);
        validOpcodes.add(Assembly.BCD);
        validOpcodes.add(Assembly.STR);
        validOpcodes.add(Assembly.LOD);
        validOpcodes.add(Assembly.SUB);
        validOpcodes.add(Assembly.SUBY);
        validOpcodes.add(Assembly.JMP0);

        //builds a regex to find all the opcodes
        String op = "[^\\S]?(";
        for (String s : validOpcodes) {
            op += s + "|";
        }
        //removes the last "|" and adds the closing parentheses
        op = op.substring(0, op.length() - 1);
        op = op + ")";
        opcodesPattern = Pattern.compile(op);
    }

    /**
     * Turns a {@link String} of assembly into a Colored Spanned, making it easier to read.
     *
     * @param assembly the text to color
     * @return some colored text
     */
    public Spanned colorAssembly(String assembly) {
        SpannableStringBuilder builder = new SpannableStringBuilder(assembly);

        //clear the background color in case it was colored because of an error message highlighting it
        builder.setSpan(new BackgroundColorSpan(Color.argb(0, 0, 0, 0)), 0, builder.length(), 0);

        //set line to default color to clear color from previous coloring
        //to prevent previous color staying on things it shouldnt be on
        colorMatches(builder, Pattern.compile("(.*)").matcher(builder), othersColor);

        //ordering is important here as it overrides previous ForegroundColorSpans
        colorMatches(builder, registerPattern.matcher(builder), registerColor);
        colorMatches(builder, opcodesPattern.matcher(builder), opcodeColor);
        colorMatches(builder, labelPattern.matcher(builder), labelColor);
        colorMatches(builder, commentPattern.matcher(builder), commentsColor);

        return builder;
    }

    /**
     * Sets all matches in {@link Matcher} in {@link SpannableStringBuilder} to {@code color}.
     *
     * @param builder
     * @param matcher
     * @param color
     */
    private void colorMatches(SpannableStringBuilder builder, Matcher matcher, int color) {
        //start at group 1 because group 0 is the entire match
        final int group = 1;
        while (matcher.find()) {
            final int start = matcher.start(group);
            builder.setSpan(new ForegroundColorSpan(color), start, matcher.end(group), 0);
        }
    }
}
