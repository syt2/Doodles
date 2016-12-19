package party.danyang.doodles.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import party.danyang.doodles.R;
import party.danyang.doodles.adapter.SimpleDoodleAdapter;
import party.danyang.doodles.databinding.ActivityContentBinding;
import party.danyang.doodles.entity.Content;
import party.danyang.doodles.entity.SimpleDoodle;
import party.danyang.doodles.net.ContentApi;
import party.danyang.doodles.net.ContentParser;
import party.danyang.doodles.utils.PreferencesHelper;
import party.danyang.doodles.utils.Utils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static rx.subscriptions.Subscriptions.unsubscribed;

public class ContentActivity extends AppCompatActivity {
    private static final String TAG = "ContentActivity";

    public static final String INTENT_NAME = "intent.name";
    public static final String INTENT_TITLE = "intent.title";
    public static final String INTENT_URL = "intent.url";
    public static final String INTENT_WIDTH = "intent.width";
    public static final String INTENT_HEIGHT = "intent.height";
    public static final String INTENT_RUN_DATE = "intent.run_date";
    private ActivityContentBinding binding;

    private String name;
    private String url;
    private String title;
    private int width;
    private int height;
    private String runDate;

    private String describe;

    private SimpleDoodleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content);
        initViews();
    }

    private void initViews() {
        initToolbar();
        initContent();
        initHistoryRecy();
    }

    private void initHistoryRecy() {
        binding.layoutNest.recy.setNestedScrollingEnabled(false);
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onToolbarMenuItemClicked(item);
                return true;
            }
        });
    }

    private void changeToolbarColor(final int colorTo) {
        int colorFrom = ((ColorDrawable) binding.toolbar.getBackground()).getColor();
        ValueAnimator colorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (int) valueAnimator.getAnimatedValue();

                binding.toolbar.setBackgroundColor(color);
                getWindow().setStatusBarColor(color);
//                getWindow().setNavigationBarColor(color);
            }
        });
        colorAnimator.setDuration(500);
        colorAnimator.start();
    }

    private void initContent() {
        Intent i = getIntent();
        if (i != null) {
            name = i.getStringExtra(INTENT_NAME);
            title = i.getStringExtra(INTENT_TITLE);
            url = i.getStringExtra(INTENT_URL);
            width = i.getIntExtra(INTENT_WIDTH, 0);
            height = i.getIntExtra(INTENT_HEIGHT, 0);
            runDate = i.getStringExtra(INTENT_RUN_DATE);
        }

        binding.layoutNest.cardImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showSaveImgDialog();
                return true;
            }
        });

