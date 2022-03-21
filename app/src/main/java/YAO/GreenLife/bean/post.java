package YAO.GreenLife.bean;

import android.graphics.Bitmap;

/**
 * @Description: 环保小贴士
 * @Param:
 * @return:
 * @Author: YAO
 * @Date: 2022/3/9
 */
public class post {


    public Bitmap post_img;
    public String post_title;
    public String post_context;

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_context() {
        return post_context;
    }

    public void setPost_context(String post_context) {
        this.post_context = post_context;
    }

    public Bitmap getPost_img() {
        return post_img;
    }

    public void setPost_img(Bitmap post_img) {
        this.post_img = post_img;
    }
}
