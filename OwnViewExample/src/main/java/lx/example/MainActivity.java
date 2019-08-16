package lx.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import lx.example.tool.OwnClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClickListener listener = new ClickListener();
        findViewById(R.id.RipplePlayerView).setOnClickListener(listener);
        findViewById(R.id.SingleTypeExpandableVerticalLinearLayout).setOnClickListener(listener);
        findViewById(R.id.SingleTypeFlowLayout).setOnClickListener(listener);
        findViewById(R.id.DragSupportFlowLayout).setOnClickListener(listener);
    }

    private class ClickListener extends OwnClickListener {
        @Override
        protected void onValidClick(View v) {
            switch (v.getId()) {
                case R.id.RipplePlayerView:
                    break;
                case R.id.SingleTypeExpandableVerticalLinearLayout:
                    startActivity(new Intent(MainActivity.this, SingleTypeExpandableVerticalLinearLayoutSample.class));
                    break;
                case R.id.SingleTypeFlowLayout:
                    startActivity(new Intent(MainActivity.this, FlowLayoutSample.class));
                    break;
                case R.id.DragSupportFlowLayout:
                    startActivity(new Intent(MainActivity.this, DragSupportSingleTypeFlowLayoutSample.class));
                    break;
            }
        }
    }
}
