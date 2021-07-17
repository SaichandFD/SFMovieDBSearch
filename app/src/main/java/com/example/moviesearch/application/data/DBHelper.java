package com.example.moviesearch.application.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.moviesearch.application.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moviefavorite.db";

    private static final int DATABASE_VERSION = 1;

    public static final String LOGTAG = "FAVORITE";

    SQLiteOpenHelper dbhandler;
    SQLiteDatabase db;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open(){
        Log.i(LOGTAG, "Database Opened");
        db = dbhandler.getWritableDatabase();
    }

    public void close(){
        Log.i(LOGTAG, "Database Closed");
        dbhandler.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteMovieEntry.COLUMN_IMDBID + " INTEGER, " +
                FavoriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_YEAR + " TEXT NOT NULL " +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

    public void addFavorite(Movie movie){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FavoriteMovieEntry.COLUMN_IMDBID, movie.getImdbID());
        values.put(FavoriteMovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(FavoriteMovieEntry.COLUMN_POSTER_PATH, movie.getPoster());
        values.put(FavoriteMovieEntry.COLUMN_YEAR, movie.getYear());

        db.insert(FavoriteMovieEntry.TABLE_NAME, null, values);
        db.close();
    }

    public void deleteFavorite(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FavoriteMovieEntry.TABLE_NAME, FavoriteMovieEntry.COLUMN_IMDBID + "=?", new String[]{id});
    }

    public List<Movie> getAllFavorite(){
        String[] columns = {
                FavoriteMovieEntry._ID,
                FavoriteMovieEntry.COLUMN_TITLE,
                FavoriteMovieEntry.COLUMN_IMDBID,
                FavoriteMovieEntry.COLUMN_POSTER_PATH,
                FavoriteMovieEntry.COLUMN_YEAR

        };
        String sortOrder =
                FavoriteMovieEntry._ID + " ASC";
        List<Movie> favoriteList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(FavoriteMovieEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                sortOrder);

        if (cursor.moveToFirst()){
            do {
                Movie movie = new Movie();
                movie.setImdbID(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_IMDBID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_TITLE)));
                movie.setPoster(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_POSTER_PATH)));
                movie.setYear(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_YEAR)));
                favoriteList.add(movie);

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return favoriteList;
    }

}
