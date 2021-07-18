package com.example.moviesearch.application.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.moviesearch.application.model.Movie
import java.util.*

class DataBaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    var dbhandler: SQLiteOpenHelper? = null
    var db: SQLiteDatabase? = null
    fun open() {
        Log.i(LOGTAG, "Database Opened")
        db = dbhandler!!.writableDatabase
    }

    override fun close() {
        Log.i(LOGTAG, "Database Closed")
        dbhandler!!.close()
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteMovieEntry.COLUMN_IMDBID + " INTEGER, " +
                FavoriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_YEAR + " TEXT NOT NULL " +
                "); "
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    fun addFavorite(movie: Movie) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(FavoriteMovieEntry.COLUMN_IMDBID, movie.getImdbID())
        values.put(FavoriteMovieEntry.COLUMN_TITLE, movie.getTitle())
        values.put(FavoriteMovieEntry.COLUMN_POSTER_PATH, movie.getPoster())
        values.put(FavoriteMovieEntry.COLUMN_YEAR, movie.getYear())
        db.insert(FavoriteMovieEntry.TABLE_NAME, null, values)
        db.close()
    }

    fun deleteFavorite(id: String?) {
        val db = this.writableDatabase
        db.delete(
            FavoriteMovieEntry.TABLE_NAME,
            FavoriteMovieEntry.COLUMN_IMDBID + "=?",
            arrayOf(id)
        )
    }

    val allFavorite: List<Movie>
        get() {
            val columns = arrayOf(
                FavoriteMovieEntry._ID,
                FavoriteMovieEntry.COLUMN_TITLE,
                FavoriteMovieEntry.COLUMN_IMDBID,
                FavoriteMovieEntry.COLUMN_POSTER_PATH,
                FavoriteMovieEntry.COLUMN_YEAR
            )
            val sortOrder: String = FavoriteMovieEntry._ID + " ASC"
            val favoriteList: MutableList<Movie> = ArrayList()
            val db = this.readableDatabase
            val cursor = db.query(
                FavoriteMovieEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                sortOrder
            )
            if (cursor.moveToFirst()) {
                do {
                    val movie = Movie()
                    movie.setImdbID(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_IMDBID)))
                    movie.setTitle(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_TITLE)))
                    movie.setPoster(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_POSTER_PATH)))
                    movie.setYear(cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_YEAR)))
                    favoriteList.add(movie)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return favoriteList
        }

    companion object {
        private const val DATABASE_NAME = "moviefavorite.db"
        private const val DATABASE_VERSION = 1
        const val LOGTAG = "FAVORITE"
    }
}