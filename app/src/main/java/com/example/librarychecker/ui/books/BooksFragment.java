package com.example.librarychecker.ui.books;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.librarychecker.databinding.FragmentBooksBinding;
import com.example.librarychecker.firestore.FirestoreHelper;
import com.example.librarychecker.models.Book;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BooksFragment extends Fragment {

    private FragmentBooksBinding binding;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBooksBinding.inflate(inflater, container, false);
        setupRecyclerView();
        loadBooksFromFirestore();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirestoreHelper.checkIfUserIsAdmin(currentUser.getUid(), isAdmin -> {
                if (isAdmin) {
                    binding.addBookButton.setVisibility(View.VISIBLE);
                    binding.addBookButton.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), AddBookActivity.class);
                        startActivity(intent);
                    });
                }
            });
        }

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(bookList, book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra("book", book);
            startActivity(intent);
        });
        binding.booksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.booksRecyclerView.setAdapter(bookAdapter);
    }

    private void loadBooksFromFirestore() {
        FirestoreHelper.getAllBooks(
                books -> {
                    bookList.clear();
                    bookList.addAll(books);
                    bookAdapter.updateBooks(bookList);
                    binding.progressBar.setVisibility(View.GONE);
                },
                errorMessage -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Ошибка загрузки: " + errorMessage, Toast.LENGTH_LONG).show();
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooksFromFirestore(); // Обновляем список после возвращения
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}