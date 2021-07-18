package com.example.moviesearch.application.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.moviesearch.application.R
import com.example.moviesearch.application.data.DataBaseHelper
import com.example.moviesearch.application.data.FavoriteMovieEntry
import com.example.moviesearch.application.model.Movie
import com.example.moviesearch.application.model.MovieDetail
import com.example.moviesearch.application.network.VolleyNetwork
import com.example.moviesearch.application.network.VolleyNetwork.OnMovieDetailResponseCallBack
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar

class MovieDetailsActivity : AppCompatActivity(),
    OnMovieDetailResponseCallBack {
    private var mProgressBar: ProgressBar? = null
    private var favoriteBtn: ImageButton? = null
    private var dbHelper: DataBaseHelper? = null
    private var dataBase: SQLiteDatabase? = null
    private var isFavorite = false
    private var movieDetail: MovieDetail? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_details)
        val dbHelper = DataBaseHelper(this)
        dataBase = dbHelper.writableDatabase
        val imageUrl = intent.getStringExtra(IMAGE_URL)
        val movieTitle = intent.getStringExtra(MOVIE_TITLE)
        val imdbID = intent.getStringExtra(IMDB)
        Glide.with(this).load(imageUrl).into((findViewById<View>(R.id.main_backdrop) as ImageView))
        mProgressBar = findViewById<View>(R.id.progress_spinner) as ProgressBar
        favoriteBtn = findViewById<View>(R.id.favorite_button) as ImageButton
        val collapsingToolbarLayout =
            findViewById<View>(R.id.main_collapsing) as CollapsingToolbarLayout
        collapsingToolbarLayout.title = movieTitle
        if (isAlreadyFavorited(imdbID)) {
            isFavorite = true
            favoriteBtn?.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.favorite,
                    applicationContext.theme
                )
            )
        } else {
            isFavorite = false
            favoriteBtn?.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.normal,
                    applicationContext.theme
                )
            )
        }
        favoriteBtn?.setOnClickListener { v: View? ->
            if (isFavorite) {
                isFavorite = false
                favoriteBtn?.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.normal,
                        applicationContext.theme
                    )
                )
                deleteFavorite()
                Snackbar.make(
                    favoriteBtn!!, "Removed from Favorite",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                isFavorite = true
                favoriteBtn?.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.favorite,
                        applicationContext.theme
                    )
                )
                saveFavorite()
                Snackbar.make(
                    favoriteBtn!!, "Added to Favorite",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        val volleyNetwork = VolleyNetwork()
        volleyNetwork.makeMovieDetailsRequest(applicationContext, imdbID, this)
    }

    private fun deleteFavorite() {
        dbHelper = DataBaseHelper(applicationContext)
        dbHelper?.deleteFavorite(movieDetail?.imdbID)
    }

    private fun saveFavorite() {
        dbHelper = DataBaseHelper(applicationContext)
        val favorite = Movie()
        favorite.setYear(movieDetail?.year)
        favorite.setPoster(movieDetail?.poster)
        favorite.setImdbID(movieDetail?.imdbID)
        favorite.setTitle(movieDetail?.title)
        dbHelper?.addFavorite(favorite)
    }

    override fun onSuccess(result: MovieDetail) {
        movieDetail = result
        mProgressBar?.visibility = View.GONE
        (findViewById<View>(R.id.grid_title) as TextView).text = result.title
        (findViewById<View>(R.id.grid_writers) as TextView).text = result.writer
        (findViewById<View>(R.id.grid_actors) as TextView).text = result.actors
        (findViewById<View>(R.id.grid_director) as TextView).text = result.director
        (findViewById<View>(R.id.grid_genre) as TextView).text = result.genre
        (findViewById<View>(R.id.grid_released) as TextView).text = result.released
        (findViewById<View>(R.id.grid_plot) as TextView).text = result.plot
        (findViewById<View>(R.id.grid_runtime) as TextView).text = result.runtime
    }

    fun isAlreadyFavorited(searchItem: String?): Boolean {
        val projection = arrayOf(
            FavoriteMovieEntry._ID,
            FavoriteMovieEntry.COLUMN_YEAR,
            FavoriteMovieEntry.COLUMN_TITLE,
            FavoriteMovieEntry.COLUMN_IMDBID,
            FavoriteMovieEntry.COLUMN_POSTER_PATH
        )
        val selection = FavoriteMovieEntry.COLUMN_IMDBID + " =?"
        val selectionArgs = arrayOf(searchItem)
        val limit = "1"
        val cursor = dataBase!!.query(
            FavoriteMovieEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null,
            limit
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    companion object {
        const val MOVIE_TITLE = "movie_title"
        const val IMAGE_URL = "image_url"
        const val IMDB = "imdb"
    }
}