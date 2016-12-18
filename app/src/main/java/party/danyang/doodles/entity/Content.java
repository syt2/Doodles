package party.danyang.doodles.entity;

import java.util.List;

/**
 * Created by dream on 16-12-12.
 */

public class Content {
    private String runDate;
    private String title;
    private String doodleDescribe;
    private String url;
    private List<SimpleDoodle> histroyDoodles;
//    todo
//    private List<SimpleDoodle> relateDoodles;

    public String getDoodleDescribe() {
        return doodleDescribe;
    }

    public void setDoodleDescribe(String doodleDescribe) {
        this.doodleDescribe = doodleDescribe;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

//    public List<SimpleDoodle> getRelateDoodles() {
//        return relateDoodles;
//    }
//
//    public void setRelateDoodles(List<SimpleDoodle> relateDoodles) {
//        this.relateDoodles = relateDoodles;
//    }

    public List<SimpleDoodle> getHistroyDoodles() {
        return histroyDoodles;
    }

    public void setHistroyDoodles(List<SimpleDoodle> histroyDoodles) {
        this.histroyDoodles = histroyDoodles;
    }
}
