package com.example.librarychecker.ui.books;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.librarychecker.R;
import com.example.librarychecker.databinding.ActivityBookDetailBinding;
import com.example.librarychecker.firestore.FirestoreHelper;
import com.example.librarychecker.models.Book;
import com.example.librarychecker.models.User;
import com.example.librarychecker.notifications.ReturnReminderReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private Book book;
    private String currentUserId;
    private boolean isAdmin = false;
    private ActivityBookDetailBinding binding;
    private List<User> reservedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        book = (Book) getIntent().getSerializableExtra("book");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        displayBookDetails();
        checkAdminStatus();

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void displayBookDetails() {
        binding.titleTextView.setText(book.getTitle());
        binding.authorTextView.setText("Автор: " + book.getAuthor());
        binding.genreTextView.setText("Жанр: " + book.getGenre());
        binding.descriptionTextView.setText("Описание: " + book.getDescription());
        updateAvailableCount();

        Glide.with(this)
                .load(book.getImageUrl())
                .placeholder(R.drawable.book_placeholder)
                .into(binding.coverImageView);

        updateActionButton();
    }

    private void updateAvailableCount() {
        List<String> reservedBy = book.getReservedBy();
        if (reservedBy == null) reservedBy = new ArrayList<>();
        int availableCount = book.getCount() - reservedBy.size();
        binding.countTextView.setText("Доступно: " + availableCount);

        if (isAdmin && reservedBy != null && !reservedBy.isEmpty()) {
            binding.countTextView.append(" (Забронирована)");
        } else if (isAdmin) {
            binding.countTextView.append(" (Не забронирована)");
        }
    }

    private void updateActionButton() {
        List<String> reservedBy = book.getReservedBy();
        if (reservedBy == null) reservedBy = new ArrayList<>();
        int availableCount = book.getCount() - reservedBy.size();

        if (isAdmin) {
            binding.actionButton.setText("Снять бронь");
            binding.actionButton.setEnabled(true);
            binding.actionButton.setOnClickListener(v -> showReservedUsers());
        } else if (reservedBy.contains(currentUserId)) {
            binding.actionButton.setText("Снять бронь");
            binding.actionButton.setEnabled(true);
            binding.actionButton.setOnClickListener(v -> cancelReservation());
        } else if (availableCount > 0) {
            binding.actionButton.setText("Забронировать");
            binding.actionButton.setEnabled(true);
            binding.actionButton.setOnClickListener(v -> reserveBook());
        } else {
            binding.actionButton.setText("Нет свободных экземпляров");
            binding.actionButton.setEnabled(false);
        }
    }

    private void reserveBook() {
        List<String> reservedBy = book.getReservedBy();
        List<String> reservationDates = book.getReservationDates();

        reservedBy.add(currentUserId);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reservationDates.add(currentDate);

        FirebaseFirestore.getInstance().collection("books")
                .document(book.getId())
                .update("reservedBy", reservedBy, "reservationDates", reservationDates)
                .addOnSuccessListener(unused -> {
                    FirebaseFirestore.getInstance().collection("users")
                            .document(currentUserId)
                            .update("reservedBooks", com.google.firebase.firestore.FieldValue.arrayUnion(book.getId()))
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Книга забронирована", Toast.LENGTH_SHORT).show();
                                updateAvailableCount();
                                updateActionButton();

                                Calendar returnDate = Calendar.getInstance();
                                returnDate.add(Calendar.DAY_OF_YEAR, 7); // Срок возврата через 7 дней
                                scheduleReturnReminder(book.getTitle(), returnDate.getTime());
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelReservation() {
        List<String> reservedBy = book.getReservedBy();
        List<String> reservationDates = book.getReservationDates();
        int index = reservedBy.indexOf(currentUserId);
        if (index != -1) {
            reservedBy.remove(index);
            reservationDates.remove(index);
            FirebaseFirestore.getInstance().collection("books")
                    .document(book.getId())
                    .update("reservedBy", reservedBy, "reservationDates", reservationDates)
                    .addOnSuccessListener(unused -> {
                        FirebaseFirestore.getInstance().collection("users")
                                .document(currentUserId)
                                .update("reservedBooks", com.google.firebase.firestore.FieldValue.arrayRemove(book.getId()))
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(this, "Бронь снята", Toast.LENGTH_SHORT).show();
                                    updateAvailableCount();
                                    updateActionButton();
                                    finish(); // Возвращаемся на предыдущий экран
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления книги: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Ошибка: Вы не забронировали эту книгу", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkAdminStatus() {
        FirestoreHelper.checkIfUserIsAdmin(currentUserId, isAdmin -> {
            this.isAdmin = isAdmin;
            updateAvailableCount();
            updateActionButton();
        });
    }

    private void showReservedUsers() {
        List<String> reservedBy = book.getReservedBy();
        if (reservedBy == null || reservedBy.isEmpty()) {
            Toast.makeText(this, "Эта книга никем не забронирована", Toast.LENGTH_SHORT).show();
            return;
        }

        reservedUsers.clear();
        binding.reservedByContainer.removeAllViews();
        binding.reservedByContainer.setVisibility(View.VISIBLE);

        for (String userId : reservedBy) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                reservedUsers.add(user);
                                int index = reservedBy.indexOf(userId);
                                String reservationDate = (index >= 0 && index < book.getReservationDates().size()) ? book.getReservationDates().get(index) : "Неизвестно";
                                addUserToList(user, userId, reservationDate);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void addUserToList(User user, String userId, String reservationDate) {
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(8, 8, 8, 8);

        TextView userText = new TextView(this);
        userText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        userText.setText(user.getName() + " " + user.getSurname() + " (Забронировано: " + reservationDate + ")");
        userLayout.addView(userText);

        Button removeButton = new Button(this);
        removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        removeButton.setText("Снять");
        removeButton.setOnClickListener(v -> confirmRemoveReservation(userId, user.getName() + " " + user.getSurname(), reservationDate));
        userLayout.addView(removeButton);

        binding.reservedByContainer.addView(userLayout);
    }

    private void confirmRemoveReservation(String userId, String userName, String reservationDate) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите снять бронь книги \"" + book.getTitle() + "\" у пользователя " + userName + "? Забронировано: " + reservationDate)
                .setPositiveButton("Да", (dialog, which) -> removeUserReservation(userId))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void removeUserReservation(String userId) {
        List<String> reservedBy = book.getReservedBy();
        List<String> reservationDates = book.getReservationDates();
        int index = reservedBy.indexOf(userId);
        if (index != -1 && index < reservationDates.size()) {
            reservedBy.remove(index);
            reservationDates.remove(index);
            FirebaseFirestore.getInstance().collection("books")
                    .document(book.getId())
                    .update("reservedBy", reservedBy, "reservationDates", reservationDates)
                    .addOnSuccessListener(unused -> {
                        FirebaseFirestore.getInstance().collection("users")
                                .document(userId)
                                .update("reservedBooks", com.google.firebase.firestore.FieldValue.arrayRemove(book.getId()))
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(this, "Бронь снята", Toast.LENGTH_SHORT).show();
                                    updateAvailableCount();
                                    updateActionButton();
                                    showReservedUsers();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления книги: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Ошибка: Пользователь не найден в списке забронировавших", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleReturnReminder(String bookTitle, Date returnDate) {
        long triggerTime = System.currentTimeMillis() + 10 * 1000;

        /*
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTime(returnDate);
        reminderTime.add(Calendar.DAY_OF_YEAR, -2); // Напоминание за 2 дня до возврата
        long triggerTime = reminderTime.getTimeInMillis();
        */

        Intent intent = new Intent(this, ReturnReminderReceiver.class);
        intent.putExtra("bookTitle", bookTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                bookTitle.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }
}