package com.belchingjalapeno.androidchip8;

import android.media.AudioManager;
import android.media.ToneGenerator;

import com.belchingjalapeno.androidchip8.chip8.TonePlayer;

/**
 *
 */
public class AndroidTonePlayer implements TonePlayer {
    ToneGenerator toneGenerator;

    public AndroidTonePlayer() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    }

    @Override
    public void play() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
    }

    @Override
    public void stop() {
        toneGenerator.stopTone();
    }

    @Override
    public void dispose() {
        stop();
        toneGenerator.release();
    }
}
