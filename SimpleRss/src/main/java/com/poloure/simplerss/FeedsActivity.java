package com.poloure.simplerss;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.Set;

public
class FeedsActivity extends ActionBarActivity
{
   static final         String READ_ITEMS                  = "read_items.txt";
   static final         String SETTINGS_DIR                = "settings" + File.separatorChar;
   static final         String FILTER_LIST                 = "filter_list.txt";
   private static final int    ALARM_SERVICE_START         = 1;
   private static final int    ALARM_SERVICE_STOP          = 0;
   private static final long   MINUTE_VALUE                = 60000L;
   private static final int    DEFAULT_REFRESH_TIME        = 120;
   private static final int    TAGS_LIST_VIEW_FADE_IN_TIME = 240;
   static Handler s_serviceHandler;
   String m_previousActionBarTitle;
   private ViewPager             m_feedsViewPager;
   private ListAdapter           m_adapterNavDrawer;
   private DrawerLayout          m_drawerLayout;
   private ActionBarDrawerToggle m_drawerToggle;
   private ActionBar             m_actionBar;
   private FragmentManager       m_fragmentManager;
   private String                m_applicationFolder;
   private Resources             m_resources;

   static
   void showAddFilterDialog(Context context, FragmentManager fragmentManager)
   {
      LayoutInflater inflater = LayoutInflater.from(context);
      View addFilterLayout = inflater.inflate(R.layout.add_filter_dialog, null);

      String cancelText = context.getString(R.string.cancel_dialog);
      String addText = context.getString(R.string.add_dialog);
      String addFilterText = context.getString(R.string.add_filter);

      String applicationFolder = getApplicationFolder(context);

      String fragmentTag = "android:switcher:" + FragmentManage.VIEW_PAGER_ID + ':' + 2;
      ListFragment listFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
      BaseAdapter adapter = (BaseAdapter) listFragment.getListAdapter();

      DialogInterface.OnClickListener onClickAdd = new OnClickFilterDialogAdd(addFilterLayout,
            adapter, applicationFolder);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(addFilterText);
      build.setView(addFilterLayout);
      build.setNegativeButton(cancelText, null);
      build.setPositiveButton(addText, onClickAdd);
      build.show();
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      m_resources = getResources();
      m_actionBar = getSupportActionBar();
      m_applicationFolder = getApplicationFolder(this);
      m_fragmentManager = getSupportFragmentManager();
      LayoutInflater layoutInflater = getLayoutInflater();
      DisplayMetrics displayMetrics = m_resources.getDisplayMetrics();

      /* Get what 12DP is for the AdapterNavDrawer compound drawable. */
      int twelveDp = Math.round(displayMetrics.density * 12);

      /* Load the read items to the AdapterTag class. */
      if(0 == AdapterTags.S_READ_ITEM_TIMES.size())
      {
         Set<Long> set = Read.longSet(READ_ITEMS, m_applicationFolder);
         AdapterTags.S_READ_ITEM_TIMES.addAll(set);
      }

      /* Get the navigation drawer titles. */
      String[] navigationTitles = m_resources.getStringArray(R.array.navigation_titles);

      m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      m_adapterNavDrawer = new AdapterNavDrawer(navigationTitles, twelveDp, layoutInflater);

      /* Configure the ActionBar. */
      m_actionBar.setIcon(R.drawable.rss_icon);
      m_actionBar.setDisplayHomeAsUpEnabled(true);
      m_actionBar.setHomeButtonEnabled(true);
      m_actionBar.setTitle(navigationTitles[0]);

      /* Delete the log file. */
      File file = new File(m_applicationFolder + Write.LOG_FILE);
      file.delete();

      /* Make the settings directory. */
      File folder = new File(m_applicationFolder + SETTINGS_DIR);
      folder.mkdirs();

      /* Create the navigation drawer and set all the listeners for it. */
      m_drawerToggle = new OnClickDrawerToggle(this, m_drawerLayout);
      m_drawerLayout.setDrawerListener(m_drawerToggle);

      ListView navigationList = (ListView) findViewById(R.id.navigation_drawer);
      navigationList.setAdapter(m_adapterNavDrawer);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment feedFragment = FragmentFeeds.newInstance();
         Fragment fragmentManage = FragmentManage.newInstance();
         Fragment fragmentSettings = FragmentSettings.newInstance();

         int frame = R.id.content_frame;

         FragmentTransaction transaction = m_fragmentManager.beginTransaction();
         transaction.add(frame, feedFragment, navigationTitles[0]);
         transaction.add(frame, fragmentManage, navigationTitles[1]);
         transaction.add(frame, fragmentSettings, navigationTitles[2]);
         transaction.hide(fragmentManage);
         transaction.hide(fragmentSettings);
         transaction.commit();
      }
   }

   static
   String getApplicationFolder(Context context)
   {
      /* Check the media state for the desirable state. */
      String state = Environment.getExternalStorageState();

      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(state))
      {
         return null;
      }

      File externalFilesDir = context.getExternalFilesDir(null);
      return externalFilesDir.getAbsolutePath() + File.separatorChar;
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      m_drawerToggle.onConfigurationChanged(newConfig);
   }

   @Override
   protected
   void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      setServiceIntent(ALARM_SERVICE_START);

      Write.longSet(READ_ITEMS, AdapterTags.S_READ_ITEM_TIMES, m_applicationFolder);
   }

   private
   void setServiceIntent(int state)
   {
      String[] settingTitles = m_resources.getStringArray(R.array.settings_function_titles);

      /* Load the ManageFeedsRefresh boolean value from settings. */
      String[] check = Read.file(SETTINGS_DIR + settingTitles[1] + ".txt", m_applicationFolder);
      boolean refresh = 0 == check.length || !Boolean.parseBoolean(check[0]);

      if(refresh && ALARM_SERVICE_START == state)
      {
         return;
      }

      /* Load the ManageFeedsRefresh time from settings. */
      String[] settings = Read.file(SETTINGS_DIR + settingTitles[2] + ".txt", m_applicationFolder);

      int time = 0 == settings.length || 0 == settings[0].length()
            ? DEFAULT_REFRESH_TIME
            : Integer.parseInt(settings[0]);

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = new Intent(this, ServiceUpdate.class);
      intent = configureServiceIntent(intent, 0, settingTitles, m_applicationFolder);

      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START == state)
      {
         long interval = time * MINUTE_VALUE;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
      }
      else if(ALARM_SERVICE_STOP == state)
      {
         am.cancel(pendingIntent);
      }
   }

   private static
   Intent configureServiceIntent(Intent intent, int page, String[] settingTitles,
         String applicationFolder)
   {
      /* Load notification boolean. */
      String settingFileName = SETTINGS_DIR + settingTitles[3] + ".txt";
      String[] check = Read.file(settingFileName, applicationFolder);

      boolean notificationsEnabled = 0 != check.length && Boolean.parseBoolean(check[0]);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notificationsEnabled);
      return intent;
   }

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();

      String feeds = m_resources.getStringArray(R.array.navigation_titles)[0];
      m_actionBar.setTitle(feeds);

      m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
      m_drawerToggle.setDrawerIndicatorEnabled(true);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();
      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      m_drawerToggle.syncState();

      AsyncRefreshNavigationAdapter.newInstance((BaseAdapter) m_adapterNavDrawer, m_actionBar,
            m_applicationFolder, 0);

      m_feedsViewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
      String[] navigationTitles = m_resources.getStringArray(R.array.navigation_titles);

      /* Create the OnItemClickLister for the navigation list. */
      AdapterView.OnItemClickListener onClick = new OnClickNavDrawerItem(m_fragmentManager,
            m_actionBar, m_drawerLayout, m_adapterNavDrawer, m_feedsViewPager, navigationTitles);

      ListView navigationList = (ListView) findViewById(R.id.navigation_drawer);
      navigationList.setOnItemClickListener(onClick);
   }

   @Override
   public
   boolean onCreateOptionsMenu(Menu menu)
   {
      menu.clear();

      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.activity, menu);

      /* Set the refreshItem to spin if the service is running. The handler will stop it in due
      time. */
      MenuItem refreshItem = menu.findItem(R.id.refresh);
      boolean serviceRunning = isServiceRunning();
      setRefreshingIcon(serviceRunning, refreshItem);
      return true;
   }

   /* Changes the ManageFeedsRefresh menu item to an animation if m_mode = true. */
   static
   void setRefreshingIcon(boolean isSpinning, MenuItem item)
   {
      /* Change it depending on the m_mode. */
      if(isSpinning)
      {
         MenuItemCompat.setActionView(item, R.layout.progress_circle);
      }
      else
      {
         MenuItemCompat.setActionView(item, null);
      }
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the title and the title says R.string.offline. */
      String navigationTitle = getNavigationTitle();
      CharSequence menuText = item.getTitle();

      String offline = getString(R.string.offline);
      String addFeed = getString(R.string.add_feed);
      String jumpTo = getString(R.string.unread);
      String refresh = getString(R.string.refresh);

      if(offline.equals(navigationTitle))
      {
         onBackPressed();
      }
      else if(menuText.equals(addFeed))
      {
         /* Deal with the case of add filter. */
         ViewPager viewPager = (ViewPager) findViewById(FragmentManage.VIEW_PAGER_ID);
         if(null != viewPager && 2 == viewPager.getCurrentItem() && viewPager.isShown())
         {
            showAddFilterDialog(this, m_fragmentManager);
            return true;
         }

         /* The rest is the case of add/edit feed. */
         String fragmentTag = "android:switcher:" + FragmentManage.VIEW_PAGER_ID + ':' + 1;

         ListFragment listFragment = (ListFragment) m_fragmentManager.findFragmentByTag(
               fragmentTag);

         ListView listView = null == listFragment ? null : listFragment.getListView();

         ListFragmentManageFeeds.showEditDialog(listView, -1, m_applicationFolder, this);
      }
      else if(menuText.equals(jumpTo))
      {
         int currentPage = m_feedsViewPager.getCurrentItem();

         String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + currentPage;

         ListFragment listFragment = (ListFragment) m_fragmentManager.findFragmentByTag(
               fragmentTag);
         ListView listViewTags = listFragment.getListView();

         gotoLatestUnread(listViewTags);
      }
      else if(menuText.equals(refresh))
      {
         refreshFeeds(item);
      }
      else
      {
         return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
      }

      return true;
   }

   static
   void gotoLatestUnread(ListView listView)
   {
      Adapter listAdapter = listView.getAdapter();

      int itemCount = listAdapter.getCount() - 1;
      for(int i = itemCount; 0 <= i; i--)
      {
         FeedItem feedItem = (FeedItem) listAdapter.getItem(i);
         if(!AdapterTags.S_READ_ITEM_TIMES.contains(feedItem.m_itemTime))
         {
            listView.setSelection(i);
            break;
         }
      }

      if(!listView.isShown() || 0 == itemCount)
      {
         Animation animation = new AlphaAnimation(0.0F, 1.0F);
         animation.setDuration(TAGS_LIST_VIEW_FADE_IN_TIME);
         listView.setAnimation(animation);
         listView.setVisibility(View.VISIBLE);
      }
   }

   String getNavigationTitle()
   {
      CharSequence title = m_actionBar.getTitle();
      return title.toString();
   }

   /* Updates and refreshes the tags with any new content. */
   private
   void refreshFeeds(MenuItem menuItem)
   {
      MenuItemCompat.setActionView(menuItem, R.layout.progress_circle);

      /* Set the service handler in FeedsActivity so we can check and call it from ServiceUpdate. */
      s_serviceHandler = new ServiceHandler(m_fragmentManager, menuItem, m_applicationFolder);

      int currentPage = m_feedsViewPager.getCurrentItem();

      String[] settingTitles = m_resources.getStringArray(R.array.settings_function_titles);

      Intent intent = new Intent(this, ServiceUpdate.class);
      intent = configureServiceIntent(intent, currentPage, settingTitles, m_applicationFolder);
      startService(intent);
   }

   private
   boolean isServiceRunning()
   {
      ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
      for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(
            Integer.MAX_VALUE))
      {
         String className = service.service.getClassName();
         String serviceName = ServiceUpdate.class.getName();
         if(serviceName.equals(className))
         {
            return true;
         }
      }
      return false;
   }

   void setNavigationTitle(CharSequence title, boolean saveTitle)
   {
      if(saveTitle)
      {
         m_previousActionBarTitle = getNavigationTitle();
      }
      m_actionBar.setTitle(title);
   }
}