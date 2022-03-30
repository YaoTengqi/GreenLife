package YAO.GreenLife.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import YAO.GreenLife.adapter.PostAdapter;
import YAO.GreenLife.bean.post;

public class DetailsActivity extends AppCompatActivity {
    private Uri imageUri = null;
    private final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "output_image.jpg";
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;
    List<post> post_list = new ArrayList<>();
    RecyclerView mRecyclerView;
    PostAdapter mMyAdapter;
    Bitmap cache_image = null;
    String cache_code = null;

    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();

        imageView = findViewById(R.id.details_iv);
        TextView textView = findViewById(R.id.details_tv);


        /**
         * @Description: 判断是 从相册中挑选照片 还是 拍照/直接查看结果
         * @Author: YAO
         * @Date: 2022/3/18
         */
        String requestCode = intent.getStringExtra("requestCode");

        if (requestCode == null) {
            yourSelectedImage = BitmapFactory.decodeFile(filePath);
        } else {
            Uri imgUri = Uri.parse(intent.getStringExtra("imgUri"));
            try {
                yourSelectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //再查看identifyCode,判断用什么算法
        String identifyCode = intent.getStringExtra("identifyCode");

        //当选择的是垃圾分类识别时 ————> 调用YoloV5算法
        if ("garbage".equals(identifyCode)) {

            boolean ret_init = yolov5ncnn.Init(getResources().getAssets());
            if (!ret_init) {
                Log.e("MainActivity", "yolov5ncnn Init failed");
            }


            YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(yourSelectedImage, false);

            Log.d("YAO", String.valueOf(objects));

            showObjects(objects);
        }


        //当选择的是昆虫识别时 ————> 调用ResNet18算法
        else if ("insect".equals(identifyCode)) {
            Bitmap bitmap = null;
            Module module = null;
            try {
                // creating bitmap from packaged into app android asset 'image.jpg',
                // app/src/main/assets/image.jpg
                bitmap = yourSelectedImage;
                // loading serialized torchscript module from packaged into app android asset model.pt,
                // app/src/model/assets/model.pt
                module = LiteModuleLoader.load(assetFilePath(this, "328.ptl"));
            } catch (IOException e) {
                Log.e("PytorchHelloWorld", "Error reading assets", e);
                finish();
            }

            // showing image on UI
            ImageView imageView = findViewById(R.id.details_iv);
            imageView.setImageBitmap(bitmap);

            // preparing input tensor
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

            // running the model
            final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

            // getting tensor content as java array of floats
            final float[] scores = outputTensor.getDataAsFloatArray();

            // searching for the index with maximum score
            float maxScore = -Float.MAX_VALUE;
            int maxScoreIdx = -1;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxScoreIdx = i;
                }
            }

            String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];

            // showing className on UI
            textView = findViewById(R.id.details_tv);
//            textView.setText(className);

            post post_identify = new post();
            int position = 0;
            for (int i = 0; i < 22; i++) {
                if (className == ImageNetClasses.IMAGENET_CLASSES[i]) {
                    position = i;
                }
            }
            post_identify.post_img = bitmap;
            post_identify.post_context = ImageNetClasses.IMAGENET_CLASSES_INTRODUCTION[position];
            post_identify.post_title = className;
            post_list.add(post_identify);


            cache_image = bitmap;
            cache_code = "insect";
        }

        //当选择的是查看按钮时
        else {
            if (cache_image == null) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.please_upload_image));
                textView.setText("请先上传图片");
            } else if ("garbage".equals(cache_code)) {
                YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(cache_image, false);
                showObjects(objects);
            } else {
                imageView.setImageBitmap(cache_image);
            }
        }


        /**
         * @Description: RecyclerView的设置
         * @Param: [savedInstanceState]
         * @return: void
         * @Author: YAO
         * @Date: 2022/3/9
         */
        //获取RecyclerView对象
        mRecyclerView = findViewById(R.id.details_rv);


        //设置adapter
        mMyAdapter = new PostAdapter(this, post_list);
        mRecyclerView.setAdapter(mMyAdapter);

        //设置layoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //设置Decoration分割线
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider, null));
        mRecyclerView.addItemDecoration(decoration);


    }

    /**
     * @Description: 将Yolov5的识别结果展示在页面上
     * @Param: [objects]
     * @return: void
     * @Author: YAO
     * @Date: 2022/3/18
     */

    private void showObjects(YoloV5Ncnn.Obj[] objects) {

        if (objects == null) {
            imageView.setImageBitmap(yourSelectedImage);
            return;
        }

        // draw objects on bitmap
        Bitmap rgba = yourSelectedImage.copy(Bitmap.Config.ARGB_8888, true);

        final int[] colors = new int[]{
                Color.rgb(54, 67, 244),
                Color.rgb(99, 30, 233),
                Color.rgb(176, 39, 156),
                Color.rgb(183, 58, 103),
                Color.rgb(181, 81, 63),
                Color.rgb(243, 150, 33),
                Color.rgb(244, 169, 3),
                Color.rgb(212, 188, 0),
                Color.rgb(136, 150, 0),
                Color.rgb(80, 175, 76),
                Color.rgb(74, 195, 139),
                Color.rgb(57, 220, 205),
                Color.rgb(59, 235, 255),
                Color.rgb(7, 193, 255),
                Color.rgb(0, 152, 255),
                Color.rgb(34, 87, 255),
                Color.rgb(72, 85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125, 96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(100);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++) {
            paint.setColor(colors[i % 19]);

            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

            Bitmap new_bitmap = Bitmap.createBitmap(yourSelectedImage, (int) (objects[i].x), (int) (objects[i].y), (int) (objects[i].w), (int) (objects[i].h));
            post post_identify = new post();
            String[] split = objects[i].label.split(":");
            post_identify.post_img = new_bitmap;
            post_identify.post_context = split[0] + "\n" + "准确率：" + String.format("%.1f", objects[i].prob * 100) + "%";
            post_identify.post_title = split[1];
            post_list.add(post_identify);
            // draw filled text inside image
            {
                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";

                float text_width = textpaint.measureText(text);
                float text_height = -textpaint.ascent() + textpaint.descent();

                float x = objects[i].x;
                float y = objects[i].y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > rgba.getWidth())
                    x = rgba.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
            }
        }

        imageView.setImageBitmap(rgba);
        cache_image = rgba;
        cache_code = "garbage";
    }


    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}