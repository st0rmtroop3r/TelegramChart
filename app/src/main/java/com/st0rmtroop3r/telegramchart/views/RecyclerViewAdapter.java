package com.st0rmtroop3r.telegramchart.views;

import android.content.res.Resources;
import android.view.ViewGroup;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<ChartData> chartDataList = new ArrayList<>();
    private Resources.Theme theme;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new ChartView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.view.setCharData(chartDataList.get(position), position);
        if (theme != null) {
            holder.view.switchTheme(theme);
        }
    }

    @Override
    public int getItemCount() {
        return chartDataList.size();
    }

    public void setDataList(List<ChartData> list) {
        chartDataList.clear();
        chartDataList.addAll(list);
    }

    public void setTheme(Resources.Theme newTheme) {
        theme = newTheme;
    }

    public void switchTheme(Resources.Theme newTheme) {
        theme = newTheme;
//        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ChartView view;
        public ViewHolder(@NonNull ChartView itemView) {
            super(itemView);
            view = itemView;
        }
    }
}
