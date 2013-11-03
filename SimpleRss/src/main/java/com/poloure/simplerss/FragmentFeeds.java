package com.poloure.simplerss;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/* Must be public for rotation. */
public
class FragmentFeeds extends Fragment
{
   static final         int VIEW_PAGER_ID        = 0x1000;
   private static final int PAGER_TITLE_STRIP_ID = 165143;

   static
   Fragment newInstance()
   {
      return new FragmentFeeds();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      ActionBarActivity context = (ActionBarActivity) getActivity();

      if(null == container)
      {
         return new View(context);
      }

      setHasOptionsMenu(true);
      Resources resources = getResources();
      ActionBar actionBar = context.getSupportActionBar();
      FragmentManager fragmentManager = context.getSupportFragmentManager();
      String applicationFolder = FeedsActivity.getApplicationFolder(context);
      String allTag = resources.getString(R.string.all_tag);

      ListView navigationList = (ListView) context.findViewById(R.id.navigation_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      ViewPager.OnPageChangeListener onTagPageChange = new OnPageChangeTags(fragmentManager,
            actionBar, navigationAdapter, applicationFolder);

      PagerAdapter adapter = new PagerAdapterFeeds(fragmentManager);
      PagerAdapterFeeds.getTagsFromDisk(applicationFolder, allTag);
      adapter.notifyDataSetChanged();

      /* Create the ViewPager. */
      ViewPager viewPager = ViewPagerStrip.newInstance(context, PAGER_TITLE_STRIP_ID);
      viewPager.setAdapter(adapter);
      viewPager.setOnPageChangeListener(onTagPageChange);
      viewPager.setId(VIEW_PAGER_ID);

      return viewPager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      MenuItem refreshMenu = menu.findItem(R.id.refresh);
      MenuItem unreadMenu = menu.findItem(R.id.unread);
      MenuItem addFeedMenu = menu.findItem(R.id.add_feed);

      if(null != refreshMenu && null != unreadMenu && null != addFeedMenu)
      {
         refreshMenu.setVisible(true);
         unreadMenu.setVisible(true);
         addFeedMenu.setVisible(true);
      }
   }
}