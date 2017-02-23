package com.raspcontrol.wirnlab.raspcontrol;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by Jota on 12/03/2016.
 */
public class SSHConnection extends AsyncTask<MenuPrincipal, String, String> {

    private JSch jsch;
    private Channel myChannel;
    private PrintWriter toChannel;
    private Session myLocalSession;
    private MenuPrincipal menu;
    private ServerInfo server;

    public SSHConnection(ServerInfo server){
        this.server = server;
    }

    @Override
    protected String doInBackground(MenuPrincipal... men) {
        jsch = new JSch();
        menu = men[0];

        // Loading...
        menu.Cargando("Conectando", "Espere mientras conecta...");

        connect();
        return null;
    }

    private void connect() {
        if (server.getHost().isEmpty())
            return;

        String hostname = server.getHost();
        try {
            JSch jsch = new JSch();

            myLocalSession = jsch.getSession(server.getUser(), server.getHost(), 22);
            //myLocalSession=jsch.getSession(user, "192.168.1.104", 22);

            myLocalSession.setPassword(server.getPass());

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

        menu.FinDeCarga();

        compruebaEstRTorrent();
    }

    public void sendCommand(final String command) {
        //menu.Cargando("Enviando comando", "Por favor espere...");
        if (myLocalSession != null && myLocalSession.isConnected()) {
            try {
                toChannel.println(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //menu.FinDeCarga();
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
        menu.runOnUiThread(new Runnable() {
            public void run() {
                // use data here
                menu.setLineConsola(data);
            }
        });
    }

    public void OnNuevoChar(final String data) {
        menu.runOnUiThread(new Runnable() {
            public void run() {
                // use data here
                menu.setCharConsola(data);
            }
        });
    }

    public void exit(){
        myChannel.disconnect();
        myLocalSession.disconnect();
    }

    public void compruebaEstRTorrent(){
        // Para comprobar si rTorrent está activo
        menu.Cargando("Comprobando estado de rTorrent", "Por favor espere");
        try {
            Thread.sleep(1000);
            sendCommand("systemctl list-units | grep rtorrent");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menu.compruebaRTorrentActivo();
        menu.FinDeCarga();
    }
}
