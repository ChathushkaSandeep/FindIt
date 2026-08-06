package com.findit.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.findit.app.models.Claim;
import com.findit.app.models.Item;
import com.findit.app.models.NotificationItem;
import com.findit.app.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FindItDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FindItDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_CLAIMS = "claims";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // USERS Table Columns
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_STATUS = "status";

    // ITEMS Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_DATE = "date";
    private static final String KEY_TYPE = "type"; // "LOST", "FOUND"
    private static final String KEY_ITEM_STATUS = "status"; // "PENDING", "VERIFIED", "RECOVERED", "REJECTED"
    private static final String KEY_IMAGE_URI = "image_uri";

    // CLAIMS Table Columns
    private static final String KEY_ITEM_ID = "item_id";
    private static final String KEY_CLAIMANT_ID = "claimant_id";
    private static final String KEY_PROOF_DESC = "proof_description";
    private static final String KEY_PROOF_IMAGE = "proof_image_uri";
    private static final String KEY_CLAIM_STATUS = "status"; // "PENDING", "APPROVED", "REJECTED"
    private static final String KEY_ADMIN_NOTES = "admin_notes";

    // NOTIFICATIONS Table Columns
    private static final String KEY_NOTIF_USER_ID = "user_id";
    private static final String KEY_NOTIF_TITLE = "title";
    private static final String KEY_NOTIF_MSG = "message";
    private static final String KEY_NOTIF_TYPE = "type";
    private static final String KEY_NOTIF_IS_READ = "is_read";

    public FindItDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_FULL_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PHONE + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_ROLE + " TEXT,"
                + KEY_USER_STATUS + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";

        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_ITEM_STATUS + " TEXT,"
                + KEY_IMAGE_URI + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";

        String CREATE_CLAIMS_TABLE = "CREATE TABLE " + TABLE_CLAIMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ITEM_ID + " INTEGER,"
                + KEY_CLAIMANT_ID + " INTEGER,"
                + KEY_PROOF_DESC + " TEXT,"
                + KEY_PROOF_IMAGE + " TEXT,"
                + KEY_CLAIM_STATUS + " TEXT,"
                + KEY_ADMIN_NOTES + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";

        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NOTIF_USER_ID + " INTEGER,"
                + KEY_NOTIF_TITLE + " TEXT,"
                + KEY_NOTIF_MSG + " TEXT,"
                + KEY_NOTIF_TYPE + " TEXT,"
                + KEY_NOTIF_IS_READ + " INTEGER,"
                + KEY_CREATED_AT + " TEXT" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
        db.execSQL(CREATE_CLAIMS_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLAIMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void seedDefaultData(SQLiteDatabase db) {
        // Seed Admin Account
        ContentValues adminValues = new ContentValues();
        adminValues.put(KEY_FULL_NAME, "System Admin");
        adminValues.put(KEY_EMAIL, "admin@findit.com");
        adminValues.put(KEY_PHONE, "0771234567");
        adminValues.put(KEY_PASSWORD, "admin123");
        adminValues.put(KEY_ROLE, "ADMIN");
        adminValues.put(KEY_USER_STATUS, "ACTIVE");
        adminValues.put(KEY_CREATED_AT, getCurrentTimestamp());
        db.insert(TABLE_USERS, null, adminValues);

        // Seed Sample User
        ContentValues userValues = new ContentValues();
        userValues.put(KEY_FULL_NAME, "John Doe");
        userValues.put(KEY_EMAIL, "john@gmail.com");
        userValues.put(KEY_PHONE, "0719876543");
        userValues.put(KEY_PASSWORD, "user123");
        userValues.put(KEY_ROLE, "USER");
        userValues.put(KEY_USER_STATUS, "ACTIVE");
        userValues.put(KEY_CREATED_AT, getCurrentTimestamp());
        long johnId = db.insert(TABLE_USERS, null, userValues);

        // Seed Sample Lost & Found Items
        ContentValues item1 = new ContentValues();
        item1.put(KEY_USER_ID, johnId);
        item1.put(KEY_TITLE, "Black Leather Wallet");
        item1.put(KEY_DESCRIPTION, "Lost near Main Library entrance. Contains ID card and drivers license.");
        item1.put(KEY_CATEGORY, "Electronics & Accessories");
        item1.put(KEY_LOCATION, "Main Library");
        item1.put(KEY_DATE, "2026-08-01");
        item1.put(KEY_TYPE, "LOST");
        item1.put(KEY_ITEM_STATUS, "VERIFIED");
        item1.put(KEY_IMAGE_URI, "");
        item1.put(KEY_CREATED_AT, getCurrentTimestamp());
        db.insert(TABLE_ITEMS, null, item1);

        ContentValues item2 = new ContentValues();
        item2.put(KEY_USER_ID, johnId);
        item2.put(KEY_TITLE, "Silver Apple Watch Series 7");
        item2.put(KEY_DESCRIPTION, "Found on bench in Student Cafeteria. Black sport band.");
        item2.put(KEY_CATEGORY, "Electronics & Accessories");
        item2.put(KEY_LOCATION, "Student Cafeteria");
        item2.put(KEY_DATE, "2026-08-03");
        item2.put(KEY_TYPE, "FOUND");
        item2.put(KEY_ITEM_STATUS, "VERIFIED");
        item2.put(KEY_IMAGE_URI, "");
        item2.put(KEY_CREATED_AT, getCurrentTimestamp());
        db.insert(TABLE_ITEMS, null, item2);
    }

    // ==========================================
    // MEMBER 1: USER & PROFILE OPERATIONS
    // ==========================================

    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FULL_NAME, user.getFullName());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_PHONE, user.getPhone());
        values.put(KEY_PASSWORD, user.getPassword());
        values.put(KEY_ROLE, user.getRole() != null ? user.getRole() : "USER");
        values.put(KEY_USER_STATUS, "ACTIVE");
        values.put(KEY_CREATED_AT, getCurrentTimestamp());
        return db.insert(TABLE_USERS, null, values);
    }

    public User checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_EMAIL + "=? AND " + KEY_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public boolean updateProfile(int userId, String fullName, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FULL_NAME, fullName);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PHONE, phone);
        int rows = db.update(TABLE_USERS, values, KEY_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID},
                KEY_ID + "=? AND " + KEY_PASSWORD + "=?",
                new String[]{String.valueOf(userId), oldPassword}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(KEY_PASSWORD, newPassword);
            return db.update(TABLE_USERS, values, KEY_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        User u = new User();
        u.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        u.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FULL_NAME)));
        u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)));
        u.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)));
        u.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)));
        u.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)));
        u.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STATUS)));
        u.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
        return u;
    }

    // ==========================================
    // MEMBER 1 & 2: ITEM REPORTING & SEARCH
    // ==========================================

    public long insertItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, item.getUserId());
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_LOCATION, item.getLocation());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_TYPE, item.getType());
        values.put(KEY_ITEM_STATUS, item.getStatus() != null ? item.getStatus() : "VERIFIED");
        values.put(KEY_IMAGE_URI, item.getImageUri());
        values.put(KEY_CREATED_AT, getCurrentTimestamp());

        long itemId = db.insert(TABLE_ITEMS, null, values);

        // Check potential match alerts
        if (itemId > 0) {
            checkAndGenerateMatchAlerts(item);
        }

        return itemId;
    }

    public boolean markItemAsRecovered(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_STATUS, "RECOVERED");
        int rows = db.update(TABLE_ITEMS, values, KEY_ID + "=?", new String[]{String.valueOf(itemId)});
        
        if (rows > 0) {
            Item item = getItemById(itemId);
            if (item != null) {
                addNotification(item.getUserId(), "Item Recovered",
                        "Your item '" + item.getTitle() + "' has been marked as recovered!", "ITEM_RECOVERED");
            }
        }
        return rows > 0;
    }

    public Item getItemById(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT i.*, u." + KEY_FULL_NAME + " as user_name, u." + KEY_PHONE + " as user_phone FROM "
                + TABLE_ITEMS + " i LEFT JOIN " + TABLE_USERS + " u ON i." + KEY_USER_ID + " = u." + KEY_ID
                + " WHERE i." + KEY_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(itemId)});
        Item item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = cursorToItem(cursor);
            cursor.close();
        }
        return item;
    }

    public List<Item> getItemsByType(String type) {
        return searchAndFilterItems("", "All", "", "", type, "VERIFIED", "Newest");
    }

    public List<Item> getItemsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> itemList = new ArrayList<>();
        String query = "SELECT i.*, u." + KEY_FULL_NAME + " as user_name, u." + KEY_PHONE + " as user_phone FROM "
                + TABLE_ITEMS + " i LEFT JOIN " + TABLE_USERS + " u ON i." + KEY_USER_ID + " = u." + KEY_ID
                + " WHERE i." + KEY_USER_ID + "=? ORDER BY i." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                itemList.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return itemList;
    }

    public List<Item> searchAndFilterItems(String keyword, String category, String location, String date, String type, String statusFilter, String sortBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> list = new ArrayList<>();

        StringBuilder query = new StringBuilder("SELECT i.*, u." + KEY_FULL_NAME + " as user_name, u." + KEY_PHONE + " as user_phone FROM "
                + TABLE_ITEMS + " i LEFT JOIN " + TABLE_USERS + " u ON i." + KEY_USER_ID + " = u." + KEY_ID + " WHERE 1=1 ");

        List<String> argsList = new ArrayList<>();

        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            query.append("AND i.").append(KEY_TYPE).append("=? ");
            argsList.add(type);
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            query.append("AND i.").append(KEY_ITEM_STATUS).append("=? ");
            argsList.add(statusFilter);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND (i.").append(KEY_TITLE).append(" LIKE ? OR i.").append(KEY_DESCRIPTION).append(" LIKE ?) ");
            String wild = "%" + keyword.trim() + "%";
            argsList.add(wild);
            argsList.add(wild);
        }

        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query.append("AND i.").append(KEY_CATEGORY).append("=? ");
            argsList.add(category);
        }

        if (location != null && !location.trim().isEmpty()) {
            query.append("AND i.").append(KEY_LOCATION).append(" LIKE ? ");
            argsList.add("%" + location.trim() + "%");
        }

        if (date != null && !date.trim().isEmpty()) {
            query.append("AND i.").append(KEY_DATE).append("=? ");
            argsList.add(date.trim());
        }

        if ("Oldest".equalsIgnoreCase(sortBy)) {
            query.append("ORDER BY i.").append(KEY_ID).append(" ASC");
        } else if ("Name A-Z".equalsIgnoreCase(sortBy)) {
            query.append("ORDER BY i.").append(KEY_TITLE).append(" ASC");
        } else {
            query.append("ORDER BY i.").append(KEY_ID).append(" DESC");
        }

        Cursor cursor = db.rawQuery(query.toString(), argsList.toArray(new String[0]));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private Item cursorToItem(Cursor cursor) {
        Item item = new Item();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        item.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY)));
        item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)));
        item.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
        item.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
        item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_STATUS)));
        item.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URI)));
        item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));

        try {
            item.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
            item.setUserPhone(cursor.getString(cursor.getColumnIndexOrThrow("user_phone")));
        } catch (Exception ignored) {}

        return item;
    }

    // ==========================================
    // MEMBER 3: CLAIMS & NOTIFICATIONS
    // ==========================================

    public long submitClaim(Claim claim) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_ID, claim.getItemId());
        values.put(KEY_CLAIMANT_ID, claim.getClaimantId());
        values.put(KEY_PROOF_DESC, claim.getProofDescription());
        values.put(KEY_PROOF_IMAGE, claim.getProofImageUri());
        values.put(KEY_CLAIM_STATUS, "PENDING");
        values.put(KEY_ADMIN_NOTES, "");
        values.put(KEY_CREATED_AT, getCurrentTimestamp());
        return db.insert(TABLE_CLAIMS, null, values);
    }

    public List<Claim> getClaimsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Claim> list = new ArrayList<>();
        String query = "SELECT c.*, i." + KEY_TITLE + " as item_title FROM "
                + TABLE_CLAIMS + " c LEFT JOIN " + TABLE_ITEMS + " i ON c." + KEY_ITEM_ID + " = i." + KEY_ID
                + " WHERE c." + KEY_CLAIMANT_ID + "=? ORDER BY c." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToClaim(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Claim> getAllClaims() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Claim> list = new ArrayList<>();
        String query = "SELECT c.*, i." + KEY_TITLE + " as item_title, u." + KEY_FULL_NAME + " as claimant_name FROM "
                + TABLE_CLAIMS + " c LEFT JOIN " + TABLE_ITEMS + " i ON c." + KEY_ITEM_ID + " = i." + KEY_ID
                + " LEFT JOIN " + TABLE_USERS + " u ON c." + KEY_CLAIMANT_ID + " = u." + KEY_ID
                + " ORDER BY c." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToClaim(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean updateClaimStatus(int claimId, String status, String adminNotes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CLAIM_STATUS, status);
        values.put(KEY_ADMIN_NOTES, adminNotes);
        int rows = db.update(TABLE_CLAIMS, values, KEY_ID + "=?", new String[]{String.valueOf(claimId)});

        if (rows > 0) {
            // Find claimant and notify
            Claim claim = getClaimById(claimId);
            if (claim != null) {
                String type = "APPROVED".equalsIgnoreCase(status) ? "CLAIM_APPROVED" : "CLAIM_REJECTED";
                addNotification(claim.getClaimantId(), "Claim " + status,
                        "Your claim for '" + claim.getItemTitle() + "' was " + status.toLowerCase()
                                + ". Notes: " + adminNotes, type);
            }
        }
        return rows > 0;
    }

    public Claim getClaimById(int claimId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, i." + KEY_TITLE + " as item_title, u." + KEY_FULL_NAME + " as claimant_name FROM "
                + TABLE_CLAIMS + " c LEFT JOIN " + TABLE_ITEMS + " i ON c." + KEY_ITEM_ID + " = i." + KEY_ID
                + " LEFT JOIN " + TABLE_USERS + " u ON c." + KEY_CLAIMANT_ID + " = u." + KEY_ID
                + " WHERE c." + KEY_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(claimId)});
        Claim claim = null;
        if (cursor != null && cursor.moveToFirst()) {
            claim = cursorToClaim(cursor);
            cursor.close();
        }
        return claim;
    }

    private Claim cursorToClaim(Cursor cursor) {
        Claim c = new Claim();
        c.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        c.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ITEM_ID)));
        c.setClaimantId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CLAIMANT_ID)));
        c.setProofDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROOF_DESC)));
        c.setProofImageUri(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROOF_IMAGE)));
        c.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLAIM_STATUS)));
        c.setAdminNotes(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADMIN_NOTES)));
        c.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));

        try {
            c.setItemTitle(cursor.getString(cursor.getColumnIndexOrThrow("item_title")));
        } catch (Exception ignored) {}

        try {
            c.setClaimantName(cursor.getString(cursor.getColumnIndexOrThrow("claimant_name")));
        } catch (Exception ignored) {}

        return c;
    }

    // Notifications CRUD
    public long addNotification(int userId, String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIF_USER_ID, userId);
        values.put(KEY_NOTIF_TITLE, title);
        values.put(KEY_NOTIF_MSG, message);
        values.put(KEY_NOTIF_TYPE, type);
        values.put(KEY_NOTIF_IS_READ, 0);
        values.put(KEY_CREATED_AT, getCurrentTimestamp());
        return db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public List<NotificationItem> getUserNotifications(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<NotificationItem> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NOTIFICATIONS, null, KEY_NOTIF_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, KEY_ID + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                NotificationItem item = new NotificationItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                item.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_USER_ID)));
                item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIF_TITLE)));
                item.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIF_MSG)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIF_TYPE)));
                item.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_IS_READ)) == 1);
                item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private void checkAndGenerateMatchAlerts(Item newItem) {
        // If a LOST item was created, find FOUND items in same category, and vice versa.
        String targetType = "LOST".equalsIgnoreCase(newItem.getType()) ? "FOUND" : "LOST";
        List<Item> candidates = searchAndFilterItems("", newItem.getCategory(), "", "", targetType, "VERIFIED", "Newest");

        for (Item candidate : candidates) {
            if (candidate.getUserId() != newItem.getUserId()) {
                String msg = "A potential matching " + newItem.getType().toLowerCase() + " item ('"
                        + newItem.getTitle() + "') was reported in category " + newItem.getCategory() + ". Check it out!";
                addNotification(candidate.getUserId(), "Matching Item Alert", msg, "MATCH_ALERT");
            }
        }
    }

    // ==========================================
    // MEMBER 4: ADMIN PANEL & REPORTS
    // ==========================================

    public List<User> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, KEY_ID + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean updateUserStatus(int userId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_STATUS, status);
        return db.update(TABLE_USERS, values, KEY_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public List<Item> getPendingItems() {
        return searchAndFilterItems("", "All", "", "", "ALL", "PENDING", "Newest");
    }

    public boolean updateItemStatus(int itemId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_STATUS, status);
        return db.update(TABLE_ITEMS, values, KEY_ID + "=?", new String[]{String.valueOf(itemId)}) > 0;
    }

    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS, KEY_ID + "=?", new String[]{String.valueOf(itemId)}) > 0;
    }

    // Admin Report Statistics
    public int getCount(String tableName, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + tableName + (selection != null ? " WHERE " + selection : "");
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalUsers() {
        return getCount(TABLE_USERS, null, null);
    }

    public int getTotalLostItems() {
        return getCount(TABLE_ITEMS, KEY_TYPE + "=?", new String[]{"LOST"});
    }

    public int getTotalFoundItems() {
        return getCount(TABLE_ITEMS, KEY_TYPE + "=?", new String[]{"FOUND"});
    }

    public int getRecoveredItemsCount() {
        return getCount(TABLE_ITEMS, KEY_ITEM_STATUS + "=?", new String[]{"RECOVERED"});
    }

    public int getPendingClaimsCount() {
        return getCount(TABLE_CLAIMS, KEY_CLAIM_STATUS + "=?", new String[]{"PENDING"});
    }
}
