package com.example.books;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity {
    private RecyclerView rvBooks;
    private ProgressBar mLoadingProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        mLoadingProgress = findViewById(R.id.pb_loading);
        rvBooks = findViewById(R.id.rv_books);
        LinearLayoutManager booksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvBooks.setLayoutManager(booksLayoutManager);


        try {
            URL bookUrl = ApiUtil.buildUrl("programming");
            new BookQueryTask().execute(bookUrl);
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    public class BookQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String result = null;

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
            }
            ArrayList<Book> books = ApiUtil.getBooksFromJson(result);
             String resultString = "";

            BooksAdapter booksAdapter = new BooksAdapter(books);
            rvBooks.setAdapter(booksAdapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgress.setVisibility(View.VISIBLE);
        }
    }
}
