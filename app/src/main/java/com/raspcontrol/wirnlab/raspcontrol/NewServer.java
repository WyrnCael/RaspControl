package com.raspcontrol.wirnlab.raspcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by Jota on 22/02/2017.
 */

public class NewServer extends Activity {
    private DAOSql dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_server);
        dao = new DAOSql();

        Button anadir = (Button) findViewById(R.id.button_add);
        EditText nombre = (EditText)findViewById(R.id.name_text);
        EditText ip = (EditText)findViewById(R.id.ip_text);
        EditText usuario = (EditText)findViewById(R.id.user_text);
        EditText pass = (EditText)findViewById(R.id.pass_text);
        final ServerInfo server = new ServerInfo(nombre.getText().toString(), ip.getText().toString(), usuario.getText().toString(), pass.getText().toString());
        Log.d("Server", server.getNombre() + server.getHost() + server.getUser() + server.getPass());
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.addServer(NewServer.this, server);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Añadido", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
