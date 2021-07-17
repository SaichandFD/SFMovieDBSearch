package com.example.moviesearch.application.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.moviesearch.application.R;
import com.example.moviesearch.application.data.DBHelper;
import com.example.moviesearch.application.data.FavoriteMovieEntry;
import com.example.moviesearch.application.model.Movie;
import com.example.moviesearch.application.model.MovieDetail;
import com.example.moviesearch.application.network.VolleyNetwork;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

public class MovieDetailsActivity extends AppCompatActivity implements VolleyNetwork.OnNetworkDetailResponseCallBack {

    private ProgressBar mProgressBar;
    public static final String MOVIE_TITLE = "movie_title";
    public static final String IMAGE_URL = "image_url";
    public static final String IMDB = "imdb";
    private ImageButton favoriteBtn;
    private DBHelper dbHelper;
    private Movie favorite;
    private SQLiteDatabase dataBase;
    private boolean isFavorite = false;
    private MovieDetail movieDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);

        DBHelper dbHelper = new DBHelper(this);
        dataBase = dbHelper.getWritableDatabase();

        String imageUrl = getIntent().getStringExtra(IMAGE_URL);
        String movieTitle = getIntent().getStringExtra(MOVIE_TITLE);
        String imdbID = getIntent().getStringExtra(IMDB);
        Glide.with(this).load(imageUrl).into((ImageView) findViewById(R.id.main_backdrop));
        mProgressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        favoriteBtn = (ImageButton) findViewById(R.id.favorite_button);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        collapsingToolbarLayout.setTitle(movieTitle);

        if (isAlreadyFavorited(imdbID)) {
            isFavorite = true;
            favoriteBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.favorite, getApplicationContext().getTheme()));
        }else{
            isFavorite = false;
            favoriteBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.normal, getApplicationContext().getTheme()));
        }

        favoriteBtn.setOnClickListener(v -> {
            if(isFavorite) {
                isFavorite = false;
                favoriteBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.normal, getApplicationContext().getTheme()));
                deleteFavorite();
                Snackbar.make(favoriteBtn, "Removed from Favorite",
                        Snackbar.LENGTH_SHORT).show();
            }else {
                isFavorite = true;
                favoriteBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.favorite, getApplicationContext().getTheme()));
                saveFavorite();
                Snackbar.make(favoriteBtn, "Added to Favorite",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

    VolleyNetwork volleyNetwork = new VolleyNetwork();
        volleyNetwork.makeMovieDetailsRequest(getApplicationContext(),imdbID, this);
}

    private void deleteFavorite() {
        dbHelper = new DBHelper(getApplicationContext());
        dbHelper.deleteFavorite(movieDetail.imdbID);
    }
    private void saveFavorite(){
        dbHelper = new DBHelper(getApplicationContext());
        favorite = new Movie();
        favorite.setYear(movieDetail.year);
        favorite.setPoster(movieDetail.poster);
        favorite.setImdbID(movieDetail.imdbID);
        favorite.setTitle(movieDetail.title);
        dbHelper.addFavorite(favorite);
    }

    @Override
    public void onSuccess(MovieDetail result) {
        movieDetail = result;
        mProgressBar.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.grid_title)).setText(result.title);
        ((TextView) findViewById(R.id.grid_writers)).setText(result.writer);
        ((TextView) findViewById(R.id.grid_actors)).setText(result.actors);
        ((TextView) findViewById(R.id.grid_director)).setText(result.director);
        ((TextView) findViewById(R.id.grid_genre)).setText(result.genre);
        ((TextView) findViewById(R.id.grid_released)).setText(result.released);
        ((TextView) findViewById(R.id.grid_plot)).setText(result.plot);
        ((TextView) findViewById(R.id.grid_runtime)).setText(result.runtime);
    }

    public boolean isAlreadyFavorited(String searchItem) {

        String[] projection = {
                FavoriteMovieEntry._ID,
                FavoriteMovieEntry.COLUMN_YEAR,
                FavoriteMovieEntry.COLUMN_TITLE,
                FavoriteMovieEntry.COLUMN_IMDBID,
                FavoriteMovieEntry.COLUMN_POSTER_PATH

        };
        String selection = FavoriteMovieEntry.COLUMN_IMDBID + " =?";
        String[] selectionArgs = {searchItem};
        String limit = "1";

        Cursor cursor = dataBase.query(FavoriteMovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}
