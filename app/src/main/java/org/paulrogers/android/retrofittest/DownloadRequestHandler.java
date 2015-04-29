package org.paulrogers.android.retrofittest;

/**
 * The Interface for any class that can download an image for a given URL
 * Created by paulrogers on 4/28/15.
 */
public interface DownloadRequestHandler {

    /**
     * Request an image download from the given URL
     * @param imageUrl - The image URL
     */
    void requestDownload(String imageUrl);

}
