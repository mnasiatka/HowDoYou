package com.ythogh.howdoyou;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PagePagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_page, container, false);
        TextView tvPage = (TextView) rootView.findViewById(R.id.PAGEFRAGMENT_TEXTVIEW_TEST);
        Bundle extras = this.getArguments();
        if (extras != null) {
            int position = extras.getInt("position");
            System.out.println("position: " + position);
            tvPage.setText("Page: " + (position + 1));
        } else {
            System.out.println("null");
            tvPage.setText("Page: x");
        }
        return rootView;
    }
}
