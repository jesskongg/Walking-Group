package ca.cmpt276.walkinggroup.app.model;

//Rewards class contains all of the available rewards

import android.widget.Toast;

import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class Rewards {

    public final String BOY_HEAD_1 = "Boy Head 1";
    public final String BOY_HEAD_2 = "Boy Head 2";
    public final String BOY_HEAD_3 = "Boy Head 3";
    public final String BOY_HEAD_4 = "Boy Head 4";
    public final String GIRL_HEAD_1 = "Girl Head 1";
    public final String GIRL_HEAD_2 = "Girl Head 2";
    public final String GIRL_HEAD_3 = "Girl Head 3";
    public final String GIRL_HEAD_4 = "Girl Head 4";

    private static Rewards rewards;

    private Rewards(){}

    public static Rewards getInstance(){
        if(rewards == null){
            rewards = new Rewards();
        }
        return rewards;
    }

    public String getTitle(Integer totalPoints){

        if(totalPoints >= 5000){
            return "Conquerer";
        } else if(totalPoints >= 3000){
            return "DragonBorn";
        } else if(totalPoints >= 1800){
            return "Hero";
        } else if(totalPoints >= 1000){
            return "Honoured Knight";
        } else if(totalPoints >= 500){
            return "Knight";
        } else if(totalPoints >= 200){
            return "Expert Squire";
        } else if(totalPoints >= 100){
            return "Squire";
        } else if(totalPoints >= 50){
            return "Villager";
        } else {
            return "Newborn";
        }
    }

    public Integer calculateWalksUntilNextTitle(Integer totalPoints){
        final Integer POINTS_PER_WALK = 10;

        if(totalPoints >= 5000){
            return 0;
        } else if(totalPoints >= 3000){
            return (5000 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 1800){
            return (3000 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 1000){
            return (1800 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 500){
            return (1000 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 200){
            return (500 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 100){
            return (200 - totalPoints)/POINTS_PER_WALK;
        } else if(totalPoints >= 50){
            return (100 - totalPoints)/POINTS_PER_WALK;
        } else {
            return (50 - totalPoints)/POINTS_PER_WALK;
        }
    }

    public boolean canMakeIconPurchase(){

        Session session = Session.getInstance();
        User currentUser = session.getSessionUser();

        Integer currentPoints = currentUser.getCurrentPoints();

        if(currentPoints >= 60){
            return true;
        } else {
            return false;
        }
    }

}
