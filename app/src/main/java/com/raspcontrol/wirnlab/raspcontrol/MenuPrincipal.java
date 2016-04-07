package com.raspcontrol.wirnlab.raspcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class MenuPrincipal extends ActionBarActivity {

    private TextView prueba;
    private ListView listviewSalidas;
    private ArrayAdapter adapter;
    private ArrayList<String> lineasTerminal;
    private RelativeLayout layoutPrincipal;
    private SSHConnection myConn;
    private ProgressDialog progress;
    EditText comandoPersonalizado;
    String prompt;
    String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        listviewSalidas = (ListView)findViewById(R.id.listViewSalidas);
        lineasTerminal = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_terminal, lineasTerminal);
        listviewSalidas.setAdapter(adapter);
        layoutPrincipal = (RelativeLayout) findViewById(R.id.layoutMenuPrincipal);
        prompt = null;


        // Recibimos los datos
        Intent myIntent = getIntent(); // gets the previously created intent
        String ip = myIntent.getStringExtra("ip");
        host = ip;
        setTitle(host);

        myConn = new SSHConnection(host);
        //compruebaRTorrentActivo();

        // Botones
        Button reiniciarKodi = (Button) findViewById(R.id.reiniciarKodi);

        reiniciarKodi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Reiniciar Kodi")
                            .setMessage("¿Seguro que desea reiniciar Kodi?")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    myConn.sendCommand("sudo systemctl restart mediacenter.service");
                                }
                            }).setNegativeButton("No", null).show();
            }
        });

        Button reiniciar = (Button) findViewById(R.id.reiniciar);

        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Reinicar")
                        .setMessage("¿Seguro que desea reiniciar el sistema?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myConn.sendCommand("reboot");
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        Button apagar = (Button) findViewById(R.id.apagar);

        apagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Apagar")
                        .setMessage("¿Seguro que desea apagar el sistema?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myConn.sendCommand("sudo shutdown -P");
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        final Button personalizado = (Button) findViewById(R.id.personalizado);

        comandoPersonalizado = (EditText)findViewById(R.id.comandoPersonalizado);

        personalizado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConn.sendCommand(comandoPersonalizado.getText().toString());
                comandoPersonalizado.setText("");
            }
        });

        // Para arrastar el terminal al desplegar el teclado, ARREGLAR, no deja hacer scroll.
        final View activityRootView = findViewById(R.id.listViewSalidas);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    //listviewSalidas.setSelection(adapter.getCount() - 1);
                }
            }
        });

        listviewSalidas.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                listviewSalidas.setSelection(adapter.getCount() - 1);
            }
        });

        // Para esribir en el terminal lo que se está tecleanado.
        comandoPersonalizado.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged (Editable s){
                if(lineasTerminal.size() > 0){
                    if(prompt == null) prompt  = lineasTerminal.get(lineasTerminal.size()-1);
                }
                listviewSalidas.setSelection(adapter.getCount() - 1);
                if(s.length() > 0) {
                    setCharConsola(prompt + s.toString());
                }
                else{
                    setCharConsola(prompt);
                }
            }
        });

        // Enter para enviar
        comandoPersonalizado.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
               // If the event is a key-down event on the "enter" button
                if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_UP)) {
                    // Perform action on key press
                    personalizado.callOnClick();
                    return true;
                }
                return false;
            }
        });

        Button kore = (Button) findViewById(R.id.kore);

        kore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchKore = getPackageManager().getLaunchIntentForPackage("org.xbmc.kore");
                if(launchKore != null)
                    startActivity(launchKore);
                else{
                    Toast toast = Toast.makeText(MenuPrincipal.this, "¡Kore no está instalado!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        Button iniciarRTorrent = (Button) findViewById(R.id.iniciarRTorrent);

        iniciarRTorrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Iniciar rTorrent")
                        .setMessage("¿Seguro que desea iniciar rTorrent?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myConn.sendCommand("sudo systemctl start rtorrent.service");
                                myConn.compruebaEstRTorrent();
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        Button reiniciarRTorrent = (Button) findViewById(R.id.reiniciarRTorrent);

        reiniciarRTorrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Reinicar rTorrent")
                        .setMessage("¿Seguro que desea reiniciar rTorrent?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myConn.sendCommand("sudo systemctl restart rtorrent.service");
                                myConn.compruebaEstRTorrent();
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        Button pararRTorrent = (Button) findViewById(R.id.pararRTorrent);

        pararRTorrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this).setIcon(android.R.drawable.ic_menu_info_details).setTitle("Parar rTorrent")
                        .setMessage("¿Seguro que desea parar rTorrent?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myConn.sendCommand("sudo systemctl stop rtorrent.service");
                                myConn.compruebaEstRTorrent();
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        myConn.execute(this);

    }

    public void setCharConsola(String linea){
        if(lineasTerminal.size() > 0) lineasTerminal.remove(lineasTerminal.size()-1);
        lineasTerminal.add(linea);
        adapter.notifyDataSetChanged();
        listviewSalidas.setSelection(adapter.getCount() - 1);
    }

    public void setLineConsola(String linea){
        lineasTerminal.add(linea);
        adapter.notifyDataSetChanged();
        listviewSalidas.setSelection(adapter.getCount() - 1);
    }

    private boolean isInstalled(Intent intent) {
        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    public void compruebaRTorrentActivo(){
        this.runOnUiThread(new Runnable() {
            public void run() {
                //myConn.estadoRTorrent();
                String respuesta = lineasTerminal.get(lineasTerminal.size() - 2);
                if(respuesta.indexOf("active") > -1){
                    layoutPrincipal.setBackgroundColor(Color.parseColor("#89e599"));
                    lineasTerminal.remove(lineasTerminal.size() - 2);
                }
                else{
                    layoutPrincipal.setBackgroundColor(Color.parseColor("#f38989"));
                }
                // Para borrar la consulta del rTorrent
                lineasTerminal.remove(lineasTerminal.size() - 2);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void Cargando(final String titulo, final String mensaje) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                progress = new ProgressDialog(MenuPrincipal.this);
                progress.setTitle(titulo);
                progress.setMessage(mensaje);
                progress.show();
            }
        });
    }

    public void FinDeCarga() {
        progress.dismiss();
    }

    @Override
    public void onBackPressed() {
        myConn.exit();
        finish();
    }

}
