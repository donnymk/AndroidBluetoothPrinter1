package com.example.androidbluetoothprinter1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PrintingCallback {

    private final int GALLERY_REQ_CODE = 1000;
    ImageView imgGallery;

    Printing printing;
    Button btn_unpair_pair, btn_print, btn_print_image;

    // inisialisasi data Bitmap
    Bitmap bmNormal, bmGrayScale;

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
                //printText();
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

    // tampilkan gambar di image view (untuk pertama kali)
    private void embedImages() {
        ArrayList<Printable> printables = new ArrayList<>();

        //Load image
        Picasso.get()
                .load(R.drawable.bwposter)
                .resize(396, 704)
                .centerInside()
                //.into(imgGallery);
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //bitmap.setWidth(208);
                        bmGrayScale = toGrayscale(bitmap);
                        imgGallery.setImageBitmap(bmGrayScale);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    // cetak gambar
    private void printImages() {
        ArrayList<Printable> printables = new ArrayList<>();

        //Load image
/*        Picasso.get()
                .load(R.drawable.foldedhand)
                .resize(400, 400)
                .centerInside()
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //printables.add(new ImagePrintable.Builder(bitmap).build());
                        printables.add(new ImagePrintable.Builder(toGrayscale(bitmap))
                                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                                .build());

                        //printing.print(printables);
                        Printooth.INSTANCE.printer().print(printables);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });*/
        printables.add(new ImagePrintable.Builder(bmGrayScale)
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                .build());
        Printooth.INSTANCE.printer().print(printables);
    }

    // sumber https://stackoverflow.com/questions/58791821/printooth-is-not-printing-the-image
    public static Bitmap toGrayscale(Bitmap srcImage) {

        Bitmap bmpGrayscale = Bitmap.createBitmap(srcImage.getWidth(),
                srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);
        return bmpGrayscale;
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
        // referensi http://android-er.blogspot.com/2015/11/convert-bitmap-to-grayscale-using.html
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
                // for gallery
                //imgGallery.setImageURI(data.getData());
                Uri dataUri = data.getData();
/*                int w = imgGallery.getWidth();
                int h = imgGallery.getHeight();
                Toast.makeText(MainActivity.this,
                        dataUri.toString() + "\n" +
                                w + " : " + h,
                        Toast.LENGTH_LONG).show();
                try {
                    bmNormal = bmGrayScale = null;
                    bmNormal = loadScaledBitmap(dataUri, w, h);
                    bmGrayScale = toGrayscale(bmNormal);
                    
                    imgGallery.setImageBitmap(bmGrayScale);
                    Toast.makeText(MainActivity.this,
                            bmGrayScale.getWidth() + " x " + bmGrayScale.getHeight(),
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }*/
                Picasso.get()
                        .load(dataUri)
                        .resize(396, 704)
                        .centerInside()
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                bmGrayScale = toGrayscale(bitmap);
                                imgGallery.setImageBitmap(bmGrayScale);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            }
        }

    }

    @Override
    public void disconnected() {

    }

    /*
reference:
Load scaled bitmap
http://android-er.blogspot.com/2013/08/load-scaled-bitmap.html
 */
    private Bitmap loadScaledBitmap(Uri src, int req_w, int req_h) throws FileNotFoundException {

        Bitmap bm = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getBaseContext().getContentResolver().openInputStream(src),
                null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, req_w, req_h);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeStream(
                getBaseContext().getContentResolver().openInputStream(src), null, options);

        return bm;
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}