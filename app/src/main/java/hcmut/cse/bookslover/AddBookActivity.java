package hcmut.cse.bookslover;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.utils.APIRequest;

public class AddBookActivity extends AppCompatActivity {

    EditText intro;
    EditText title;
    EditText author;
    EditText year;
    EditText genre;
    Button btnAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        setTitle("Thêm sách");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intro = (EditText) findViewById(R.id.intro);
        title = (EditText) findViewById(R.id.title);
        author = (EditText) findViewById(R.id.author);
        year = (EditText) findViewById(R.id.year);
        genre = (EditText) findViewById(R.id.genre);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        intro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.intro) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String txtTitle = title.getText().toString();
                String txtAuthor = author.getText().toString();
                String txtGenre = genre.getText().toString();
                String txtIntro = intro.getText().toString();
                int publishYear = Integer.parseInt(year.getText().toString());

                if (txtTitle.isEmpty() || txtAuthor.isEmpty())
                    Toast.makeText(getApplicationContext(), "Bạn phải nhập tên sách và tác giả!", Toast.LENGTH_SHORT).show();

                RequestParams params = new RequestParams();
                params.put("title", txtTitle);
                params.put("author", txtAuthor);
                params.put("genres", txtGenre);
                params.put("publishYear", publishYear);
                params.put("review", txtIntro);

                APIRequest.post("books", params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                        try {
                            int status = r.getInt("status");
                            if (status == 1) {
                                Toast.makeText(getApplicationContext(), "Sách của bạn đã được thêm!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Thêm sách thất bại!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                        Toast.makeText(getApplicationContext(), "Thêm sách thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
