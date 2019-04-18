package com.cahstudio.registrasidigital.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cahstudio.registrasidigital.R;
import com.cahstudio.registrasidigital.api.ApiInterface;
import com.cahstudio.registrasidigital.api.ApiService;
import com.cahstudio.registrasidigital.model.Instansi;
import com.cahstudio.registrasidigital.model.Tamu;
import com.cahstudio.registrasidigital.presenter.RegisterPresenter;
import com.cahstudio.registrasidigital.view.RegisterView;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/*
* Author by: Arifandis Winata
* Nickname: Fandis
*
* */

public class RegistrasiActivity extends AppCompatActivity implements RegisterView {
    private TextView tvNama,tvNoTelp;
    private AutoCompleteTextView actvInstansi;
    private SignaturePad signaturePad;
    private Button btnClearSignature, btnSubmit;
    private ImageButton btnMenu;

    private ApiInterface api;
    private RegisterPresenter presenter;
    private File mFile;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder popupDialog, exportDialog;

    private String PREF_NAME = "READ2019";
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        tvNama = findViewById(R.id.register_namaEt);
        tvNoTelp = findViewById(R.id.register_telpEt);
        actvInstansi = findViewById(R.id.register_instansiACTV);
        signaturePad = findViewById(R.id.register_signaturePad);
        btnClearSignature = findViewById(R.id.register_clearSignatureBtn);
        btnSubmit = findViewById(R.id.register_submitBtn);
        btnMenu = findViewById(R.id.register_menuBtn);

        pref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        editor = getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit();

        api = ApiService.getService().create(ApiInterface.class);
        presenter = new RegisterPresenter(this,api);
        setupLoadingDialog();
        setupExportDialog();
        setupTentangDialog();
        presenter.retrieveInstansi();

        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundResource(R.color.grey);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(getApplicationContext(),v);

