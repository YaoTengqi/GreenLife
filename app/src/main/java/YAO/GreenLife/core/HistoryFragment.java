package YAO.GreenLife.core;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import YAO.GreenLife.adapter.HistoryAdapter;
import YAO.GreenLife.bean.history;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryFragment extends Fragment {
    RecyclerView mRecyclerView;
    List<history> history_list = new ArrayList<>();
    HistoryAdapter mMyAdapter;
    String user_id;

    static String url_user_history = "http://59.110.10.33:9999/search";//发送识别历史的后端路径

//    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_history, null);


        /**
         * @Description: RecyclerView的设置
         * @Param: [savedInstanceState]
         * @return: void
         * @Author: YAO
         * @Date: 2022/3/9
         */
        //获取RecyclerView对象
        mRecyclerView = view.findViewById(R.id.hole_rv);

        Bundle bundle = this.getArguments();//得到从Activity传来的数据
        user_id = bundle.getString("user_id");
        Log.d("user_id_identify", user_id);


        HttpUrl.Builder httpBuilder = HttpUrl.parse(url_user_history).newBuilder();
        httpBuilder.addQueryParameter("user_id", user_id);


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(httpBuilder.build())
                            .method("post", new FormBody.Builder().build())
                            .build();
                    OkHttpClient client = new OkHttpClient.Builder()
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build();
                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String result = null;
                    try {
                        result = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = jsonArray.length() - 1; i >= 0; i--) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            history history = new history();
                            history.setImg_str(jsonObject.getString("img_str"));
                            history.setResult(jsonObject.getString("result"));
                            history.setTime(jsonObject.getString("time"));
                            history_list.add(history);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } finally {
                }
            }
        });
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //设置adapter
        mMyAdapter = new HistoryAdapter(getActivity(), history_list);
        mRecyclerView.setAdapter(mMyAdapter);

        //设置layoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        //设置Decoration分割线
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider, null));
        mRecyclerView.addItemDecoration(decoration);


        return view;
    }
}