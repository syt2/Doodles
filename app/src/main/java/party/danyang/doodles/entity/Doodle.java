package party.danyang.doodles.entity;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import party.danyang.doodles.Utils;

/**
 * Created by dream on 16-12-12.
 */

public class Doodle {
    static enum Week {
        Sun, Mon, Tue, Wed, Thu, Fri, Sat
    }

    @Expose
    @SerializedName("hires_height")
    private int height;
    @Expose
    @SerializedName("hires_width")
    private int width;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("hires_url")
    private String url;
    @Expose
    @SerializedName("run_date_array")
    private List<Integer> date;

    public String getMonthString() {
        if (this.date != null && this.date.size() == 3) {
            return date.get(1) + "-" + date.get(2);
        }
        return null;
    }

    public String getWeekString() {
        if (this.date != null && this.date.size() == 3) {
            return Week.values()[(Utils.getWeek(date.get(0), date.get(1), date.get(2)) - 1) % 7].toString();
        }
        return null;
    }

    public String getDateString() {
        if (this.date != null && this.date.size() == 3) {
            return date.get(0) + "-" + date.get(1) + "-" + date.get(2);
        }
        return null;
    }

    public List<Integer> getDate() {
        return date;
    }

    public void setDate(List<Integer> date) {
        this.date = date;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        if (url.startsWith("//")) {
            url = TextUtils.concat("https:", url).toString();
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
