package com.raspcontrol.wirnlab.raspcontrol;

import android.util.Log;

/**
 * Created by Jota on 23/02/2017.
 */

public class ServerInfo {
    private String host;
    private String nombre;
    private String user;
    private String pass;

    public ServerInfo(){ }

    public ServerInfo(String nombre, String host, String user, String pass){
        this.nombre = nombre;
        this.host = host;
        this.user = user;
        this.pass = pass;
        Log.v("ServerInfo", this.nombre + this.user + this.user + this.pass);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
