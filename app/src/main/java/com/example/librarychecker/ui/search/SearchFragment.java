package com.example.librarychecker.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.librarychecker.R;
import com.example.librarychecker.firestore.FirestoreHelper;
import com.example.librarychecker.models.Book;
import com.example.librarychecker.ui.books.BookAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private BookAdapter bookAdapter;
    private List<Book> allBooks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookAdapter = new BookAdapter(new ArrayList<>(), null);
        searchRecyclerView.setAdapter(bookAdapter);

        loadBooksFromFirestore();

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        return view;
    }

    private void loadBooksFromFirestore() {
        FirestoreHelper.getAllBooks(books -> {
            allBooks.clear();
            allBooks.addAll(books);
            bookAdapter.updateBooks(allBooks);
        }, error -> {
            // Ошибка загрузки книг
        });
    }

    private void filterBooks(String query) {
        List<Book> filtered = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                    book.getGenre().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(book);
            }
        }
        bookAdapter.updateBooks(filtered);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooksFromFirestore(); // Обновляем список после возвращения
    }
}