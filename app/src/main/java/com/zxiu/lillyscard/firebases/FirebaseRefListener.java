package com.zxiu.lillyscard.firebases;

import java.util.List;

/**
 * Created by Xiu on 11/17/2016.
 */

public interface FirebaseRefListener<T> {
    public void onSuccess(List<T> items);
}
