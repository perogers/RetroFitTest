/*
 * Copyright 2015 Paul E. Rogers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
