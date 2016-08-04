package com.igpink.app.sundyzhou.igpinkutils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.igpink.app.sundyzhou.igpinkutilslib.widget.alert.AlertView;
import com.igpink.app.sundyzhou.igpinkutilslib.widget.alert.OnItemClickListener;

public class MainActivity extends AppCompatActivity {


    private Button showShare;
    private Activity activity;

    private Context context;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActivity(this);
        context = MainActivity.this;
        init();


        showShare.setOnClickListener(new OnClick());
    }

    private void init() {
        showShare = (Button) findViewById(R.id.showShare);
    }

    private class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.showShare:{

                    Bundle bundle = new Bundle();

                    /**
                     *
                     * */
                    AlertView alertView = new AlertView("选择分享", null, null, null, null, context, AlertView.Style.ActionShare, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                        }
                    }, AlertView.TYPE_APP, bundle, getActivity());
                    alertView.setCancelable(true);
                    alertView.show();
                }
                    break;
            }
        }
    }
}
