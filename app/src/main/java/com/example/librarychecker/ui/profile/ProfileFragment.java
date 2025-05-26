package com.example.librarychecker.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.librarychecker.LoginActivity;
import com.example.librarychecker.databinding.FragmentProfileBinding;
import com.example.librarychecker.firestore.FirestoreHelper;
import com.example.librarychecker.models.Book;
import com.example.librarychecker.models.User;
import com.example.librarychecker.ui.books.BookAdapter;
import com.example.librarychecker.ui.books.BookDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private BookAdapter adapter;
    private List<Book> reservedBooks = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookAdapter(reservedBooks, book -> {
            Intent intent = new Intent(getContext(), BookDetailActivity.class);
            intent.putExtra("book", book);
            startActivity(intent);
        });
        binding.recyclerView.setAdapter(adapter);

        loadUserInfo();
        FirestoreHelper.checkIfUserIsAdmin(currentUserId, isAdmin -> {
            if (isAdmin) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.reservedBooksTitle.setVisibility(View.GONE);
            } else {
                loadReservedBooks();
            }
        });

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    private void loadUserInfo() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            binding.nameTextView.setText(user.getName() + " " + user.getSurname());
                            binding.emailTextView.setText(user.getEmail());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show());
    }

    private void loadReservedBooks() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null && user.getReservedBooks() != null && !user.getReservedBooks().isEmpty()) {
                            reservedBooks.clear();
                            binding.reservedBooksTitle.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            for (String bookId : user.getReservedBooks()) {
                                db.collection("books")
                                        .document(bookId)
                                        .get()
                                        .addOnSuccessListener(bookDoc -> {
                                            if (bookDoc.exists()) {
                                                Book book = bookDoc.toObject(Book.class);
                                                book.setId(bookId);
                                                List<String> reservedBy = book.getReservedBy();
                                                List<String> reservationDates = book.getReservationDates();
                                                int index = reservedBy != null ? reservedBy.indexOf(currentUserId) : -1;
                                                if (index >= 0 && index < reservationDates.size()) {
                                                    book.setDescription("Забронировано: " + reservationDates.get(index));
                                                } else {
                                                    book.setDescription("Дата неизвестна");
                                                }
                                                reservedBooks.add(book);
                                                adapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка загрузки книги: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            binding.reservedBooksTitle.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "У вас нет забронированных книг", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка загрузки профиля: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}