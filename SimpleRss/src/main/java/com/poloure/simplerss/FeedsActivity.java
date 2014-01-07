/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.Set;

public
class FeedsActivity extends Activity
{
   private static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   static Handler s_serviceHandler;
   private String m_previousActionBarTitle;
   private ViewPager m_feedsViewPager;
   private ListAdapter m_adapterNavDrawer;
   private ActionBarDrawerToggle m_drawerToggle;
   private String m_applicationFolder;

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

   static
   void gotoLatestUnread(ListView listView)
   {
      Adapter listAdapter = listView.getAdapter();

      int itemCount = listAdapter.getCount() - 1;
      for(int i = itemCount; 0 <= i; i--)
      {
         FeedItem feedItem = (FeedItem) listAdapter.getItem(i);
         if(!AdapterTags.READ_ITEM_TIMES.contains(feedItem.m_time))
         {
            listView.setSelection(i);
            break;
         }
      }
   }

   private static
   View makeProgressBar(Context context)
   {
      ProgressBar progressBar = new ProgressBar(context);
      Utilities.setPaddingEqual(progressBar, Utilities.getDp(7.0F));

      return progressBar;
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      m_applicationFolder = getApplicationFolder(this);

      /* Load the read items to the AdapterTag class. */
      if(AdapterTags.READ_ITEM_TIMES.isEmpty())
      {
         Set<Long> set = Read.longSet(READ_ITEMS, m_applicationFolder);
         AdapterTags.READ_ITEM_TIMES.addAll(set);
      }

      /* Get the navigation drawer titles. */
      Resources resources = getResources();
      String[] navigationTitles = resources.getStringArray(R.array.navigation_titles);

      m_adapterNavDrawer = new AdapterNavigationDrawer(navigationTitles, this);

      /* Configure the ActionBar. */
      final ActionBar actionBar = getActionBar();
      actionBar.setIcon(R.drawable.rss_icon);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setTitle(navigationTitles[0]);

      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

      /* Create the navigation drawer and set all the listeners for it. */
      m_drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
            R.string.drawer_open, R.string.drawer_close)
      {
         final String m_navigationText = getString(R.string.navigation_title);

         @Override
         public
         void onDrawerOpened(View drawerView)
         {
            setNavigationTitle(m_navigationText, true);
         }

         @Override
         public
         void onDrawerClosed(View drawerView)
         {
            /* If the title is still R.string.navigation_title, change it to the previous title. */
            CharSequence titleChars = actionBar.getTitle();
            String title = titleChars.toString();

            if(m_navigationText.equals(title))
            {
               String previousTitle = m_previousActionBarTitle;
               setNavigationTitle(previousTitle, false);
            }
         }
      };
      drawerLayout.setDrawerListener(m_drawerToggle);

      /* The R.id.content_frame is child 0, the R.id.navigation_list is child 1. */
      ListView navigationList = (ListView) drawerLayout.getChildAt(1);
      navigationList.setAdapter(m_adapterNavDrawer);

