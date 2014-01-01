package com.poloure.simplerss;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class AsyncRefreshPage extends AsyncTask<Integer, Object, Void>
{
   private static final int MAX_DESCRIPTION_LENGTH = 360;
   private static final int MIN_DESCRIPTION_LENGTH = 8;
   private final String m_applicationFolder;
   private final ListView m_listView;
   private final boolean m_isAllTag;

   private
   AsyncRefreshPage(ListView listView, String applicationFolder, boolean isAllTag)
   {
      m_listView = listView;
      m_applicationFolder = applicationFolder;
      m_isAllTag = isAllTag;
   }

   static
   void newInstance(int pageNumber, ListView listView, String storage, boolean isAllTag)
   {
      AsyncTask<Integer, Object, Void> task = new AsyncRefreshPage(listView, storage, isAllTag);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
   }

   /*private static
   boolean containsArabic(String text)
   {
      char[] chars = text.toCharArray();
      for(int i : chars)
      {
         if(0x600 <= i && 0x6ff >= i)
         {
            return true;
         }
         if(0x750 <= i && 0x77f >= i)
         {
            return true;
         }
         if(0xfb50 <= i && 0xfc3f >= i)
         {
            return true;
         }
         if(0xfe70 <= i && 0xfefc >= i)
         {
            return true;
         }
      }
      return false;
   }*/

   @Override
   protected
   Void doInBackground(Integer... page)
   {
      int pageNumber = page[0];
      String tag = PagerAdapterFeeds.getTagsArray()[pageNumber];
      String thumbnailDir = File.separatorChar + ServiceUpdate.THUMBNAIL_DIR;
      String contentFile = File.separatorChar + ServiceUpdate.CONTENT;

      /* Create a String array to store the description lines. */
      String[] desLines = new String[3];

      String[][] feedsIndex = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 't');
      if(0 == feedsIndex.length)
      {
         return null;
      }
      String[] feedNames = feedsIndex[0];
      String[] feedTags = feedsIndex[1];

      Comparator<Long> reverse = Collections.reverseOrder();
      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>(reverse);

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();
      final List<Long> timeListInAdapter = adapterTag.getTimeList();

      int feedsLength = feedNames.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(m_isAllTag || feedTags[j].contains(tag))
         {
            String[][] content = Read
                  .csvFile(feedNames[j] + contentFile, m_applicationFolder, 't', 'l', 'b', 'i', 'p',
                        'x', 'y', 'z');

            if(0 == content.length)
            {
               return null;
            }

            String[] titles = content[0];
            String[] links = content[1];
            String[] trimmedLinks = content[2];
            String[] imageUrls = content[3];
            String[] times = content[4];
            String[] descriptionsX = content[5];
            String[] descriptionsY = content[6];
            String[] descriptionsZ = content[7];

            String feedThumbnailDir = feedNames[j] + thumbnailDir;

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               data.m_imageName = "";
               if(null != imageUrls[i])
               {
                  int lastSlash = imageUrls[i].lastIndexOf(File.separatorChar) + 1;
                  data.m_imageName = feedThumbnailDir + imageUrls[i].substring(lastSlash);

                  /* If we have not downloaded the image yet, fake no image. */
                  File image = new File(m_applicationFolder + data.m_imageName);
                  if(!image.exists())
                  {
                     data.m_imageName = "";
                  }
               }

               boolean desTooShort = null == descriptionsX[i] ||
                                     MIN_DESCRIPTION_LENGTH > descriptionsX[i].length();

               data.m_itemDescriptionOne = desTooShort ? "" : descriptionsX[i];
               data.m_itemDescriptionTwo = desTooShort ? "" : descriptionsY[i];
               data.m_itemDescriptionThree = desTooShort ? "" : descriptionsZ[i];

               data.m_itemTime = fastParseLong(times[i]);

               data.m_itemTitle = null == titles[i] ? "" : titles[i];
               data.m_url = null == trimmedLinks[i] ? "" : trimmedLinks[i];
               data.m_urlFull = links[i];

               /* TODO */
               /*if(containsArabic(itemTitle))
               {
                  editable.append((char) 0x200F);
               }*/

               /* Do not add duplicates, do not add read items if opacity == 0 */
               boolean notInAdapter = !timeListInAdapter.contains(data.m_itemTime);

               if(notInAdapter)
               {
                  map.put(data.m_itemTime, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      adapterTag.m_isReadingItems = false;

      int mapSize = map.size();
      Collection<FeedItem> itemCollection = map.values();
      Set<Long> longSet = map.keySet();
      Long[] longArray = longSet.toArray(new Long[mapSize]);
      List<Long> longList = Arrays.asList(longArray);

      if(0 < itemCollection.size())
      {
         publishProgress(itemCollection, longList);
      }
      return null;
   }

   private static
   long fastParseLong(String s)
   {
      if(null == s)
      {
         return 0L;
      }
      char[] chars = s.toCharArray();
      long num = 0L;

      for(char c : chars)
      {
         int value = (int) c - 48;
         num = num * 10L + (long) value;
      }
      return num;
   }

   @Override
   protected
   void onPostExecute(Void result)
   {
      /* Resume Read item checking. */
      Adapter adapterTag = m_listView.getAdapter();
      ((AdapterTags) adapterTag).m_isReadingItems = true;
   }

   @Override
   protected
   void onProgressUpdate(Object... values)
   {
      int top = 0;
      int index = 0;
      long timeBefore = 0L;
      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();
      final List<Long> timeListInAdapter = adapterTag.getTimeList();

      boolean notFirstLoad = 0 != m_listView.getCount();

      /* Find the exact mPosition in the list. */
      if(notFirstLoad)
      {
         /* Get the time of the top item. */
         index = m_listView.getFirstVisiblePosition();
         timeBefore = timeListInAdapter.get(index);

         View v = m_listView.getChildAt(0);
         top = null == v ? 0 : v.getTop();
         if(0 != top)
         {
            View childAt = m_listView.getChildAt(1);
            if(null != childAt)
            {
               top = childAt.getTop();
            }
         }
      }

      adapterTag.prependArray(values[0], values[1]);
      adapterTag.notifyDataSetChanged();

      /* If this was the first time loading the tag data, jump to the latest unread item. */
      if(notFirstLoad)
      {
         /* We now need to find the position of the item with the time timeBefore. */
         /* NOTE Do not change anything in itemList. */
         int timeListSize = timeListInAdapter.size();
         int i = 0;
         while(i < timeListSize && 0 == index)
         {
            boolean sameItem = timeBefore == timeListInAdapter.get(i);
            if(sameItem)
            {
               index = i + 1;
            }
            i++;
         }

         int listViewPaddingTop = m_listView.getPaddingTop();
         m_listView.setSelectionFromTop(index, top - listViewPaddingTop);
      }
      else
      {
         m_listView.setBackgroundColor(Color.TRANSPARENT);
         FeedsActivity.gotoLatestUnread(m_listView);
      }
   }
}
