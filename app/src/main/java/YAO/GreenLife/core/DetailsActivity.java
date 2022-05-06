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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import YAO.GreenLife.adapter.PostAdapter;
import YAO.GreenLife.bean.history;
import YAO.GreenLife.bean.post;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity {
    private Uri imageUri = null;
    private final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "output_image.jpg";
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;
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

            boolean ret_init = yolov5ncnn.Init(getResources().getAssets());
            if (!ret_init) {
                Log.e("MainActivity", "yolov5ncnn Init failed");
            }


            YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(yourSelectedImage, false);

            Log.d("YAO", String.valueOf(objects));

            showObjects(objects);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //将图片、时间戳、识别结果发送到后台历史数据库中
                    byte[] pic_byte = getBitmapByte(yourSelectedImage);
                    String pic_str = Base64.getEncoder().encodeToString(pic_byte);
//                    byte[] pic_byte_after = Base64.getDecoder().decode(pic_str);
                    history send_history = new history(pic_str, result, current_time, user_id);
                    Log.d("history_user_id", user_id);
                    Log.d("pic", result);
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
                    //将图片、时间戳、识别结果发送到后台历史数据库中
                    HttpUrl.Builder httpBuilder = sendHistory(yourSelectedImage, user_id, current_time, result);
                    try {
                        Request request = new Request.Builder()
                                .url(httpBuilder.build())
                                .method("post", new FormBody.Builder().build())
                                .build();
                        OkHttpClient client = new OkHttpClient.Builder()
                                .readTimeout(20, TimeUnit.SECONDS)
                                .build();
                        Response response = client.newCall(request).execute();

                        String result2 = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(result2);
//                return_code = jsonObject.getString("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            if (i++ < objects.length)
                result += post_identify.post_title + ",";
            else
                result += post_identify.post_title;
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
        String body = com.alibaba.fastjson.JSONObject.toJSONString(send_history);

        String msg = null;
        //构建HttpClient实例
        HttpClient httpClient = new HttpClient();
        //设置请求超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        //设置响应超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(60000);

        //构造PostMethod的实例
        PostMethod postMethod = new PostMethod(url_history);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        Map<String, Object> map = com.alibaba.fastjson.JSONObject.parseObject(body, Map.class);
        Set<String> set = map.keySet();
        for (String s : set) {
            System.out.println(map.get(s).toString());
            postMethod.addParameter(s, map.get(s).toString());
        }
        try {
            //执行post请求
            httpClient.executeMethod(postMethod);
            //可以对响应回来的报文进行处理
            msg = postMethod.getResponseBodyAsString();
            System.out.printf(msg);
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //关闭连接释放资源的方法
            postMethod.releaseConnection();
            //((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }

        return msg;
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