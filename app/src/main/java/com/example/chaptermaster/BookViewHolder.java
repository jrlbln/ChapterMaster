package com.example.chaptermaster;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class BookViewHolder extends RecyclerView.ViewHolder {

    private TextView titleTextView;
    private TextView authorTextView;
    private ImageView coverImageView;

    public BookViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.bookTitle);
        authorTextView = itemView.findViewById(R.id.bookAuthor);
        coverImageView = itemView.findViewById(R.id.bookCover);
    }

    public void bind(Book book) {
        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());

        Glide.with(itemView.getContext()).load(book.getImageUrl()).into(coverImageView);
    }
}
