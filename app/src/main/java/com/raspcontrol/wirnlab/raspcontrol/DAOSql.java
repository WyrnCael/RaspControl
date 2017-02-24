package com.raspcontrol.wirnlab.raspcontrol;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Jota on 23/02/2017.
 */

public class DAOSql {

    public void addServer(Context context, ServerInfo server){
        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        IpsSqlHelper usdbh =
                new IpsSqlHelper(context, "DBServers", null, 2);

        SQLiteDatabase db = usdbh.getWritableDatabase();

        //Si hemos abierto correctamente la base de datos
        if(db != null)
        {
            //Insertamos el host
            db.execSQL("INSERT INTO Servers (nombre, ip, usuario, password ) " +
                    "VALUES ('" + server.getNombre() + "', '" + server.getHost() + "', '" + server.getUser() +
                    "', '" + server.getPass() +"')");

            //Cerramos la base de datos
            db.close();
        }
    }

    public ArrayList<String> getNames(Context context){
        ArrayList<String> nombres = new ArrayList<String>();

        //Abrimos la base de datos 'DBUsuarios' en modo lectura
        IpsSqlHelper usdbh =
                new IpsSqlHelper(context, "DBServers", null, 2);

        SQLiteDatabase db = usdbh.getReadableDatabase();

        String query = "SELECT nombre FROM Servers";

        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()) {
            nombres.add(c.getString(c.getColumnIndex("nombre")));
        }
        c.close();
        db.close();

        return nombres;
    }

    public ServerInfo getServer(Context context, String name){
        ServerInfo server = new ServerInfo();
        IpsSqlHelper usdbh =
                new IpsSqlHelper(context, "DBServers", null, 2);

        SQLiteDatabase db = usdbh.getReadableDatabase();

        String query = "SELECT * FROM Servers WHERE nombre = '" + name + "'";

        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()) {
            server.setNombre(c.getString(c.getColumnIndex("nombre")));
            server.setHost(c.getString(c.getColumnIndex("ip")));
            server.setUser(c.getString(c.getColumnIndex("usuario")));
            server.setPass(c.getString(c.getColumnIndex("password")));
        }

        c.close();
        db.close();

        return server;
    }
}
