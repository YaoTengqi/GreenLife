package YAO.GreenLife.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import YAO.GreenLife.adapter.PostAdapter;
import YAO.GreenLife.bean.history;
import YAO.GreenLife.bean.post;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity {
    private Uri imageUri = null;
    private final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "output_image.jpg";
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;
    Bitmap history_bitmap = null;
    private List<post> post_list = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private PostAdapter mMyAdapter;
    private Bitmap cache_image = null;
    private String cache_code = null;
    private long timecurrentTimeMillis = System.currentTimeMillis();//时间戳工具
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss", Locale.getDefault());//格式化时间戳
    private String result;
    private String user_id;
    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();
    private NanoDet nanoDet = new NanoDet();
    private int width;
    private int height;


    static String url_history = "http://59.110.10.33:9999/upload";//发送识别历史的后端路径
//    String url_history = "http://localhost:9999/upload";//发送识别历史的后端路径

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        imageView = findViewById(R.id.details_iv);
        TextView textView = findViewById(R.id.details_tv);

        Intent intent = getIntent();
        user_id = intent.getExtras().getString("user_id");
        Log.d("user_id_details", user_id);


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

        String current_time = sdf.format(timecurrentTimeMillis);//获取当前时间戳

        //当选择的是垃圾分类识别时 ————> 调用YoloV5算法
        if ("garbage".equals(identifyCode)) {

            boolean ret_init = YoloV5Ncnn.Init(getResources().getAssets());
            YoloV5Ncnn.Init(getResources().getAssets());
            if (!ret_init) {
                Log.e("MainActivity", "yolov5ncnn Init failed");
            }


            YoloV5Ncnn.Obj[] objects = YoloV5Ncnn.Detect(yourSelectedImage, false);

            Log.d("YAO", String.valueOf(objects));

            history_bitmap = showObjects(objects);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.5f, 0.5f);//裁剪图片大小
                    history_bitmap = Bitmap.createBitmap(history_bitmap, 0, 0, history_bitmap.getWidth(),
                            history_bitmap.getHeight(), matrix, true);
                    //将图片、时间戳、识别结果发送到后台历史数据库中
                    byte[] pic_byte = getBitmapByte(history_bitmap);
                    String pic_str = Base64.getEncoder().encodeToString(pic_byte);
//                    byte[] pic_byte_after = Base64.getDecoder().decode(pic_str);
//                    System.out.println(pic_byte_after.length);
                    history send_history = new history(pic_str, result, current_time, user_id);
                    Log.d("history_user_id", user_id);
                    Log.d("pic", result);
                    String msg = postHttp(send_history);
                    System.out.println(msg);
                }
            }).start();
        }


        //当选择的是Nanodet时 ————> 调用Nanodet算法
        if ("nanodet".equals(identifyCode)) {

            width = yourSelectedImage.getWidth();
            height = yourSelectedImage.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(yourSelectedImage, 0, 0, Math.min(width, height), Math.min(width, height));
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            nanoDet.init(getAssets(), false);

            Box[] objects = null;

            objects = nanoDet.detect(mutableBitmap, 0.3, 0.7);

            Log.d("YAO", String.valueOf(objects));

            history_bitmap = drawBoxRects(mutableBitmap, objects);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.5f, 0.5f);//裁剪图片大小
                    history_bitmap = Bitmap.createBitmap(history_bitmap, 0, 0, history_bitmap.getWidth(),
                            history_bitmap.getHeight(), matrix, true);
                    //将图片、时间戳、识别结果发送到后台历史数据库中
                    byte[] pic_byte = getBitmapByte(history_bitmap);
                    String pic_str = Base64.getEncoder().encodeToString(pic_byte);
//                    byte[] pic_byte_after = Base64.getDecoder().decode(pic_str);
//                    System.out.println(pic_byte_after.length);
                    history send_history = new history(pic_str, result, current_time, user_id);
//                    Log.d("history_user_id", user_id);
//                    Log.d("pic", result);
                    String msg = postHttp(send_history);
                    System.out.println(msg);
                }
            }).start();
        }


        //当选择的是昆虫识别时 ————> 调用MobileNetV3算法
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
            result = className;

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


            new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.5f, 0.5f);//裁剪图片大小
                    history_bitmap = Bitmap.createBitmap(yourSelectedImage, 0, 0, yourSelectedImage.getWidth(),
                            yourSelectedImage.getHeight(), matrix, true);
                    //将图片、时间戳、识别结果发送到后台历史数据库中
                    byte[] pic_byte = getBitmapByte(yourSelectedImage);
                    String pic_str = Base64.getEncoder().encodeToString(pic_byte);
