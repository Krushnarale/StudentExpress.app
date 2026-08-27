package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final String COLLECTION_NAME = "users";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    private FirebaseAuth getFirebaseAuth() {
        return FirebaseConfig.getFirebaseAuth();
    }

    @Override
    public Optional<User> findById(String id) {
        return findByUid(id);
    }

    @Override
    public Optional<User> findByUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return Optional.empty();
        }
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(uid.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseUserFromSnapshot(snapshot, uid.trim()));
            }
        } catch (Throwable e) {
            System.err.println("[UserDAOImpl] Error finding user by UID from Firestore: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanEmail = email.trim().toLowerCase();

        // 1. Query Cloud Firestore "users" collection
        Firestore db = getFirestore();
        if (db != null) {
            try {
                ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME).whereEqualTo("email", cleanEmail).get();
                List<QueryDocumentSnapshot> docs = queryFuture.get().getDocuments();
                if (docs != null && !docs.isEmpty()) {
                    return Optional.of(parseUserFromSnapshot(docs.get(0), cleanEmail));
                }

                DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(cleanEmail).get().get();
                if (snapshot != null && snapshot.exists()) {
                    return Optional.of(parseUserFromSnapshot(snapshot, cleanEmail));
                }
            } catch (Throwable e) {
                System.err.println("[UserDAOImpl] Firestore findByEmail error: " + e.getMessage());
            }
        }

        // 2. Fetch directly from Firebase Authentication (FirebaseAuth)
        FirebaseAuth auth = getFirebaseAuth();
        if (auth != null) {
            try {
                UserRecord userRecord = auth.getUserByEmail(cleanEmail);
                if (userRecord != null) {
                    System.out.println("[UserDAOImpl] Fetched user account from Firebase Auth: " + userRecord.getUid() + " | " + userRecord.getEmail());

                    if (db != null) {
                        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(userRecord.getUid()).get().get();
                        if (snapshot != null && snapshot.exists()) {
                            return Optional.of(parseUserFromSnapshot(snapshot, cleanEmail));
                        }
                    }

                    String name = (userRecord.getDisplayName() != null && !userRecord.getDisplayName().isEmpty()) 
                            ? userRecord.getDisplayName() 
                            : cleanEmail.split("@")[0];
                    String phone = userRecord.getPhoneNumber() != null ? userRecord.getPhoneNumber() : "";

                    User authUser = new User(
                        userRecord.getUid(),
                        name,
                        cleanEmail,
                        phone,
                        User.Role.STUDENT,
                        "123456"
                    );
                    save(authUser);
                    return Optional.of(authUser);
                }
            } catch (Throwable e) {
                System.err.println("[UserDAOImpl] Firebase Auth lookup exception: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    private User parseUserFromSnapshot(DocumentSnapshot snapshot, String fallbackKey) {
        String uid = snapshot.getString("uid");
        if (uid == null) uid = snapshot.getString("id");
        if (uid == null) uid = snapshot.getId();
        if (uid == null) uid = fallbackKey;

        String name = snapshot.getString("name");
        String email = snapshot.getString("email");
        String phone = snapshot.getString("phone");
        String college = snapshot.getString("college");
        String branch = snapshot.getString("branch");
        String roleStr = snapshot.getString("role");
        String profileImage = snapshot.getString("profileImage");
        String profilePublicId = snapshot.getString("profilePublicId");
        Long createdAtLong = snapshot.getLong("createdAt");
        Long updatedAtLong = snapshot.getLong("updatedAt");

        User.Role role = User.Role.STUDENT;
        if (roleStr != null) {
            try {
                role = User.Role.valueOf(roleStr);
            } catch (Exception ignored) {}
        }
        String finalEmail = (email != null && !email.trim().isEmpty()) ? email : fallbackKey;
        return new User(
            uid,
            name != null ? name : "User",
            finalEmail,
            phone != null ? phone : "",
            college != null ? college : "COEP Pune",
            branch != null ? branch : "Computer Engineering",
            role,
            "123456",
            profileImage != null ? profileImage : "",
            profilePublicId != null ? profilePublicId : "",
            createdAtLong != null ? createdAtLong : System.currentTimeMillis(),
            updatedAtLong != null ? updatedAtLong : System.currentTimeMillis()
        );
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                String email = doc.getString("email");
                if (email != null) {
                    list.add(parseUserFromSnapshot(doc, email));
                }
            }
        } catch (Throwable e) {
            System.err.println("[UserDAOImpl] Error fetching all users: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(User user) {
        if (user == null || user.getEmail() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        String docId = (user.getUid() != null && !user.getUid().trim().isEmpty()) 
                ? user.getUid().trim() 
                : user.getEmail().trim().toLowerCase();

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("uid", docId);
            docData.put("name", user.getName() != null ? user.getName() : "");
            docData.put("email", user.getEmail().trim().toLowerCase());
            docData.put("phone", user.getPhone() != null ? user.getPhone() : "");
            docData.put("college", user.getCollege() != null ? user.getCollege() : "Campus");
            docData.put("branch", user.getBranch() != null ? user.getBranch() : "General");
            docData.put("role", user.getRole() != null ? user.getRole().name() : User.Role.STUDENT.name());
            docData.put("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
            docData.put("profilePublicId", user.getProfilePublicId() != null ? user.getProfilePublicId() : "");
            docData.put("createdAt", user.getCreatedAt() > 0 ? user.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .set(docData);
            result.get();
            System.out.println("[UserDAOImpl] User profile saved successfully to Firestore doc: " + docId);
            return true;
        } catch (Throwable e) {
            System.err.println("[UserDAOImpl] Failed to save user profile to Firestore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveWithAuth(User user, String password) {
        if (user == null || user.getEmail() == null) return false;

        FirebaseAuth auth = getFirebaseAuth();
        if (auth != null) {
            try {
                UserRecord userRecord = null;
                try {
                    userRecord = auth.getUserByEmail(user.getEmail().trim().toLowerCase());
                } catch (Throwable ignored) {}

                if (userRecord == null) {
                    CreateRequest request = new CreateRequest()
                            .setEmail(user.getEmail().trim().toLowerCase())
                            .setEmailVerified(false)
                            .setPassword(password != null && password.length() >= 6 ? password : "DefaultPassword123")
                            .setDisplayName(user.getName())
                            .setDisabled(false);

                    userRecord = auth.createUser(request);
                    System.out.println("[FirebaseAuth] Created new auth user: " + userRecord.getUid() + " | " + userRecord.getEmail());
                }

                boolean authCreated = false;
                if (userRecord != null) {
                    user.setUid(userRecord.getUid());
                    authCreated = true;
                }

                boolean fsSaved = save(user);
                return authCreated || fsSaved;
            } catch (Throwable e) {
                System.err.println("[FirebaseAuth] Firebase Auth exception: " + e.getMessage());
            }
        }

        return save(user);
    }

    @Override
    public boolean delete(String id) {
        if (id == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            db.collection(COLLECTION_NAME).document(id.trim()).delete().get();
            return true;
        } catch (Throwable e) {
            System.err.println("[UserDAOImpl] Error deleting user: " + e.getMessage());
            return false;
        }
    }
}
