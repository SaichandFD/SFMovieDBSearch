package com.example.moviesearch.application.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieDetail{
    @SerializedName("Title")
    public String title;
    @SerializedName("Year")
    public String year;
    @SerializedName("Rated")
    public String rated;
    @SerializedName("Released")
    public String released;
    @SerializedName("Runtime")
    public String runtime;
    @SerializedName("Genre")
    public String genre;
    @SerializedName("Director")
    public String director;
    @SerializedName("Writer")
    public String writer;
    @SerializedName("Actors")
    public String actors;
    @SerializedName("Plot")
    public String plot;
    @SerializedName("Language")
    public String language;
    @SerializedName("Country")
    public String country;
    @SerializedName("Awards")
    public String awards;
    @SerializedName("Poster")
    public String poster;
    @SerializedName("Ratings")
    public List<Rating> ratings;
    @SerializedName("Metascore")
    public String metascore;
    public String imdbRating;
    public String imdbVotes;
    public String imdbID;
    @SerializedName("Type")
    public String type;
    @SerializedName("DVD")
    public String dVD;
    @SerializedName("BoxOffice")
    public String boxOffice;
    @SerializedName("Production")
    public String production;
    @SerializedName("Website")
    public String website;
    @SerializedName("Response")
    public String response;
}
