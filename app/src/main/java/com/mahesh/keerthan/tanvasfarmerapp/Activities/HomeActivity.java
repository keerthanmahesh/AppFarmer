package com.mahesh.keerthan.tanvasfarmerapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultBus;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultEvent;
import com.mahesh.keerthan.tanvasfarmerapp.APICall;
import com.mahesh.keerthan.tanvasfarmerapp.Adapters.drawerAdapter;
import com.mahesh.keerthan.tanvasfarmerapp.DataClasses.District;
import com.mahesh.keerthan.tanvasfarmerapp.DataClasses.UserClass;
import com.mahesh.keerthan.tanvasfarmerapp.DataClasses.Villages;
import com.mahesh.keerthan.tanvasfarmerapp.DrawerItem;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.AddFarmerFragment;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.AddMultipleFarmersFragment;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.EditFarmerFragment;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.QuestionFragment;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.ReportsFragment;
import com.mahesh.keerthan.tanvasfarmerapp.FragmentClasses.UpdateQuestionsFragment;
import com.mahesh.keerthan.tanvasfarmerapp.R;
import com.mahesh.keerthan.tanvasfarmerapp.RequestBuilder;
import com.mahesh.keerthan.tanvasfarmerapp.SimpleItem;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.OkHttpClient;

