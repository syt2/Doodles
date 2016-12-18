package party.danyang.doodles;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import party.danyang.doodles.entity.Content;
import party.danyang.doodles.entity.MonthDoodle;
import rx.functions.Action1;

/**
 * Created by dream on 16-8-9.
 */
public class Utils {
    public static int getYearOfNow() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    public static int getMonthOfNow() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        return month + 1;
    }

    public static int getDayOfNow() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static int getWeekOfNow() {
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    public static int getWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    public static int getWeek(int y, int m, int d) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(y, m - 1, d);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static void setRefresher(@NonNull final SwipeRefreshLayout refresher, final boolean isRefresh) {
        refresher.post(new Runnable() {
            @Override
            public void run() {
                refresher.setRefreshing(isRefresh);
            }
        });
    }

    public static int getPaletteColor(Bitmap bitmap) {
        int color = -12417291;
        Palette p = Palette.from(bitmap).generate();
        Palette.Swatch vibrant =
                p.getVibrantSwatch();
        Palette.Swatch vibrantdark =
                p.getDarkVibrantSwatch();
        Palette.Swatch vibrantlight =
                p.getLightVibrantSwatch();
        Palette.Swatch Muted =
                p.getMutedSwatch();
        Palette.Swatch Muteddark =
                p.getDarkMutedSwatch();
        Palette.Swatch Mutedlight =
                p.getLightMutedSwatch();

        if (vibrant != null) {
            color = vibrant.getRgb();
        } else if (vibrantdark != null) {
            color = vibrantdark.getRgb();
        } else if (vibrantlight != null) {
            color = vibrantlight.getRgb();
        } else if (Muted != null) {
            color = Muted.getRgb();
        } else if (Muteddark != null) {
            color = Muteddark.getRgb();
        } else if (Mutedlight != null) {
            color = Mutedlight.getRgb();
        }
        return color;
    }

    public static boolean isIntentSafe(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    public static Uri getLocalBitmapUri(Context context, Bitmap bmp, String name) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + name + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bmpUri = FileProvider.getUriForFile(context, "party.danyang.doodles.provider", file);
            } else {
                bmpUri = Uri.fromFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static void shareItem(final Context context, final String name, final String url, final String title, final String describe) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        intent.setDataAndType(Utils.getLocalBitmapUri(ContentActivity.this, resource, name), "image/");
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_STREAM, Utils.getLocalBitmapUri(context, resource, name));
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                        intent.putExtra(Intent.EXTRA_TEXT, describe);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        if (Utils.isIntentSafe(context, intent)) {
                            context.startActivity(intent);
                        }
                    }
                });
    }

    public static void saveImg(final Activity activity, final String url, final String name, final View snackView) {
        new RxPermissions(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            File dir = new File(Environment.getExternalStorageDirectory(), "download_doodles");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            File file = new File(dir, name + ".gif");
                            DownloadManager downloadManager = (DownloadManager)
                                    activity.getSystemService(Context.DOWNLOAD_SERVICE);
                            Uri uri = Uri.parse(url);
                            DownloadManager.Request request = new DownloadManager.Request(uri);
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                            request.setDestinationUri(Uri.fromFile(file));
                            request.setTitle(name);
                            request.setDescription(file.getAbsolutePath());
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setMimeType("image/jpeg");
                            request.allowScanningByMediaScanner();
                            downloadManager.enqueue(request);
                        } else {
                            Snackbar.make(snackView, R.string.miss_permission, Snackbar.LENGTH_SHORT)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            saveImg(activity, url, name, snackView);
                                        }
                                    }).show();
                        }
                    }
                });
    }

    public static void clipToClipboard(Context context, String text) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", text);
        cmb.setPrimaryClip(clipData);
    }

    public static void cacheDoodle(Context context, String key, MonthDoodle monthDoodle) {
        ACache cache = ACache.get(context);
//        todo  current month saved 1 day
//        cache.put(key, new Gson().toJson(monthDoodle, MonthDoodle.class), ACache.TIME_DAY);
        cache.put(key, new Gson().toJson(monthDoodle, MonthDoodle.class));
    }

    public static void cacheContent(Context context, String key, Content content) {
        ACache cache = ACache.get(context);
        cache.put(key, new Gson().toJson(content, Content.class));
    }

    public static MonthDoodle getDoodleFromCache(Context context, String key) {
        ACache cache = ACache.get(context);
        String jsonString = cache.getAsString(key);
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        return new Gson().fromJson(jsonString, MonthDoodle.class);
    }

    public static Content getContenFromCache(Context context, String key) {
        ACache cache = ACache.get(context);
        String jsonString = cache.getAsString(key);
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        return new Gson().fromJson(jsonString, Content.class);
    }
}
