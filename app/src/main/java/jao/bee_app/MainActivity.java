package jao.bee_app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button, button2, button3, button4;

        button = (Button)findViewById(R.id.button);



        button.setOnClickListener(new OnClickListener() {



            @Override
            public void onClick(View v) {

                EditText text = (EditText)findViewById(R.id.editText);
                final String ip_address = text.getText().toString();

                new AsyncTask<Integer, Void, String>() {
                    @Override
                    protected String doInBackground(Integer... params) {

                        try {
                            return executeRemoteCommand("pi", "123", ip_address, 22,"mysql -u root -p123 monitorAbelhas -e 'select * from data;'");
                        } catch (Exception e) {
                            return "Connection failed";
                        }

                    }

                    @Override
                    protected void onPostExecute(String result){
                        File file;
                        FileOutputStream outputStream;
                        if((result.equalsIgnoreCase("Connection failed") || !(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)))){
                            Toast.makeText(getApplicationContext(),"Erro de conexão", Toast.LENGTH_SHORT).show();
                        }else{
                            try {

                                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "dados_abelhas.txt");

                                outputStream = new FileOutputStream(file);
                                outputStream.write(result.getBytes());
                                outputStream.close();

                                Toast.makeText(getApplicationContext(), "Dados salvos em arquivo", Toast.LENGTH_LONG).show();
                            } catch (Throwable t) {
                                Toast.makeText(getApplicationContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                }.execute(1);


            }
        });

        button2 = (Button)findViewById(R.id.button2);

        button2.setOnClickListener(new OnClickListener() {



            @Override
            public void onClick(View v) {

                EditText text = (EditText)findViewById(R.id.editText);
                final String ip_address = text.getText().toString();

                new AsyncTask<Integer, Void, String>() {
                    @Override
                    protected String doInBackground(Integer... params) {
                        try {
                            executeRemoteCommand("pi", "123", ip_address, 22,"mysql -u root -p123 monitorAbelhas -e 'delete from data;'");
                        } catch (Exception e) {
                            return "Connection failed";
                        }
                        return "deleted";
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        if(result.equalsIgnoreCase("Connection failed")){
                            Toast.makeText(getApplicationContext(),"Erro de conexão", Toast.LENGTH_SHORT).show();
                        }else{
                            try {
                                Toast.makeText(getApplicationContext(), "Banco de dados limpo.", Toast.LENGTH_LONG).show();
                            } catch (Throwable t) {
                                Toast.makeText(getApplicationContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                }.execute(1);

            }


        });

        button4 = (Button)findViewById(R.id.button4);

        button4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Ajuda");
                alert.setMessage("Digite o endereço IP do sensor que deseja fazer a extração/exclusão dos dados.\n\n" +
                        "Os endereços variam no último octeto, de 192.168.0.1 até 192.168.0.11.");
                alert.setPositiveButton("OK",null);
                alert.show();
            }
        });

        button3 = (Button)findViewById(R.id.button3);

        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Sobre");
                alert.setMessage("Software desenvolvido durante o Projeto: Um sistema móvel aplicado a Apicultura (Fase 3), na Universidade Estadual de Maringá - UEM.\n\n" +
                        "Desenvolvido por: João Marcus Velasques Faria (j.marcus.faria@gmail.com).\n\n" +
                        "Orientador do projeto: Anderson Faustino da Silva.");
                alert.setPositiveButton("OK",null);
                alert.show();
            }
        });

    }



    public static String executeRemoteCommand(String username, String password, String hostname, int port, String command)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        InputStream inputStream = channelssh.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append('\n');
        }

        channelssh.disconnect();

        return stringBuilder.toString();
    }

}