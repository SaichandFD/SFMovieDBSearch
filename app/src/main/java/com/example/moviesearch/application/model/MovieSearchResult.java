package com.example.moviesearch.application.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieSearchResult {
    @SerializedName("Search")
    public List<Movie> search;
    public String totalResults;
    @SerializedName("Response")
    public String response;

    public List<Movie> getSearch() {
        return search;
    }

    public void setSearch(List<Movie> Search) {
        this.search = Search;
    }

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String totalResults) {
        this.totalResults = totalResults;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}

