package org.sogrey.app2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.sogrey.jarinterface.Md5JarInterface;
import org.sogrey.md5.impl.Md5Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtResult = findViewById(R.id.txt_result);
        txtResult.setText(new Md5Utils().getMd5("123456"));
    }
}
