package com.ch_l.downbuttonrecorder;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class recordAdapter extends RecyclerView.Adapter<recordAdapter.ViewHolder> {
    ArrayList<recordItem> recordItems;

    public recordAdapter(ArrayList<recordItem> recordItems) {
        this.recordItems = recordItems;
    }

    @NonNull
    @Override
    public recordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull recordAdapter.ViewHolder holder, int position) {
        holder.item_name.setText(recordItems.get(position).getItem_name());
        holder.item_date.setText(recordItems.get(position).item_date);
        holder.item_size.setText(recordItems.get(position).item_size);
        holder.item_time.setText(recordItems.get(position).item_time);

        int item_position=holder.getAdapterPosition();


    }

    @Override
    public int getItemCount() {
        return recordItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name;
        TextView item_time;
        TextView item_date;
        TextView item_size;
        TextView item_textView_name;
        TextView tv_ding,tv_fduration;
        LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            item_name = (TextView) itemView.findViewById(R.id.item_name);
            item_time = (TextView) itemView.findViewById(R.id.item_time);
            item_date = (TextView) itemView.findViewById(R.id.item_date);
            item_size = (TextView) itemView.findViewById(R.id.item_size);
            item_textView_name=(TextView)itemView.findViewById(R.id.tv_stname);
            tv_ding=(TextView)itemView.findViewById(R.id.tv_ding);
            tv_fduration=(TextView)itemView.findViewById(R.id.tv_fduration);
            linearLayout=(LinearLayout)itemView.findViewById(R.id.Linear1);

        }
    }
}
