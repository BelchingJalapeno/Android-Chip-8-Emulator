package com.belchingjalapeno.androidchip8.activitys;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.belchingjalapeno.androidchip8.R;
import com.belchingjalapeno.androidchip8.assembly.assembler.Assembler;
import com.belchingjalapeno.androidchip8.assembly.assembler.AssemblyTextColorer;
import com.belchingjalapeno.androidchip8.assembly.assembler.exceptions.AssemblerException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class AssemblyActivity extends ActionBarActivity {

    public static final String intentExtraProgram = "com.belchingjalapeno.androidchip8.activitys.AssemblyActivity:program";
    public static final String intentExtraFileName = "com.belchingjalapeno.androidchip8.activitys.AssemblyActivity:fileName";
    private final AssemblyTextColorer assemblyTextColorer = new AssemblyTextColorer();
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assembly);

        Intent intent = getIntent();
        String assemblyProgramData = intent.getStringExtra(intentExtraProgram);
        fileName = intent.getStringExtra(intentExtraFileName);
        if (fileName != null) {
            getSupportActionBar().setTitle(fileName);
        } else {
            getSupportActionBar().setTitle("untitled");
        }
        final EditText viewById = (EditText) findViewById(R.id.programTextEdit);
        if (assemblyProgramData != null) {
            viewById.setText(assemblyTextColorer.colorAssembly(assemblyProgramData));
        }
        viewById.addTextChangedListener(new TextWatcher() {
            boolean handled = false;
            int end = 0;
            int start = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!handled) {
                    this.start = start;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                end = start + (count - before);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!handled) {
                    handled = true;
                    Editable text = viewById.getText();
                    String editViewText = text.toString();
                    int i = editViewText.substring(0, start).lastIndexOf("\n");
                    String substring = editViewText.substring(i + 1);
                    String[] split = substring.split("\n");
                    if (split.length == 0) {
                        handled = false;
                        return;
                    }
                    String assembly1 = split[0];
                    Spanned format = assemblyTextColorer.colorAssembly(assembly1);

                    if (text.length() > 0) {
                        final int st = i + 1;
                        final int en = Math.min(i + 1 + assembly1.length(), text.length());
                        text.replace(st, en, format, 0, format.length());
                    }
                } else {
                    handled = false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assembly, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_load) {
            String[] fileNames = fileList();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Load");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    AssemblyActivity.this,
                    android.R.layout.select_dialog_singlechoice);

            for (String fileName : fileNames) {
                arrayAdapter.add(fileName);
            }

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fileName = arrayAdapter.getItem(which);
                    EditText viewById = (EditText) findViewById(R.id.programTextEdit);
                    viewById.setText((assemblyTextColorer.colorAssembly(readFile(fileName))), TextView.BufferType.SPANNABLE);
                    getSupportActionBar().setTitle(fileName);
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

        if (id == R.id.action_run) {
            Toast.makeText(this, "Checking ...", Toast.LENGTH_SHORT).show();
            final EditText viewById = (EditText) findViewById(R.id.programTextEdit);
            final String value = viewById.getText().toString();
            try {
                new Assembler().assemble(value);
            } catch (AssemblerException e) {
                Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                final String s = value.split("\n")[e.getLineNumber() - 1];
                final int index = value.indexOf(s, e.getStart());
                final int endex = index + s.length();
                viewById.setSelection(index);
                viewById.getText().setSpan(new BackgroundColorSpan(Color.argb(60, 130, 0, 0)), index, endex, 0);
                return true;
            }
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(AssemblyActivity.intentExtraProgram, ((EditText) findViewById(R.id.programTextEdit)).getText().toString());
            intent.putExtra(AssemblyActivity.intentExtraFileName, fileName);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_save) {
            if (fileName != null) {
                save();
            } else {
                saveAs();
            }
            return true;
        }
        if (id == R.id.action_save_as) {
            saveAs();
            return true;
        }

        if (id == R.id.action_new) {
            fileName = null;
            ((EditText) findViewById(R.id.programTextEdit)).setText("");
            getSupportActionBar().setTitle("new");
        }

        if (id == R.id.action_delete) {
            String[] fileNames = fileList();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AssemblyActivity.this,
                    android.R.layout.select_dialog_singlechoice);

            for (String fileName : fileNames) {
                arrayAdapter.add(fileName);
            }

            final Context c = this;
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String fileName = arrayAdapter.getItem(which);
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setMessage("Delete (" + fileName + ")?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteFile(fileName);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
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

    private void save() {
        Toast.makeText(getBaseContext(), "Saving : " + fileName, Toast.LENGTH_LONG).show();
        String text = ((EditText) findViewById(R.id.programTextEdit)).getText().toString();
        boolean saved = saveFile(fileName, text);
        if (saved) {
            Toast.makeText(getBaseContext(), "Save Success!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Save FAILED!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveAs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = input.getText().toString();
                save();
                getSupportActionBar().setTitle(fileName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * @param fileName
     * @param text
     * @return save success
     */
    private boolean saveFile(String fileName, String text) {
        FileOutputStream outputStream;
        BufferedWriter writer;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(text);
            writer.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String readFile(String fileName) {
        FileInputStream outputStream;
        BufferedReader reader;

        StringBuilder builder = new StringBuilder();
        try {
            outputStream = openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(outputStream));
            String s;
            while ((s = reader.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
            }
            outputStream.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
