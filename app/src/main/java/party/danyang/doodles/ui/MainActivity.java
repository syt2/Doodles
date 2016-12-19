package party.danyang.doodles.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import party.danyang.doodles.R;
import party.danyang.doodles.adapter.DoodleAdapter;
import party.danyang.doodles.databinding.ActivityMainBinding;
import party.danyang.doodles.entity.Doodle;
import party.danyang.doodles.entity.MonthDoodle;
import party.danyang.doodles.net.DoodleApi;
import party.danyang.doodles.utils.PreferencesHelper;
import party.danyang.doodles.utils.Utils;
import party.danyang.doodles.widget.GroupRecyclerView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static rx.subscriptions.Subscriptions.unsubscribed;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private DoodleAdapter adapter;
    private boolean isLoad;
    private int y, m;

    private long lastClickTime;
    private boolean inCustomDateMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        initToolbar();
        initRecy();
        initRefresh();
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onToolbarMenuItemClicked(item);
                return true;
            }
        });
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - lastClickTime < 500) {
                    binding.recy.getRecyclerView().smoothScrollToPosition(0);
                } else {
                    lastClickTime = System.currentTimeMillis();
                }
            }
        });
    }

    private void initRecy() {
        adapter = new DoodleAdapter(this);

        adapter.setOnClickListener(new DoodleAdapter.OnClickListener() {
            @Override
            public void onClickChild(View v, Doodle child) {
                if (TextUtils.isEmpty(child.getName())) {
                    Log.e(TAG, "null item name");
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);

                intent.putExtra(ContentActivity.INTENT_TITLE, child.getTitle());
                intent.putExtra(ContentActivity.INTENT_NAME, child.getName());
                intent.putExtra(ContentActivity.INTENT_URL, child.getUrl());
                intent.putExtra(ContentActivity.INTENT_WIDTH, child.getWidth());
                intent.putExtra(ContentActivity.INTENT_HEIGHT, child.getHeight());
                intent.putExtra(ContentActivity.INTENT_RUN_DATE, child.getDateString());
//                gif cannot show successful
//                Pair<View, String> p1 = Pair.create((View) v.findViewById(R.id.img), "translation.img");
                Pair<View, String> p2 = Pair.create((View) v.findViewById(R.id.title), "translation.title");

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(MainActivity.this, p2);
                ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
            }
        });
        binding.recy.setStickyEnable(true);
        binding.recy.setAdapter(adapter);
        binding.recy.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (binding.recy.getLayoutManager().findLastVisibleItemPosition()
                        == binding.recy.getLayoutManager().getItemCount() - 1 && !inCustomDateMode) {
                    loadMore();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(MainActivity.this)
                            .resumeRequests();
                } else {
                    Glide.with(MainActivity.this)
                            .pauseRequests();
                }
            }
        });
        //解决从sticky header下拉时触发刷新的行为
        binding.recy.setTouched(new GroupRecyclerView.TouchedInView() {
            @Override
            public void touchedInsideSticky() {
                if (binding.recy.getLayoutManager().findFirstCompletelyVisibleItemPosition() != 0) {
                    binding.refresh.setEnabled(false);
                }
            }

            @Override
            public void touchedOutsideSticky() {
                if (!inCustomDateMode) {
                    binding.refresh.setEnabled(true);
                }
            }
        });
        y = Utils.getYearOfNow();
        m = Utils.getMonthOfNow();
        isLoad = false;
        inCustomDateMode = false;
        loadData();
    }

    private void initRefresh() {
        binding.refresh.setColorSchemeResources(
                R.color.md_red_500, R.color.md_yellow_500, R.color.md_green_500, R.color.md_blue_500);
        binding.refresh.setProgressViewOffset(true, 0, 200);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                y = Utils.getYearOfNow();
                m = Utils.getMonthOfNow();
                loadData(true);
            }
        });
    }

    public void loadMore() {
        if (isLoad) return;
        m--;
        if (m == 0) {
            y--;
            m = 12;
        }
        loadData();
    }

    private void setDoodlesToView(final int y, final int m, MonthDoodle monthDoodle) {
        if (y == Utils.getYearOfNow() && m == Utils.getMonthOfNow()) {
            adapter.clearGroups();
        }
        adapter.addGroup(monthDoodle);

        //solved while customData--->timeline
        if (y == Utils.getYearOfNow() && m == Utils.getMonthOfNow()) {
            binding.recy.updateStickyHeader(adapter, monthDoodle);
        }
    }

    private void loadData() {
        loadData(false);
    }

    private void loadData(boolean fromSwipLoad) {
        if (!fromSwipLoad && PreferencesHelper.getLoadFromCacheFirst(this)) {
            MonthDoodle monthDoodle = Utils.getDoodleFromCache(MainActivity.this, y + "-" + m);
            if (monthDoodle != null
                    && monthDoodle.getChildrenList() != null && monthDoodle.getChildrenList().size() > 0) {
                setDoodlesToView(y, m, monthDoodle);
                isLoad = false;
                Utils.setRefresher(binding.refresh, false);
                Log.e(TAG, "load data " + y + "-" + m + " from cache");
                return;
            }
        }

        Utils.setRefresher(binding.refresh, true);
        isLoad = true;
        DoodleApi.load(String.valueOf(y), String.valueOf(m))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Doodle>>() {
                    @Override
                    public void onCompleted() {
                        isLoad = false;
                        Utils.setRefresher(binding.refresh, false);
                        unsubscribed();
                    }

                    @Override
                    public void onError(Throwable e) {
                        isLoad = false;
                        Utils.setRefresher(binding.refresh, false);

                        if (y != Utils.getYearOfNow() || m != Utils.getMonthOfNow()) {
                            m++;
                            if (m == 13) {
                                y++;
                                m = 1;
                            }
                        }
                        Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        loadMore();
                                    }
                                }).show();
                        unsubscribed();
                    }

                    @Override
                    public void onNext(List<Doodle> doodles) {
                        if (doodles == null || doodles.size() == 0) {
                            Snackbar.make(binding.getRoot(), R.string.content_empty, Snackbar.LENGTH_LONG).show();
                            isLoad = false;
                            Utils.setRefresher(binding.refresh, false);
                            return;
                        }
                        MonthDoodle monthDoodle = new MonthDoodle();
                        monthDoodle.setList(doodles);
                        List<Integer> date = new ArrayList<Integer>();
                        date.add(y);
                        date.add(m);
                        monthDoodle.setDate(date);

                        setDoodlesToView(y, m, monthDoodle);

                        Log.e(TAG, "cached " + y + "-" + m);
                        Utils.cacheDoodle(MainActivity.this, y + "-" + m, monthDoodle);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_timeline)
                .setVisible(inCustomDateMode);
        menu.findItem(R.id.action_load_from_cache_first)
                .setChecked(PreferencesHelper.getLoadFromCacheFirst(this));
        return true;
    }

    private void onToolbarMenuItemClicked(final MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_calender:
                showDatePickerDialog();
                binding.refresh.setEnabled(false);
                break;
            case R.id.action_timeline:
                inCustomDateMode = false;
                y = Utils.getYearOfNow();
                m = Utils.getMonthOfNow();
                binding.refresh.setEnabled(true);
                loadData();
                invalidateOptionsMenu();
                break;
            case R.id.action_about:
                showAboutDialog();
                break;
            case R.id.action_load_from_cache_first:
                PreferencesHelper.setLoadFromCacheFirst(this, !menuItem.isChecked());
                invalidateOptionsMenu();
                break;
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.layout_about_dialog);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                ++m;
                if (y * 10000 + m * 100 + d >
                        Utils.getYearOfNow() * 10000 + Utils.getMonthOfNow() * 100 + Utils.getDayOfNow()) {
                    Snackbar.make(binding.getRoot(), R.string.get_doodle_beyond_today, Snackbar.LENGTH_LONG)
                            .show();
                } else if (y * 10000 + m * 100 + d < 19980830) {
                    Snackbar.make(binding.getRoot(), R.string.get_doodle_before_19980830, Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    inCustomDateMode = true;
                    loadCustomDateData(y, m, d);
                    invalidateOptionsMenu();
                }
            }
        }, Utils.getYearOfNow(), Utils.getMonthOfNow() - 1, Utils.getDayOfNow());
        dialog.show();
    }

    private void setCustomDoodlesToView(List<Doodle> doodles, final int day) {
        MonthDoodle monthDoodle = new MonthDoodle();
        MonthDoodle monthDoodleInThatDay = new MonthDoodle();

        List<Doodle> doodlesInThatDay = new ArrayList<Doodle>();
        for (Doodle doodle : doodles) {
            if (doodle.getDate() != null &&
                    doodle.getDate().get(2) == day) {
                doodlesInThatDay.add(doodle);
            }
        }
        for (Doodle doodle : doodlesInThatDay) {
            doodles.remove(doodle);
        }
        if (doodlesInThatDay.size() == 0) {
            Snackbar.make(binding.getRoot(), R.string.no_doodle_in_select_day, Snackbar.LENGTH_SHORT).show();
        }


        adapter.clearGroups();
        if (doodlesInThatDay.size() > 0) {
            List<Integer> dateInThatDay = new ArrayList<Integer>();
            dateInThatDay.add(doodlesInThatDay.get(0).getDate().get(0));
            dateInThatDay.add(doodlesInThatDay.get(0).getDate().get(1));
            dateInThatDay.add(doodlesInThatDay.get(0).getDate().get(2));
            monthDoodleInThatDay.setDate(dateInThatDay);
            monthDoodleInThatDay.setList(doodlesInThatDay);
            adapter.addGroup(monthDoodleInThatDay);
        }
        if (doodles.size() > 0) {
            List<Integer> date = new ArrayList<Integer>();
            date.add(doodles.get(0).getDate().get(0));
            date.add(doodles.get(0).getDate().get(1));
            monthDoodle.setDate(date);
            monthDoodle.setList(doodles);
            adapter.addGroup(monthDoodle);
        }

        if (doodlesInThatDay.size() != 0) {
            binding.recy.updateStickyHeader(adapter, monthDoodleInThatDay);
        } else {
            binding.recy.updateStickyHeader(adapter, monthDoodle);
        }
    }

    private void loadCustomDateData(final int year, final int month, final int day) {
        if (PreferencesHelper.getLoadFromCacheFirst(this)) {
            MonthDoodle monthDoodle = Utils.getDoodleFromCache(MainActivity.this, year + "-" + month);
            if (monthDoodle != null) {
                setCustomDoodlesToView(monthDoodle.getChildrenList(), day);
                isLoad = false;
                Utils.setRefresher(binding.refresh, false);
                return;
            }
        }
        Utils.setRefresher(binding.refresh, true);
        isLoad = true;
        DoodleApi.load(String.valueOf(year), String.valueOf(month))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Doodle>>() {
                    @Override
                    public void onCompleted() {
                        isLoad = false;
                        Utils.setRefresher(binding.refresh, false);
                        unsubscribed();
                    }

                    @Override
                    public void onError(Throwable e) {
                        isLoad = false;
                        Utils.setRefresher(binding.refresh, false);

                        Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        loadCustomDateData(year, month, day);
                                    }
                                }).show();
                        unsubscribed();
                    }

                    @Override
                    public void onNext(List<Doodle> doodles) {
                        if (doodles == null || doodles.size() == 0) {
                            Snackbar.make(binding.getRoot(), R.string.content_empty, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            loadCustomDateData(year, month, day);
                                        }
                                    }).show();

                            isLoad = false;
                            Utils.setRefresher(binding.refresh, false);
                            return;
                        }

                        //
                        MonthDoodle md = new MonthDoodle();
                        List<Integer> d = new ArrayList<Integer>();
                        d.add(doodles.get(0).getDate().get(0));
                        d.add(doodles.get(0).getDate().get(1));
                        md.setDate(d);
                        md.setList(doodles);
                        Log.e(TAG, "cached " + year + "-" + month);
                        Utils.cacheDoodle(MainActivity.this, year + "-" + month, md);
                        //

                        setCustomDoodlesToView(doodles, day);
                    }
                });
    }
}