      /* Create the FragmentFeeds that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment feedFragment = FragmentFeeds.newInstance();

         FragmentManager manager = getFragmentManager();
         FragmentTransaction transaction = manager.beginTransaction();
         transaction.add(R.id.content_frame, feedFragment, navigationTitles[0]);
         transaction.commit();
      }
   }

   void setNavigationTitle(CharSequence title, boolean saveTitle)
   {
      ActionBar actionBar = getActionBar();
      if(saveTitle)
      {
         CharSequence titleChars = actionBar.getTitle();
         m_previousActionBarTitle = titleChars.toString();
      }
      actionBar.setTitle(title);
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      m_drawerToggle.syncState();

      final ActionBar actionBar = getActionBar();
      Resources resources = getResources();

      AsyncNavigationAdapter.newInstance(this, m_applicationFolder, 0);

      m_feedsViewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
      final String[] navigationTitles = resources.getStringArray(R.array.navigation_titles);

      /* Create the OnItemClickLister for the navigation list. */
      AdapterView.OnItemClickListener onClick = new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {

            /* Close the drawer on any click. This will call the OnDrawerClose of the
            DrawerToggle. */
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawers();

            boolean tagWasClicked = 3 < position;
            boolean feedsWasClicked = 0 == position;
            int currentPage = m_feedsViewPager.getCurrentItem();
            boolean clickedDifferentPage = currentPage != position;
            String feedTitle = navigationTitles[0];

            /* Determine the new title based on the position of the item clicked. */
            String selectedTitle = tagWasClicked ? feedTitle : navigationTitles[position];

            /* If the item selected was a tag, change the FragmentFeeds ViewPager to that page. */
            if(tagWasClicked && clickedDifferentPage)
            {
               m_feedsViewPager.setCurrentItem(position - 4);
            }

            /* Set the ActionBar title without saving the previous title. */
            actionBar.setTitle(selectedTitle);

            boolean update = feedsWasClicked || tagWasClicked;
            String unreadText = getString(R.string.subtitle_unread);
            String subtitle = update ? unreadText + ' ' + m_adapterNavDrawer.getItem(currentPage)
                  : null;
            actionBar.setSubtitle(subtitle);

            /* Hide all the fragments*/
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            for(String navigationTitle : navigationTitles)
            {
               Fragment frag = manager.findFragmentByTag(navigationTitle);
               if(null != frag)
               {
                  transaction.hide(frag);
               }
            }

            /* Get the selected fragment. */
            Fragment selectedFragment = manager.findFragmentByTag(selectedTitle);

            if(null == selectedFragment)
            {
               Fragment fragment = 1 == position ? FragmentManage.newInstance()
                     : FragmentSettings.newInstance();

               transaction.add(R.id.content_frame, fragment, selectedTitle);
            }
            else
            {
               transaction.show(selectedFragment);
            }

            transaction.commit();
         }
      };

      ListView navigationList = (ListView) findViewById(R.id.navigation_list);
      navigationList.setOnItemClickListener(onClick);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);

   }

   private
   void setServiceIntent(int state)
   {
      /* Load the ManageFeedsRefresh boolean value from settings. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      boolean refreshDisabled = !preferences.getBoolean("refreshing_enabled", false);

      if(refreshDisabled && ALARM_SERVICE_START == state)
      {
         return;
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = new Intent(this, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", 0);

      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START == state)
      {
         String intervalString = preferences.getString("refresh_interval", "120");
         int refreshInterval = Integer.parseInt(intervalString);

         long interval = (long) refreshInterval * MINUTE_VALUE;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
      }
      else if(ALARM_SERVICE_STOP == state)
      {
         am.cancel(pendingIntent);
      }
   }

   @Override
   protected
   void onStop()
   {
      super.onStop();

      Write.longSet(READ_ITEMS, AdapterTags.READ_ITEM_TIMES, m_applicationFolder);

      /* Set the alarm service to go off starting now. */
      setServiceIntent(ALARM_SERVICE_START);
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
   public
   void onBackPressed()
   {
      super.onBackPressed();

      Resources resources = getResources();
      String feeds = resources.getStringArray(R.array.navigation_titles)[0];

      ActionBar actionBar = getActionBar();
      actionBar.setTitle(feeds);

      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
      m_drawerToggle.setDrawerIndicatorEnabled(true);
   }

   @Override
   public
   boolean onCreateOptionsMenu(Menu menu)
   {
      menu.clear();

      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.action_bar_menu, menu);

      /* Set the refreshItem to spin if the service is running. The handler will stop it in due
      time. */
      MenuItem refreshItem = menu.findItem(R.id.refresh);

      View refreshIcon = isServiceRunning() ? makeProgressBar(this) : null;
      MenuItemCompat.setActionView(refreshItem, refreshIcon);

      /* Update the MenuItem in the ServiceHandler so when the service finishes, the icon changes
         correctly.*/
      ServiceHandler.s_refreshItem = refreshItem;

      return true;
   }

   private
   boolean isServiceRunning()
   {
      ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
      for(ActivityManager.RunningServiceInfo service : manager
            .getRunningServices(Integer.MAX_VALUE))
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

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      CharSequence menuText = item.getTitle();

      String addFeed = getString(R.string.add_feed);
      String jumpTo = getString(R.string.unread);
      String refresh = getString(R.string.refresh);

      if(menuText.equals(addFeed))
      {
         int position = -1;
         Dialog dialog = DialogEditFeed.newInstance(this, position, m_applicationFolder);
         dialog.show();
      }
      else if(menuText.equals(jumpTo))
      {
         int currentPage = m_feedsViewPager.getCurrentItem();

         FragmentManager manager = getFragmentManager();
         ListFragment listFragment = (ListFragment) manager
               .findFragmentByTag(FragmentFeeds.FRAGMENT_ID_PREFIX + currentPage);

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

   /* Updates and refreshes the tags with any new content. */
   private
   void refreshFeeds(MenuItem menuItem)
   {
      MenuItemCompat.setActionView(menuItem, makeProgressBar(this));

      /* Set the service handler in FeedsActivity so we can check and call it from ServiceUpdate. */
      FragmentManager manager = getFragmentManager();
      s_serviceHandler = new ServiceHandler(manager, menuItem, m_applicationFolder);

      int currentPage = m_feedsViewPager.getCurrentItem();

      Intent intent = new Intent(this, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", currentPage);
      startService(intent);
   }
}
