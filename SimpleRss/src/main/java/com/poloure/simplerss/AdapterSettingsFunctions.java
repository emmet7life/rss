package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterSettingsFunctions extends BaseAdapter
{
   private static final int   TYPE_HEADING        = 0;
   private static final int   TYPE_CHECKBOX       = 1;
   private static final int   TYPE_SEEK_BAR       = 2;
   private static final int[] TYPES               = {TYPE_HEADING, TYPE_CHECKBOX, TYPE_SEEK_BAR};
   private static final int   MAX_REFRESH_MINUTES = 1440;
   private static final int   MAX_HISTORY_ITEMS   = 1000;
   private final String[]       m_functionTitles;
   private final String[]       m_functionSummaries;
   private final Context        m_context;
   private final String         m_applicationFolder;
   private final LayoutInflater m_layoutInflater;
   private       TextView       m_titleView;

   AdapterSettingsFunctions(Context context, String applicationFolder, String[] adapterTitles,
         String[] adapterSummaries)
   {
      m_context = context;
      m_applicationFolder = applicationFolder;
      m_functionTitles = adapterTitles.clone();
      m_functionSummaries = adapterSummaries.clone();
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   @Override
   public
   int getCount()
   {
      return m_functionTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_functionTitles[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      int viewType = getItemViewType(position);
      String title = m_functionTitles[position];
      String summary = m_functionSummaries[position];

      if(TYPE_HEADING == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            m_titleView = (TextView) view.findViewById(R.id.settings_heading);
         }

         m_titleView.setText(title);
      }
      else if(TYPE_CHECKBOX == viewType)
      {
         view = null == convertView ? new LayoutCheckBox(m_context) : convertView;
         ((LayoutCheckBox) view).showItem(title, summary, m_applicationFolder);
      }
      else if(TYPE_SEEK_BAR == viewType)
      {
         view = null == convertView ? new LayoutSeekBar(m_context) : convertView;

         int max = 2 == position ? MAX_REFRESH_MINUTES : MAX_HISTORY_ITEMS;

         ((LayoutSeekBar) view).showItem(title, summary, max, m_applicationFolder);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      if(0 == position || 4 == position)
      {
         return TYPE_HEADING;
      }
      else
      {
         return 2 == position || 5 == position ? TYPE_SEEK_BAR : TYPE_CHECKBOX;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }
}