package org.sogrey.loadjar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.sogrey.jarinterface.Md5JarInterface;
import org.sogrey.loadjar.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    TextView txtResult;
    String jarName = "proguard-MD5-dex_V1.0.jar",
            className = "org.sogrey.md5.impl.Md5Utils";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = findViewById(R.id.txt_result);
    }

    public void loadDex(View view) {
        File cacheFile = FileUtils.getCacheDir(getApplicationContext());
        File libFile = new File(cacheFile, "lib");
        if (!libFile.exists()) libFile.mkdirs();
        String internalPath = cacheFile.getAbsolutePath() + File.separator + "lib" + File.separator + jarName;
        File desFile = new File(internalPath);
        try {
            if (!desFile.exists()) {
                desFile.createNewFile();
                FileUtils.copyFiles(this, jarName, desFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //下面开始加载dex class
        DexClassLoader dexClassLoader = new DexClassLoader(internalPath, libFile.getAbsolutePath(), null, getClassLoader());
        try {
            //加载的类名为jar文件里面完整类名，写错会找不到此类hh
            Class libClazz = dexClassLoader.loadClass(className);
            final Md5JarInterface md5JarInterface = (Md5JarInterface) libClazz.newInstance();
            if (md5JarInterface != null) {
                txtResult.post(new Runnable() {
                    @Override
                    public void run() {
                        txtResult.setText(md5JarInterface.getMd5("123456"));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
