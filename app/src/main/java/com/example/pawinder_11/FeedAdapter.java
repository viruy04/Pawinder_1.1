package com.example.pawinder_11;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        String petName = pet.getName();

        holder.tvName.setTag(petName);

        // 1. Включаем прогресс-бар, скрываем и текст, и обе картинки
        holder.progressBarItem.setVisibility(View.VISIBLE);
        holder.tvName.setVisibility(View.INVISIBLE);
        holder.ivPhoto.setVisibility(View.INVISIBLE);
        holder.ivPhotoBg.setVisibility(View.INVISIBLE);

        // 2. Запускаем перевод через DeepL
        TranslationManager.translateText(context, petName, new TranslationManager.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                if (petName.equals(holder.tvName.getTag())) {
                    holder.tvName.setText(translatedText);
                    holder.progressBarItem.setVisibility(View.GONE);
                    holder.tvName.setVisibility(View.VISIBLE);
                    holder.ivPhoto.setVisibility(View.VISIBLE);
                    holder.ivPhotoBg.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onError(Throwable t) {
                if (petName.equals(holder.tvName.getTag())) {
                    holder.tvName.setText(petName);
                    holder.progressBarItem.setVisibility(View.GONE);
                    holder.tvName.setVisibility(View.VISIBLE);
                    holder.ivPhoto.setVisibility(View.VISIBLE);
                    holder.ivPhotoBg.setVisibility(View.VISIBLE);
                }
            }
        });

        // 3. Glide загружает картинку в фоне (пока крутится прогресс-бар перевода)
        String photoUrl = FeedLogicProvider.getInstance().resolvePhotoUrl(
                pet.getPhotoUrl(),
                "https://pawinder-server.onrender.com/");
        if (photoUrl != null) {
            Glide.with(context)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(darkPlaceholder)
                    .error(darkPlaceholder)
                    .into(holder.ivPhotoBg);

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
        ProgressBar progressBarItem; // Объявляем ProgressBar

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivFeedPhoto);
            ivPhotoBg = itemView.findViewById(R.id.ivFeedPhotoBg);
            tvName = itemView.findViewById(R.id.tvFeedName);
            progressBarItem = itemView.findViewById(R.id.progressBarItem); // Инициализируем
        }
    }
}