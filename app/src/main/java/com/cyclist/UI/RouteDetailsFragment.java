package com.cyclist.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyclist.R;
import com.cyclist.logic.common.Utils;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RouteDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteDetailsFragment extends Fragment {
    private static final String LENGTH_TAG = "length";
    private static final String DURATION_TAG = "duration";

    private Double duration, length;

    private TextView lengthTV;
    private TextView durationTV;
    private ImageView closeIcon;
    private RelativeLayout layout;
    private closeListener listener;

    public RouteDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param duration resource id of the icon.
     * @param length   text to be presented.
     * @return A new instance of fragment RouteDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RouteDetailsFragment newInstance(Double duration, Double length) {
        RouteDetailsFragment fragment = new RouteDetailsFragment();
        Bundle args = new Bundle();
        if (duration != null)
            args.putDouble(LENGTH_TAG, length);
        if (length != null)
            args.putDouble(DURATION_TAG, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            duration = getArguments().getDouble(DURATION_TAG);
            length = getArguments().getDouble(LENGTH_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.route_details_fragment, container, false);
        durationTV = view.findViewById(R.id.routeTime);
        lengthTV = view.findViewById(R.id.routeLength);
        closeIcon = view.findViewById(R.id.stopIcon);
        layout = view.findViewById(R.id.routeDetailsLayout);
        if (length != null && duration != null) {
            durationTV.setText(Utils.getTimeString(duration, getResources().getString(R.string.minutes_text)));
            lengthTV.setText(String.format(Locale.getDefault(), " %02.3f km", length));
            closeIcon.setOnClickListener(view1 -> listener.onCloseClick());
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
        return view;
    }

    public void setListener(closeListener listener) {
        this.listener = listener;
    }

    interface closeListener {
        void onCloseClick();
    }
}
