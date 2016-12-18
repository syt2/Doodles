package party.danyang.doodles.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import party.danyang.doodles.R;
import party.danyang.doodles.entity.SimpleDoodle;

/**
 * Created by dream on 16-8-8.
 */
public class SimpleDoodleAdapter extends RecyclerView.Adapter<SimpleDoodleAdapter.ViewHolder> {
    private List<SimpleDoodle> data;
    private Context context;

    public SimpleDoodleAdapter(Context context) {
        this.context = context;
        data = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setNewDatas(List<SimpleDoodle> list) {
        this.data.clear();
        data.addAll(list);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(context)
                .load(data.get(position).getImgUrl())
                .into(holder.img);
        if (mOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.onClick(view, data.get(position));
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return mOnClickListener.onLongClick(view, data.get(position));
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_simple_doodle, parent, false));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }

    public interface OnClickListener {
        void onClick(View v, SimpleDoodle simpleDoodle);

        boolean onLongClick(View v, SimpleDoodle simpleDoodle);
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

}
