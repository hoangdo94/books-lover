package hcmut.cse.bookslover.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import hcmut.cse.bookslover.R;
import hcmut.cse.bookslover.models.Book;

public class BookThumbAdapter extends RecyclerView.Adapter<BookThumbAdapter.ViewHolder> {
    private ArrayList<Book> books;
    private Context context;
    OnItemClickListener mItemClickListener;

    public BookThumbAdapter(Context context, ArrayList<Book> books) {
        this.books = books;
        this.context = context;
    }

    @Override
    public BookThumbAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_thumb_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookThumbAdapter.ViewHolder viewHolder, int i) {
        Picasso.with(context)
                .load(books.get(i).getAbsoluteCoverUrl())
                .placeholder(R.drawable.book_default)
                .resize(320, 480).centerCrop().into(viewHolder.img_book_thumb);
        viewHolder.tv_title.setText(books.get(i).getTitle());
        viewHolder.tv_author.setText(books.get(i).getAuthor());
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_title;
        private TextView tv_author;
        private ImageView img_book_thumb;
        public ViewHolder(View view) {
            super(view);
            tv_title = (TextView)view.findViewById(R.id.tv_title);
            tv_author = (TextView)view.findViewById(R.id.tv_author);
            img_book_thumb = (ImageView) view.findViewById(R.id.img_book_thumb);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}

