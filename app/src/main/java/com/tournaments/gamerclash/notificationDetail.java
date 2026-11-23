package com.tournaments.gamerclash;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class notificationDetail extends AppCompatActivity {
    TextView title, date, detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_detail);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Find views by their IDs
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        detail = findViewById(R.id.detail);

        // Retrieve data from intent
        String titleText = getIntent().getStringExtra("title");
        String dateText = getIntent().getStringExtra("time");
        String detailText = getIntent().getStringExtra("detail");

        // Set data to TextViews
        title.setText(titleText);
        date.setText(dateText);

        // Set detail text as clickable HTML if contains <a> tag
        if (detailText != null && detailText.contains("<a")) {
            detail.setText(Html.fromHtml(detailText, Html.FROM_HTML_MODE_LEGACY));
            detail.setMovementMethod(LinkMovementMethod.getInstance()); // make it clickable
        } else {
            detail.setText(detailText); // normal text if no HTML
        }
    }
}
