package at.allaboutapps.a3hiring;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import at.allaboutapps.a3hiring.api.models.Club;

public class DetailActivity extends AppCompatActivity {

    // club to show
    Club club;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        club = getIntent().getParcelableExtra(MainActivity.PASS_CLUB_TO_DETAIL_KEY);

        // find toolbar, set the title
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(club.component1());
        setSupportActionBar(toolbar);
        // set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load logo with Glide library
        // check with listener, if loading has failed - when failed, set placeholder
        final ImageView logoView = findViewById(R.id.activity_detail_logo);
        Glide.with(getBaseContext())
                .load(club.component4())
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        logoView.setImageResource(R.drawable.club_placeholder);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(logoView);

        // set the text for the country of the club
        TextView nationView = findViewById(R.id.activity_detail_nation);
        nationView.setText(club.component1());

        // set the text for the club and format with Html.fromHtml to display the name of the club bold
        TextView sentenceView = findViewById(R.id.activity_detail_sentence);
        String sentence = String.format(getResources().getString(R.string.activity_detail_sentence), club.component1(), club.component2(), club.component3());
        Spanned span = Html.fromHtml(sentence);
        sentenceView.setText(span);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            // use onBackPressed to prevent reloading
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
