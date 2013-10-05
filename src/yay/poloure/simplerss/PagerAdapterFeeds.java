package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   PagerAdapterFeeds(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public
   int getCount()
   {
      return FeedsActivity.s_currentTags.length;
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return new FragmentCard();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return FeedsActivity.s_currentTags[position];
   }
}