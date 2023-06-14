package com.it.univai.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.it.univai.R;

import java.util.List;

public class LanguageSpinnerAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> languages;

    public LanguageSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> languages) {
        super(context, resource, languages);
        this.context = context;
        this.languages = languages;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.language_dropdown_item, parent, false);

        TextView state = row.findViewById(R.id.language_text);
        ImageView flag = row.findViewById(R.id.language_flag);

        state.setText(languages.get(position));
        if(position == 0) {
            flag.setImageDrawable(context.getDrawable(R.drawable.italian_flag));
        } else {
            flag.setImageDrawable(context.getDrawable(R.drawable.english_flag));
        }

        return row;
    }
}
