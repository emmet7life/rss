package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   private final Fragment    m_fragment;
   private final BaseAdapter m_navigationAdapter;

   OnPageChangeTags(Fragment fragment, BaseAdapter navigationAdapter)
   {
      m_fragment = fragment;
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   public
   void onPageSelected(int pos)
   {
      FragmentManager fragmentManager = m_fragment.getFragmentManager();
      ListView listView = Util.getFeedListView(pos, fragmentManager);

      ActionBarActivity activity = (ActionBarActivity) m_fragment.getActivity();

      String[] tags = Read.file(Constants.TAG_LIST, activity);

      String unread = (String) m_navigationAdapter.getItem(pos);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(tags[pos] + " | " + unread);

      ListAdapter adapter = listView.getAdapter();
      if(0 == adapter.getCount())
      {
         Context context = m_fragment.getActivity();
         Update.page(m_navigationAdapter, pos, fragmentManager, context);
      }
   }
}
