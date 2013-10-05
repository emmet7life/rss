package yay.poloure.simplerss;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentManage extends Fragment
{

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setRetainInstance(false);
      setHasOptionsMenu(true);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      ViewPager pager = new ViewPager(Util.getContext());
      Constants.PAGER_TAB_STRIPS[1] = new PagerTabStrip(Util.getContext());

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      pager.addView(Constants.PAGER_TAB_STRIPS[1], layoutParams);
      pager.setAdapter(new PagerAdapterManage(FeedsActivity.s_fragmentManager));
      pager.setId(0x2000);

      Constants.PAGER_TAB_STRIPS[1].setDrawFullUnderline(true);
      Constants.PAGER_TAB_STRIPS[1].setGravity(Gravity.START);
      Constants.PAGER_TAB_STRIPS[1].setPadding(0, AdapterCard.EIGHT / 2, 0, AdapterCard.EIGHT / 2);
      Constants.PAGER_TAB_STRIPS[1].setTextColor(Color.WHITE);
      Constants.PAGER_TAB_STRIPS[1].setBackgroundColor(Color.parseColor("#404040"));
      Util.setStripColor(Constants.PAGER_TAB_STRIPS[1]);

      return pager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      FeedsActivity.s_optionsMenu = menu;
      FeedsActivity.s_optionsMenu.clear();

      inflater.inflate(R.menu.manage_overflow, FeedsActivity.s_optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.s_optionsMenu, inflater);
   }
}