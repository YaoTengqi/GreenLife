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

import YAO.GreenLife.bean.post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.myHolder> {

    public Context context;
    public List<post> post_list;

    public PostAdapter(Context post_context, List<post> list) {
        context = post_context;
        post_list = list;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        myHolder myHolder = new myHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        post post = post_list.get(position);
        holder.post_context.setText(post.post_context);
        holder.post_title.setText("#" + post.post_title);
        if (post.post_img == null) {
            holder.imageView.setImageResource(R.drawable.recycle);
        } else {
            holder.imageView.setImageBitmap(post.post_img);
        }
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }


    /**
     * @Description: 创建自己的Holder, 获取item对象
     * @Param:
     * @return:
     * @Author: YAO
     * @Date: 2022/3/5
     */
    public class myHolder extends RecyclerView.ViewHolder {
        TextView post_title;
        TextView post_context;
        ImageView imageView;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            post_title = itemView.findViewById(R.id.post_title);
            post_context = itemView.findViewById(R.id.post_context);
            imageView = itemView.findViewById(R.id.post_img);
        }
    }
}
