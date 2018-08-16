package ca.cmpt276.walkinggroup.dataobjects;

import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that your group can change the format of in (almost) any way you like
 * to encode the rewards that this user has earned.
 *
 * This class gets serialized/deserialized as part of a User object. Server stores it as
 * a JSON string, so it has no direct knowledge of what it contains.
 * (Rewards may not be used during first project iteration or two)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarnedRewards {

    private String title = "Newborn";
    private List<String> earnedTitles = new ArrayList<>();
    private List<String> purchasedIcons = new ArrayList<>();

    private String currentIcon = "Boy Head 1";

    private Integer selectedBackground = 1;

    // Needed for JSON deserialization
    public EarnedRewards() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addEarnedTitle(String title){
        earnedTitles.add(title);
    }

    public List<String> getEarnedTitlesList(){
        return earnedTitles;
    }

    public void addIconPurchase(String icon){
        this.purchasedIcons.add(icon);
    }

    public boolean isIconPurchased(String icon){
        if(purchasedIcons.contains(icon)){
            return true;
        }
        return false;
    }

    public List<String> getPurchasedIcons(){
        return purchasedIcons;
    }

    public String getCurrentIcon() {
        return currentIcon;
    }

    public void setCurrentIcon(String currentIcon) {
        this.currentIcon = currentIcon;
    }

    public int getSelectedBackground() {
        return selectedBackground;
    }

    public void setSelectedBackground(int selectedBackground) {
        this.selectedBackground = selectedBackground;
    }


    @Override
    public String toString() {
        return "EarnedRewards{" +
                "title='" + title + '\'' +
                ", purchasedIcons=" + purchasedIcons +
                ", currentIcon=" + currentIcon +
                ", selectedBackground=" + selectedBackground +
                '}';
    }
}