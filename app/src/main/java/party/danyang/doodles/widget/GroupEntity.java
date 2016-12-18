package party.danyang.doodles.widget;

import java.util.List;

/**
 * Created by dream on 16-12-13.
 */

public interface GroupEntity<T> {
    List<T> getChildrenList();

    boolean shouldShow();
}
