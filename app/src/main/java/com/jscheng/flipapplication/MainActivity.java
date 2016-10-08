package com.jscheng.flipapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jscheng.flipapplication.view.NewFlipLayout;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private LayoutInflater inflater;
    private int num = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = getLayoutInflater();
        LinearLayout contentview = (LinearLayout)findViewById(R.id.content_main);
        contentview.removeAllViews();

        for(int i=0;i<3;i++) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.removeAllViews();
            linearLayout.setGravity(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.setPadding(0,5,0,5);
            for(int j=0;j<3;j++){
                NewFlipLayout card = (NewFlipLayout) inflater.inflate(R.layout.layout_card,null);
                card.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f));
                TextView textView = (TextView) card.findViewById(R.id.back_textview);
                textView.setText(""+num++);
                linearLayout.addView(card);
            }
            contentview.addView(linearLayout);
        }
    }
}
