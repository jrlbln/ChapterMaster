package com.example.chaptermaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class add_book extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 123;

    private EditText bookTitleEditText;
    private EditText bookAuthorEditText;
    private ImageView bookCoverImageView;
    private Button saveButton;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        bookTitleEditText = findViewById(R.id.bookTitle);
        bookAuthorEditText = findViewById(R.id.bookAuthor);
        bookCoverImageView = findViewById(R.id.bookImage);
        saveButton = findViewById(R.id.btnSave);

        bookCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = bookTitleEditText.getText().toString();
                String author = bookAuthorEditText.getText().toString();

                if (selectedImageUri != null) {
                    uploadImageToStorage(title, author);
                } else {
                    Toast.makeText(add_book.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();

            bookCoverImageView.setImageURI(selectedImageUri);

            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop() // Crop the image to fit the ImageView
                    .placeholder(R.drawable.ic_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(bookCoverImageView);
        }
    }


    private void uploadImageToStorage(final String title, final String author) {
        if (selectedImageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference imageRef = storageRef.child("book_covers/" + System.currentTimeMillis());

            UploadTask uploadTask = imageRef.putFile(selectedImageUri);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String imageUrl = task.getResult().toString();
                                    saveBookDetails(title, author, imageUrl);
                                } else {
                                    Toast.makeText(add_book.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(add_book.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveBookDetails(String title, String author, String imageUrl) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");

        String bookId = booksRef.push().getKey();

        if (bookId != null) {
            Map<String, Object> bookData = new HashMap<>();
            bookData.put("id", bookId);
            bookData.put("title", title);
            bookData.put("author", author);
            bookData.put("imageUrl", imageUrl);

            booksRef.child(bookId).setValue(bookData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(add_book.this, "Book added successfully", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(add_book.this, MainActivity.class);
                                startActivity(intent);

                                finish();
                            } else {
                                Toast.makeText(add_book.this, "Failed to add book", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
