package com.example.aadpractica;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aadpractica.operaciones.AfterPermissionsCheck;
import com.example.aadpractica.settings.RecordarArchivoSettings;
import com.example.aadpractica.settings.SettingsActivity;
import com.example.aadpractica.settings.UbicacionSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int NONE = -1;
    private static final int INTERN = 0; // radio id: interno
    private static final int PRIVATE = 2;
    private static final String KEY_ARCHIVO = "archivo";
    private static final int ID_PERMISO_LEER_ESCRIBIR = 4;
    private static final String TAG = "xyzyx" + MainActivity.class.getName();

    private static final int ID_PERMISO_LEER_CONTACTOS = 1;
    private List <Contacto> contactos;
    private List <String> telefonos;


    private Button btMostrar, btExportar, btEscribir;
    private RadioGroup rgTipo;
    private EditText etNombre;
    private TextView tvLista;
    private TextView prueba;

    private String name, value;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        assignEvents();

    }

    private void initComponents() {
        btMostrar = findViewById(R.id.btMostrar);
        btExportar = findViewById(R.id.btExportar);
        rgTipo = findViewById(R.id.rgTipo);
        btEscribir = findViewById(R.id.btEscribir);
        etNombre = findViewById(R.id.etNombre);
        etNombre.setText(readPreferences());
    }

    private void assignEvents() {
        btMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPermisos();
            }
        });

        btExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportarCSV();
            }
        });

        btEscribir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    writeFile();

            }
        });
    }

    private void writeFile() {
        if (isValues() && !value.isEmpty()) {
            if (type == PRIVATE) {
                checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.tituloExplicacion, R.string.mensajeExplicacion,
                        new AfterPermissionsCheck() {
                            @Override
                            public void doTheJob() {
                                writeNotes();
                            }
                        });
            } else {
                writeNotes();
            }
        }
    }

    private void writeNotes()  {
        contactos = getListaContactos();
        String salidaArchivo = etNombre.getText().toString();

        try {
            File f = new File(salidaArchivo);
            FileWriter fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < contactos.size(); i++) {
                bw.write(String.valueOf(contactos.get(i)));
                bw.newLine();

            }
            bw.close();
            fw.close();
            prueba.setText("CORRECTO");
        }catch(IOException e){
            prueba.setText(e.getMessage());
        }
    }

    private void checkPermissions(String permiso, int titulo, int mensaje, AfterPermissionsCheck apc) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                explain(R.string.tituloExplicacion, R.string.mensajeExplicacion, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        ID_PERMISO_LEER_ESCRIBIR);
            }
        } else {
            apc.doTheJob();
            //writeNotes();
            //readNotes();
        }
    }

    private void explain(int title, int message, final String permissions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.respSi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permissions}, ID_PERMISO_LEER_ESCRIBIR);
            }
        });
        builder.setNegativeButton(R.string.respNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private File getFile(int type) {
        return MainActivity.getFile(this, type);
    }

    private static int getCheckedType(int item) { //Le pasamos el radioButton pulsado
        int tipo = NONE;
        switch (item) {
            case R.id.rbInterno:
                tipo = INTERN;
                break;
            case R.id.rbPrivado:
                tipo = PRIVATE;
                break;
        }
        return tipo;
    }

    private boolean isValues() {
        name = etNombre.getText().toString().trim(); // Devuelve el contenido más informaciñon adicional, trim quita espacios iniciales
        type = MainActivity.getCheckedType(rgTipo.getCheckedRadioButtonId()); // Obtienes el radio button pulsado
        //MainActivity. (opcional)
        return !(name.isEmpty() || type == NONE); // Devuelve false si está vacío o si es -1
    }

    private static File getFile(Context context, int type) {
        File file = null;
        switch (type) {
            case INTERN:
                file = context.getFilesDir();
                break;
            case PRIVATE:
                file = context.getExternalFilesDir(null);
                break;
        }
        return file;
    }




    private void pedirPermisos() {
        // AQUI SE COMPRUEBA SI LA APP TIENE PERMISOS PARA LO QUE SOLICITAMOS
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // SI NO TUVIERA PERMISO LA APP VOLVERA A PEDIRLA
            // DEBERIA VOLVER A PREGUNTAR POR EL PERMISO
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, R.string.razon, Toast.LENGTH_LONG).show();
            }
            // 2º VEZ QUE LE PIDO PERMISO AL USUARIO
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    ID_PERMISO_LEER_CONTACTOS);
        } else {
            // Tengo permiso por lo que realizo la acción
            obtenerListaContactos();
        }

    }

    private void obtenerListaContactos() {
        contactos = getTelefonos();
        TextView tvLista = findViewById(R.id.tvLista);
        tvLista.setText(contactos.toString());
    }


    public List<Contacto> getListaContactos(){
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int indiceNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

        List<Contacto> lista = new ArrayList<>();
        Contacto contacto;
        while(cursor.moveToNext()){
            contacto = new Contacto();
            contacto.setId(cursor.getLong(indiceId));
            contacto.setNombre(cursor.getString(indiceNombre));
            //contacto.setId(1).setNombre("2");
            lista.add(contacto);
        }

        return lista;

    }

    public List<Contacto> getTelefonos(){
        contactos = getListaContactos();
        long id;

        for(int i = 0; i < contactos.size(); i++){
            id = contactos.get(i).getId();
            telefonos = getListaTelefonos(id);
            contactos.get(i).setTelefono(telefonos.toString());
        }
        return contactos;
    }

    public List<String> getListaTelefonos(long id){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String argumentos[] = new String[]{id+""};
        String orden = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        List<String> lista = new ArrayList<>();
        String numero;
        while(cursor.moveToNext()){
            numero = cursor.getString(indiceNumero);
            lista.add(numero);
        }
        return lista;
    }

    private void exportarCSV() {

    }

    private String readPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_ARCHIVO, "");
    }


    //----------MENU--------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnSettings:
                showSettings();
                return true;
            case R.id.mnRecordar:
                recordarArchivo();
                return true;
            case R.id.mnUbicacion:
                ubicacionArchivo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ubicacionArchivo() {
        Intent intent = new Intent(this, UbicacionSettings.class);
        startActivity(intent);
    }

    private void recordarArchivo() {
        Intent intent = new Intent(this, RecordarArchivoSettings.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    //-----CLASE CONTACTO--------

    private static class Contacto{
        private long id;
        private String nombre;
        private String telefono;

        public long getId() {
            return id;
        }

        public Contacto setId(long id) {
            this.id = id;
            return this;
        }

        public String getNombre() {
            return nombre;
        }

        public Contacto setNombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public String getTelefono() {
            return telefono;
        }

        public Contacto setTelefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        @Override
        public String toString() {
            return "Contacto{" +
                    "id=" + id +
                    ", nombre='" + nombre + '\'' +
                    ", telefono='" + telefono + '\'' +
                    '}';
        }
    }

}
