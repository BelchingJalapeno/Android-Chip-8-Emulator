package com.belchingjalapeno.androidchip8.activitys;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.belchingjalapeno.androidchip8.AndroidTonePlayer;
import com.belchingjalapeno.androidchip8.Chip8System;
import com.belchingjalapeno.androidchip8.CustomCanvas;
import com.belchingjalapeno.androidchip8.R;
import com.belchingjalapeno.androidchip8.assembly.AssemblyProgramCounterFinder;
import com.belchingjalapeno.androidchip8.assembly.assembler.Assembler;
import com.belchingjalapeno.androidchip8.assembly.assembler.AssemblyTextColorer;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.AssemblerException;
import com.belchingjalapeno.androidchip8.assembly.disassembler.Disassembler;
import com.belchingjalapeno.androidchip8.chip8.Input;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static final String sharedPrefAssemblyFileData = "com.belchingjalapeno.androidchip8.activitys.MainActivity:assemblyFileData";
    private static final String sharedPrefFileName = "com.belchingjalapeno.androidchip8.activitys.MainActivity:fileName";
    private static final String sharedPrefCyclePerSecond = "com.belchingjalapeno.androidchip8.activitys.MainActivity:cyclePerSecond";
    private static final int MAX_CYCLES_PER_SECOND = 120;

    private Thread thread;
    private boolean running = false;
    private Chip8System chip8System;

    private String assemblyFileData;
    private String fileName;

    private boolean step = false;
    private boolean isPaused = false;

    private int cycles_per_second = 60;
    private Spanned format;
    private AssemblyProgramCounterFinder assemblyProgramCounterFinder;
    private AssemblyTextColorer assemblyTextColorer;
    private Assembler assembler = new Assembler();

    private static short[] loadProgram(InputStream inputStream) throws IOException {
        int holder;
        ArrayList<Short> data = new ArrayList<>();
        while ((holder = inputStream.read()) != -1) {
            data.add((short) holder);
        }
        short[] dat = new short[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dat[i] = data.get(i);
        }
        inputStream.close();
        return dat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chip8System = new Chip8System((CustomCanvas) findViewById(R.id.customCanvas), new AndroidTonePlayer());
        assemblyTextColorer = new AssemblyTextColorer();
        assemblyProgramCounterFinder = new AssemblyProgramCounterFinder();
        setupInput();

        assemblyFileData = getIntent().getStringExtra(AssemblyActivity.intentExtraProgram);
        fileName = getIntent().getStringExtra(AssemblyActivity.intentExtraFileName);
        if (assemblyFileData == null) {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            assemblyFileData = preferences.getString(sharedPrefAssemblyFileData, null);
            fileName = preferences.getString(sharedPrefFileName, null);
            cycles_per_second = preferences.getInt(sharedPrefCyclePerSecond, 60);
        }

        getSupportActionBar().setTitle(fileName);

        Button stepButton = (Button) findViewById(R.id.step_button);
        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                step = true;
            }
        });
        ToggleButton pausePlayButton = (ToggleButton) findViewById(R.id.pause_resume_button);
        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    return;
                }
                ToggleButton button = (ToggleButton) v;
                isPaused = button.isChecked();
                final View debugInfoView = findViewById(R.id.debug_info);
                ViewPropertyAnimator animate = debugInfoView.animate();
                if (!isPaused) {
                    animate.alpha(0);
                    animate.setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            debugInfoView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    debugInfoView.setVisibility(View.VISIBLE);
                    updateDebugInfo();
                    animate.setListener(null);
                    animate.alpha(1);
                }
            }
        });

        if (assemblyFileData != null) {
            startChip8();
        }
    }

    private void startChip8() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (assemblyFileData != null) {
                    try {
                        chip8System.load(assembler.assemble(assemblyFileData));
                        //temp fix to allow debugger to work
                        String assembledDisassemblerData = new Disassembler().disAssemble(new Assembler().assemble(MainActivity.this.assemblyFileData));
                        format = assemblyTextColorer.colorAssembly(assembledDisassemblerData);
                    } catch (final AssemblerException e) {
                        e.printStackTrace();
                        toast("line:" + e.getLineNumber() + ":" + e.getMessage());
                    }
                } else {
                    toast("Assembly File Data == null");
                    return;
                }
                try {
                    running = true;
                    //main chip-8 loop
                    while (running) {
                        long startTime = System.currentTimeMillis();
                        if (!isPaused) {
                            chip8System.step();
                        } else if ((isPaused && step)) {
                            step = false;
                            chip8System.step();
                            updateDebugInfo();
                        }
                        try {
                            int fps = 1000 / cycles_per_second;
                            long diff = System.currentTimeMillis() - startTime;
                            if (diff < fps) {
                                Thread.sleep(fps - diff);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            toast(e.getMessage());
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }
            }
        }, "Chip 8 System Thread");

        thread.start();
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateDebugInfo() {
        final int programCounter = chip8System.getProgramCounter();
        final Spanned highlighted = assemblyProgramCounterFinder.findAndReplace(programCounter, format);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText disassembleView = (EditText) findViewById(R.id.debugTextView);
                //clear background selection from before
                disassembleView.setText(highlighted);
                int start = assemblyProgramCounterFinder.findStart(programCounter, format);
                //scrolls the view down to where we just highlighted the currently executing line
                if (start > 0) {
                    disassembleView.setSelection(start);
                }
                disassembleView.setEnabled(false);
            }
        });
        updateRegisters();
    }

    /**
     * Updates the debug register table. Should only be called when the debug table is visible.
     */
    private void updateRegisters() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateReg(R.id.v0_value, "0x" + Integer.toHexString(chip8System.getVReg(0x0)));
                updateReg(R.id.v1_value, "0x" + Integer.toHexString(chip8System.getVReg(0x1)));
                updateReg(R.id.v2_value, "0x" + Integer.toHexString(chip8System.getVReg(0x2)));
                updateReg(R.id.v3_value, "0x" + Integer.toHexString(chip8System.getVReg(0x3)));
                updateReg(R.id.v4_value, "0x" + Integer.toHexString(chip8System.getVReg(0x4)));
                updateReg(R.id.v5_value, "0x" + Integer.toHexString(chip8System.getVReg(0x5)));
                updateReg(R.id.v6_value, "0x" + Integer.toHexString(chip8System.getVReg(0x6)));
                updateReg(R.id.v7_value, "0x" + Integer.toHexString(chip8System.getVReg(0x7)));
                updateReg(R.id.v8_value, "0x" + Integer.toHexString(chip8System.getVReg(0x8)));
                updateReg(R.id.v9_value, "0x" + Integer.toHexString(chip8System.getVReg(0x9)));
                updateReg(R.id.va_value, "0x" + Integer.toHexString(chip8System.getVReg(0xa)));
                updateReg(R.id.vb_value, "0x" + Integer.toHexString(chip8System.getVReg(0xb)));
                updateReg(R.id.vc_value, "0x" + Integer.toHexString(chip8System.getVReg(0xc)));
                updateReg(R.id.vd_value, "0x" + Integer.toHexString(chip8System.getVReg(0xd)));
                updateReg(R.id.ve_value, "0x" + Integer.toHexString(chip8System.getVReg(0xe)));
                updateReg(R.id.vf_value, "0x" + Integer.toHexString(chip8System.getVReg(0xf)));

                updateReg(R.id.i_value, "0x" + Integer.toHexString(chip8System.getIReg()));
            }
        });
    }

    private void updateReg(int id, String text) {
        TextView vfReg = (TextView) findViewById(id);
        vfReg.setText(text);
    }

    /**
     * Sets all of the buttons up that are used to control the chip-8 system.
     */
    private void setupInput() {
        Button button0 = (Button) findViewById(R.id.button0);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);
        Button button7 = (Button) findViewById(R.id.button7);
        Button button8 = (Button) findViewById(R.id.button8);
        Button button9 = (Button) findViewById(R.id.button9);
        Button buttonA = (Button) findViewById(R.id.buttonA);
        Button buttonB = (Button) findViewById(R.id.buttonB);
        Button buttonC = (Button) findViewById(R.id.buttonC);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        Button buttonE = (Button) findViewById(R.id.buttonE);
        Button buttonF = (Button) findViewById(R.id.buttonF);
        setupButton(button0, 0x00);
        setupButton(button1, 0x01);
        setupButton(button2, 0x02);
        setupButton(button3, 0x03);
        setupButton(button4, 0x04);
        setupButton(button5, 0x05);
        setupButton(button6, 0x06);
        setupButton(button7, 0x07);
        setupButton(button8, 0x08);
        setupButton(button9, 0x09);
        setupButton(buttonA, 0x0A);
        setupButton(buttonB, 0x0B);
        setupButton(buttonC, 0x0C);
        setupButton(buttonD, 0x0D);
        setupButton(buttonE, 0x0E);
        setupButton(buttonF, 0x0F);
    }

    /**
     * Sets up the on screen buttons to trigger a {@link com.belchingjalapeno.androidchip8.chip8.Input.KeyEvent} for the chip-8 system.
     *
     * @param button the Android UI Button to link to trigger the key event
     * @param key    the chip-8 key(0x0-0xF) that gets linked to the Android UI Button
     */
    private void setupButton(Button button, final int key) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Input.KeyEvent keyEvent = new Input.KeyEvent();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        keyEvent.key = (byte) key;
                        keyEvent.state = true;
                        chip8System.fireKeyEvent(keyEvent);
                    case MotionEvent.ACTION_UP:
                        keyEvent.key = (byte) key;
                        keyEvent.state = false;
                        chip8System.fireKeyEvent(keyEvent);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_assembly) {
            Intent intent = new Intent(this, AssemblyActivity.class);
            intent.putExtra(AssemblyActivity.intentExtraProgram, assemblyFileData);
            intent.putExtra(AssemblyActivity.intentExtraFileName, fileName);
            running = false;
            chip8System.shutDown();
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_cycles_per_second) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cycles per second");

            final EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(cycles_per_second + "");
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cycles_per_second = Math.min(MAX_CYCLES_PER_SECOND, Integer.valueOf(input.getText().toString()));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
        }
        if (id == R.id.load_builtins) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Load");


            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
            final AssetManager assetManager = getResources().getAssets();
            try {
                String[] files = assetManager.list("builtins");
                for (String fileName : files) {
                    arrayAdapter.add(fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    running = false;
                    chip8System.shutDown();
                    chip8System = new Chip8System((CustomCanvas) findViewById(R.id.customCanvas), new AndroidTonePlayer());
                    fileName = arrayAdapter.getItem(which);
                    try {
                        short[] program = loadProgram((assetManager.open(fileName)));
                        chip8System.load(program);
                        assemblyFileData = new Disassembler().disAssemble(program);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    running = false;
                    getSupportActionBar().setTitle(fileName);

                    startChip8();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        savePreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    private void savePreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(sharedPrefAssemblyFileData, assemblyFileData);
        edit.putString(sharedPrefFileName, fileName);
        edit.putInt(sharedPrefCyclePerSecond, cycles_per_second);
        edit.apply();
    }
}
