package com.raspcontrol.wirnlab.raspcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    private ListView listview;
    private ArrayAdapter adapter;
    private ArrayList<String> lineasTerminal;
    Connection myConn;
    EditText comandoPersonalizado;
    String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        listview = (ListView)findViewById(R.id.listViewSalidas);
        lineasTerminal = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_terminal, lineasTerminal);
        listview.setAdapter(adapter);



        // Recibimos los datos
        Intent myIntent = getIntent(); // gets the previously created intent
        String ip = myIntent.getStringExtra("ip");
        host = ip;
        setTitle(host);

       myConn = new Connection();

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
                                    myConn.sendCommand("systemctl restart mediacenter.service");
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

        Button personalizado = (Button) findViewById(R.id.personalizado);

        comandoPersonalizado = (EditText)findViewById(R.id.comandoPersonalizado);

        personalizado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConn.sendCommand(comandoPersonalizado.getText().toString());
                comandoPersonalizado.setText("");
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

        myConn.execute(this);

    }

    public void setCharConsola(String linea){
        if(lineasTerminal.size() > 0) lineasTerminal.remove(lineasTerminal.size()-1);
        lineasTerminal.add(linea);
        adapter.notifyDataSetChanged();
        listview.setSelection(adapter.getCount() - 1);
    }

    public void setLineConsola(String linea){
        lineasTerminal.add(linea);
        adapter.notifyDataSetChanged();
        listview.setSelection(adapter.getCount() - 1);
    }

    private boolean isInstalled(Intent intent) {
        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    @Override
    public void onBackPressed() {
        myConn.exit();
        finish();
    }

     private class Connection extends AsyncTask<MenuPrincipal, String, String> {

         JSch jsch;
         Channel myChannel;
         PrintWriter toChannel;
         Session myLocalSession;
         MenuPrincipal menu;
         ProgressDialog progress;

         @Override
         protected String doInBackground(MenuPrincipal... men) {
             jsch = new JSch();
             menu = men[0];

             // Loading...
             Cargando("Conectando", "Espere mientras conecta...");

             connect();
             return null;
         }

         private void connect() {
             if (host.isEmpty())
                 return;

             String hostname = host;
             try {
                 JSch jsch = new JSch();
                 String user = "osmc";

                 myLocalSession = jsch.getSession(user, host, 22);
                 //myLocalSession=jsch.getSession(user, "192.168.1.104", 22);

                 myLocalSession.setPassword("osmc");

                 myLocalSession.setConfig("StrictHostKeyChecking", "no");

                 myLocalSession.connect();   // making a connection with timeout.

                 myChannel = myLocalSession.openChannel("shell");

                 InputStream inStream = myChannel.getInputStream();

                 OutputStream outStream = myChannel.getOutputStream();
                 toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);

                 myChannel.connect();
                 readerThread(new InputStreamReader(inStream));


                 Thread.sleep(100);
             } catch (JSchException e) {
                 String message = e.getMessage();
                 if (message.contains("UnknownHostException")) {
                     //menu.setTextConsola(">>>>> Unknow Host. Please verify hostname.");
                 } else if (message.contains("socket is not established")) {
                     //menu.setTextConsola(">>>>> Can't connect to the server for the moment.");
                 } else if (message.contains("Auth fail")) {
                 }
                 //menu.setTextConsola(">>>>> Please verify login and password");
                 else if (message.contains("Connection refused")) {
                 }
                 //menu.setTextConsola(">>>>> The server refused the connection");
                 else
                     System.out.println("*******Unknown ERROR********");

                 System.out.println(e.getMessage());
                 System.out.println(e + "****connect()");
             } catch (IOException e) {
                 System.out.println(e);
                 //menu.setTextConsola(">>>>> Error when reading data streams from the server");
             } catch (Exception e) {
                 e.printStackTrace();
             }
             FinDeCarga();
         }

         public void sendCommand(final String command) {
             Cargando("Enviando comando", "Por favor espere...");
             if (myLocalSession != null && myLocalSession.isConnected()) {
                 try {
                     toChannel.println(command);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             FinDeCarga();
         }

         void readerThread(final InputStreamReader tout) {
             Thread read2 = new Thread() {
                 @Override
                 public void run() {
                     StringBuilder line = new StringBuilder();
                     char toAppend = ' ';
                     try {
                         while (true) {
                             try {
                                 while (tout.ready()) {
                                     toAppend = (char) tout.read();
                                     if (toAppend == '\n') {
                                         System.out.println(line.toString());
                                         OnNuevaLinea(line.toString());
                                         line.setLength(0);
                                     } else {
                                         line.append(toAppend);
                                         OnNuevoChar(line.toString());
                                     }
                                 }
                             } catch (Exception e) {
                                 e.printStackTrace();
                                 System.out.println("\n\n\n************errorrrrrrr reading character**********\n\n\n");
                             }
                         }
                     } catch (Exception ex) {
                         System.out.println(ex);
                         try {
                             tout.close();
                         } catch (Exception e) {
                         }
                     }
                 }
             };
             read2.start();
         }

         public void OnNuevaLinea(final String data) {
             runOnUiThread(new Runnable() {
                 public void run() {
                     // use data here
                     menu.setLineConsola(data);
                 }
             });
         }

         public void OnNuevoChar(final String data) {
             runOnUiThread(new Runnable() {
                 public void run() {
                     // use data here
                     menu.setCharConsola(data);
                 }
             });
         }

         public void Cargando(final String titula, String mensaje) {
             runOnUiThread(new Runnable() {
                 public void run() {
                     // use data here
                     progress = new ProgressDialog(MenuPrincipal.this);
                     progress.setTitle("Conectando");
                     progress.setMessage("Espere mientra conecta...");
                     progress.show();
                 }
             });
         }

         public void FinDeCarga() {
             runOnUiThread(new Runnable() {
                 public void run() {
                     // use data here
                     progress.dismiss();
                     ;
                 }
             });
         }

         private void exit(){
             myChannel.disconnect();
             myLocalSession.disconnect();
         }
     }
}
