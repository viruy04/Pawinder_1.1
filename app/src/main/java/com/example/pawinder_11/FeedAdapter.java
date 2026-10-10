package com.example.pawinder_11;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pawinder_11.model.Pet;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final Context context;
    private final List<Pet> petList;
    private final ColorDrawable darkPlaceholder = new ColorDrawable(Color.parseColor("#1C1C1E"));

    public FeedAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed_pet, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Pet pet = petList.get(position);

        // Только имя, без описания
        holder.tvName.setText(pet.getName());

        String photoUrl = pet.getPhotoUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            if (photoUrl.startsWith("/")) {
                photoUrl = "https://pawinderserver-production.up.railway.app" + photoUrl;
            }

            // 1. Фоновое изображение (TikTok-стиль: растянутое с centerCrop)
            Glide.with(context)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(darkPlaceholder)
                    .error(darkPlaceholder)
                    .into(holder.ivPhotoBg);

            // 2. Основное изображение (в оригинальных пропорциях: fitCenter)
            Glide.with(context)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .placeholder(darkPlaceholder)
                    .error(darkPlaceholder)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhotoBg.setImageDrawable(darkPlaceholder);
            holder.ivPhoto.setImageDrawable(darkPlaceholder);
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto, ivPhotoBg;
        TextView tvName;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivFeedPhoto);
            ivPhotoBg = itemView.findViewById(R.id.ivFeedPhotoBg);
            tvName = itemView.findViewById(R.id.tvFeedName);
        }
    }
}