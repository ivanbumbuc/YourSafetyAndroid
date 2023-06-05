package com.example.yoursafetyandroid.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.menu.SettingsFragment;

import java.util.ArrayList;

public class ListAdapterPersons extends ArrayAdapter<String> {
    private ArrayList<String> list;
    private Context context;


    public ListAdapterPersons(Context context, ArrayList<String> items)
    {
        super(context, R.layout.lista_persoane,items);
        this.context=context;
        list=items;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater layout = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView=layout.inflate(R.layout.lista_persoane,null);
            TextView numar=convertView.findViewById(R.id.numar);
            numar.setText(position+1+".");
            TextView nume=convertView.findViewById(R.id.nume);
            nume.setText(list.get(position));
            ImageView delete=convertView.findViewById(R.id.sterge);
            delete.setOnClickListener(view -> SettingsFragment.deletePerson(position));
        }
        return convertView;
    }

}