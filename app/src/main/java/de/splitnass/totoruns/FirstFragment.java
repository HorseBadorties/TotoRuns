package de.splitnass.totoruns;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;


public class FirstFragment extends Fragment implements OnMapReadyCallback {


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
        MapView mapView = (MapView)result.findViewById(R.id.mapView);
        //mapView.getMapAsync((MainActivity)getActivity());
        mapView.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Map is ready", Toast.LENGTH_SHORT).show();
    }
}
