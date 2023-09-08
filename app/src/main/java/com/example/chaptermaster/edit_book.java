package com.example.chaptermaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class edit_book extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 123;

    private EditText bookTitleEditText;
    private EditText bookAuthorEditText;
    private ImageView bookCoverImageView;
    private Button saveButton;
    private Button deleteButton;

    private Uri selectedImageUri = null;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        bookTitleEditText = findViewById(R.id.bookTitle);
        bookAuthorEditText = findViewById(R.id.bookAuthor);
        bookCoverImageView = findViewById(R.id.bookImage);
        saveButton = findViewById(R.id.btnSave);
        deleteButton = findViewById(R.id.btnDelete);

        bookCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("bookId")) {
            bookId = intent.getStringExtra("bookId");
            // Load book data based on bookId and populate the UI fields
            loadBookData();
        } else {
            // Handle the case when bookId is null or not provided
            Toast.makeText(this, "Book ID is missing", Toast.LENGTH_SHORT).show();
            // You may want to finish() this activity or navigate back to the previous activity here
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = bookTitleEditText.getText().toString();
                String author = bookAuthorEditText.getText().toString();

                // Check if selectedImageUri is not null before attempting to upload
                if (selectedImageUri != null) {
                    uploadImageToStorage(title, author);
                } else {
                    // If selectedImageUri is null, save the book details without modifying the image URL
                    saveBookDetails(title, author, null);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement book deletion logic here
                deleteBook();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            bookCoverImageView.setImageURI(selectedImageUri);
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
                                    Toast.makeText(edit_book.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(edit_book.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveBookDetails(String title, String author, String imageUrl) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");

        // Create a map to update only the provided fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("author", author);

        if (imageUrl != null) {
            // Update the imageUrl only if it's provided
            updates.put("imageUrl", imageUrl);
        }

        // Update the book data based on the bookId with the provided fields
        booksRef.child(bookId).updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(edit_book.this, "Book updated successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to MainActivity or any desired activity
                    finish();
                } else {
                    Toast.makeText(edit_book.this, "Failed to update book", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void loadBookData() {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books").child(bookId);
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book != null) {
                        bookTitleEditText.setText(book.getTitle());
                        bookAuthorEditText.setText(book.getAuthor());

                        // Load and set the book cover image here
                        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                            // You can use a library like Picasso, Glide, or any other to load the image
                            // For example, using Picasso:
                            Picasso.get().load(book.getImageUrl()).into(bookCoverImageView);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(edit_book.this, "Failed to load book data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteBook() {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
        booksRef.child(bookId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(edit_book.this, "Book deleted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to MainActivity or any desired activity
                    finish();
                } else {
                    Toast.makeText(edit_book.this, "Failed to delete book", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
