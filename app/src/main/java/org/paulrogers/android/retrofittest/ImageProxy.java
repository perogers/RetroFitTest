package org.paulrogers.android.retrofittest;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by paulrogers on 4/27/15.
 */
public class ImageProxy {
    Context mContext = null;

    private static ImageProxy mInstance = null;

    Bitmap mBitmap = null;

    private ImageProxy(Context context) {
        mContext = context;
    }

    public static ImageProxy getInstance(Context context) {
        if ( mInstance == null) {
            mInstance = new ImageProxy(context);
        }
        return mInstance;
    }


    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
