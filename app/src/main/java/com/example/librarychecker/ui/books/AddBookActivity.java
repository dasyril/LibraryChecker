package com.example.librarychecker.ui.books;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.librarychecker.R;
import com.example.librarychecker.models.Book;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.UUID;

public class AddBookActivity extends AppCompatActivity {

    private EditText titleEdit, authorEdit, genreEdit, descriptionEdit, imageUrlEdit, countEdit;
    private Button addButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        titleEdit = findViewById(R.id.editTitle);
        authorEdit = findViewById(R.id.editAuthor);
        genreEdit = findViewById(R.id.editGenre);
        descriptionEdit = findViewById(R.id.editDescription);
        imageUrlEdit = findViewById(R.id.editImageUrl);
        countEdit = findViewById(R.id.editCount);
        addButton = findViewById(R.id.buttonAddBook);
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        addButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String author = authorEdit.getText().toString().trim();
            String genre = genreEdit.getText().toString().trim();
            String description = descriptionEdit.getText().toString().trim();
            String imageUrl = imageUrlEdit.getText().toString().trim();
            String countStr = countEdit.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(genre)
                    || TextUtils.isEmpty(description) || TextUtils.isEmpty(imageUrl) || TextUtils.isEmpty(countStr)) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            try {
                count = Integer.parseInt(countStr);
                if (count <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное количество экземпляров", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = UUID.randomUUID().toString();
            Book book = new Book(
                    id,
                    title,
                    author,
                    genre,
                    description,
                    imageUrl,
                    count,
                    new ArrayList<>(),  // reservedBy
                    new ArrayList<>(),  // reservedUntil
                    new ArrayList<>()   // reservationDates
            );

            db.collection("books").document(id).set(book)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Книга добавлена", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
