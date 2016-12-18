package party.danyang.doodles.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by dream on 16-12-13.
 */

public class GroupRecyclerView extends FrameLayout {
    private static final String TAG = "GroupRecyclerView";

    private Context mContext;

    private RecyclerView mRecy;
    private LinearLayoutManager mLayoutManager;
    private GroupAdapter mAdapter;
    private RecyclerView.ViewHolder mStickyViewHolder;
    private GroupEntity mStickyGroup;
    private boolean mStickyEnable;

    private long lastClickTime;

    public GroupRecyclerView(Context context) {
        this(context, null);
    }

    public GroupRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        mRecy = new RecyclerView(context) {
            @Override
            public boolean fling(int velocityX, int velocityY) {
                return super.fling(velocityX, velocityY / 2);//抛掷速度
            }
        };
        mRecy.setVerticalScrollBarEnabled(false);
        addView(mRecy, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mLayoutManager = new LinearLayoutManager(context);
//        {
//            @Override
//            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//                return super.scrollVerticallyBy(dy, recycler, state);
//            }
//        };
        mRecy.setLayoutManager(mLayoutManager);
        mRecy.setHasFixedSize(true);
        mRecy.setScrollingTouchSlop(10);
        initListener();
    }

    public void initListener() {
        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!mStickyEnable) return;
                int firstItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (mStickyViewHolder != null && mAdapter.getItemCount() > firstItemPosition) {
                    boolean isGroupType = mAdapter.isGroupType(firstItemPosition);
                    int groupPosition = mAdapter.getGroupPosition(firstItemPosition);
                    GroupEntity group = mAdapter.getGroup(groupPosition);

                    if (!group.shouldShow() && mStickyViewHolder.itemView.getVisibility() == VISIBLE) {
                        mStickyGroup = null;
                        mStickyViewHolder.itemView.setVisibility(INVISIBLE);
                    }


                    if (firstItemPosition + 1 < mAdapter.getItemCount()) {
                        int nextFirstVisibleGroupPosition = mAdapter.getGroupPosition(firstItemPosition + 1);
                        //不一样的group---> change
                        if (nextFirstVisibleGroupPosition != groupPosition) {
                            View nextFirstVisibleGroupView = mLayoutManager.findViewByPosition(firstItemPosition + 1);
                            if (nextFirstVisibleGroupView.getTop() <= mStickyViewHolder.itemView.getHeight() && group.shouldShow()) {
                                mStickyViewHolder.itemView.setTranslationY(
                                        nextFirstVisibleGroupView.getTop() - mStickyViewHolder.itemView.getHeight());
                            }
                            stickyNewViewHolder(mAdapter, group);
                        } else if (mStickyViewHolder.itemView.getTranslationY() != 0) {
                            mStickyViewHolder.itemView.setTranslationY(0);
                        }
                    }

                    if (isGroupType) {
                        stickyNewViewHolder(mAdapter, group);
                    } else if (dy < 0 && mStickyViewHolder.itemView.getVisibility() != VISIBLE) {
                        View nextGroupView = mLayoutManager.findViewByPosition(firstItemPosition);
                        if (nextGroupView.getBottom() >= mStickyViewHolder.itemView.getHeight()) {
                            stickyNewViewHolder(mAdapter, group);
                        }
                    }
                }
            }
        });
    }

    public <TC, TG extends GroupEntity<TC>> void updateStickyHeader(GroupAdapter<TC, TG> adapter, TG group) {
        if (mStickyViewHolder.itemView.getVisibility() != VISIBLE) {
            mStickyViewHolder.itemView.setVisibility(VISIBLE);
        }
        mStickyGroup = group;
        adapter.onBindGroupViewHolder(mStickyViewHolder, group);
    }

    private <TC, TG extends GroupEntity<TC>> void stickyNewViewHolder(GroupAdapter<TC, TG> adapter, TG group) {
        if (group.shouldShow() && !group.equals(mStickyGroup)) {
            if (mStickyViewHolder.itemView.getVisibility() != VISIBLE) {
                mStickyViewHolder.itemView.setVisibility(VISIBLE);
            }
            mStickyGroup = group;
            adapter.onBindGroupViewHolder(mStickyViewHolder, group);
        }
    }

    private <TC, TG extends GroupEntity<TC>> void initStickyView(final GroupAdapter<TC, TG> adapter) {
        mStickyViewHolder = adapter.onCreateGroupViewHolder(mRecy);

        //////////双机事件
        mStickyViewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - lastClickTime < 500) {
                    int firstItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                    int groupPosition = mAdapter.getGroupPosition(firstItemPosition);
                    mRecy.smoothScrollToPosition(mAdapter.getCombinePosition(groupPosition));
                } else {
                    lastClickTime = System.currentTimeMillis();
                }
            }
        });

        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == mRecy) {
                mStickyViewHolder.itemView.setVisibility(INVISIBLE);
                addView(mStickyViewHolder.itemView, i + 1);
                return;
            }
        }
    }

    public <TC, TG extends GroupEntity<TC>> void setAdapter(GroupAdapter<TC, TG> adapter) {
        this.mAdapter = adapter;
        mRecy.setAdapter(adapter);
        if (mStickyEnable) {
            initStickyView(adapter);
        }
    }

    public void setStickyEnable(boolean enable) {
        this.mStickyEnable = enable;
    }

    public RecyclerView getRecyclerView() {
        return mRecy;
    }

    public LinearLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    //solve pull while touched inside sticky header view
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() >= mStickyViewHolder.itemView.getLeft() && ev.getX() <= mStickyViewHolder.itemView.getRight() &&
                    ev.getY() >= mStickyViewHolder.itemView.getTop() && ev.getY() <= mStickyViewHolder.itemView.getBottom()) {
                touched.touchedInsideSticky();
            } else {
                touched.touchedOutsideSticky();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setTouched(TouchedInView touched) {
        this.touched = touched;
    }

    private TouchedInView touched;

    public interface TouchedInView {
        void touchedInsideSticky();

        void touchedOutsideSticky();
    }

}
