package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Rewards;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ShopActivity extends AppCompatActivity {

    private static final String TAG = "ShopActivity";
    private WGServerProxy proxy;
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();
        currentUser = session.getSessionUser();

        setupUI();
        setupButtonClick();

    }

    private void setupUI() {

        Button boyHead2Button = findViewById(R.id.boy_head2_shop_btn);
        Button boyHead3Button = findViewById(R.id.boy_head3_shop_btn);
        Button boyHead4Button = findViewById(R.id.boy_head4_shop_btn);
        Button girlHead2Button = findViewById(R.id.girl_head2_shop_btn);
        Button girlHead3Button = findViewById(R.id.girl_head3_shop_btn);
        Button girlHead4Button = findViewById(R.id.girl_head4_shop_btn);

        EarnedRewards earnedRewards = currentUser.getRewards();
        Rewards rewards = Rewards.getInstance();

        if(earnedRewards.isIconPurchased(rewards.BOY_HEAD_2)){
            boyHead2Button.setText(R.string.shop_use_btn);
        }

        if(earnedRewards.isIconPurchased(rewards.BOY_HEAD_3)){
            boyHead3Button.setText(R.string.shop_use_btn);
        }

        if(earnedRewards.isIconPurchased(rewards.BOY_HEAD_4)){
            boyHead4Button.setText(R.string.shop_use_btn);
        }

        if(earnedRewards.isIconPurchased(rewards.GIRL_HEAD_2)){
            girlHead2Button.setText(R.string.shop_use_btn);
        }

        if(earnedRewards.isIconPurchased(rewards.GIRL_HEAD_3)){
            girlHead3Button.setText(R.string.shop_use_btn);
        }

        if(earnedRewards.isIconPurchased(rewards.GIRL_HEAD_4)){
            girlHead4Button.setText(R.string.shop_use_btn);
        }
    }

    private void setupButtonClick() {

        Button boyHead1Button = findViewById(R.id.boy_head1_shop_btn);
        Button boyHead2Button = findViewById(R.id.boy_head2_shop_btn);
        Button boyHead3Button = findViewById(R.id.boy_head3_shop_btn);
        Button boyHead4Button = findViewById(R.id.boy_head4_shop_btn);
        Button girlHead1Button = findViewById(R.id.girl_head1_shop_btn);
        Button girlHead2Button = findViewById(R.id.girl_head2_shop_btn);
        Button girlHead3Button = findViewById(R.id.girl_head3_shop_btn);
        Button girlHead4Button = findViewById(R.id.girl_head4_shop_btn);

        Rewards rewards = Rewards.getInstance();

        boyHead1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.BOY_HEAD_1, boyHead1Button);
            }
        });

        boyHead2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.BOY_HEAD_2, boyHead2Button);
            }
        });

        boyHead3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.BOY_HEAD_3, boyHead3Button);
            }
        });

        boyHead4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.BOY_HEAD_4, boyHead4Button);
            }
        });

        girlHead1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.GIRL_HEAD_1, girlHead1Button);
            }
        });

        girlHead2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.GIRL_HEAD_2, girlHead2Button);
            }
        });

        girlHead3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.GIRL_HEAD_3, girlHead3Button);
            }
        });

        girlHead4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIconSelection(rewards.GIRL_HEAD_4, girlHead4Button);
            }
        });

    }

    private void setupIconSelection(String iconName, Button button) {
        Rewards rewards = Rewards.getInstance();
        EarnedRewards earnedRewards = currentUser.getRewards();

        if(earnedRewards.isIconPurchased(iconName)){
            Toast.makeText(ShopActivity.this, "Icon switched", Toast.LENGTH_SHORT).show();
            earnedRewards.setCurrentIcon(iconName);
            currentUser.setRewards(earnedRewards);
        } else if(rewards.canMakeIconPurchase()) {
            earnedRewards.addIconPurchase(iconName);
            earnedRewards.setCurrentIcon(iconName);
            currentUser.setRewards(earnedRewards);

            Toast.makeText(ShopActivity.this,
                    "Icon purchased and switched", Toast.LENGTH_SHORT).show();
            deductCurrentPoints();
            button.setText(R.string.shop_use_btn);

        } else {
            Toast.makeText(ShopActivity.this, "Not enough points", Toast.LENGTH_SHORT).show();
        }
    }

    private void deductCurrentPoints() {
        Integer currentPoints = currentUser.getCurrentPoints();
        currentUser.setCurrentPoints(currentPoints - 10);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ShopActivity.class);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Uploading to the server - Please wait", Toast.LENGTH_SHORT).show();
        Call<User> caller = proxy.editUser(currentUser.getId(), currentUser);
        ProxyBuilder.callProxy(this, caller, returnedUser -> {
            Log.d(TAG, "User updated on server");
            Intent intent = MainMenuActivity.makeIntent(ShopActivity.this);
            startActivity(intent);
            super.onBackPressed();
        });
    }
}
