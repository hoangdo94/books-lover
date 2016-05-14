package hcmut.cse.bookslover.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        // Get the data item for this position
        Comment comment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comments_list_item, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.cm_name);
        TextView content = (TextView) convertView.findViewById(R.id.cm_comment);
        TextView time_ago = (TextView) convertView.findViewById(R.id.cm_time_ago);
        ImageView avatar = (ImageView) convertView.findViewById(R.id.cm_avatar);
        // Populate the data into the template view using the data object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            time_ago.setText(getTimeAgo(sdf.parse(comment.getCreatedAt())));
        } catch (ParseException e) {
            time_ago.setText("Vừa xong");
            e.printStackTrace();
        }
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

    public Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public String getTimeAgo(Date date) {

        if(date == null) {
            return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            return null;
        }

        int dim = getTimeDistanceInMinutes(time);

        if (dim == 0) {
            return "Vừa xong";
        } else if (dim == 1) {
            return "1 phút trước";
        } else if (dim >= 2 && dim <= 44) {
            return dim + " phút trước";
        } else if (dim >= 45 && dim <= 89) {
            return "1 tiếng trước";
        } else if (dim >= 90 && dim <= 1439) {
            return (Math.round(dim / 60)) + " tiếng trước";
        } else if (dim >= 1440 && dim <= 2519) {
            return "1 ngày trước";
        } else if (dim >= 2520 && dim <= 43199) {
            return (Math.round(dim / 1440)) + " ngày trước";
        } else if (dim >= 43200 && dim <= 86399) {
            return "1 tháng trước";
        } else if (dim >= 86400 && dim <= 525599) {
            return (Math.round(dim / 43200)) + " tháng trước";
        } else if (dim >= 525600 && dim <= 655199) {
            return "1 năm trước";
        } else if (dim >= 655200 && dim <= 914399) {
            return "Hơn 1 năm trước";
        } else if (dim >= 914400 && dim <= 1051199) {
            return "2 năm trước";
        } else if (dim > 1051200) {
            return (Math.round(dim / 525600)) + " năm trước";
        }
        return "Vừa xong";
    }

    private int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }
}
