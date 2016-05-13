package hcmut.cse.bookslover.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hcmut.cse.bookslover.R;
import hcmut.cse.bookslover.models.Comment;

/**
 * Created by huy on 5/11/2016.
 */
public class CommentAdapter  extends ArrayAdapter<Comment> {
    public ArrayList<Comment> comment_list;
    Context cContext;
    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        cContext = context;
        comment_list = comments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Comment comment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comments_list_item, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.cm_name);
        TextView content = (TextView) convertView.findViewById(R.id.cm_comment);
        ImageView avatar = (ImageView) convertView.findViewById(R.id.cm_avatar);
        // Populate the data into the template view using the data object
        name.setText(comment.getUser().getUsername());
        content.setText(comment.getContent());
        Picasso.with(cContext)
                .load(comment.getUser().getAbsoluteAvatarUrl())
                .resize(200, 200)
                .centerCrop()
                .into(avatar);
        // Return the completed view to render on screen
        return convertView;
    }
}
