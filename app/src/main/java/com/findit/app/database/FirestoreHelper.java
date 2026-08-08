package com.findit.app.database;

import com.findit.app.models.Claim;
import com.findit.app.models.Item;
import com.findit.app.models.NotificationItem;
import com.findit.app.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirestoreHelper {

    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final CollectionReference itemsRef;
    private final CollectionReference claimsRef;
    private final CollectionReference notificationsRef;

    public interface Callback<T> {
        void onSuccess(T result);

        void onFailure(Exception e);
    }

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        itemsRef = db.collection("items");
        claimsRef = db.collection("claims");
        notificationsRef = db.collection("notifications");
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // ==========================================
    // USER & PROFILE OPERATIONS
    // ==========================================

    public void registerUser(User user, Callback<String> callback) {
        user.setCreatedAt(getCurrentTimestamp());
        user.setStatus("ACTIVE");
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        usersRef.add(user)
                .addOnSuccessListener(documentReference -> {
                    user.setId(documentReference.getId());
                    // Update document with ID
                    documentReference.set(user).addOnSuccessListener(aVoid -> callback.onSuccess(user.getId()));
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void checkLogin(String email, String password, Callback<User> callback) {
        usersRef.whereEqualTo("email", email).whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateProfile(String userId, String fullName, String email, String phone, Callback<Boolean> callback) {
        usersRef.document(userId)
                .update("fullName", fullName, "email", email, "phone", phone)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void changePassword(String userId, String oldPassword, String newPassword, Callback<Boolean> callback) {
        usersRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.getPassword().equals(oldPassword)) {
                usersRef.document(userId).update("password", newPassword)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                        .addOnFailureListener(e -> callback.onSuccess(false));
            } else {
                callback.onSuccess(false);
            }
        }).addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void getUserById(String userId, Callback<User> callback) {
        usersRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot.toObject(User.class)))
                .addOnFailureListener(callback::onFailure);
    }

    // ==========================================
    // ITEM REPORTING & SEARCH
    // ==========================================

    public void insertItem(Item item, Callback<String> callback) {
        item.setCreatedAt(getCurrentTimestamp());
        item.setStatus(item.getStatus() != null ? item.getStatus() : "VERIFIED");

        itemsRef.add(item)
                .addOnSuccessListener(documentReference -> {
                    item.setId(documentReference.getId());
                    documentReference.set(item).addOnSuccessListener(aVoid -> {
                        callback.onSuccess(item.getId());
                        checkAndGenerateMatchAlerts(item);
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void markItemAsRecovered(String itemId, Callback<Boolean> callback) {
        itemsRef.document(itemId).update("status", "RECOVERED")
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(true);
                    getItemById(itemId, new Callback<Item>() {
                        @Override
                        public void onSuccess(Item item) {
                            if (item != null) {
                                addNotification(item.getUserId(), "Item Recovered",
                                        "Your item '" + item.getTitle() + "' has been marked as recovered!",
                                        "ITEM_RECOVERED", new Callback<String>() {
                                            @Override
                                            public void onSuccess(String result) {
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void getItemById(String itemId, Callback<Item> callback) {
        itemsRef.document(itemId).get().addOnSuccessListener(snapshot -> {
            Item item = snapshot.toObject(Item.class);
            if (item != null && item.getUserId() != null) {
                // Populate user name and phone
                getUserById(item.getUserId(), new Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            item.setUserName(user.getFullName());
                            item.setUserPhone(user.getPhone());
                        }
                        callback.onSuccess(item);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onSuccess(item);
                    }
                });
            } else {
                callback.onSuccess(item);
            }
        }).addOnFailureListener(callback::onFailure);
    }

    public void getItemsByType(String type, Callback<List<Item>> callback) {
        searchAndFilterItems("", "All", "", "", type, "VERIFIED", "Newest", callback);
    }

    public void getItemsByUserId(String userId, Callback<List<Item>> callback) {
        itemsRef.whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Item> items = populateUsersForItems(queryDocumentSnapshots.toObjects(Item.class), callback);
        }).addOnFailureListener(callback::onFailure);
    }

    public void searchAndFilterItems(String keyword, String category, String location, String date, String type,
            String statusFilter, String sortBy, Callback<List<Item>> callback) {
        Query query = itemsRef;

        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            query = query.whereEqualTo("type", type);
        }
        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }
        if (date != null && !date.trim().isEmpty()) {
            query = query.whereEqualTo("date", date.trim());
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Item> results = queryDocumentSnapshots.toObjects(Item.class);
            List<Item> filtered = new ArrayList<>();
            for (Item item : results) {
                boolean match = true;
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.toLowerCase();
                    if (!(item.getTitle() != null && item.getTitle().toLowerCase().contains(kw)) &&
                            !(item.getDescription() != null && item.getDescription().toLowerCase().contains(kw))) {
                        match = false;
                    }
                }
                if (location != null && !location.trim().isEmpty()) {
                    if (item.getLocation() == null
                            || !item.getLocation().toLowerCase().contains(location.toLowerCase())) {
                        match = false;
                    }
                }
                if (match)
                    filtered.add(item);
            }
            populateUsersForItems(filtered, callback);
        }).addOnFailureListener(callback::onFailure);
    }

    private List<Item> populateUsersForItems(List<Item> items, Callback<List<Item>> callback) {
        if (items.isEmpty()) {
            callback.onSuccess(items);
            return items;
        }
        // In a real app we'd batch fetch, but here for simplicity we just fetch in a
        // loop.
        int[] count = { 0 };
        for (Item item : items) {
            if (item.getUserId() != null) {
                getUserById(item.getUserId(), new Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            item.setUserName(user.getFullName());
                            item.setUserPhone(user.getPhone());
                        }
                        count[0]++;
                        if (count[0] == items.size())
                            callback.onSuccess(items);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        count[0]++;
                        if (count[0] == items.size())
                            callback.onSuccess(items);
                    }
                });
            } else {
                count[0]++;
                if (count[0] == items.size())
                    callback.onSuccess(items);
            }
        }
        return items;
    }

    // ==========================================
    // CLAIMS & NOTIFICATIONS
    // ==========================================

    public void submitClaim(Claim claim, Callback<String> callback) {
        claim.setStatus("PENDING");
        claim.setAdminNotes("");
        claim.setCreatedAt(getCurrentTimestamp());
        claimsRef.add(claim).addOnSuccessListener(documentReference -> {
            claim.setId(documentReference.getId());
            documentReference.set(claim).addOnSuccessListener(aVoid -> callback.onSuccess(claim.getId()));
        }).addOnFailureListener(callback::onFailure);
    }

    public void getClaimsByUserId(String userId, Callback<List<Claim>> callback) {
        claimsRef.whereEqualTo("claimantId", userId).get().addOnSuccessListener(snapshots -> {
            List<Claim> claims = snapshots.toObjects(Claim.class);
            populateFieldsForClaims(claims, callback);
        }).addOnFailureListener(callback::onFailure);
    }

    public void getAllClaims(Callback<List<Claim>> callback) {
        claimsRef.get().addOnSuccessListener(snapshots -> {
            List<Claim> claims = snapshots.toObjects(Claim.class);
            populateFieldsForClaims(claims, callback);
        }).addOnFailureListener(callback::onFailure);
    }

    public void updateClaimStatus(String claimId, String status, String adminNotes, Callback<Boolean> callback) {
        claimsRef.document(claimId).update("status", status, "adminNotes", adminNotes).addOnSuccessListener(aVoid -> {
            callback.onSuccess(true);
            getClaimById(claimId, new Callback<Claim>() {
                @Override
                public void onSuccess(Claim claim) {
                    if (claim != null) {
                        String type = "APPROVED".equalsIgnoreCase(status) ? "CLAIM_APPROVED" : "CLAIM_REJECTED";
                        addNotification(claim.getClaimantId(), "Claim " + status,
                                "Your claim for '" + claim.getItemTitle() + "' was " + status.toLowerCase()
                                        + ". Notes: " + adminNotes,
                                type, new Callback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                    }
                                });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }).addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void getClaimById(String claimId, Callback<Claim> callback) {
        claimsRef.document(claimId).get().addOnSuccessListener(snapshot -> {
            Claim claim = snapshot.toObject(Claim.class);
            List<Claim> list = new ArrayList<>();
            if (claim != null)
                list.add(claim);
            populateFieldsForClaims(list, new Callback<List<Claim>>() {
                @Override
                public void onSuccess(List<Claim> result) {
                    if (result.isEmpty())
                        callback.onSuccess(null);
                    else
                        callback.onSuccess(result.get(0));
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }).addOnFailureListener(callback::onFailure);
    }

    private void populateFieldsForClaims(List<Claim> claims, Callback<List<Claim>> callback) {
        if (claims.isEmpty()) {
            callback.onSuccess(claims);
            return;
        }
        int[] count = { 0 };
        for (Claim c : claims) {
            getItemById(c.getItemId(), new Callback<Item>() {
                @Override
                public void onSuccess(Item item) {
                    if (item != null)
                        c.setItemTitle(item.getTitle());
                    getUserById(c.getClaimantId(), new Callback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user != null)
                                c.setClaimantName(user.getFullName());
                            count[0]++;
                            if (count[0] == claims.size())
                                callback.onSuccess(claims);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            count[0]++;
                            if (count[0] == claims.size())
                                callback.onSuccess(claims);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    count[0]++;
                    if (count[0] == claims.size())
                        callback.onSuccess(claims);
                }
            });
        }
    }

    // Notifications CRUD
    public void addNotification(String userId, String title, String message, String type, Callback<String> callback) {
        NotificationItem n = new NotificationItem();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        n.setCreatedAt(getCurrentTimestamp());
        notificationsRef.add(n).addOnSuccessListener(doc -> {
            n.setId(doc.getId());
            doc.set(n);
            callback.onSuccess(doc.getId());
        }).addOnFailureListener(callback::onFailure);
    }

    public void getUserNotifications(String userId, Callback<List<NotificationItem>> callback) {
        notificationsRef.whereEqualTo("userId", userId).get().addOnSuccessListener(snapshots -> {
            callback.onSuccess(snapshots.toObjects(NotificationItem.class));
        }).addOnFailureListener(callback::onFailure);
    }

    private void checkAndGenerateMatchAlerts(Item newItem) {
        String targetType = "LOST".equalsIgnoreCase(newItem.getType()) ? "FOUND" : "LOST";
        searchAndFilterItems("", newItem.getCategory(), "", "", targetType, "VERIFIED", "Newest",
                new Callback<List<Item>>() {
                    @Override
                    public void onSuccess(List<Item> candidates) {
                        for (Item candidate : candidates) {
                            if (!candidate.getUserId().equals(newItem.getUserId())) {
                                String msg = "A potential matching " + newItem.getType().toLowerCase() + " item ('"
                                        + newItem.getTitle() + "') was reported in category " + newItem.getCategory()
                                        + ". Check it out!";
                                addNotification(candidate.getUserId(), "Matching Item Alert", msg, "MATCH_ALERT",
                                        new Callback<String>() {
                                            @Override
                                            public void onSuccess(String result) {
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }

    // ==========================================
    // ADMIN PANEL & REPORTS
    // ==========================================

    public void getAllUsers(Callback<List<User>> callback) {
        usersRef.get().addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.toObjects(User.class)))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserStatus(String userId, String status, Callback<Boolean> callback) {
        usersRef.document(userId).update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void getPendingItems(Callback<List<Item>> callback) {
        searchAndFilterItems("", "All", "", "", "ALL", "PENDING", "Newest", callback);
    }

    public void updateItemStatus(String itemId, String status, Callback<Boolean> callback) {
        itemsRef.document(itemId).update("status", status).addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void deleteItem(String itemId, Callback<Boolean> callback) {
        itemsRef.document(itemId).delete().addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onSuccess(false));
    }

    public void getTotalUsers(Callback<Integer> callback) {
        usersRef.get().addOnSuccessListener(s -> callback.onSuccess(s.size()));
    }

    public void getTotalLostItems(Callback<Integer> callback) {
        itemsRef.whereEqualTo("type", "LOST").get().addOnSuccessListener(s -> callback.onSuccess(s.size()));
    }

    public void getTotalFoundItems(Callback<Integer> callback) {
        itemsRef.whereEqualTo("type", "FOUND").get().addOnSuccessListener(s -> callback.onSuccess(s.size()));
    }

    public void getRecoveredItemsCount(Callback<Integer> callback) {
        itemsRef.whereEqualTo("status", "RECOVERED").get().addOnSuccessListener(s -> callback.onSuccess(s.size()));
    }

    public void getPendingClaimsCount(Callback<Integer> callback) {
        claimsRef.whereEqualTo("status", "PENDING").get().addOnSuccessListener(s -> callback.onSuccess(s.size()));
    }
}
