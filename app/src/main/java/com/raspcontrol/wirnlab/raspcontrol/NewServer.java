package com.raspcontrol.wirnlab.raspcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Jota on 22/02/2017.
 */

public class NewServer extends ActionBarActivity {
    private DAOSql dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_server);
        setTitle("Añadir Servidor");
        dao = new DAOSql();

        Button anadir = (Button) findViewById(R.id.button_add);
        Button cancelar = (Button) findViewById(R.id.button_cancel);
        final EditText nombre = (EditText)findViewById(R.id.name_text);
        final EditText ip = (EditText)findViewById(R.id.ip_text);
        final EditText usuario = (EditText)findViewById(R.id.user_text);
        final EditText pass = (EditText)findViewById(R.id.pass_text);

        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerInfo server = new ServerInfo(nombre.getText().toString(), ip.getText().toString(), usuario.getText().toString(), pass.getText().toString());
                dao.addServer(NewServer.this, server);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Añadido", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Cancelar", true);
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });
    }

}
