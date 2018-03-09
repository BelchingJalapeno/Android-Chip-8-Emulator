package com.belchingjalapeno.androidchip8.assembly;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

/**
 *
 */
public class AssemblyProgramCounterFinder {

    public Spanned findAndReplace(int programCounter, Spanned text) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] split = text.toString().split("\n");
        builder.append(text);
        for (String line : split) {
            String cs = ";0X" + Integer.toHexString(programCounter);
            if (line.toUpperCase().contains(cs.toUpperCase())) {
                int i = text.toString().indexOf(line);
                builder.setSpan(new BackgroundColorSpan(Color.rgb(130, 0, 0)), i, i + line.length(), 0);
                return builder;
            }
        }
        return builder;
    }

    public int findStart(int programCounter, Spanned text) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] split = text.toString().split("\n");
        builder.append(text);
        for (String line : split) {
            String cs = ";0X" + Integer.toHexString(programCounter);
            if (line.toUpperCase().contains(cs.toUpperCase())) {
                int i = text.toString().indexOf(line);
                return i;
            }
        }
        return -1;
    }
}
