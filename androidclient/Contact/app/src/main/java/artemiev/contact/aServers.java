package artemiev.contact;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static android.R.attr.id;

public class aServers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_servers);
        LinearLayout ServersList = (LinearLayout) findViewById(R.id.LLServers);

        //ArrayList ButtonArray = new ArrayList(8);
        //(Button) ButtonArray.get(0)
        //ButtonArray.add(btn)

        int ServerNumber = 20;
        int ID = 500;

        for (int i = 0; i < ServerNumber; i++) {
            Button btn = new Button(this);
            btn.setId(ID);
            btn.setText("pisos");

            btn.setOnClickListener(bServerClick);
            ServersList.addView(btn);

            ID++;
        }
    }

    private final View.OnClickListener bServerClick = new View.OnClickListener() {
        public void onClick(View v){
            //setContentView(R.layout.activity_a_servers);
            //Intent i = new Intent(this, aServers.class);
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(aServers.this);

            switch (v.getId()) {
                case 500: dlgAlert.setMessage("1"); break;
                case 501: dlgAlert.setMessage("2"); break;
                case 502: Intent i = new Intent(aServers.this, aGameRoom.class);
                          startActivity(i);
                          dlgAlert.setMessage("3"); break;
                default:  dlgAlert.setMessage(">3"); break;
            }

            dlgAlert.setTitle("App Title");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }
    };
}
