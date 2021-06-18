package com.cromarmot.androidofficialtutorial;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IntentChooseDialog extends Dialog implements IntentChooserAdapter.IntentChooserClickListener {
    private Intent mIntent;

    public IntentChooseDialog(@NonNull Context context, Intent intent) {
        super(context);
        mIntent = intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null && getWindow().getAttributes() != null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            getWindow().getDecorView().setPadding(0, 0, 0, 0);
            params.gravity = Gravity.BOTTOM;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(params);
        }
        setContentView(R.layout.dialog_intent_chooser);
        initView();
    }

    private RecyclerView recyclerView;
    private TextView tvTitle;

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTitle = findViewById(R.id.tv_title);

        tvTitle.setText("请选择打开应用");
        List<ResolveInfo> infos = getContext().getPackageManager().queryIntentActivities(mIntent, PackageManager.MATCH_DEFAULT_ONLY);
        IntentChooserAdapter adapter = new IntentChooserAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);
        adapter.setData(infos);
        adapter.setIntentChooserClickListener(this);
    }

    @Override
    public void onClick(ResolveInfo resolveInfo) {
        mIntent.setPackage(resolveInfo.activityInfo.packageName);
        mIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
        Log.i("intentChooser", "packageName = " + resolveInfo.activityInfo.packageName + ";activityInfo.name = " + resolveInfo.activityInfo.name);
        if (mIntent.getData() != null) {
            getContext().grantUriPermission(resolveInfo.activityInfo.packageName, mIntent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        getContext().startActivity(mIntent);
        dismiss();
    }
}
