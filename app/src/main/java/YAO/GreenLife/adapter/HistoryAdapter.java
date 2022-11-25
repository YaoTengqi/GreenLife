package YAO.GreenLife.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import java.util.Base64;
import java.util.List;

import YAO.GreenLife.bean.history;

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
        byte[] pic_byte_after = Base64.getDecoder().decode(history.img_str);
        Bitmap img_bitmap = BitmapFactory.decodeByteArray(pic_byte_after, 0, pic_byte_after.length);
        holder.history_result.setText("检测结果:" + history.result);
        holder.history_time.setText("检测时间:" + history.time);
        holder.history_imageView.setImageBitmap(img_bitmap);
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
        ImageView history_imageView;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            history_result = itemView.findViewById(R.id.history_result);
            history_time = itemView.findViewById(R.id.history_time);
            history_imageView = itemView.findViewById(R.id.history_img);
        }
    }
}
