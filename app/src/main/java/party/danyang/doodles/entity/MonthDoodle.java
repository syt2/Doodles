package party.danyang.doodles.entity;

import java.util.List;

import party.danyang.doodles.widget.GroupEntity;

/**
 * Created by dream on 16-12-14.
 */

public class MonthDoodle implements GroupEntity<Doodle> {
    List<Doodle> list;
    private List<Integer> date;

    @Override
    public boolean shouldShow() {
        return true;
    }

    @Override
    public List<Doodle> getChildrenList() {
        return list;
    }

    public List<Integer> getDate() {
        return date;
    }

    public void setDate(List<Integer> date) {
        this.date = date;
    }

    public void setList(List<Doodle> list) {
        this.list = list;
    }

    public String getDateString() {
        String dateString = "";
        if (date != null && date.size() > 0) {
            dateString += date.get(0);
            for (int i = 1; i < date.size(); i++) {
                dateString += "-" + date.get(i);
            }
        }
        return dateString;
    }
}