//        binding.layoutNest.cardImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loadImgToIv();
//            }
//        });

        binding.layoutNest.cardTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (TextUtils.isEmpty(title)) {
                    return false;
                }
                Utils.clipToClipboard(ContentActivity.this, title + "\n" + runDate);
                Snackbar.make(binding.getRoot(), R.string.copy_to_clipboard, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        binding.layoutNest.cardBlog.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (TextUtils.isEmpty(describe)) {
                    return false;
                }
                Utils.clipToClipboard(ContentActivity.this, describe);
                Snackbar.make(binding.getRoot(), R.string.copy_to_clipboard, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        if (!TextUtils.isEmpty(url)) {
            loadImgToIv();

            binding.layoutNest.title.setText(title);
            binding.layoutNest.time.setText(runDate);
        } else {
            binding.layoutNest.cardImg.setVisibility(View.INVISIBLE);
            binding.layoutNest.cardTitle.setVisibility(View.GONE);
        }

        binding.layoutNest.cardHistory.setVisibility(View.GONE);
        binding.layoutNest.cardBlog.setVisibility(View.GONE);

        loadContent();
    }

    private void loadImgToIv() {
        if (width == 0 || height == 0) {
            Glide.with(ContentActivity.this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .into(new GlideDrawableImageViewTarget(binding.layoutNest.img) {
                        @Override
                        public void onStop() {
                            super.onStop();

                            Glide.with(ContentActivity.this)
                                    .load(url)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            if (resource != null) {
                                                changeToolbarColor(Utils.getPaletteColor(resource));
                                            }
                                        }
                                    });
                        }
                    });
        } else {
            Glide.with(ContentActivity.this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new GlideDrawableImageViewTarget(binding.layoutNest.img) {
                        @Override
                        public void onStop() {
                            super.onStop();

                            if (width != 0 && height != 0)
                                binding.layoutNest.img.setOriginalSize(width, height);
                            Glide.with(ContentActivity.this)
                                    .load(url)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            if (resource != null) {
                                                changeToolbarColor(Utils.getPaletteColor(resource));
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    private void setContentToView(Content content) {
        initOthersContent(content);

        describe = content.getDoodleDescribe();

        if (TextUtils.isEmpty(url)) {
            url = content.getUrl();
            title = content.getTitle();
            runDate = content.getRunDate();
        }
    }

    private void loadContent() {
        if (PreferencesHelper.getLoadFromCacheFirst(this)) {
            Content content = Utils.getContenFromCache(this, name);
            if (content != null) {
                Log.e(TAG, "load content " + name + " from cache");
                setContentToView(content);
                binding.layoutNest.progressbar.hide();
                return;
            }
        }

        ContentApi.load(name)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        binding.layoutNest.progressbar.hide();
                        unsubscribed();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        loadContent();
                                    }
                                }).show();
                        binding.layoutNest.progressbar.hide();
                        unsubscribed();
                    }

                    @Override
                    public void onNext(String s) {
                        if (TextUtils.isEmpty(s)) {
                            onError(new Exception(getString(R.string.content_empty)));
                            return;
                        }
                        Content content = ContentParser.parserDoodleContent(s);
                        setContentToView(content);

                        Log.e(TAG, "cache " + name);
                        Utils.cacheContent(ContentActivity.this, name, content);
                    }
                });
    }

    private void initOthersContent(final Content content) {
        if (TextUtils.isEmpty(url)) {
            Glide.with(this)
                    .load(content.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .into(new GlideDrawableImageViewTarget(binding.layoutNest.img) {
                        @Override
                        public void onStop() {
                            super.onStop();
                            binding.layoutNest.cardImg.setVisibility(View.VISIBLE);
                            Glide.with(ContentActivity.this)
                                    .load(content.getUrl())
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            if (resource != null) {
                                                changeToolbarColor(Utils.getPaletteColor(resource));
                                            }
                                        }
                                    });
                        }
                    });
            binding.layoutNest.cardTitle.setVisibility(View.VISIBLE);
            binding.layoutNest.title.setText(content.getTitle());
            binding.layoutNest.time.setText(content.getRunDate());
        }

        if (content.getDoodleDescribe() != null &&
                !TextUtils.isEmpty(content.getDoodleDescribe().trim())) {
            binding.layoutNest.cardBlog.setVisibility(View.VISIBLE);
            binding.layoutNest.content.setText(content.getDoodleDescribe());
        }

        if (content.getHistroyDoodles() != null &&
                content.getHistroyDoodles().size() > 0) {
            binding.layoutNest.cardHistory.setVisibility(View.VISIBLE);
            adapter = new SimpleDoodleAdapter(this);
            adapter.setNewDatas(content.getHistroyDoodles());
            adapter.setOnClickListener(new SimpleDoodleAdapter.OnClickListener() {
                @Override
                public void onClick(View v, SimpleDoodle simpleDoodle) {
                    Intent intent = new Intent(ContentActivity.this, ContentActivity.class);
                    intent.putExtra(INTENT_NAME, simpleDoodle.getName());
                    startActivity(intent);
                }

                @Override
                public boolean onLongClick(View v, SimpleDoodle simpleDoodle) {
                    Snackbar.make(binding.getRoot(), simpleDoodle.getTitle(), Snackbar.LENGTH_SHORT).show();
                    return true;
                }
            });

            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            binding.layoutNest.recy.setLayoutManager(layoutManager);
            binding.layoutNest.recy.setAdapter(adapter);
        }
    }

    private void showSaveImgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_img);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Utils.saveImg(ContentActivity.this, url, name, binding.getRoot());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    private void onToolbarMenuItemClicked(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Utils.shareItem(this, name, url, title, describe);
        } else if (id == R.id.action_link) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/doodles/" + name));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Snackbar.make(binding.getRoot(), R.string.not_a_legal_link, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
