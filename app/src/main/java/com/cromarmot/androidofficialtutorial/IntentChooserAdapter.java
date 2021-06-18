package com.cromarmot.androidofficialtutorial;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IntentChooserAdapter extends RecyclerView.Adapter<IntentChooserAdapter.IntentChooserHolder> {
    private List<ResolveInfo> mData = null;
    private IntentChooserClickListener mListener;

    public void setData(List<ResolveInfo> resolveInfos) {
        this.mData = resolveInfos;
        notifyDataSetChanged();
    }

    public void setIntentChooserClickListener(IntentChooserClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public IntentChooserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_intent_chooser, viewGroup, false);
        return new IntentChooserHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IntentChooserHolder intentChooserHolder, int i) {
        ResolveInfo info = mData.get(i);
        intentChooserHolder.bind(info, mListener);
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public static interface IntentChooserClickListener {
        void onClick(ResolveInfo resolveInfo);
    }

    static class IntentChooserHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvName;

        public IntentChooserHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bind(final ResolveInfo info, final IntentChooserClickListener onClickListener) {
            PackageManager pm = ivIcon.getContext().getPackageManager();
            ivIcon.setImageDrawable(info.activityInfo.loadIcon(pm));
            tvName.setText(info.activityInfo.loadLabel(pm));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) onClickListener.onClick(info);
                }
            });
        }
    }
}
