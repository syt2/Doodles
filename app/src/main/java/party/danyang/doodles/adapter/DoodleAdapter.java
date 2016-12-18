package party.danyang.doodles.adapter;

import android.content.Context;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import party.danyang.doodles.R;
import party.danyang.doodles.entity.Doodle;
import party.danyang.doodles.entity.MonthDoodle;
import party.danyang.doodles.widget.GroupAdapter;

/**
 * Created by dream on 16-12-13.
 */

public class DoodleAdapter extends GroupAdapter<Doodle, MonthDoodle> {
    private static final int TYPE_HEADER = -1;
    private static final int TYPE_CONTENT = -2;

    private Context context;

    public DoodleAdapter(Context context) {
        this.context = context;
    }

    @Override
    public void onBindChildViewHolder(RecyclerView.ViewHolder holder, final Doodle child) {
        final ContentViewHolder cvh = (ContentViewHolder) holder;
        cvh.title.setText(child.getTitle());
        cvh.month.setText(child.getMonthString());
        cvh.week.setText(child.getWeekString());

        Glide.with(context)
                .load(child.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(cvh.img) {
                    @Override
                    public void onStart() {
                        super.onStart();
                        cvh.progressBar.show();
                    }

                    @Override
                    public void onStop() {
                        super.onStop();
                        cvh.progressBar.hide();
                    }
                });

        if (mOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.onClickChild(view, child);
                }
            });
        }
    }

    @Override
    public void onBindGroupViewHolder(RecyclerView.ViewHolder holder, final MonthDoodle group) {
        ((HeaderViewHolder) holder).header.setText(group.getDateString());
    }

    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent) {
        return new ContentViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doodle, parent, false));
    }

    @Override
    public RecyclerView.ViewHolder onCreateGroupViewHolder(ViewGroup parent) {
        return new HeaderViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doodle_header, parent, false));
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder {
        public TextView week;
        public TextView month;
        public ImageView img;
        public TextView title;

        public ContentLoadingProgressBar progressBar;

        public ContentViewHolder(View itemView) {
            super(itemView);
            week = (TextView) itemView.findViewById(R.id.week);
            month = (TextView) itemView.findViewById(R.id.month);
            title = (TextView) itemView.findViewById(R.id.title);
            img = (ImageView) itemView.findViewById(R.id.img);

            progressBar = (ContentLoadingProgressBar) itemView.findViewById(R.id.progressbar);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header);
        }
    }

    public interface OnClickListener {
//        void onClickGroup(View v, MonthDoodle group);

        void onClickChild(View v, Doodle child);
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }
}
