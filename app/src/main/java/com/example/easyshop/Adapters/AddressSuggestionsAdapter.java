package com.example.easyshop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.R;

import java.util.List;

public class AddressSuggestionsAdapter extends RecyclerView.Adapter<AddressSuggestionsAdapter.ViewHolder> {

    private List<String> suggestions;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String suggestion);
    }

    public AddressSuggestionsAdapter(List<String> suggestions, OnItemClickListener onItemClickListener) {
        this.suggestions = suggestions;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.suggestionTextView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> {
            onItemClickListener.onItemClick(suggestion);
            clearSuggestions();
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateSuggestions(List<String> newSuggestions) {
        suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    public void clearSuggestions() {
        suggestions.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionTextView = itemView.findViewById(R.id.suggestionTextView);
        }
    }
}
