package com.yoavfranco.wikigame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.adapters.SearchItemsAdapter;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.SearchItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yoav on 02/04/17.
 */

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.searchItemsRecycler)
    RecyclerView searchItemsRecycler;
    SearchItemsAdapter searchItemsAdapter;

    ArrayList<SearchItem> startItems;
    ArrayList<SearchItem> targetItems;
    ArrayList<String> startSubjects;
    ArrayList<String> targetSubjects;

    String type;

    WikiGameAPI wikiGameAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        ButterKnife.bind(this);

        wikiGameAPI = new WikiGameAPI();

        Intent intent = getIntent();
        this.type = intent.getStringExtra("type");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout searchContainer = (LinearLayout) findViewById(R.id.search_container);
        final EditText toolbarSearchView = (EditText) findViewById(R.id.search_view);
        final ImageView searchClearButton = (ImageView) findViewById(R.id.search_clear);

        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
                if(s.length() == 0)
                    searchClearButton.setVisibility(View.GONE);
                else
                    searchClearButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Clear search text when clear button is tapped
        searchClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarSearchView.setText("");
            }
        });

        searchClearButton.setVisibility(View.GONE);

        if (startItems == null || targetItems == null )
            refreshArticles();
    }

    private void refreshArticles() {
        wikiGameAPI.getArticlesAsync(new WikiGameInterface(this) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                parseServerResponse(response);
            }
        });
    }

    public void parseServerResponse(JSONObject response) {
        try {
            startItems = new ArrayList<>();
            targetItems = new ArrayList<>();
            startSubjects = new ArrayList<>();
            targetSubjects = new ArrayList<>();

            JSONArray startSubjectsJSON = response.getJSONArray("start_subjects");
            JSONArray targetSubjectsJSON = response.getJSONArray("target_subjects");

            for (int i = 0; i  < startSubjectsJSON.length(); i++) {
                JSONObject subject = startSubjectsJSON.getJSONObject(i);
                String subjectName = subject.getString("name");
                startSubjects.add(subjectName);
                JSONArray startArticles = subject.getJSONArray("articles");
                for (int j = 0; j < startArticles.length(); j++) {
                    startItems.add(new SearchItem(startArticles.getString(j), subjectName));
                }
            }
            for (int i = 0; i  < targetSubjectsJSON.length(); i++) {
                JSONObject subject = targetSubjectsJSON.getJSONObject(i);
                String subjectName = subject.getString("name");
                targetSubjects.add(subjectName);
                JSONArray targetArticles = subject.getJSONArray("articles");
                for (int j = 0; j < targetArticles.length(); j++) {
                    targetItems.add(new SearchItem(targetArticles.getString(j), subjectName));
                }
            }
        } catch (JSONException e) {
            ErrorDialogs.showBadResponseDialog(this, true);
            e.printStackTrace();
        }

        updateUI();
    }

    public void updateUI() {
        searchItemsAdapter = new SearchItemsAdapter(this, this.type.equals("start") ? startItems : targetItems, type);
        searchItemsRecycler.setAdapter(searchItemsAdapter);
    }

    private boolean contains(String startSubject) {
        for(int i = 0; i < startSubjects.size(); i++)
        {
            if(startSubjects.get(i).equals(startSubject))
                return true;
        }
        return  false;
    }

    void filter(String text){
        ArrayList<SearchItem> items = this.type.equals("start") ? startItems : targetItems;
        List<SearchItem> temp = new ArrayList();
        if(items != null) {
            for(SearchItem item: items){
                //or use .contains(text)
                if(item.getTitle().toLowerCase().indexOf(text.toLowerCase()) != -1 ){
                    temp.add(item);
                }
            }

            searchItemsAdapter.updateList(temp);
        }
    }
}
