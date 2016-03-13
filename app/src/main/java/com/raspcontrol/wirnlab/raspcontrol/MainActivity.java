package com.raspcontrol.wirnlab.raspcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> ips;
    ArrayAdapter adapter;
    EditText textAnadir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ips = new ArrayList<>();
        ListView listview = (ListView)findViewById(R.id.listViewIps);
        adapter = new ArrayAdapter<>(this, R.layout.list_ips, ips);
        listview.setAdapter(adapter);
        listview.smoothScrollToPosition(0);

        // Asigno lista de ips
        cargaIpDB();

        // Detecto ip pulsada
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Elemento seleccionado:
                String selected = ((TextView) view.findViewById(R.id.textViewIps)).getText().toString();

                // Nueva actividad
                Intent myIntent = new Intent(MainActivity.this, MenuPrincipal.class);
                myIntent.putExtra("ip", selected);
                startActivity(myIntent);
            }
        });
        Button anadir = (Button) findViewById(R.id.anadir);

        textAnadir = (EditText)findViewById(R.id.nuevaIp);

        textAnadir.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    textAnadir.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textAnadir, InputMethodManager.SHOW_IMPLICIT);
                }
        });

        textAnadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAnadir.setText("");
            }
        });

       anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardaIpDB(textAnadir.getText().toString());
                cargaIpDB();
            }
        });

    }

    private void cargaIpDB(){
        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        IpsSqlHelper usdbh =
                new IpsSqlHelper(this, "DBIPS", null, 1);

        SQLiteDatabase db = usdbh.getReadableDatabase();

        String query = "SELECT ip FROM IPS";

        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()) {
            ips.add(c.getString(0));
        }
        c.close();
        db.close();
        adapter.notifyDataSetChanged();
    }


    private void guardaIpDB(String ip){
        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        IpsSqlHelper usdbh =
                new IpsSqlHelper(this, "DBIPS", null, 1);

        SQLiteDatabase db = usdbh.getWritableDatabase();

        //Si hemos abierto correctamente la base de datos
        if(db != null)
        {
            //Insertamos 5 usuarios de ejemplo
            db.execSQL("INSERT INTO IPS (codigo, ip) " +
                    "VALUES (" + ips.size() + ", '" + ip +"')");

            //Cerramos la base de datos
            db.close();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Salir")
                .setMessage("¿Seguro que deseas salir?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("No", null).show();
    }
}
