package ca.cmpt276.walkinggroup.app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import ca.cmpt276.walkinggroup.app.R;

/*
 * CustomInfoWindowAdapter class implements a customized marker info window UI
 *
 * This class was inspired by the following youtube tutorial:
 * https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void rendorWindowText(Marker marker, View view){
        String title = marker.getTitle();
        TextView titleView = view.findViewById(R.id.map_info_title_text);
        if(!title.equals("")){
            titleView.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView titleSnippetView = view.findViewById(R.id.map_snippet_text);

        if(!snippet.equals("")){
            titleSnippetView.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendorWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendorWindowText(marker, mWindow);
        return mWindow;
    }


}
