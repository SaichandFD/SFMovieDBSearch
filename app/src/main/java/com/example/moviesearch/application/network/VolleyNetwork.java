package com.example.moviesearch.application.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.moviesearch.application.model.MovieDetail;
import com.example.moviesearch.application.model.MovieSearchResult;
import com.google.gson.Gson;

public class VolleyNetwork {

    private RequestQueue requestQueue;

    public void makeMovieSearchRequest(Context context, String name, OnMovieSearchResponseCallBack callBack) {
        String url = "https://www.omdbapi.com/?type=movie&s=" + name + "&apikey=9544fe4a";
        requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                (Response.Listener<String>) response -> {
                    Gson gson = new Gson();
                    MovieSearchResult movieSearchResult = gson.fromJson(response, MovieSearchResult.class);
                    callBack.onSuccess(movieSearchResult);
                }, (Response.ErrorListener) error -> {
            callBack.onSuccess(null);
        });
        requestQueue.add(stringRequest);
    }

    public void makeMovieDetailsRequest(Context context, String imdb, OnMovieDetailResponseCallBack callBack) {
        String url = "https://www.omdbapi.com/?plot=full&i=" + imdb + "&apikey=9544fe4a";
        requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                (Response.Listener<String>) response -> {
                    Gson gson = new Gson();
                    MovieDetail movieDetail = gson.fromJson(response, MovieDetail.class);
                    callBack.onSuccess(movieDetail);
                }, (Response.ErrorListener) error -> {
            callBack.onSuccess(null);
        });
        requestQueue.add(stringRequest);
    }

    public interface OnMovieSearchResponseCallBack {
        void onSuccess(MovieSearchResult result);
    }

    public interface OnMovieDetailResponseCallBack {
        void onSuccess(MovieDetail result);
    }
}
