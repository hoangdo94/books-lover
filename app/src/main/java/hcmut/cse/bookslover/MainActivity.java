package hcmut.cse.bookslover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pushbots.push.Pushbots;
import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.Header;
import org.json.*;
import hcmut.cse.bookslover.models.Book;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;
import hcmut.cse.bookslover.utils.BookThumbAdapter;
import hcmut.cse.bookslover.utils.EndlessRecyclerViewScrollListener;
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

    // pagination & filter
    int type = 1; // 1: all books, 2: posted books, 3: favorites books
    String userId;
    int currentPage;
    int totalPage;
    int totalItem;
    String search;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;
    private TextView resultCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gson = new Gson();

        initPaginationAndFilter();

        initToolbarAndDrawerViews();

        initNavigationView();

        initSwipeRefreshLayout();

        initRecyclerView();
    }

    private void initPaginationAndFilter() {
        currentPage = 1;
        totalPage = 1;
        search = null;
        resultCount = (TextView) findViewById(R.id.search_result_count);
        resultCount.setVisibility(View.GONE);
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
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivityForResult(intent, 2);
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
        } else if (requestCode == 2 && data.getStringExtra("avatar").equals("1")){
            View headerView = navigationView.getHeaderView(0);
            ImageView avatar = (ImageView) headerView.findViewById(R.id.iv_avatar);
            Picasso.with(getApplicationContext())
                    .load(CredentialsPrefs.getCurrentUser().getAbsoluteAvatarUrl())
                    .resize(120, 120).centerCrop().into(avatar);
        } else if (requestCode == 3) { // book details
            if (resultCode == RESULT_OK) {
                if(data.getBooleanExtra("deleted", false)) {
                    fetchInitialData();
                }
            }
        } else if (requestCode == 4) { // add book
            if (resultCode == RESULT_OK) {
                fetchInitialData();
            }
        }
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorLime);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchInitialData();
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

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener((GridLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                System.out.println("load more Page" + page + " " + totalItemsCount);
                fetchMoreData();
            }
        });

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
                fetchInitialData();
            }
        });
    }

    private void fetchInitialData() {
        currentPage = 1;
        int size = books.size();
        books.clear();
        bookThumbAdapter.notifyItemRangeRemoved(0, size);

        String requestRoute = (type != 3)?("books?page=1"):("favorites/user/" + userId + "?page=1");
        if (type == 2) {
            requestRoute += "&userId=" + userId;
        }
        if (search != null) {
            requestRoute += "&search=" + search;
        }
        System.out.println(requestRoute);

        APIRequest.get(requestRoute, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                try {
                    totalPage = r.getInt("pages");
                    currentPage = r.getInt("page");
                    totalItem = r.getInt("total");
                    System.out.println("PAGE " + currentPage + "/" + totalPage + ", total " + totalItem);
                    if (totalItem == 0 && search == null) {
                        resultCount.setVisibility(View.VISIBLE);
                        resultCount.setText("Không có sách nào");
                    } else {
                        resultCount.setText("Tìm thấy " + totalItem + " kết quả phù hợp");
                    }
                    JSONArray bs = r.getJSONArray("data");
                    for (int i = 0; i < bs.length(); i++) {
                        JSONObject b = bs.getJSONObject(i);
                        Book book = gson.fromJson(b.toString(), Book.class);
                        books.add(book);
                    }
                    bookThumbAdapter.notifyItemRangeInserted(0, books.size());
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

    private void fetchMoreData() {
        if (currentPage >= totalPage) return;
        currentPage++;
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                String requestRoute = (type != 3)?("books?page=" + currentPage):("favorites/user/" + userId + "?page=" + currentPage);
                if (type == 2) {
                    requestRoute += "&userId=" + userId;
                }
                if (search != null) {
                    requestRoute += "&search=" + search;
                }
                System.out.println(requestRoute);

                APIRequest.get(requestRoute, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                        try {
                            totalPage = r.getInt("pages");
                            currentPage = r.getInt("page");
                            totalItem = r.getInt("total");
                            System.out.println("PAGE " + currentPage + "/" + totalPage + ", total " + totalItem);
                            JSONArray bs = r.getJSONArray("data");
                            for (int i = 0; i < bs.length(); i++) {
                                JSONObject b = bs.getJSONObject(i);
                                Book book = gson.fromJson(b.toString(), Book.class);
                                books.add(book);
                                bookThumbAdapter.notifyItemInserted(books.size() - 1);
                            }
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
        });
    }

    private void showBookDetails(Book book) {
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra("data", gson.toJson(book, Book.class).toString());
        startActivityForResult(intent, 3);
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
            navMenu.findItem(R.id.nav_favorite_books).setVisible(true);
            navMenu.findItem(R.id.nav_posted_books).setVisible(true);
        } else {
            avatar.setImageResource(R.mipmap.ic_launcher);
            usernameTV.setText("Đăng nhập/Đăng ký");
            emailTV.setText("Chưa đăng nhập!");
            navMenu.setGroupVisible(R.id.menu_top, false);
            navMenu.setGroupVisible(R.id.menu_bottom, false);
            navMenu.findItem(R.id.nav_favorite_books).setVisible(false);
            navMenu.findItem(R.id.nav_posted_books).setVisible(false);
            if (type != 1) {
                setTitle("Book Lovers");
                type = 1;
                closeMenuSearch();
                clearSearch();
                mSearchAction.setVisible(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(isSearchOpened) {
            handleMenuSearch();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void closeMenuSearch() {
        ActionBar action = getSupportActionBar();
        action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
        action.setDisplayShowTitleEnabled(true); //show the title in the action bar

        //add the search icon in the action bar
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white));

        isSearchOpened = false;
    }

    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar(); //get the actionbar

        if(isSearchOpened){ //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white));

            isSearchOpened = false;

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
            clearSearch();

        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            edtSeach = (EditText)action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String search = edtSeach.getText().toString();
                        if (search != null && search.length() > 0) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
                            doSearch(search);
                        }
                        return true;
                    }
                    return false;
                }
            });

            edtSeach.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSeach, InputMethodManager.SHOW_IMPLICIT);

            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_cancel_white));

            isSearchOpened = true;
        }
    }

    private void clearSearch() {
        resultCount.setVisibility(View.GONE);
        search = null;
        fetchInitialData();
    }

    private void doSearch(String searchTerm) {
        resultCount.setText("Đang tìm kiếm...");
        resultCount.setVisibility(View.VISIBLE);
        search = searchTerm;
        fetchInitialData();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all_books) {
            if (type != 1) {
                type = 1;
                setTitle("Book Lovers");
                closeMenuSearch();
                clearSearch();
                mSearchAction.setVisible(true);
            }
        } else if(id == R.id.nav_posted_books) {
            if (type != 2) {
                type = 2;
                if (CredentialsPrefs.isLoggedIn()) {
                    userId = CredentialsPrefs.getCurrentUser().get_id();
                }
                setTitle("Sách đã đăng");
                closeMenuSearch();
                clearSearch();
                mSearchAction.setVisible(true);
            }
        } else if(id == R.id.nav_favorite_books) {
            if (type != 3) {
                type = 3;
                if (CredentialsPrefs.isLoggedIn()) {
                    userId = CredentialsPrefs.getCurrentUser().get_id();
                }
                setTitle("Sách yêu thích");
                closeMenuSearch();
                clearSearch();
                mSearchAction.setVisible(false);
            }
        } else if (id == R.id.nav_new) {
            if (CredentialsPrefs.isLoggedIn()) {
                Intent intent = new Intent(this, AddBookActivity.class);
                startActivityForResult(intent, 4);
            }
            else
                Toast.makeText(getApplicationContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            Toast.makeText(getApplicationContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            CredentialsPrefs.clearCredentials();
            Pushbots.sharedInstance().setAlias(null);
            updateAuthenticationInfoOnNavbar();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
