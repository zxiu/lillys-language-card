package com.zxiu.lillyscard.firebases;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zxiu.lillyscard.entities.CardGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Xiu on 11/16/2016.
 */

public class FirebaseRef<T> {
    static String TAG = FirebaseRef.class.getSimpleName();

    static String HOST = "https://lillys-card.firebaseio.com";

    public static enum PATH {
        PUBLIC("public"), INITIATE("initiate"), GROUPS("groups");
        String part;

        PATH(String part) {
            this.part = part;
        }

        @Override
        public String toString() {
            return part;
        }
    }

    public String key;
    public DatabaseReference ref;
    boolean isSingle;
    boolean loaded = false;

    T item;
    List<T> items = new ArrayList<>();

    public FirebaseRef(final boolean isList, PATH... paths) {
        this.isSingle = isList;
        StringBuilder $ = new StringBuilder(HOST);
        for (PATH path : paths) {
            $.append("/").append(path);
        }
        this.key = $.toString();
        this.ref = FirebaseDatabase.getInstance().getReferenceFromUrl(key);
        this.ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isSingle) {
                    item = (T) dataSnapshot.getValue();
                } else {
                    items = (List<T>) dataSnapshot.getValue(isSingle);
                }
                loaded = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public FirebaseRef(PATH... paths) {
        this(false, paths);
    }

    Set<FirebaseRefListener<T>> firebaseRefListeners = new HashSet<>();

    public FirebaseRef<T> addFirebaseRefListener(FirebaseRefListener<T> listener) {
        if (loaded) {
            listener.onSuccess(items);
        }
        firebaseRefListeners.add(listener);
        return this;
    }

    public static FirebaseRef<CardGroup> cardGroupFirebaseRef = new FirebaseRef<>(PATH.PUBLIC, PATH.GROUPS);
}
