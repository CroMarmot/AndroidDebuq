package com.example.aotm1;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    private static  final  String TAG = "InfoFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoFragment newInstance(String param1, String param2) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void getInfo(View view){
        ArrayList<String> infoString = new ArrayList<>();
        int whichAndroidVersion= Build.VERSION.SDK_INT;
        Log.i(TAG, "SDK_INT: "+whichAndroidVersion);
        infoString.add("SDK_INT: " + whichAndroidVersion);

        String androidOS = Build.VERSION.RELEASE;
        Log.i(TAG, "RELEASE: "+androidOS);
        infoString.add( "RELEASE: "+androidOS);

        String manufacturer = Build.MANUFACTURER;
        Log.i(TAG, "MANUFACTURER: "+manufacturer);
        infoString.add(  "MANUFACTURER: "+manufacturer);

        String model = Build.MODEL;
        Log.i(TAG, "MODEL: "+model);
        infoString.add(  "MODEL: "+model);
        //
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item,infoString);
        ListView listView = (ListView) view.findViewById(R.id.info_list);
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_info_fragment_info, container, false);

        getInfo(view);

        return view;
    }
}