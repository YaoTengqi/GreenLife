package YAO.GreenLife.bean;

public class history {
    public String img_str;
    public String result;
    public String time;
    public String user_id;

    public history(String img_str, String result, String time, String user_id) {
        this.img_str = img_str;
        this.result = result;
        this.time = time;
        this.user_id = user_id;
    }


    public String getImg_str() {
        return img_str;
    }

    public void setImg_str(String img_str) {
        this.img_str = img_str;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
