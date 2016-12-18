package party.danyang.doodles.widget;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by dream on 16-12-13.
 */

public abstract class GroupAdapter<TC, TG extends GroupEntity<TC>>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "GroupAdapter";
    private static final int INVALID_POSITION = -1;

    private static final int TYPE_GROUP = 19940216;
    private static final int TYPE_CHILD = 19960104;

    private List<TG> groupList = new ArrayList<>();

    private List<PositionEntity> positionInfo = new ArrayList<>();

    public abstract RecyclerView.ViewHolder onCreateGroupViewHolder(ViewGroup parent);

    public abstract RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent);

    public abstract void onBindGroupViewHolder(RecyclerView.ViewHolder holder, TG group);

    public abstract void onBindChildViewHolder(RecyclerView.ViewHolder holder, TC child);

    public void onBindGroupViewHolder(RecyclerView.ViewHolder holder, int groupPosition) {
        onBindGroupViewHolder(holder, groupList.get(groupPosition));
    }

    public void onBindChildViewHolder(RecyclerView.ViewHolder holder, int groupPosition, int childPosition) {
        onBindChildViewHolder(holder, groupList.get(groupPosition).getChildrenList().get(childPosition));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP:
                return onCreateGroupViewHolder(parent);
            case TYPE_CHILD:
            default:
                return onCreateChildViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_GROUP:
                onBindGroupViewHolder(holder, getGroupPosition(position));
                break;
            case TYPE_CHILD:
            default:
                int groupPosition = getGroupPosition(position);
                int childPosition = getChildPosition(position, groupPosition);
                onBindChildViewHolder(holder, groupPosition, childPosition);
        }
    }

    @Override
    public int getItemViewType(int combinePosition) {
        for (PositionEntity pe : positionInfo) {
            if (pe.startPosition == combinePosition) return TYPE_GROUP;
            else if (pe.startPosition > combinePosition) return TYPE_CHILD;
        }
        return TYPE_CHILD;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getGroupPosition(int combinePosition) {
        for (PositionEntity pe : positionInfo) {
            if (combinePosition >= pe.startPosition && combinePosition < pe.endPosition) {
                return pe.groupPosition;
            }
        }
        return INVALID_POSITION;
    }

    public int getChildPosition(int combinePosition, int groupPosition) {
        if (groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException();
        }
        if (combinePosition > positionInfo.get(groupPosition).startPosition &&
                combinePosition < positionInfo.get(groupPosition).endPosition) {
            return combinePosition - positionInfo.get(groupPosition).startPosition - 1;
        }
        return INVALID_POSITION;
    }

    public int getChildPosition(int combinePosition) {
        int groupPosition = getGroupPosition(combinePosition);
        return getChildPosition(combinePosition, groupPosition);
    }

    public int getCombinePosition(int groupPosition) {
        if (groupPosition >= getGroupCount()) {
            return getItemCount();
        }
        return positionInfo.get(groupPosition).startPosition;
    }

    public int getCombinePosition(int groupPosition, int childPosition) {
        if (groupPosition >= getGroupCount()) {
            return getItemCount();
        }
        PositionEntity pe = positionInfo.get(groupPosition);
        if (childPosition >= pe.groupSize) {
            return getCombinePosition(groupPosition + 1);
        }
        if (pe.startPosition + childPosition >= pe.endPosition) {
            return INVALID_POSITION;
        }
        return pe.startPosition + childPosition + 1;
    }

    public void updatePositionInfo(int groupPosition) {
        for (int i = positionInfo.size() - 1; i >= groupPosition; i--) {
            positionInfo.remove(i);
        }
        for (int i = groupPosition; i < getGroupCount(); i++) {
            int preEndPosition = i > 0 ? positionInfo.get(i - 1).endPosition : 0;
            PositionEntity pe = new PositionEntity(i, preEndPosition, getChildrenCount(i));
            positionInfo.add(pe);
        }
    }

    public void updatePositionInfo() {
        positionInfo.clear();
        for (int i = 0; i < getGroupCount(); i++) {
            int preEndPosition = i > 0 ? positionInfo.get(i - 1).endPosition : 0;
            PositionEntity pe = new PositionEntity(i, preEndPosition, getChildrenCount(i));
            positionInfo.add(pe);
        }
    }

    public boolean isGroupType(int combinePosition) {
        return getItemViewType(combinePosition) == TYPE_GROUP;
    }

    @Override
    public int getItemCount() {
        int sum = getGroupCount();
        for (int i = 0; i < getGroupCount(); i++) {
            sum += getChildrenCount(i);
        }
        return sum;
    }

    public int getGroupCount() {
        return groupList.size();
    }

    public int getChildrenCount(int groupPosition) {
        if (groupPosition >= getGroupCount()) {
            return 0;
        }
        if (groupList.get(groupPosition).getChildrenList() == null) {
            return 0;
        }
        return groupList.get(groupPosition).getChildrenList().size();
    }

    private class PositionEntity {
        int groupPosition;
        int groupSize;
        int startPosition;
        int endPosition;

        PositionEntity() {
        }

        PositionEntity(int groupPosition, int startPosition, int groupSize) {
            this.groupPosition = groupPosition;
            this.groupSize = groupSize;
            this.startPosition = startPosition;
            this.endPosition = startPosition + groupSize + 1;
        }
    }

    ///////////////////////////////

    public TG getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    public TC getChild(int groupPosition, int childPosition) {
        return groupList.get(groupPosition).getChildrenList().get(childPosition);
    }

    public List<TG> getGroups() {
        return groupList;
    }

    public List<TC> getChildren(int groupPosition) {
        return groupList.get(groupPosition).getChildrenList();
    }

    private final Object lock = new Object();

    public void setNewDatas(List<TG> newData) {
        synchronized (lock) {
            groupList.clear();
            groupList.addAll(newData);
            updatePositionInfo();
            notifyDataSetChanged();
        }
    }

    public boolean addGroup(TG tg) {
        synchronized (lock) {
            int lastIndex = getGroupCount();
            if (groupList.add(tg)) {
                updatePositionInfo(lastIndex);
                notifyItemRangeInserted(getCombinePosition(lastIndex), tg.getChildrenList().size() + 1);
                return true;
            } else {
                return false;
            }
        }
    }

    public void addGroup(int groupPosition, TG tg) {
        synchronized (lock) {
            groupList.add(groupPosition, tg);
            updatePositionInfo(groupPosition);
            notifyItemRangeInserted(getCombinePosition(groupPosition), tg.getChildrenList().size() + 1);
        }
    }

    public boolean addGroups(List<? extends TG> collection) {
        synchronized (lock) {
            int lastIndex = getGroupCount();
            if (groupList.addAll(collection)) {
                int size = collection.size();
                for (TG tg : collection) {
                    size += tg.getChildrenList().size();
                }
                updatePositionInfo(lastIndex);
                notifyItemRangeInserted(getCombinePosition(lastIndex), size);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean addGroups(int groupPosition, List<? extends TG> collection) {
        synchronized (lock) {
            if (groupList.addAll(groupPosition, collection)) {
                int size = collection.size();
                for (TG tg : collection) {
                    size += tg.getChildrenList().size();
                }
                updatePositionInfo(groupPosition);
                notifyItemRangeInserted(getCombinePosition(groupPosition), size);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean addChild(int groupPosition, TC tc) {
        synchronized (lock) {
            int lastIndex = getChildrenCount(groupPosition);
            if (getChildren(groupPosition).add(tc)) {
                updatePositionInfo(groupPosition);
                notifyItemInserted(getCombinePosition(groupPosition, lastIndex));
                return true;
            } else {
                return false;
            }
        }
    }

    public void addChild(int groupPosition, int childPosition, TC tc) {
        synchronized (lock) {
            getChildren(groupPosition).add(childPosition, tc);

            updatePositionInfo(groupPosition);
            notifyItemInserted(getCombinePosition(groupPosition, childPosition));
        }
    }

    public boolean addChildren(int groupPosition, List<? extends TC> collection) {
        synchronized (lock) {
            int lastIndex = getChildrenCount(groupPosition);
            if (getChildren(groupPosition).addAll(collection)) {
                updatePositionInfo(groupPosition);
                notifyItemRangeInserted(getCombinePosition(groupPosition, lastIndex), collection.size());
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean addChildren(int groupPosition, int childrenPosition, List<? extends TC> collection) {
        synchronized (lock) {
            if (getChildren(groupPosition).addAll(childrenPosition, collection)) {
                updatePositionInfo(groupPosition);
                notifyItemRangeInserted(getCombinePosition(groupPosition, childrenPosition), collection.size());
                return true;
            } else {
                return false;
            }
        }
    }

    public void clearGroups() {
        synchronized (lock) {
            int size = getItemCount();
            if (size > 0) {
                groupList.clear();
                updatePositionInfo(0);
                notifyItemRangeRemoved(0, size);
            }
        }
    }

    public void clearChildren(int groupPosition) {
        synchronized (lock) {
            int size = getChildrenCount(groupPosition);
            getChildren(groupPosition).clear();
            updatePositionInfo(groupPosition);
            notifyItemRangeRemoved(getCombinePosition(groupPosition) + 1, size);
        }
    }

    public TG removeGroup(int groupPosition) {
        synchronized (lock) {
            int size = getChildrenCount(groupPosition) + 1;
            TG tg = groupList.remove(groupPosition);
            updatePositionInfo(groupPosition);
            notifyItemRangeRemoved(getCombinePosition(groupPosition), size);
            return tg;
        }
    }

    public TC removeChild(int groupPosition, int childPosition) {
        synchronized (lock) {
            TC tc = getChildren(groupPosition).remove(childPosition);
            updatePositionInfo(groupPosition);
            notifyItemRemoved(getCombinePosition(groupPosition, childPosition));
            return tc;
        }
    }

    public int indexOfGroup(TG tg) {
        return groupList.indexOf(tg);
    }

    public int indexOfChild(int groupPosition, TC tc) {
        return getChildren(groupPosition).indexOf(tc);
    }

    public int lastIndexOfGroup(TG tg) {
        return groupList.lastIndexOf(tg);
    }

    public int lastIndexOfChild(int groupPosition, TC tc) {
        return getChildren(groupPosition).lastIndexOf(tc);
    }

    public boolean isGroupEmpty() {
        return groupList.isEmpty();
    }

    public boolean isChildrenEmpty(int groupPosition) {
        return getChildren(groupPosition).isEmpty();
    }

    public ListIterator<TG> groupListIterator() {
        return groupList.listIterator();
    }

    public ListIterator<TG> groupListIterator(int groupPosition) {
        return groupList.listIterator(groupPosition);
    }

    public ListIterator<TC> childListIterator(int groupPosition) {
        return getChildren(groupPosition).listIterator();
    }

    public ListIterator<TC> childListIterator(int groupPosition, int childPosition) {
        return getChildren(groupPosition).listIterator(childPosition);
    }

}
