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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InstructionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionsFragment extends Fragment {
    private static final String ICON_RES_ID = "icon_resource_id";
    private static final String TEXT_TAG = "instruction_text";

    private Integer iconID;
    private String text;
    private TextView textView;
    private ImageView imageView;
    private RelativeLayout layout;

    public InstructionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param iconID resource id of the icon.
     * @param text text to be presented.
     * @return A new instance of fragment InstructionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructionsFragment newInstance(Integer iconID, String text) {
        InstructionsFragment fragment = new InstructionsFragment();
        Bundle args = new Bundle();
        if (iconID != null)
            args.putInt(ICON_RES_ID, iconID);
        if (text != null)
            args.putString(TEXT_TAG, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            iconID = getArguments().getInt(ICON_RES_ID);
            text = getArguments().getString(TEXT_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_instructions, container, false);
        textView = view.findViewById(R.id.directionsText);
        imageView = view.findViewById(R.id.directionsIcon);
        layout = view.findViewById(R.id.instructions_layout);
        if (text != null && iconID != null) {
            textView.setText(text);
            imageView.setImageDrawable(getResources().getDrawable(iconID));
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
        return view;
    }

}
