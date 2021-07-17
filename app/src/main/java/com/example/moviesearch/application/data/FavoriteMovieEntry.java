package com.example.moviesearch.application.data;

import android.provider.BaseColumns;

public class FavoriteMovieEntry implements BaseColumns {
    public static final String TABLE_NAME = "favorite";
    public static final String COLUMN_IMDBID = "imdbid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_PATH = "posterpath";
    public static final String COLUMN_YEAR = "movieyear";
}
