package com.example.librarychecker.firestore;

import androidx.annotation.NonNull;
import com.example.librarychecker.models.Book;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;


public class FirestoreHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Коллекции
    public static final String COLLECTION_BOOKS = "books";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_AVATARS = "profile_avatars";

    // Получение ссылок на коллекции
    public static CollectionReference getBooksCollection() {
        return db.collection(COLLECTION_BOOKS);
    }

    public static CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_USERS);
    }

    public static CollectionReference getAvatarsCollection() {
        return db.collection(COLLECTION_AVATARS);
    }

    // Получение документа по ID
    public static DocumentReference getBookById(String bookId) {
        return getBooksCollection().document(bookId);
    }

    public static DocumentReference getUserById(String userId) {
        return getUsersCollection().document(userId);
    }

    public static DocumentReference getAvatarById(String avatarId) {
        return getAvatarsCollection().document(avatarId);
    }

    public static void getAllBooks(
            @NonNull Consumer<List<Book>> onSuccess,
            @NonNull Consumer<String> onFailure
    ) {
        getBooksCollection().get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> books = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Book book = doc.toObject(Book.class);
                        book.setId(doc.getId());
                        books.add(book);
                    }
                    onSuccess.accept(books);
                })
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }

    public static void checkIfUserIsAdmin(String uid, AdminStatusCallback callback) {
        getUserById(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        callback.onChecked(Boolean.TRUE.equals(isAdmin));
                    } else {
                        callback.onChecked(false);
                    }
                })
                .addOnFailureListener(e -> callback.onChecked(false));
    }

    public static void addBook(Book book,
                               @NonNull Runnable onSuccess,
                               @NonNull Consumer<String> onFailure) {
        getBookById(book.getId()).set(book)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }

    public static void deleteBook(String bookId,
                                  @NonNull Runnable onSuccess,
                                  @NonNull Consumer<String> onFailure) {
        getBookById(bookId).delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }

    public static void clearReservation(String bookId,
                                        @NonNull Runnable onSuccess,
                                        @NonNull Consumer<String> onFailure) {
        getBookById(bookId)
                .update("reservedBy", null, "dueDate", null, "reservationDueDate", null)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }






}
