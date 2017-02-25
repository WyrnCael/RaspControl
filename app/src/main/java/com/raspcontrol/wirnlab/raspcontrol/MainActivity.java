package com.raspcontrol.wirnlab.raspcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private ArrayList<String> names;
    private ArrayAdapter adapter;
    private DAOSql dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dao = new DAOSql();

        names = dao.getNames(this);
        ListView listview = (ListView)findViewById(R.id.listViewIps);
        adapter = new ArrayAdapter<>(this, R.layout.list_ips, names);
        listview.setAdapter(adapter);
        listview.smoothScrollToPosition(0);

        // Detecto ip pulsada
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Elemento seleccionado:
                String selected = ((TextView) view.findViewById(R.id.textViewIps)).getText().toString();

                // Nueva actividad
                Intent myIntent = new Intent(MainActivity.this, MenuPrincipal.class);
                myIntent.putExtra("nombre", selected);
                startActivity(myIntent);
            }
        });
        Button anadir = (Button) findViewById(R.id.anadir);

       anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, NewServer.class);
                startActivityForResult(myIntent, 1);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    names = dao.getNames(this);
                    adapter.clear();
                    adapter.addAll(names);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
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
