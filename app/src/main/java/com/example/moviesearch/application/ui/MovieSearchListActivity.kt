package com.example.moviesearch.application.ui

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.moviesearch.application.R
import com.example.moviesearch.application.data.DataBaseHelper
import com.example.moviesearch.application.model.Movie
import com.example.moviesearch.application.model.MovieSearchResult
import com.example.moviesearch.application.network.VolleyNetwork
import com.example.moviesearch.application.network.VolleyNetwork.OnMovieSearchResponseCallBack
import com.example.moviesearch.application.util.Utils
import com.google.android.material.snackbar.Snackbar

class MovieSearchListActivity : AppCompatActivity(),
    OnMovieSearchResponseCallBack {
    private var mSearchEditText: EditText? = null
    private var mMovieListRecyclerView: RecyclerView? = null
    private var mMovieAdapter: MovieRecyclerViewAdapter? = null
    private var mProgressBar: ProgressBar? = null
    private var dbHelper: DataBaseHelper? = null
    private var dataBase: SQLiteDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_search_list)
        dbHelper = DataBaseHelper(this)
        dataBase = dbHelper?.writableDatabase
        mSearchEditText = findViewById<View>(R.id.search_edittext) as EditText
        mSearchEditText?.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                startSearch()
                handled = true
            }
            handled
        }
        val mSearchButton = findViewById<View>(R.id.search_button) as Button
        mMovieListRecyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
        mSearchButton.setOnClickListener { v: View? -> startSearch() }
        mMovieAdapter = MovieRecyclerViewAdapter(null)
        mMovieListRecyclerView?.adapter = mMovieAdapter
        val gridLayoutManager = StaggeredGridLayoutManager(
            resources.getInteger(R.integer.grid_column_count),
            StaggeredGridLayoutManager.VERTICAL
        )
        mMovieListRecyclerView?.itemAnimator = null
        mMovieListRecyclerView?.layoutManager = gridLayoutManager
        mProgressBar = findViewById<View>(R.id.progress_spinner) as ProgressBar
    }

    inner class MovieRecyclerViewAdapter(private var mValues: List<Movie>?) :
        RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.movie_card_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val movie = mValues!![position]
            val title = movie.getTitle()
            val imdbId = movie.getImdbID()
            val year = movie.getYear()
            holder.titleView.text = title
            holder.yearView.text = year
            if (isFavoriteMovie(movie.getImdbID())) {
                holder.favoriteView.visibility = View.VISIBLE
            } else {
                holder.favoriteView.visibility = View.GONE
            }
            val imageUrl: String = if (movie.getPoster() != "N/A") {
                movie.getPoster()
            } else {
                resources.getString(R.string.default_poster)
            }
            holder.thumbNailImageView.layout(0, 0, 0, 0)
            Glide.with(this@MovieSearchListActivity).load(imageUrl).into(holder.thumbNailImageView)
            holder.view.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@MovieSearchListActivity,
                    MovieDetailsActivity::class.java
                )
                intent.putExtra(
                    MovieDetailsActivity.MOVIE_TITLE,
                    title
                )
                intent.putExtra(
                    MovieDetailsActivity.IMAGE_URL,
                    imageUrl
                )
                intent.putExtra(
                    MovieDetailsActivity.IMDB,
                    imdbId
                )
                val options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MovieSearchListActivity,
                        holder.thumbNailImageView, "poster"
                    )
                startActivity(intent, options.toBundle())
            }
        }

        override fun getItemCount(): Int {
            return if (mValues == null) {
                0
            } else mValues!!.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(
            view
        ) {
            val titleView: TextView = view.findViewById<View>(R.id.movie_title) as TextView
            val yearView: TextView = view.findViewById<View>(R.id.movie_year) as TextView
            val thumbNailImageView: ImageView = view.findViewById<View>(R.id.thumbnail) as ImageView
            val favoriteView: ImageView = view.findViewById<View>(R.id.favorite_button) as ImageView

        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            Glide.with(holder.thumbNailImageView.context).clear(holder.thumbNailImageView)
        }

        fun swapData(items: List<Movie>?) {
            if (items != null) {
                mValues = items
                notifyDataSetChanged()
            } else {
                mValues = null
            }
        }
    }

    private fun startSearch() {
        if (Utils.isNetworkAvailable(applicationContext)) {
            Utils.hideSoftKeyboard(this@MovieSearchListActivity)
            val movieTitle = mSearchEditText?.text.toString().trim { it <= ' ' }
            if (movieTitle.isNotEmpty()) {
                val volleyNetwork = VolleyNetwork()
                volleyNetwork.makeMovieSearchRequest(applicationContext, movieTitle, this)
                mProgressBar?.visibility = View.VISIBLE
                mMovieListRecyclerView?.visibility = View.GONE
            } else Snackbar.make(
                mMovieListRecyclerView!!,
                resources.getString(R.string.snackbar_title_empty),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            Snackbar.make(
                mMovieListRecyclerView!!,
                resources.getString(R.string.network_not_available),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onSuccess(result: MovieSearchResult) {
        mProgressBar?.visibility = View.GONE
        mMovieListRecyclerView?.visibility = View.VISIBLE
        if (result.getResponse() == "True") {
            mMovieAdapter?.swapData(result.getSearch())
        } else {
            mMovieAdapter?.swapData(null)
            Snackbar.make(
                mMovieListRecyclerView!!,
                resources.getString(R.string.snackbar_title_not_found), Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_favorites, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorites) {
            mMovieListRecyclerView?.visibility = View.VISIBLE
            mMovieAdapter?.swapData(favoriteMovies)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isFavoriteMovie(imdbID: String): Boolean {
        val favoriteMovies =
            favoriteMovies
        if (favoriteMovies != null) {
            for (movie in favoriteMovies) {
                if (movie.getImdbID() == imdbID) {
                    return true
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        mMovieAdapter?.notifyDataSetChanged()
    }

    private val favoriteMovies: List<Movie>?
        get() = dbHelper?.allFavorite
}