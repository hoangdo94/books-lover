package hcmut.cse.bookslover.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import hcmut.cse.bookslover.R;
import hcmut.cse.bookslover.models.User;

/**
 * Created by huy on 4/21/2016.
 */
public class CustomAdapter extends ArrayAdapter<User> {
    public ArrayList<User> persons_list;
    public CustomAdapter(Context context, ArrayList<User> persons) {
        super(context, 0, persons);
        persons_list = persons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User person = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.userinfo_listview, parent, false);
        }
        // Lookup view for data population
        TextView username = (TextView) convertView.findViewById(R.id.username);
        TextView email = (TextView) convertView.findViewById(R.id.email);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView age = (TextView) convertView.findViewById(R.id.age);
        TextView website = (TextView) convertView.findViewById(R.id.website);
        // Populate the data into the template view using the data object
        username.setText(person.getUsername());
        email.setText(person.getEmail());
        name.setText(person.getName());
        if (person.getMeta().getAge() > -1) {
            age.setText(String.valueOf(person.getMeta().getAge()));
        }
        website.setText(person.getMeta().getWebsite());
        // Return the completed view to render on screen
        return convertView;
    }

}
