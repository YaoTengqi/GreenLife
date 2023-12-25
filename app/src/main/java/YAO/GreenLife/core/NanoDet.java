package YAO.GreenLife.core;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class NanoDet {
    static {
        System.loadLibrary("yolov5ncnn");
    }

    public static native void init(AssetManager manager, boolean useGPU);
    public static native Box[] detect(Bitmap bitmap, double threshold, double nms_threshold);
}
