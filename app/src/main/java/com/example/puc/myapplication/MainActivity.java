package com.example.puc.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Button carregar, salvar, camera, enviar, texto;
    EditText txSuperior, txInferior;
    TextView txt01, txt02;
    ImageView imagem;
    String arqImagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        carregar = findViewById(R.id.carregar);
        salvar = findViewById(R.id.salvar);
        camera = findViewById(R.id.camera);
        enviar = findViewById(R.id.enviar);
        texto = findViewById(R.id.texto);
        txSuperior = findViewById(R.id.txSuperior);
        txInferior = findViewById(R.id.txInferior);
        txt01 = findViewById(R.id.txt01);
        txt02 = findViewById(R.id.txt02);
        imagem = findViewById(R.id.imageView);

        salvar.setEnabled(false);
        enviar.setEnabled(false);

        if (!temCamera()) {
            camera.setEnabled(true);
        }
            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(i, 1);

                }
            });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enviar();

            }
        });

        texto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                txt01.setText(txSuperior.getText().toString());
                txt02.setText(txInferior.getText().toString());
                txSuperior.setText("");
                txInferior.setText("");

            }
        });

        carregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 2);

            }
        });

        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View relCenter = findViewById(R.id.relCenter);
                Bitmap bitmap = screenShot(relCenter);
                arqImagem = "Meme_" +System.currentTimeMillis()+".png";
                armazenar(bitmap, arqImagem);
                enviar.setEnabled(true);

            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri pegarImagem = data.getData();
            String[] diretorio = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pegarImagem, diretorio, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(diretorio[0]);
            String imagemDir = cursor.getString(index);
            cursor.close();
            imagem.setImageBitmap(BitmapFactory.decodeFile(imagemDir));
            salvar.setEnabled(true);
            enviar.setEnabled(false);
        } else if (requestCode == 1 && resultCode == RESULT_OK) {

            Bundle bundle = data.getExtras();
            Bitmap foto = (Bitmap) bundle.get("data");
            imagem.setImageBitmap(foto);
            salvar.setEnabled(true);
            enviar.setEnabled(false);

        }
    }


    void enviar() {

        String diretorio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Meme/" + arqImagem;
        File f = new File(diretorio);
        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Compartilhar usando..."));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case 1: {

                if (grantResults.length <= 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Sem permissão!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    {

                    }
                    return;
                }
            }


        }

    }


    public static Bitmap screenShot(View v) {

        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;

    }

    public void armazenar(Bitmap bitmap, String arquivo) {

        String diretorio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Meme";
        File dir = new File(diretorio);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(diretorio, arquivo);

        try {

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(this, "Erro na hora de salvar!", Toast.LENGTH_SHORT).show();


        }

    }

    public Boolean temCamera() {

        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

}
