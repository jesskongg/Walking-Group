package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.EarnedTitlesDialog;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Rewards;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.edit_info_activities.EditUsersMenuActivity;
import ca.cmpt276.walkinggroup.app.user_interface.group_activities.ManageGroupsActivity;
import ca.cmpt276.walkinggroup.app.user_interface.group_activities.SelectGroupActivity;
import ca.cmpt276.walkinggroup.app.user_interface.message_activities.PanicActivity;
import ca.cmpt276.walkinggroup.app.user_interface.sign_up_in_activities.SignUpActivity;
import ca.cmpt276.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "MainMenuActivity";
    private WGServerProxy proxy;
    private Session session;
    private User currentUser;
    private Boolean finishedLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        session = Session.getInstance();
        setupSession();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (!finishedLoading) {
            Toast.makeText(MainMenuActivity.this, "Loading, please wait ...", Toast.LENGTH_SHORT).show();
            return false;
        }

        int id = item.getItemId();

        if (id == R.id.nav_edit_profile) {
            Intent intent = EditUsersMenuActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_change_monitor) {
            Intent intent = ChangeMonitorActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_monitored_by_me) {
            Intent intent = ListMonitorActivity.makeIntent(
                    MainMenuActivity.this, ListMonitorActivity.MONITORED);
            startActivity(intent);
        } else if (id == R.id.nav_my_monitors) {
            Intent intent = ListMonitorActivity.makeIntent(MainMenuActivity.this,
                    ListMonitorActivity.MONITORING);
            startActivity(intent);
        } else if (id == R.id.nav_group_features) {
            Intent intent = ManageGroupsActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_start_walk) {
            Intent intent = SelectGroupActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_dashboard) {
            Intent intent = DashboardActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_sign_out) {
            Preferences.clear();
            Intent intent = SignUpActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_panic) {
            verifyChild(session.getSessionUser());
        } else if (id == R.id.nav_perimission) {
            Intent intent = PermissionActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_history) {
            Intent intent = HistoryActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_leaderboard) {
            Intent intent = LeaderboardActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        } else if (id == R.id.nav_shop) {
            Intent intent = ShopActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupSession() {
        proxy = ProxyBuilder.getProxy(getString(R.string.cmpt276_server_api_key),
                Preferences.getToken(),1);
        session.saveSessionProxy(proxy);

        Call<User> userCaller = proxy.getUserByEmail(Preferences.getEmail());
        ProxyBuilder.callProxy(this, userCaller, returnedCurrentUser -> {

            session.saveSessionUser(returnedCurrentUser);
            currentUser = session.getSessionUser();

            boolean flag = false;
            if(currentUser.getCurrentPoints() == null){
                flag = true;
                Integer currentPoints = 0;
                currentUser.setCurrentPoints(currentPoints);
            }

            if(currentUser.getTotalPointsEarned() == null) {
                flag = true;
                Integer totalPoints = 0;
                currentUser.setTotalPointsEarned(totalPoints);
            }

            if(currentUser.getRewards() == null){
                flag = true;
                EarnedRewards newEarnedRewards = new EarnedRewards();
                Rewards rewards = Rewards.getInstance();
                newEarnedRewards.addEarnedTitle("Newborn");
                newEarnedRewards.setCurrentIcon(rewards.BOY_HEAD_1);
                newEarnedRewards.addIconPurchase(rewards.BOY_HEAD_1);
                newEarnedRewards.addIconPurchase(rewards.GIRL_HEAD_1);
                Log.d(TAG, "bah"+newEarnedRewards.getPurchasedIcons());
                currentUser.setRewards(newEarnedRewards);
            }

            if(flag == true){
                Call<User> caller = proxy.editUser(currentUser.getId(), currentUser);
                ProxyBuilder.callProxy(this, caller,
                        returnedEditedUser -> Log.d(TAG, "logUserUpdate: " + returnedEditedUser.toString()));
            }

            updateUserRewards();
            displayUserInfo();
            displayUserStickers();
            finishedLoading = true;
        });
    }

    private void updateUserRewards() {

        Integer totalPointsEarned = currentUser.getTotalPointsEarned();
        Rewards rewards = Rewards.getInstance();

        String currentTitle = rewards.getTitle(totalPointsEarned);
        EarnedRewards usersEarnedRewards = currentUser.getRewards();

        usersEarnedRewards.setTitle(currentTitle);
        List<String> titlesList = usersEarnedRewards.getEarnedTitlesList();

        if(!titlesList.contains(currentTitle)){
            usersEarnedRewards.addEarnedTitle("\n");
            usersEarnedRewards.addEarnedTitle(currentTitle);
            currentUser.setRewards(usersEarnedRewards);
            Call<User> caller = proxy.editUser(currentUser.getId(), currentUser);
            ProxyBuilder.callProxy(this, caller, doNothing -> Log.d(TAG, "updateUserRewards: user updated"));
        }
    }

    private void displayUserInfo() {
        TextView nameView = findViewById(R.id.nav_header_name);
        TextView emailView = findViewById(R.id.nav_header_email);
        TextView profileNameView = findViewById(R.id.profile_name_text);
        TextView profileTitleView = findViewById(R.id.profile_title_text);
        TextView profileCurrentPointsView = findViewById(R.id.profile_current_points_text);
        TextView profileTotalPointsView = findViewById(R.id.profile_total_points_text);
        TextView profileRequiredPointsText = findViewById(R.id.profile_walks_to_next_title_text);
        ImageView profileUserIconView = findViewById(R.id.user_icon_view);

        String name = currentUser.getName();
        String email = currentUser.getEmail();
        String title = currentUser.getRewards().getTitle();
        Integer currentPoints = currentUser.getCurrentPoints();
        Integer totalPoints = currentUser.getTotalPointsEarned();

        Rewards rewards = Rewards.getInstance();
        String userIcon = currentUser.getRewards().getCurrentIcon();
        Integer walksToNextTitle = rewards.calculateWalksUntilNextTitle(totalPoints);

        nameView.setText(name);
        emailView.setText(email);

        String nameOutput = "Name: " + name;
        String currentPointsOutput = "Current Points: " + Integer.toString(currentPoints);
        String totalPointsOutput = "Total Score: " + Integer.toString(totalPoints);
        String requiredPointsOutput = "Next Title: " + walksToNextTitle + " Walks";

        if(userIcon.equals(rewards.BOY_HEAD_1)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.boy_head1));
        } else if(userIcon.equals(rewards.BOY_HEAD_2)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.boy_head2));
        } else if(userIcon.equals(rewards.BOY_HEAD_3)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.boy_head3));
        } else if(userIcon.equals(rewards.BOY_HEAD_4)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.boy_head4));
        } else if(userIcon.equals(rewards.GIRL_HEAD_1)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.girl_head1));
        } else if(userIcon.equals(rewards.GIRL_HEAD_2)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.girl_head2));
        } else if(userIcon.equals(rewards.GIRL_HEAD_3)){
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.girl_head3));
        } else if(userIcon.equals(rewards.GIRL_HEAD_4)) {
            profileUserIconView.setImageDrawable(getDrawable(R.drawable.girl_head4));
        }
        profileTitleView.setText(title);
        profileNameView.setText(nameOutput);
        profileCurrentPointsView.setText(currentPointsOutput);
        profileTotalPointsView.setText(totalPointsOutput);
        profileRequiredPointsText.setText(requiredPointsOutput);

        setupTitleClick();
    }

    private void displayUserStickers() {
        ImageView penguinStickerView = findViewById(R.id.penguin_sticker);
        ImageView pelicanStickerView = findViewById(R.id.pelican_sticker);
        ImageView ostrichStickerView = findViewById(R.id.ostrich_sticker);
        ImageView pandaStickerView = findViewById(R.id.panda_sticker);
        ImageView kangarooStickerView = findViewById(R.id.kangaroo_sticker);
        ImageView giraffeStickerView = findViewById(R.id.giraffe_sticker);
        ImageView hippoStickerView = findViewById(R.id.hippo_sticker);
        ImageView tigerStickerView = findViewById(R.id.tiger_sticker);
        ImageView zebraStickerView = findViewById(R.id.zebra_sticker);
        ImageView crocodileStickerView = findViewById(R.id.crocodile_sticker);
        ImageView buffaloStickerView = findViewById(R.id.buffalo_sticker);
        ImageView racoonStickerView = findViewById(R.id.racoon_sticker);
        ImageView dogStickerView = findViewById(R.id.dog_sticker);
        ImageView snakeStickerView = findViewById(R.id.snake_sticker);
        ImageView wildcatStickerView = findViewById(R.id.wildcat_sticker);
        ImageView goatStickerView = findViewById(R.id.goat_sticker);
        ImageView sheepStickerView = findViewById(R.id.sheep_sticker);
        ImageView horseStickerView = findViewById(R.id.horse_sticker);
        ImageView catStickerView = findViewById(R.id.cat_sticker);
        ImageView slothStickerView = findViewById(R.id.sloth_sticker);

        Integer totalPoints = currentUser.getTotalPointsEarned();

        if(totalPoints >= 50){
            penguinStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 100){
            pelicanStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 200){
            ostrichStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 400){
            pandaStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 600){
            kangarooStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 800){
            giraffeStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 1100){
            hippoStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 1400){
            tigerStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 1700){
            zebraStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 2000){
            crocodileStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 2300){
            buffaloStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 2600){
            racoonStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 2900){
            dogStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 3200){
            snakeStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 3500){
            wildcatStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 3800){
            goatStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 4100){
            sheepStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 4400){
            horseStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 4700){
            catStickerView.setAlpha(1.0f);
        }

        if(totalPoints >= 5000){
            slothStickerView.setAlpha(1.0f);
        }
    }

    private void setupTitleClick() {
        TextView profileTitleView = findViewById(R.id.profile_title_text);
        profileTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                EarnedTitlesDialog dialog = new EarnedTitlesDialog();
                dialog.show(fragmentManager, "Dialog");
            }
        });

    }

    private void verifyChild(User sessionUser) {
        List<User> listMonitors = sessionUser.getMonitoredByUsers();
        if (listMonitors.size() == 0) {
            Toast.makeText(MainMenuActivity.this, "You are not a child", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = PanicActivity.makeIntent(MainMenuActivity.this);
            startActivity(intent);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MainMenuActivity.class);
    }
}
