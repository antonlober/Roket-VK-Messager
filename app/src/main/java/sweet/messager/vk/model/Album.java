package sweet.messager.vk.model;

public class Album {
    public String title, uri, id, bucket, taken, modified;
    public int count;
    public boolean vk = false;

    @Override
    public String toString() {
        return "{" +
                  "\"title\":\"" + title + "\"," +
                  "\"thumb_src\":\"" + uri + "\"," +
                  "\"bucket\":\"" + bucket + "\"," +
                  "\"taken\":\"" + taken + "\"," +
                  "\"modified\":\"" + modified + "\"," +
                  "\"size\":" + count + "," +
                  "\"id\":\"" + id + "\"," +
                  "\"vk\":\"" + (vk ? 1 : 0) + "\"" +
                "}";
    }
}
