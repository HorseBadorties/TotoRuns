package de.splitnass.totoruns;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import java.util.Date;


public class FirstFragment extends Fragment {


    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_first, container, false);
        MapView mapView = (MapView)getView().findViewById(R.id.mapView);
        ((MainActivity)getActivity()).mapViewActivated(mapView);
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setString(String text) {
        if (getView() != null) {
            ((Button)getView().findViewById(R.id.button3)).setText("Update: " + System.currentTimeMillis());
            TextView textView = (TextView) getView().findViewById(R.id.firstText);
            textView.setText(text);
        }
    }



    public void reset(View view) {
        ((MainActivity)getActivity()).reset(view);
    }

}
