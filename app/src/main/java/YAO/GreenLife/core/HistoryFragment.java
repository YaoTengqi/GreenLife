package YAO.GreenLife.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import java.util.ArrayList;
import java.util.List;

import YAO.GreenLife.adapter.HistoryAdapter;
import YAO.GreenLife.bean.history;

public class HistoryFragment extends Fragment {
    RecyclerView mRecyclerView;
    List<history> history_list = new ArrayList<>();
    HistoryAdapter mMyAdapter;

    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();

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