//                    byte[] pic_byte_after = Base64.getDecoder().decode(pic_str);
//                    System.out.println(pic_byte_after.length);
                    history send_history = new history(pic_str, result, current_time, user_id);
                    Log.d("history_user_id", user_id);
                    Log.d("pic", result);
                    String msg = postHttp(send_history);
                    System.out.println(msg);
                }
            }).start();

            cache_image = bitmap;
            cache_code = "insect";
        }

        //当选择的是查看按钮时
        else {
            if (cache_image == null) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.please_upload_image));
                textView.setText("请先上传图片");
            } else if ("garbage".equals(cache_code)) {
                YoloV5Ncnn.Obj[] objects = YoloV5Ncnn.Detect(cache_image, false);
                showObjects(objects);

            } else if ("nanodet".equals(cache_code)) {
                Box[] objects = NanoDet.detect(cache_image, 0.3, 0.7);
                drawBoxRects(cache_image, objects);
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

    private Bitmap showObjects(YoloV5Ncnn.Obj[] objects) {

        if (objects == null) {
            imageView.setImageBitmap(yourSelectedImage);
            return yourSelectedImage;
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

            //-------将识别的每一个物体显示在下方recycleView中------//
            post post_identify = new post();
            String[] split = objects[i].label.split(":");
            post_identify.post_img = new_bitmap;
            post_identify.post_context = split[0] + "\n" + "准确率：" + String.format("%.1f", objects[i].prob * 100) + "%";
            post_identify.post_title = split[1];
            if (i < objects.length - 1) {
                if (i == 0) {
                    result = post_identify.post_title + ",";
                } else {
                    result += post_identify.post_title + ",";
                }
            } else {
                if (i == 0)
                    result = post_identify.post_title;
                else
                    result += post_identify.post_title;
            }
            post_list.add(post_identify);


            //-----------------------------------------------//

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

        return rgba;
    }

    protected Bitmap drawBoxRects(Bitmap mutableBitmap, Box[] results) {
        if (results == null || results.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
        boxPaint.setTextSize(40 * mutableBitmap.getWidth() / 800.0f);
        int i = 0;
        for (Box box : results) {
            Bitmap new_bitmap = Bitmap.createBitmap(yourSelectedImage, (int) (box.x0), (int) (box.y0), (int) (box.x1 - box.x0), (int) (box.y1 - box.y0));

            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel() + String.format(Locale.CHINESE, " %.3f", box.getScore()), box.x0 + 3, box.y0 + 40 * mutableBitmap.getWidth() / 1000.0f, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);

            //-------将识别的每一个物体显示在下方recycleView中------//
            post post_identify = new post();
            String[] split = box.getLabel().split(":");
            post_identify.post_img = new_bitmap;
//            post_identify.post_context = split[1] + "\n" + "准确率：" + String.format("%.1f", box.getScore() * 100) + "%";
            post_identify.post_context = "准确率：" + String.format("%.1f", box.getScore() * 100) + "%";
            post_identify.post_title = split[0];
            if (i < results.length - 1) {
                if (i == 0) {
                    result = post_identify.post_title + ",";
                } else {
                    result += post_identify.post_title + ",";
                }
            } else {
                if (i == 0)
                    result = post_identify.post_title;
                else
                    result += post_identify.post_title;
            }
            post_list.add(post_identify);
        }


        //-----------------------------------------------//

        imageView.setImageBitmap(mutableBitmap);
        cache_image = mutableBitmap;
        cache_code = "nanoDet";
        return mutableBitmap;
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

    /**
     * @Description: 将图片、时间戳、识别结果发送到后台历史数据库中
     * @Param: [yourSelectedImage, user_id, current_time, result]
     * @return: void
     * @Author: YAO
     * @Date: 2022/4/28
     */
    public HttpUrl.Builder sendHistory(Bitmap yourSelectedImage, String user_id, String current_time, String result) {
        byte[] pic_byte = getBitmapByte(yourSelectedImage);
        String pic_string = pic_byte.toString();


        HttpUrl.Builder httpBuilder = HttpUrl.parse(url_history).newBuilder();
        httpBuilder.addQueryParameter("uid", "test6666");
        httpBuilder.addQueryParameter("utime", current_time);
        httpBuilder.addQueryParameter("ulable", result);
        httpBuilder.addQueryParameter("pinfo", pic_string);


        return httpBuilder;

    }

    public static String postHttp(history send_history) {
        String responseData = null;
        String body = com.alibaba.fastjson.JSONObject.toJSONString(send_history);
        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody RequestBody2 = RequestBody.create(type, body);
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    // 指定访问的服务器地址
                    .url(url_history).post(RequestBody2)
                    .build();
            Response response = client.newCall(request).execute();
            responseData = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseData;
    }


    /**
     * @Description: bitmap->byte[]
     * @Param: [bitmap]
     * @return: byte[]
     * @Author: YAO
     * @Date: 2022/4/28
     */
    public static byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //参数1转换类型，参数2压缩质量，参数3字节流资源
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

}