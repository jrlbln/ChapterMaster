package com.example.chaptermaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private ImageView addButton, searchIcon;
    private EditText searchEditText;
    private List<Book> allBooks;
    private LinearLayout rootLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Intent signInIntent = new Intent(MainActivity.this, signIn.class);
            startActivity(signInIntent);
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("user_books").child(user.getUid());

        recyclerView = findViewById(R.id.bookList);
        adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton = findViewById(R.id.addButton);

        searchIcon = findViewById(R.id.search_icon);
        searchEditText = findViewById(R.id.search_editText);
        rootLayout = findViewById(R.id.rootLayout);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, add_book.class);
                startActivity(intent);
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchEditTextVisibility();
            }
        });

        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSearchEditTextIfVisible(event);
                return false;
            }
        });

        allBooks = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clearBooks();
                allBooks.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null) {
                        adapter.addBook(book);
                        allBooks.add(book);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database Error: " + databaseError.getMessage());
            }
        });

        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Book book = adapter.getBook(position);

                Intent intent = new Intent(MainActivity.this, edit_book.class);
                intent.putExtra("bookId", book.getId());
                startActivity(intent);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filterBooks(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void toggleSearchEditTextVisibility() {
        if (searchEditText.getVisibility() == View.VISIBLE) {
            searchEditText.setVisibility(View.GONE);
            searchEditText.setText("");
        } else {
            searchEditText.setVisibility(View.VISIBLE);
        }
    }

    private void hideSearchEditTextIfVisible(MotionEvent event) {
        if (searchEditText.getVisibility() == View.VISIBLE) {
            int[] location = new int[2];
            searchEditText.getLocationOnScreen(location);

            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            if (x < location[0] || x > location[0] + searchEditText.getWidth() ||
                    y < location[1] || y > location[1] + searchEditText.getHeight()) {
                toggleSearchEditTextVisibility();
            }
        }
    }

    private void filterBooks(String query) {
        adapter.clearBooks();
        for (Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(query) || book.getAuthor().toLowerCase().contains(query)) {
                adapter.addBook(book);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
