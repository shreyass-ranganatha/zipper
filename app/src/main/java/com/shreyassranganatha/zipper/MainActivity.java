package com.shreyassranganatha.zipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss", Locale.getDefault());
    SimpleDateFormat rf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    String homepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Zipper";
    File appdirc = new File(homepath);

    TextView count_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        permission setup
        this.requestPermissions(new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        }, 1001);


//        bootstrap
        Log.d("CreatorTests", appdirc.getPath());

        if (!appdirc.exists())
            Log.d("CreatorTests", "created `appdirc`? " + appdirc.mkdirs());


        count_textview = findViewById(R.id.count_textview);


//        media intent
        Intent media_intent = new Intent(Intent.ACTION_PICK);
        media_intent.setType("image/*,video/*");
        media_intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Button button_make_zip = findViewById(R.id.button_make_zip);
        button_make_zip.setOnClickListener((v) -> media_fetch_intent_launcher.launch(media_intent));
    }

    public void write_collections() {
        LinearLayout frame_content = findViewById(R.id.frame_content);
        frame_content.removeAllViews();

        String[] dt = appdirc.list();
        if (dt != null) {
            count_textview.setText(String.valueOf(dt.length));

            int i;
            for (i=0; i<dt.length; i++) {
                TextView bv = new TextView(this);

                try {
                    Date dstr = df.parse(dt[i]);
                    assert dstr != null;

                    bv.setText(rf.format(dstr));
                } catch (Exception E) {
                    bv.setText(dt[i]);
                    Log.e("CreatorTests", E.toString());
                }

                frame_content.addView(bv);
            }

        } else { count_textview.setText("0"); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CreatorTests", "RESUMED");

        write_collections();
    }

    public static void copy(InputStream src, File dst) throws IOException {
        try (OutputStream out = Files.newOutputStream(dst.toPath())) {
            byte[] buf = new byte[1024];

            int len;
            while ((len = src.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }


    ActivityResultLauncher<Intent> media_fetch_intent_launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    try {
                        assert data != null;
                        Log.d("CreatorTests", data.getClipData().toString());


                        Date c = Calendar.getInstance().getTime();


                        File nowpath = new File(homepath + "/" + df.format(c));
                        Log.d("CreatorTests", "nowpath: " + nowpath.getPath());
                        if (!nowpath.exists())
                            Log.d("CreatorTests", "written Now? " + nowpath.mkdirs());


                        ClipData clipdata = data.getClipData();
                        for (int i=0; i<clipdata.getItemCount(); i++) {
                            try {
                                Uri uri = clipdata.getItemAt(i).getUri();
                                Log.d("CreatorTests", uri.toString());
                                Log.d("CreatorTests", uri.getPath());

                                try {
                                    File dstf = new File(nowpath.getPath() + "/" + i + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri)));
                                    copy(getContentResolver().openInputStream(uri), dstf);

                                } catch (Exception E) { Log.e("CreatorTests", E.toString()); }
                            } catch (Exception E) { Log.e("CreatorTests", E.toString()); }

                        }

                    } catch (Exception E) { Log.e("CreatorTests", E.toString()); }

                }
            }
    );

}