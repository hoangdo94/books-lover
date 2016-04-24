package hcmut.cse.bookslover;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.Header;
import org.json.*;
import hcmut.cse.bookslover.models.Book;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;
import hcmut.cse.bookslover.utils.BookThumbAdapter;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    ArrayList<Book> books = new ArrayList<Book>();
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    BookThumbAdapter bookThumbAdapter;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gson = new Gson();

        initToolbarAndDrawerViews();

        initNavigationView();

        initSwipeRefreshLayout();

        initRecyclerView();
    }

    private void initToolbarAndDrawerViews() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CredentialsPrefs.isLoggedIn()) {
                    Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

        updateAuthenticationInfoOnNavbar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // Check which request we're responding to
        if (requestCode == 1) {
            updateAuthenticationInfoOnNavbar();
        }
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorLime);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView)findViewById(R.id.book_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new LandingAnimator());
        bookThumbAdapter = new BookThumbAdapter(getApplicationContext(), books);
        recyclerView.setAdapter(new AlphaInAnimationAdapter(bookThumbAdapter));

        bookThumbAdapter.setOnItemClickListener(new BookThumbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Book book = books.get(position);
                showBookDetails(book);
            }
        });

        // Fetch data
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                fetchData();
            }
        });
    }

    private void fetchData() {

        int size = books.size();
        books.clear();
        bookThumbAdapter.notifyItemRangeRemoved(0, size);

        APIRequest.get("books", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                try {
                    JSONArray bs = r.getJSONArray("data");
                    for (int i = 0; i < bs.length(); i++) {
                        JSONObject b = bs.getJSONObject(i);
                        Book book = gson.fromJson(b.toString(), Book.class);
                        books.add(book);
                    }
                    bookThumbAdapter.notifyItemRangeInserted(0, books.size());
                    Toast.makeText(getBaseContext(), "Tải thành công", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Có lỗi xảy ra. Vui lòng thử lại", Toast.LENGTH_LONG).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                Toast.makeText(getBaseContext(), "Không thể gửi yêu cầu. Hãy kiểm tra lại kết nối Internet", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showBookDetails(Book book) {
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra("data", gson.toJson(book, Book.class).toString());
        startActivity(intent);
    }

    private void updateAuthenticationInfoOnNavbar() {
        View headerView = navigationView.getHeaderView(0);
        ImageView avatar = (ImageView) headerView.findViewById(R.id.iv_avatar);
        TextView usernameTV = (TextView) headerView.findViewById(R.id.tv_username);
        TextView emailTV = (TextView) headerView.findViewById(R.id.tv_email);
        Menu navMenu = navigationView.getMenu();
        if (CredentialsPrefs.isLoggedIn()) {
            Picasso.with(getApplicationContext())
                    .load(CredentialsPrefs.getCurrentUser().getAbsoluteAvatarUrl())
                    .resize(120, 120).centerCrop().into(avatar);
            usernameTV.setText(CredentialsPrefs.getUsername());
            emailTV.setText(CredentialsPrefs.getCurrentUser().getEmail());
            navMenu.setGroupVisible(R.id.menu_top, true);
            navMenu.setGroupVisible(R.id.menu_bottom, true);
        } else {
            avatar.setImageResource(R.mipmap.ic_launcher);
            usernameTV.setText("Đăng nhập/Đăng ký");
            emailTV.setText("Chưa đăng nhập!");
            navMenu.setGroupVisible(R.id.menu_top, false);
            navMenu.setGroupVisible(R.id.menu_bottom, false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new) {
            // Handle the camera action
        } else if (id == R.id.nav_logout) {
            Toast.makeText(getApplicationContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            CredentialsPrefs.clearCredentials();
            updateAuthenticationInfoOnNavbar();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
