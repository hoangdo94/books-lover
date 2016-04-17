package hcmut.cse.bookslover;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import hcmut.cse.bookslover.models.Book;

public class BookDetailsActivity extends AppCompatActivity {
    Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();
        Intent intent = getIntent();
        book = gson.fromJson(intent.getStringExtra("data"), Book.class);
        setTitle(book.getTitle());

        ImageView cover = (ImageView) findViewById(R.id.img_cover);
        TextView title = (TextView) findViewById(R.id.tv_title);
        TextView author = (TextView) findViewById(R.id.tv_author);
        TextView year = (TextView) findViewById(R.id.tv_year);
        TextView genres = (TextView) findViewById(R.id.tv_genres);
        HtmlTextView review = (HtmlTextView) findViewById(R.id.tv_review);

        Picasso.with(getApplicationContext())
                .load(book.getAbsoluteCoverUrl())
                .resize(240, 360)
                .centerCrop()
                .into(cover);
        try {
            title.setText(book.getTitle());
        } catch (Exception e) {
            title.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            author.setText(book.getAuthor());
        } catch (Exception e) {
            author.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            year.setText(book.getPublishYear());
        } catch (Exception e) {
            year.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            String[] gs = book.getGenres();
            String genresText = "";
            for (int i=0; i<gs.length; i++) {
                if (i > 0) genresText += ", ";
                genresText += gs[i];
            }
            if (genresText.trim().isEmpty()) genresText = getResources().getString(R.string.text_unkown);
            genres.setText(genresText);
        } catch (Exception e) {
            genres.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            review.setHtmlFromString(book.getReview(), new HtmlTextView.RemoteImageGetter());
        } catch (Exception e) {
            review.setHtmlFromString(" ", null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
