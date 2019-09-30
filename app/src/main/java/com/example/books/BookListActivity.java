package com.example.books;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private RecyclerView rvBooks;
    private ProgressBar mLoadingProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        mLoadingProgress = findViewById(R.id.pb_loading);
        rvBooks = findViewById(R.id.rv_books);
        Intent intent = getIntent();
        String query = intent.getStringExtra("Query");
        URL bookUrl;

        LinearLayoutManager booksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvBooks.setLayoutManager(booksLayoutManager);


        try {
            if (query==null || query.isEmpty()){
                bookUrl = ApiUtil.buildUrl("programming");
            }
             else {
                 bookUrl = new URL(query);
            }

            new BookQueryTask().execute(bookUrl);
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_list_menu, menu);
        final MenuItem firstItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(firstItem);
        searchView.setOnQueryTextListener(this);

        ArrayList<String> recentList = SpUtil.getQueryList(getApplicationContext());
        int itemNumber = recentList.size();
        MenuItem recentMenu;
        for (int i = 0; i < itemNumber; i++) {
            recentMenu = menu.add(Menu.NONE, i, Menu.NONE, recentList.get(i));
        }
        
        
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()){
                case R.id.action_advanced_search:
                    Intent intent = new Intent(this, SearchActivity.class);
                    startActivity(intent);

                default:
                    int position = item.getItemId() + 1;
                    String preferenceName = SpUtil.QUERY + String.valueOf(position);
                    String query = SpUtil.getPrefernceString(getApplicationContext(), preferenceName);
                    String[] prefParams = query.split("\\,");
                    String[] queryParams = new String[4];

                    for (int i = 0; i < prefParams.length; i++) {
                        queryParams[i] = prefParams[i];
                    }
                    URL bookUrl = ApiUtil.buildUrl(
                            (queryParams[0]==null)? "": queryParams[0],
                            (queryParams[1]==null)? "": queryParams[1],
                            (queryParams[2]==null)? "": queryParams[2],
                            (queryParams[3]==null)? "": queryParams[3]
                    );



                    return super.onOptionsItemSelected(item);
            }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        try {
            URL bookUrl = ApiUtil.buildUrl(query);
            new BookQueryTask().execute(bookUrl);
        }
         catch (Exception e){
            Log.d("error", e.getMessage());
         }

        return false;

    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class BookQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String result= null;

            try {
                result = ApiUtil.getJson(searchURL);
            }
            catch (IOException e){
                Log.e("Error", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView tvError = findViewById(R.id.tv_error);
            mLoadingProgress.setVisibility(View.INVISIBLE);

            if (result == null) {
                rvBooks.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.VISIBLE);
            }
             else {
                rvBooks.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.INVISIBLE);
                ArrayList<Book> books = ApiUtil.getBooksFromJson(result);

                BooksAdapter booksAdapter = new BooksAdapter(books);
                rvBooks.setAdapter(booksAdapter);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgress.setVisibility(View.VISIBLE);
        }
    }
}
