package com.example.tvapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.tvapp.R;
import com.example.tvapp.adapters.TVShowsAdapter;
import com.example.tvapp.databinding.ActivitySearch2Binding;
import com.example.tvapp.listeners.TVShowsListener;
import com.example.tvapp.models.TVShow;
import com.example.tvapp.viewmodels.SearchViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends AppCompatActivity implements TVShowsListener {

    private ActivitySearch2Binding activitySearch2Binding;
    private SearchViewModel viewModel;
    private List<TVShow> tvShows = new ArrayList<>();
    private TVShowsAdapter tvShowsAdapter;
    private int currentPage = 1;
    private int totalAvailablePages = 1;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySearch2Binding = DataBindingUtil.setContentView(this, R.layout.activity_search2);
        doInitialization();
    }

    private void doInitialization() {
        activitySearch2Binding.imageBack.setOnClickListener(view -> onBackPressed());
        activitySearch2Binding.tvShowsRecyclerView.setHasFixedSize(true);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        tvShowsAdapter = new TVShowsAdapter(tvShows, this);
        activitySearch2Binding.tvShowsRecyclerView.setAdapter(tvShowsAdapter);
        activitySearch2Binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (timer != null){
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().trim().isEmpty()){
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                currentPage = 1;
                                totalAvailablePages = 1;
                                searchTVShow(editable.toString());
                            });
                        }
                    }, 800);
                } else {
                    tvShows.clear();;
                    tvShowsAdapter.notifyDataSetChanged();
                }
            }
        });
        activitySearch2Binding.tvShowsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!activitySearch2Binding.tvShowsRecyclerView.canScrollVertically(1)){
                    if(!activitySearch2Binding.inputSearch.getText().toString().isEmpty()){
                        if(currentPage < totalAvailablePages){
                            currentPage++;
                            searchTVShow(activitySearch2Binding.inputSearch.getText().toString());
                        }
                    }
                }
            }
        });
        activitySearch2Binding.inputSearch.requestFocus();
    }

    private void searchTVShow(String query) {
        toggleLoading();
        viewModel.searchTVShow(query,currentPage).observe(this, tvShowsResponse -> {
            toggleLoading();
            if (tvShowsResponse != null) {
                totalAvailablePages = tvShowsResponse.getTotalPages();
                if(tvShowsResponse.getTvShows() != null){
                    int oldCount = tvShows.size();
                    tvShows.addAll(tvShowsResponse.getTvShows());
                    tvShowsAdapter.notifyItemRangeInserted(oldCount, tvShows.size());
                }
            }
        });
    }

    private void toggleLoading() {

        if (currentPage == 1) {
            if (activitySearch2Binding.getIsLoading() != null && activitySearch2Binding.getIsLoading()) {
                activitySearch2Binding.setIsLoading(false);
            } else {
                activitySearch2Binding.setIsLoading(true);
            }
        } else {
            if (activitySearch2Binding.getIsLoadingMore() != null && activitySearch2Binding.getIsLoadingMore()) {
                activitySearch2Binding.setIsLoadingMore(false);
            } else {
                activitySearch2Binding.setIsLoadingMore(true);
            }
        }
    }

    @Override
    public void onTVShowClicked(TVShow tvShow) {
        Intent intent = new Intent (getApplicationContext(), TVShowDetailsActivity.class);
        intent.putExtra("tvShow", tvShow);
        startActivity(intent);
    }
}