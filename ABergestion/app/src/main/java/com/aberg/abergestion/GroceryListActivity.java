package com.aberg.abergestion;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mickael on 23/01/2018.
 */



public class GroceryListActivity extends AppCompatActivity {

    private Button back;
    private ListView stockListView;
    private ArrayList <Product> al_groceryList;
    private Product p;
    private Product p2;
    private Product p3;
    private Button add;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ABergestion";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        al_groceryList = new ArrayList<>();

        File dir = new File(path);
        dir.mkdir();

        File file = new File(path +"groceryList.txt");

        System.out.println("Fichier existe : "+file.exists());
        System.out.println("Dossier existe : "+dir.exists());
        System.out.println("path : "+path);

        if(file.exists()){
            loadGroceryList(al_groceryList);
        }
        else{
            try{
                file.createNewFile();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        back = findViewById(R.id.button_back);
        add = findViewById(R.id.button_add);
        stockListView = (ListView) findViewById(R.id.listViewStock);

        showListView();

        add.setOnClickListener(BtnAdd);
        back.setOnClickListener(BtnBack);

    }

    private void showListView(){
        // Définition des colonnes
        // SimpleCursorAdapter a besoin obligatoirement d'un ID nommé "_id"
        // Ensuite on met le nombre de colonnes que l'on veut
        String[] colums = new String[]{"_id","col1","col2"};

        // Définition des données du tableau
        // On affecte au matrixCursor les colonnes que l'on vient de créer
        // On démarre le MatrixCursor
        MatrixCursor matrixCursor = new MatrixCursor(colums);
        startManagingCursor(matrixCursor);

        //On ajoute des objets au MatrixCursor
        for(int i=0;i<al_groceryList.size();i++){
            matrixCursor.addRow(new Object[]{i, al_groceryList.get(i).getName(), al_groceryList.get(i).getQuantity()+" "+al_groceryList.get(i).getForm()});
        }


        // matrixCursor.addRow(new Object[]{0, al_groceryList.get(0).getName(), al_groceryList.get(0).getQuantity()+" "+al_groceryList.get(0).getForm()});
        // matrixCursor.addRow(new Object[]{2, "col1:ligne2", "col2:ligne2"});

        // On prend les données des colonnes 1 et 2 ...
        String[]from = new String []{"col1","col2"};
        // ... pour les placer dans les TextView définis dans "row_item.xml"
        int[] to = new int[] { R.id.textView_Col1, R.id.textView_Col2};

        // On crée l'objet SimpleCursorAdapter
        // On met le context (this ici), ensuite la définition des lignes de la liste, ensuite on ajoute les lignes, on définit les colonnes, on les lient aux textView, on met le flag à 0
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.row_item,matrixCursor,from,to,0);

        // On lie la liste avec l'adapter
        stockListView.setAdapter(adapter);
    }

    private View.OnClickListener BtnBack = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GroceryListActivity.this, StockActivity.class);
            startActivity(intent);
            GroceryListActivity.this.finish();
        }

    };

    private View.OnClickListener BtnAdd = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            popupAddProduct();
        }

    };

    private void popupAddProduct(){
        //On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(GroceryListActivity.this);
        final View alertDialogView = factory.inflate(R.layout.alertdialog_add_product, null);

        //Création de l'AlertDialog
        AlertDialog.Builder popup = new AlertDialog.Builder(this);

        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        popup.setView(alertDialogView);

        //On donne un titre à l'AlertDialog
        popup.setTitle(R.string.alertDialog_addProduct);

        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //popup.setIcon(android.R.drawable.ic_dialog_alert);

        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        popup.setPositiveButton(R.string.text_add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à notre vue personnalisée (cad à alertDialogView)
                EditText productName = alertDialogView.findViewById(R.id.editText_popupProductName);
                EditText productQuantity = alertDialogView.findViewById(R.id.numericText_popupQuantity);
                EditText productType = alertDialogView.findViewById(R.id.editText_popupProductType);



                String name = productName.getText().toString();
                String quantity = productQuantity.getText().toString();
                String type = productType.getText().toString();

                if(isEmpty(name) || isEmpty(quantity) || isEmpty(type)){
                    alertDialog(getString(R.string.AlertDialog_champsrempli));
                }
                else{
                    int temp = Integer.parseInt(quantity);
                    al_groceryList.add(new Product(name,null,temp,null,null,type));
                    showListView();

                    //On affiche dans un Toast le texte contenu dans l'EditText de notre AlertDialog
                    Toast.makeText(GroceryListActivity.this, R.string.toast_productAdded, Toast.LENGTH_SHORT).show();
                }

                saveGroceryList(al_groceryList);

            } });

        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        popup.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               //On ferme la popup
            } });
        popup.show();
    }

    //Fonction pour afficher un pop up avec un message et un bouton "Ok"
    private void alertDialog(String message){
        //On crée la fenetre
        AlertDialog bugAlert = new AlertDialog.Builder(this).create();

        //On applique le message en paramètre
        bugAlert.setMessage(message);

        //On ajoute le bouton positif 'Ok' qui ferme juste la pop up
        bugAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alertDialog_ok), new AlertDialog.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //On affiche la pop up
        bugAlert.show();
    }

    //Fonction pour savoir si un String est vide
    private Boolean isEmpty(String s){
        if(s.length() != 0){
            return false;
        }
        return true;
    }

    private void saveGroceryList(ArrayList<Product> liste){
        File file = new File(path + "/groceryList.txt");

        int tailleArray = liste.size();
        String [] savedText = new String[tailleArray];
        String temp;

        for(int i=0; i < tailleArray; i++){
            temp = liste.get(i).getName()+";"/*+liste.get(i).getCategory()+";"*/+liste.get(i).getQuantity()+";"+liste.get(i).getForm();
            savedText[i] = temp;
        }

        save(file, savedText);
    }

    private void loadGroceryList(ArrayList<Product> liste){
        File file = new File(path + "/groceryList.txt");

        String[] loadText = load(file);

        String tempName;
        String tempCategory;
        int tempQuantity;
        String tempForm;
        String[] temp;

        for(int i=0; i < loadText.length;i++){
            temp = loadText[i].split(";");
            tempName = temp[0];
            //tempCategory = temp[1];
            tempQuantity = Integer.parseInt(temp[1]);
            tempForm = temp[2];

            liste.add(new Product(tempName,null,tempQuantity,null,null,tempForm));
        }
    }

    private void save(File file, String[] data){
        FileOutputStream fos = null;

        try{
            fos = openFileOutput("groceryList.txt",MODE_WORLD_READABLE);
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }

        try{
            try{
                System.out.println("dir : ");
                for(int i = 0; i < data.length; i++){
                    fos.write(data[i].getBytes());
                    if(i < data.length){
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        finally {
            try{
                fos.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private String[] load(File file){
        FileInputStream fis = null;

        try{
            fis = openFileInput(file.getName());
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int azah = 0;

        try{
            while((test = br.readLine()) != null){
                azah++;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try{
            fis.getChannel().position(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        String[] array = new String[azah];
        String line;
        int i = 0;

        try{
            while((line = br.readLine()) != null){
                array[i] = line;
                i++;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return array;
    }


}
