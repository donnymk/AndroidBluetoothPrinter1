package com.example.androidbluetoothprinter1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PrintingCallback {

    private final int GALLERY_REQ_CODE = 1000;
    ImageView imgGallery;

    Printing printing;
    Button btn_unpair_pair, btn_print, btn_print_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // area untuk menampilkan gambar dari galeri
        imgGallery = findViewById(R.id.imgGallery);
        // tombol untuk browse galeri
        Button btnGallery = (Button) findViewById(R.id.btnGallery);

        initView();
        embedImages();

        // aksi jika tombol browse galeri diklik
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery, GALLERY_REQ_CODE);
            }
        });
    }

    //fungsi yang dijalankan pertama kali aplikasi dibuka
    private void initView() {
        btn_print_image = (Button) findViewById(R.id.btnPrintImages);
        btn_unpair_pair = (Button) findViewById(R.id.btnPairUnpair);

        if (printing != null) {
            printing.setPrintingCallback(this);
        }
        //Event jika tombol sambungkan/putuskan bluetooth diklik
        btn_unpair_pair.setOnClickListener(view -> {
            if (Printooth.INSTANCE.hasPairedPrinter())
                Printooth.INSTANCE.removeCurrentPrinter();
            else {
                startActivityForResult(new Intent(MainActivity.this,
                                ScanningActivity.class),
                        ScanningActivity.SCANNING_FOR_PRINTER);
                changePairAndUnpair();
            }
        });
        //Event jika tombol print diklik
        btn_print_image.setOnClickListener(view -> {
            if (!Printooth.INSTANCE.hasPairedPrinter())
                startActivityForResult(new Intent(MainActivity.this,
                                ScanningActivity.class),
                        ScanningActivity.SCANNING_FOR_PRINTER);
                printText();
                printImages();
        });

        changePairAndUnpair();
    }

    // cetak teks
    private void printText() {
        ArrayList<Printable> printables = new ArrayList<>();
        printables.add(new RawPrintable.Builder(new byte[]{27, 100, 4}).build());

        // Add text
        printables.add(new TextPrintable.Builder()
                .setText("Struk Pemesanan")
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                .setNewLinesAfter(1)
                .build());

        //Custom text
        printables.add(new TextPrintable.Builder()
                .setText("Struk Pemesanan Custom")
                .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_60())
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_ON())
                .setNewLinesAfter(1)
                .build());

        //printing.print(printables);
        Printooth.INSTANCE.printer().print(printables);
    }

    // tampilkan gambar (untuk kebutuhan uji coba)
    private void embedImages() {
        ArrayList<Printable> printables = new ArrayList<>();

        //Load image
        Picasso.get()
                .load("https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Gmail_icon_%282020%29.svg/512px-Gmail_icon_%282020%29.svg.png")
                .into(imgGallery);
    }

    // cetak gambar
    private void printImages() {
        ArrayList<Printable> printables = new ArrayList<>();

        //Load image
        Picasso.get()
                .load("https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Gmail_icon_%282020%29.svg/512px-Gmail_icon_%282020%29.svg.png")
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        printables.add(new ImagePrintable.Builder(bitmap).build());

                        //printing.print(printables);
                        Printooth.INSTANCE.printer().print(printables);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    // perintah print ke device
    private void initPrinting() {
        //if (!Printooth.INSTANCE.hasPairedPrinter())
        if (Printooth.INSTANCE.hasPairedPrinter())
            printing = Printooth.INSTANCE.printer();
        if (printing != null)
            printing.setPrintingCallback(this);
    }

    // fungsi untuk deteksi koneksi bluetooth dan memberi label aksi pada tombol 'sambungkan/putuskan'
    // koneksi bluetooth
    private void changePairAndUnpair() {
        if (Printooth.INSTANCE.hasPairedPrinter())
            btn_unpair_pair.setText(new StringBuilder("Unpair ")
                    .append(Printooth.INSTANCE.getPairedPrinter().getName()).toString());
        else
            btn_unpair_pair.setText("Pair with Printer");
    }

    @Override
    public void connectingWithPrinter() {
        Toast.makeText(this, "Menyambungkan ke printer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionFailed(String s) {
        Toast.makeText(this, "Gagal: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String s) {
        Toast.makeText(this, "Error: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printingOrderSentSuccessfully() {
        Toast.makeText(this, "Cetak ke printer berhasil", Toast.LENGTH_SHORT).show();
    }

    //Ctrl+O

    // membaca hasil aktivitas yang dijalankan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // membaca hasil perintah print bluetooth
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER &&
                resultCode == Activity.RESULT_OK)
            initPrinting();
        changePairAndUnpair();

        // membaca hasil perintah load gambar dari galeri
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
                // for gallery

                imgGallery.setImageURI(data.getData());
            }
        }

    }

    @Override
    public void disconnected() {

    }
}