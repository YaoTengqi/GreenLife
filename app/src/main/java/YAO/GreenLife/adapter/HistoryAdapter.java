package YAO.GreenLife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import java.util.List;

import YAO.GreenLife.bean.history;
import YAO.GreenLife.bean.post;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.myHolder> {
    public Context context;
    public List<history> history_list;

    public HistoryAdapter(Context history_context, List<history> list) {
        context = history_context;
        history_list = list;
    }

    @NonNull
    @Override
    public HistoryAdapter.myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        HistoryAdapter.myHolder myHolder = new HistoryAdapter.myHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.myHolder holder, int position) {
        history history = history_list.get(position);
        holder.history_result.setText("检测结果:" + history.result);
        holder.history_time.setText("检测时间:" + history.time);
        holder.imageView.setImageResource(R.drawable.recycle);
    }

    @Override
    public int getItemCount() {
        return history_list.size();
    }


    /**
     * @Description: 创建自己的Holder, 获取item对象
     * @Param:
     * @return:
     * @Author: YAO
     * @Date: 2022/3/5
     */
    public class myHolder extends RecyclerView.ViewHolder {
        TextView history_result;
        TextView history_time;
        ImageView imageView;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            history_result = itemView.findViewById(R.id.history_result);
            history_time = itemView.findViewById(R.id.history_time);
            imageView = itemView.findViewById(R.id.history_img);
        }
    }
}
