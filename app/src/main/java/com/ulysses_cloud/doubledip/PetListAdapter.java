package com.ulysses_cloud.doubledip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tristan on 17/02/2015.
 */
public class PetListAdapter extends ArrayAdapter<Pet> {
    int resource;
    String response;
    Context context;

    public PetListAdapter(Context context, int resource, List<Pet> items) {
        super(context, resource, items);
        this.resource=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View petView;
        //Get the current pet object
        Pet thePet = getItem(position);

        //Inflate the view
        if(convertView==null)
        {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            petView= vi.inflate(resource, parent, false);
        }
        else
        {
            petView = convertView;
        }
        //Get the text boxes from the listitem.xml file
        //TextView petName =(TextView)petView.findViewById(R.id.petName);
        TextView petName =(TextView)petView;

        //Assign the appropriate data from our pet object above
        petName.setText(thePet.petName);


        return petView;
    }
}