                // This activity implements OnMenuItemClickListener
                popup.inflate(R.menu.menu);
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.laporan:
                                presenter.retrieveTamu();
                                return true;
                            case R.id.tentang:
                                popupDialog.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                if (findViewById(R.id.register_namaEt) != null || findViewById(R.id.register_instansiACTV)!= null){
                    if (tvNama.getText().toString().isEmpty() || actvInstansi.getText().toString().isEmpty()){
                        btnSubmit.setEnabled(false);
                        btnSubmit.setBackgroundResource(R.color.grey);
                    }else{
                        btnSubmit.setEnabled(true);
                        btnSubmit.setBackgroundResource(R.color.green);
                    }
                }else {
                    String nama = pref.getString("nama",null);
                    String instansi = pref.getString("instansi",null);
                    if (nama == null && instansi == null && nama.isEmpty() && instansi.isEmpty()){
                        btnSubmit.setEnabled(false);
                        btnSubmit.setBackgroundResource(R.color.grey);
                    }else {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setBackgroundResource(R.color.green);
                    }
                }
            }

            @Override
            public void onClear() {
                btnSubmit.setEnabled(false);
                btnSubmit.setBackgroundResource(R.color.grey);
            }
        });

        btnClearSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = tvNama.getText().toString();
                String instansi = actvInstansi.getText().toString();
                String telp = tvNoTelp.getText().toString();
                Bitmap tempBitmap = signaturePad.getSignatureBitmap();
                Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, tempBitmap.getWidth()/2, tempBitmap.getHeight()/2, true);

                if (nama.isEmpty() || instansi.isEmpty() || telp.isEmpty() || nama.equals("") || instansi.equals("")
                || telp.equals("")){
                    Toast.makeText(RegistrasiActivity.this, "Lengkapi form register", Toast.LENGTH_SHORT).show();
                }else {
                    try{
                        mFile = new File(getApplicationContext().getFilesDir(), nama + ".jpg");

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] bitmapData = bos.toByteArray();

                        // write byte to file
                        FileOutputStream fos = new FileOutputStream(mFile);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();

    //                    Gson gson = new Gson();
    //                    String json = gson.toJson(tamu);

                        RequestBody requestFile =
                                RequestBody.create(MediaType.parse("multipart/form-data"), mFile);

                        MultipartBody.Part file =
                                MultipartBody.Part.createFormData("image", mFile.getName(), requestFile);

                        RequestBody namaBody =
                                RequestBody.create(MediaType.parse("multipart/form-data"), nama.replace("'","`"));

                        RequestBody instansiBody =
                                RequestBody.create(MediaType.parse("multipart/form-data"), instansi);

                        RequestBody telpBody =
                                RequestBody.create(MediaType.parse("multipart/form-data"), telp);

                        presenter.addTamu(namaBody,instansiBody,telpBody,file);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupLoadingDialog(){
        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Harap tunggu sebentar ...");
        progressDialog.setIndeterminate(true);
    }

    private void setupExportDialog(){
        exportDialog = new AlertDialog.Builder(this);
        exportDialog.setTitle("Export berhasil");
        exportDialog.setMessage("File disimpan pada memori internal di folder READ 2019");

        exportDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void setupTentangDialog(){
        popupDialog = new AlertDialog.Builder(this);
        popupDialog.setTitle("Tentang");
        popupDialog.setMessage("READ2019\n" +
                "\n" +
                "Aplikasi registrasi kedatangan peserta dan tamu undangan pada acara Malam Anugrah Wisata Desa Award 2019.\n" +
                "\n" +
                "Diproduksi Oleh PT. Avemedia Nusantara.");

        popupDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (findViewById(R.id.register_namaEt) != null || findViewById(R.id.register_instansiACTV)!= null){
            editor.putString("nama",tvNama.getText().toString());
            editor.putString("instansi",actvInstansi.getText().toString());
            editor.putString("telp",tvNoTelp.getText().toString());
            editor.apply();
            outState.putParcelable("bitmap",signaturePad.getSignatureBitmap());
        }else{
            outState.putParcelable("bitmap",signaturePad.getSignatureBitmap());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            String nama = pref.getString("nama",null);
            String instansi = pref.getString("instansi",null);
            String telp = pref.getString("telp",null);
            Bitmap bitmap = (Bitmap) savedInstanceState.getParcelable("bitmap");
            if (findViewById(R.id.register_namaEt) != null && findViewById(R.id.register_instansiACTV)!= null &&
                    findViewById(R.id.register_telpEt ) != null && nama != null && instansi != null && telp != null){

                tvNama.setText(nama);
                actvInstansi.setText(instansi);
                tvNoTelp.setText(telp);
                signaturePad.setSignatureBitmap(bitmap);
            }else{
                signaturePad.setSignatureBitmap(bitmap);
            }
        }
    }

    //Load instansi data into spinner
    @Override
    public void loadInstansi(List<Instansi> instansiList) {
        List<String> array = new ArrayList<>();
        for (Instansi instansi: instansiList){
            array.add(instansi.getInstansi().toUpperCase());
        }

        if (findViewById(R.id.register_instansiACTV)!= null){
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,array);
            actvInstansi.setThreshold(1);
            actvInstansi.setAdapter(spinnerAdapter);
        }

        Log.d("list",instansiList.toString());
    }

    @Override
    public void exportToExcel(List<Tamu> tamuList) {
        Log.d("tamu",tamuList.toString());
        try {
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            } else {
                File sd = Environment.getExternalStorageDirectory();
                String csvFile = "Laporan READ 2019.xls";

                File directory = new File(sd.getAbsolutePath() + "/Read 2019");
                if (!directory.isDirectory()) {
                    directory.mkdirs();
                    directory.createNewFile();
                }

                File file = new File(directory,csvFile);
                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale("en","ID"));
                WritableWorkbook workbook;
                workbook = Workbook.createWorkbook(file,wbSettings);

                WritableSheet sheet = workbook.createSheet("laporan",0);

                //Formatted Title
                WritableFont titleFont = new  WritableFont(WritableFont.ARIAL, 14);
                titleFont.setBoldStyle(WritableFont.BOLD);

                WritableCellFormat cellFormat1 = new WritableCellFormat(titleFont);
                cellFormat1.setAlignment(Alignment.CENTRE);
                cellFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);

                //Formatted Data Header
                WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 12);
                headerFont.setBoldStyle(WritableFont.BOLD);


                WritableCellFormat cellFormat2 = new WritableCellFormat(headerFont);
                cellFormat2.setBackground(Colour.GREY_25_PERCENT);
                cellFormat2.setAlignment(Alignment.CENTRE);
                cellFormat2.setVerticalAlignment(VerticalAlignment.CENTRE);
                cellFormat2.setBorder(Border.ALL, BorderLineStyle.THIN);

                //Formatted Data Style
                WritableFont dataFont = new WritableFont(WritableFont.ARIAL, 11);

                WritableCellFormat cellFormat3 = new WritableCellFormat(dataFont);
                cellFormat3.setAlignment(Alignment.CENTRE);
                cellFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);
                cellFormat3.setBorder(Border.ALL, BorderLineStyle.THIN);

                //Formatted Total Data Style
                WritableFont dataTotalFont = new WritableFont(WritableFont.ARIAL, 11);
                dataTotalFont.setBoldStyle(WritableFont.BOLD);

                WritableCellFormat cellFormat4 = new WritableCellFormat(dataTotalFont);
                cellFormat4.setAlignment(Alignment.CENTRE);
                cellFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);
                cellFormat4.setBorder(Border.ALL, BorderLineStyle.THIN);

                sheet.setColumnView(0, 18);
                sheet.setColumnView(1, 20);
                sheet.setColumnView(2, 20);
                sheet.setColumnView(3, 18);
                sheet.mergeCells(0, 0, 8, 0);// Merge col[0-3] and row[0]
                sheet.addCell(new Label(0, 0, "Laporan READ 2019", cellFormat1));

                sheet.addCell(new Label(0, 2, "NO", cellFormat2));
                sheet.addCell(new Label(1, 2, "NAMA", cellFormat2));
                sheet.addCell(new Label(2,2,"TELP",cellFormat2));
                sheet.mergeCells(3,2,8,2);
                sheet.addCell(new Label(3,2,"INSTANSI",cellFormat2));
                sheet.addCell(new Label(9,2,"TTD",cellFormat2));

                int i = 3;
                Bitmap image;
                URL url;
                ByteArrayOutputStream stream;
                for (Tamu tamu: tamuList){
                    Log.d("namatamu",tamu.getTelp());
                    sheet.mergeCells(0,i,0,i+1);
                    sheet.addCell(new Label(0,i,tamu.getId(),cellFormat3));
                    sheet.mergeCells(1,i,1,i+1);
                    sheet.addCell(new Label(1,i,tamu.getNama(),cellFormat3));
                    sheet.mergeCells(2,i,2,i+1);
                    sheet.addCell(new Label(2,i,tamu.getTelp(),cellFormat3));
                    sheet.mergeCells(3,i,8,i+1);
                    sheet.addCell(new Label(3,i,tamu.getInstansi(),cellFormat3));

                    url = new URL(tamu.getTtd());
                    Log.d("url",tamu.getTtd());
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageData = stream.toByteArray();
                    sheet.mergeCells(9,i,9,i+1);
                    sheet.addImage(new WritableImage(9,i,1,2,imageData));

                    i+=2;
                }

                workbook.write();
                workbook.close();

                exportDialog.show();

                File open = new File(sd.getAbsolutePath() + "/Read 2019/"+csvFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(open),"application/vnd.ms-excel");
//                intent.setData(Uri.fromFile(open));
//                Intent chooser = Intent.createChooser(intent,"Pilih");
                startActivity(intent);
            }
        }catch (Exception e){
            Log.d("exception",e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvNama.setText("");
        actvInstansi.setText("");
        tvNoTelp.setText("");
        signaturePad.clear();
    }

    @Override
    public void onFail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }
}
