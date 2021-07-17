package com.example.moviesearch.application.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.moviesearch.application.R;
import com.example.moviesearch.application.data.DBHelper;
import com.example.moviesearch.application.model.Movie;
import com.example.moviesearch.application.model.MovieSearchResult;
import com.example.moviesearch.application.network.VolleyNetwork;
import com.example.moviesearch.application.util.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MovieSearchListActivity extends AppCompatActivity
        implements VolleyNetwork.OnNetworkResponseCallBack {

    private EditText mSearchEditText;
    private RecyclerView mMovieListRecyclerView;
    private MovieRecyclerViewAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private DBHelper dbHelper;
    private SQLiteDatabase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_search_list);

        dbHelper = new DBHelper(this);
        dataBase = dbHelper.getWritableDatabase();

        mSearchEditText = (EditText) findViewById(R.id.search_edittext);
        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                startSearch();
                handled = true;
            }
            return handled;
        });
        Button mSearchButton = (Button) findViewById(R.id.search_button);
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSearchButton.setOnClickListener(v -> startSearch());
        mMovieAdapter = new MovieRecyclerViewAdapter(null);
        mMovieListRecyclerView.setAdapter(mMovieAdapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(getResources().getInteger(R.integer.grid_column_count), StaggeredGridLayoutManager.VERTICAL);
        mMovieListRecyclerView.setItemAnimator(null);
        mMovieListRecyclerView.setLayoutManager(gridLayoutManager);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_spinner);
    }
    public class MovieRecyclerViewAdapter
            extends RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder> {

        private List<Movie> mValues;

        public MovieRecyclerViewAdapter(List<Movie> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_card_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final Movie movie = mValues.get(position);
            final String title = movie.getTitle();
            final String imdbId = movie.getImdbID();
            final String year = movie.getYear();
            holder.mTitleView.setText(title);
            holder.mYearView.setText(year);

            final String imageUrl;
            if (!movie.getPoster().equals("N/A")) {
                imageUrl = movie.getPoster();
            } else {
                imageUrl = getResources().getString(R.string.default_poster);
            }
            holder.mThumbImageView.layout(0, 0, 0, 0);
            Glide.with(MovieSearchListActivity.this).load(imageUrl).into(holder.mThumbImageView);

            holder.mView.setOnClickListener(v -> {
                Intent intent = new Intent(MovieSearchListActivity.this, MovieDetailsActivity.class);
                intent.putExtra(MovieDetailsActivity.MOVIE_TITLE, title);
                intent.putExtra(MovieDetailsActivity.IMAGE_URL, imageUrl);
                intent.putExtra(MovieDetailsActivity.IMDB, imdbId);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(MovieSearchListActivity.this,
                                holder.mThumbImageView, "poster");
                startActivity(intent, options.toBundle());
            });
        }

        @Override
        public int getItemCount() {
            if (mValues == null) {
                return 0;
            }
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mYearView;
            public final ImageView mThumbImageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.movie_title);
                mYearView = (TextView) view.findViewById(R.id.movie_year);
                mThumbImageView = (ImageView) view.findViewById(R.id.thumbnail);
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            Glide.with(holder.mThumbImageView.getContext()).clear(holder.mThumbImageView);
        }

        public void swapData(List<Movie> items) {
            if (items != null) {
                mValues = items;
                notifyDataSetChanged();

            } else {
                mValues = null;
            }
        }
    }

    private void startSearch() {
        if (Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.hideSoftKeyboard(MovieSearchListActivity.this);
            String movieTitle = mSearchEditText.getText().toString().trim();
            if (!movieTitle.isEmpty()) {
                VolleyNetwork volleyNetwork = new VolleyNetwork();
                volleyNetwork.makeMovieSearchRequest(getApplicationContext(), movieTitle, this);
                mProgressBar.setVisibility(View.VISIBLE);
                mMovieListRecyclerView.setVisibility(View.GONE);
            } else
                Snackbar.make(mMovieListRecyclerView,
                        getResources().getString(R.string.snackbar_title_empty),
                        Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mMovieListRecyclerView,
                    getResources().getString(R.string.network_not_available),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSuccess(MovieSearchResult result) {
        mProgressBar.setVisibility(View.GONE);
        mMovieListRecyclerView.setVisibility(View.VISIBLE);
        if (result.getResponse().equals("True")) {
            mMovieAdapter.swapData(result.getSearch());
        } else {
            mMovieAdapter.swapData(null);
            Snackbar.make(mMovieListRecyclerView,
                    getResources().getString(R.string.snackbar_title_not_found), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.favorites) {
            mMovieListRecyclerView.setVisibility(View.VISIBLE);
            mMovieAdapter.swapData(getFavoriteMovies());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Movie> getFavoriteMovies(){
        return dbHelper.getAllFavorite();
    }
}
