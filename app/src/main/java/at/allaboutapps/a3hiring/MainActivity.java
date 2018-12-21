package at.allaboutapps.a3hiring;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import at.allaboutapps.a3hiring.api.models.Club;

public class MainActivity extends AppCompatActivity {

  public static String PASS_CLUB_TO_DETAIL_KEY = "DETAIL_KEY";

  private static int SORT_MODE_DEFAULT = 0;
  private static int SORT_MODE_VALUE = 1;
  private static int selected_sort_mode = SORT_MODE_DEFAULT;

  private ProgressBar progress_bar;
  private RecyclerView football_club_recycler;
  private FootballClubAdapter football_club_recycler_adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

    // Define Recycler View and set LayoutManager
    football_club_recycler = findViewById(R.id.football_club_recycler);
    football_club_recycler.setLayoutManager(new LinearLayoutManager(this));

    // Define Progress Bar - visible while loading
    progress_bar = findViewById(R.id.progress_bar);
    // start AsyncTask to load data from url and into the RecyclerView
    new LoadJsonAsyncTask().execute(getString(R.string.football_url));

  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //TODO: handle sort item click

    if (id == R.id.menuSort) {

      // check if football_club_recycler is not null (when JSON is not loaded, Adapter is not initialized)
      if (football_club_recycler_adapter != null) {
        // change sort mode, resort list, and reload data
        selected_sort_mode = selected_sort_mode == SORT_MODE_DEFAULT ? SORT_MODE_VALUE : SORT_MODE_DEFAULT;
        football_club_recycler_adapter.sort_football_clubs();
        football_club_recycler_adapter.notifyDataSetChanged();
      }

    }

    return super.onOptionsItemSelected(item);
  }

  public class FootballClubAdapter extends RecyclerView.Adapter<FootballClubAdapter.FootballClubViewHolder> {

    ArrayList<Club> football_clubs;

    public FootballClubAdapter(ArrayList<Club> football_clubs) {
      this.football_clubs = football_clubs;
      sort_football_clubs();
    }

    @Override
    public FootballClubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = getLayoutInflater().inflate(R.layout.list_item_football_club, parent, false);
      return new FootballClubViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FootballClubViewHolder holder, int position) {
      // get football club for this list item
      final Club club = football_clubs.get(position);

      // load logo with Glide library
      // set listener - to check if loading has failed - when failed, set placeholder image
      Glide.with(getBaseContext())
              .load(club.component4())
              .listener(new RequestListener<Drawable>() {

                  @Override
                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.logo.setImageResource(R.drawable.club_placeholder);
                    return true;
                  }

                  @Override
                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                  }
              })
              .into(holder.logo);
      // set other text components
      holder.title.setText(club.component1());
      holder.nation.setText(club.component2());
      holder.value.setText(String.format(getString(R.string.list_item_value_placeholder), club.component3()));

      // set onClickListener to open DetailActivity, pass football club to Activity
      holder.logo.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent intent = new Intent(getBaseContext(), DetailActivity.class);
          intent.putExtra(PASS_CLUB_TO_DETAIL_KEY, club);
          startActivity(intent);
        }
      });

      // set visibility of divider, divider not visible at last element
      if (position == getItemCount() - 1) {
        holder.divider.setVisibility(View.GONE);
      } else {
        holder.divider.setVisibility(View.VISIBLE);
      }

    }

    @Override
    public int getItemCount() {
      return football_clubs.size();
    }

    // method for sorting football clubs
    // depending on sort mode
    // sort mode default - sorting by name
    // sort mode value - sorting by value of club
    public void sort_football_clubs() {
      if (football_clubs != null) {
        if (selected_sort_mode == SORT_MODE_DEFAULT) {
          Collections.sort(football_clubs, new Comparator<Club>() {
            @Override
            public int compare(Club club, Club t1) {
              return club.component1().compareTo(t1.component1());
            }
          });
        } else if (selected_sort_mode == SORT_MODE_VALUE){
          Collections.sort(football_clubs, new Comparator<Club>() {
            @Override
            public int compare(Club club, Club t1) {
              if (club.component3() > t1.component3()) {
                return -1;
              } else if (club.component3() == t1.component3()) {
                return 0;
              } else {
                return 1;
              }
            }
          });
        }
      }

    }

    public class FootballClubViewHolder extends RecyclerView.ViewHolder {

      ImageView logo;
      TextView title;
      TextView nation;
      TextView value;
      View divider;

      public FootballClubViewHolder(View itemView) {
        super(itemView);

        logo = itemView.findViewById(R.id.list_item_football_club_logo);
        title = itemView.findViewById(R.id.list_item_football_club_title);
        nation = itemView.findViewById(R.id.list_item_football_club_nation);
        value = itemView.findViewById(R.id.list_item_football_club_value);
        divider = itemView.findViewById(R.id.list_item_football_club_divider);

      }

    }

  }

  public class LoadJsonAsyncTask extends AsyncTask<String, Void, JsonArray> {

    // load json from url
    // with Gson library
    // return JsonArray to populate in onPostExecute
    @Override
    protected JsonArray doInBackground(String... strings) {
      try {
        URL url = new URL(strings[0]);
        URLConnection request = url.openConnection();
        request.connect();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonArray array = element.getAsJsonArray();
        return array;

      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    // populates RecyclerView with data
    @Override
    protected void onPostExecute(JsonArray football_clubs) {

      // when loading has failed, show message to user
      if (football_clubs == null) {
        Toast.makeText(getBaseContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
      } else {
        Iterator<JsonElement> iterator = football_clubs.iterator();
        ArrayList<Club> football_club_list = new ArrayList<>();
        while (iterator.hasNext()) {
          JsonElement football_club_element = iterator.next();
          Club football_club = new Gson().fromJson(football_club_element, Club.class);
          football_club_list.add(football_club);
        }
        football_club_recycler_adapter = new FootballClubAdapter(football_club_list);
        football_club_recycler.setAdapter(football_club_recycler_adapter);
        // remove progress bar - loading finished
        progress_bar.setVisibility(View.GONE);
      }

    }

  }

}
