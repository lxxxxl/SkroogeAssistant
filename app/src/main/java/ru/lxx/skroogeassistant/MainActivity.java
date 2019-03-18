package ru.lxx.skroogeassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private int requestCode=1;
    private FloatingActionButton fab;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LinearLayout addLayout = findViewById(R.id.addLayout);
                if (addLayout.getVisibility()==View.VISIBLE) {

                    addLayout.setVisibility(View.INVISIBLE);

                    try {
                        CSVWriter writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().toString() + "/SkroogeAssistant/data.csv", true), '\t');
                        String[] entries = new String[5];

                        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
                        String date = df.format(Calendar.getInstance().getTime());

                        Spinner spinner_checks = (Spinner) addLayout.findViewById(R.id.add_check);
                        Spinner spinner_cats = (Spinner) addLayout.findViewById(R.id.add_cat);
                        EditText add_sum = (EditText)  addLayout.findViewById(R.id.add_sum);
                        EditText add_comment = (EditText)  addLayout.findViewById(R.id.add_comment);

                        String check = spinner_checks.getSelectedItem().toString();
                        String sum = add_sum.getText().toString();
                        String category = spinner_cats.getSelectedItem().toString();
                        String comment = add_comment.getText().toString();

                        if (check.isEmpty() | sum.isEmpty() | category.isEmpty()){
                            Snackbar.make(view, "Check, sum, category are mandatory", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }

                        entries[0] = check;
                        entries[1] = date;
                        entries[2] = sum;
                        entries[3] = category;
                        entries[4] = comment;

                        writer.writeNext(entries);
                        writer.close();

                        readCsvFile();

                    }
                    catch (IOException e) {
                        Snackbar.make(view, "Some error occured with CSV file", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        e.printStackTrace();
                    }

                    Snackbar.make(view, "Added data", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
                else
                    addLayout.setVisibility(View.VISIBLE);

            }
        });

        TableLayout tableLayout = findViewById(R.id.table);
        tableLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout addLayout = findViewById(R.id.addLayout);
                        if (addLayout.getVisibility()==View.VISIBLE)
                            addLayout.setVisibility(View.INVISIBLE);
                    }

                }
        );

       /* ScrollView scrollView1 = findViewById(R.id.scrollView1);
        scrollView1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout addLayout = findViewById(R.id.addLayout);
                        if (addLayout.getVisibility()==View.VISIBLE)
                            addLayout.setVisibility(View.INVISIBLE);
                    }

                }
        );*/


        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, requestCode);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            Snackbar.make(findViewById(R.id.table), "Grant External storage access to app", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        initApp();

    }

    @SuppressLint("RestrictedApi")
    public void initApp(){

        try {
            File f =new File(Environment.getExternalStorageDirectory().toString() + "/SkroogeAssistant/data.csv");
            if (!f.exists()) {
                if(!f.getParentFile().exists())
                    f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write("account\tdate\tamount\tcategory\tcomment\n".getBytes());
                fos.close();
            }
        }
        catch (IOException e)
        {
            Snackbar.make(findViewById(R.id.table), "Cannot create working directory", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            e.printStackTrace();
            return;
        }

        LinearLayout addLayout = findViewById(R.id.addLayout);
        addLayout.setVisibility(View.INVISIBLE);

        try {
            Spinner spinner_checks = (Spinner) addLayout.findViewById(R.id.add_check);
            Vector<String> add_check_str = new Vector<String>();
            BufferedReader in = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/SkroogeAssistant/checks.txt"));

            String line = in.readLine();
            while (line != null) {

                add_check_str.add(line);
                line = in.readLine();
            }

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, add_check_str);
            // Specify the layout to use when the list of choices appears
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_checks.setAdapter(adapter);


            Spinner spinner_cats = (Spinner) addLayout.findViewById(R.id.add_cat);

            Vector<String> add_categories_str = new Vector<String>();
            in = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/SkroogeAssistant/categories.txt"));

            line = in.readLine();
            while (line != null) {

                add_categories_str.add(line);
                line = in.readLine();
            }

            ArrayAdapter adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, add_categories_str);
            // Specify the layout to use when the list of choices appears
            //adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner_cats.setAdapter(adapter2);

        }
        catch (Exception ex){
            Snackbar.make(findViewById(R.id.table), "Cannot find checks.txt or categories.txt. Please create them in SkroogeAssistant dir at SD card", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            fab.setVisibility(View.INVISIBLE);
            ex.printStackTrace();
        }

        readCsvFile();

    }


    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initApp();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }
        }
    }

    void readCsvFile(){
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);

        tableLayout.removeAllViews();


        // add header
        /*TableRow tableRowH = new TableRow(this);
        TextView _checkH = new TextView(this);
        _checkH.setText(R.string.check);
        tableRowH.addView(_checkH);
        TextView _dateH = new TextView(this);
        _dateH.setText(R.string.date);
        tableRowH.addView(_dateH);
        TextView _sumH = new TextView(this);
        _sumH.setText(R.string.sum);
        tableRowH.addView(_sumH);
        TextView _catH = new TextView(this);
        _catH.setText(R.string.category);
        tableRowH.addView(_catH);
        TextView _commentH = new TextView(this);
        _commentH.setText(R.string.comment);
        tableRowH.addView(_commentH);
        tableLayout.addView(tableRowH);*/


        try {

            CSVReader reader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/SkroogeAssistant/data.csv"));
            List<String[]> myEntries = reader.readAll();
            for (String[] entry:myEntries) {
                String[] cols = entry[0].split("\t");
                TableRow tableRow = new TableRow(this);

                TextView _date = new TextView(this);
                _date.setText(cols[1].replace("\"",""));
                _date.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                _date.setBackgroundResource(R.drawable.cell_shape);
                tableRow.addView(_date);

                TextView _sum = new TextView(this);
                _sum.setText(cols[2].replace("\"",""));
                _sum.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                _sum.setBackgroundResource(R.drawable.cell_shape);
                tableRow.addView(_sum);

                TextView _comment = new TextView(this);
                _comment.setText(cols[4].replace("\"",""));
                _comment.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                _comment.setBackgroundResource(R.drawable.cell_shape);
                tableRow.addView(_comment);

                tableLayout.addView(tableRow);

                TableRow tableRow2 = new TableRow(this);

                TextView _check = new TextView(this);
                _check.setText(cols[0].replace("\"",""));
                _check.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                _check.setBackgroundResource(R.drawable.cell_shape);
                tableRow2.addView(_check);

                TextView _cat = new TextView(this);
                _cat.setText(cols[3].replace("\"",""));
                _cat.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                _cat.setBackgroundResource(R.drawable.cell_shape);

                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.span = 2;

                tableRow2.addView(_cat,1,params);

                tableLayout.addView(tableRow2);

                TableRow tableRow3 = new TableRow(this);
                TextView _delim = new TextView(this);
                _delim.setText(" ");
                _delim.setTextSize(3);

                TableRow.LayoutParams params2 = new TableRow.LayoutParams();
                params2.span = 3;
                tableRow3.setBackgroundColor(0xFFE8F1F1);


                tableRow3.addView(_delim,0,params2);
                tableLayout.addView(tableRow3);




            }

        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.table), "Some error occured with CSV file", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            e.printStackTrace();
        }
    }
}