public class HomeActivity extends AppCompatActivity
        implements drawerAdapter.OnItemSelectedListener {

    private  NavigationView navigationView;
    private UserClass user;
    private Villages villageSelected;
    private District districtSelected;
    public static HomeActivity instance;
    private FragmentManager manager;
    private Toolbar toolbar;


    public static final int POS_QUESTIONNAIRE = 0;
    public static final int POS_ADDNNEWFARMER = 1;
    public static final int POS_EDITFARMERDETAILS = 2;
    public static final int POS_ADDMULTIPLEFARMERS = 3;
    public static final int POS_UPDATEQUESTIONS = 4;
    public static final int POS_REPORTS = 5;
    public drawerAdapter adapter;


    private String[] screenTitles;
    private Drawable[] screenIcons;
    public   SlidingRootNav slidingRootNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this,R.style.AmericanTypewriterSemibold);
        toolbar.setTitle("TANUVAS");

        setSupportActionBar(toolbar);
        manager = getSupportFragmentManager();
        Intent incomingIntent = getIntent();
        user = (UserClass) incomingIntent.getExtras().getSerializable("user");
        villageSelected = (Villages) incomingIntent.getExtras().getSerializable("village");

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Back));
        Drawable dr = getResources().getDrawable(R.drawable.hamburger_icon);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        final Drawable d = new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(bitmap,80,80,true));
        new getUserDistrict().execute(villageSelected.getDistrict_id());



       slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
               //.withDragDistance(220) //Horizontal translation of a view. Default == 180dp
               //.withRootViewScale(0.9f) //Content view's scale will be interpolated between 1f and 0.7f. Default == 0.65f;
               //.withRootViewElevation(0) //Content view's elevation will be interpolated between 0 and 10dp. Default == 8.
               //.withRootViewYTranslation(4)
                .inject();

        TextView name = slidingRootNav.getLayout().findViewById(R.id.fullname);
        TextView username = slidingRootNav.getLayout().findViewById(R.id.username);
        name.setText(user.getFullname());
        username.setText(user.getUsername());
        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        adapter = new drawerAdapter(Arrays.asList(
                createItemFor(POS_QUESTIONNAIRE).setChecked(true),
                createItemFor(POS_ADDNNEWFARMER),
                createItemFor(POS_EDITFARMERDETAILS),
                createItemFor(POS_ADDMULTIPLEFARMERS),
                createItemFor(POS_UPDATEQUESTIONS),
                createItemFor(POS_REPORTS)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_QUESTIONNAIRE);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setNavigationIcon(d);
            }
        });
    }



    @Override
    public void onBackPressed() {
        slidingRootNav.closeMenu(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem item = menu.findItem(R.id.action_save);
        item.setVisible(false);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getInstance().postQueue(
                new ActivityResultEvent(requestCode, resultCode, data));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*private void getUserDistrict(final int district_id){
        AsyncTask<Integer,Void,JSONObject> asyncTask = new AsyncTask<Integer, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Integer... integers) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url("http://192.168.0.103/~vandit/justtesting.php?district_id=" + district_id).build();
                try{
                    Response response = client.newCall(request).execute();

                    JSONArray array = new JSONArray(response.body().string());
                    JSONObject object = array.getJSONObject(0);
                    districtSelected = new District(object.getInt("district_id"), object.getString("en_district_name"));
                }catch( IOException e){
                    e.printStackTrace();
                }catch( JSONException e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute(district_id);
    }*/

    private class getUserDistrict extends AsyncTask<Integer,Void,JSONObject>{

        SharedPreferences sharedPreferences = HomeActivity.this.getSharedPreferences("com.keerthan.tanuvas.selectedArea", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        @Override
        protected JSONObject doInBackground(Integer... integers) {
            String district_id = integers[0].toString();
            OkHttpClient client = new OkHttpClient();
            try{
                JSONArray array = new JSONArray(APICall.GET(client, RequestBuilder.buildURL("justtesting.php",new String[]{"district_id"},new String[]{district_id})));
                JSONObject object = array.getJSONObject(0);
                return object;
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if(jsonObject!=null){
                try {
                    districtSelected = new District(jsonObject.getInt("district_id"), jsonObject.getString("en_district_name"));
                    Gson gson = new Gson();
                    String json = gson.toJson(districtSelected);
                    editor.putString("selectedDistrict",json);
                    editor.commit();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                editor.putString("selectedDistrict",null);
                editor.commit();
            }
        }
    }



    private String[] loadScreenTitles() {
        return new String[]{"Questionnaire","New Farmer","Edit Farmer Bio","Multiple Farmers","Update Questions","Reports"};

    }
    private Drawable[] loadScreenIcons(){
        return new Drawable[]{getDrawable(R.drawable.ic_questionnaire),getDrawable(R.drawable.ic_addfarmer),getDrawable(R.drawable.ic_editfarmer),getDrawable(R.drawable.ic_addmulfarmers),getDrawable(R.drawable.ic_updatequestions),getDrawable(R.drawable.ic_reports)};
    }


    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withSelectedIconTint(R.color.colorPrimary)
                .withIconTint(R.color.Black)
                .withSelectedTextTint(R.color.colorPrimary)
                .withTextTint(R.color.Black);

    }


    @Override
    public void onItemSelected(int position) {

        if (position == POS_ADDNNEWFARMER) {
            toolbar.setTitle("NEW FARMER");
            android.support.v4.app.FragmentTransaction ft1 = manager.beginTransaction();
            Fragment newFarmer = AddFarmerFragment.newInstance(villageSelected,districtSelected);
            ft1.replace(R.id.mainFragment,newFarmer).addToBackStack( "tag" ).commit();
        } else if (position == POS_ADDMULTIPLEFARMERS) {
            toolbar.setTitle("NEW FARMER");
            android.support.v4.app.FragmentTransaction ft = manager.beginTransaction();
            Fragment newFarmers = AddMultipleFarmersFragment.newInstance(villageSelected,districtSelected);
            ft.replace(R.id.mainFragment,newFarmers).commit();
        } else if (position == POS_EDITFARMERDETAILS) {
            toolbar.setTitle("EDIT FARMER");
            android.support.v4.app.FragmentTransaction newTransaction = manager.beginTransaction();
            Fragment editFarmer = EditFarmerFragment.newInstance(villageSelected,districtSelected);
            newTransaction.replace(R.id.mainFragment,editFarmer).commit();
        } else if (position == POS_QUESTIONNAIRE) {
            toolbar.setTitle("TANUVAS");
            manager.beginTransaction().replace(R.id.mainFragment,new QuestionFragment()).commit();
        } else if (position == POS_REPORTS) {
            toolbar.setTitle("REPORTS");
            manager.beginTransaction().replace(R.id.mainFragment,new ReportsFragment()).commit();
        } else if (position == POS_UPDATEQUESTIONS) {
            toolbar.setTitle("UPDATE QUESTIONS");
            manager.beginTransaction().replace(R.id.mainFragment,new UpdateQuestionsFragment()).commit();

        }

        slidingRootNav.closeMenu(true);


    }
}
