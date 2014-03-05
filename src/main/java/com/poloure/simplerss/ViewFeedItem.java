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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.text.NumberFormat;
import java.util.Locale;

class ViewFeedItem extends View
{
   static final int IMAGE_HEIGHT = 360;
   private final Paint[] m_paints = new Paint[3];
   private static final int SCREEN = Resources.getSystem().getDisplayMetrics().widthPixels;

   private Bitmap m_image;
   FeedItem m_item;
   private final int m_height;
   private final String[] m_timeInitials;
   private final NumberFormat m_numberFormat;

   private static final int[] FONT_COLORS = {
         R.color.item_title_color, R.color.item_link_color, R.color.item_description_color,
   };
   private static final int[] FONT_SIZES = {
         R.dimen.item_title_size, R.dimen.item_link_size, R.dimen.item_description_size,
   };

   ViewFeedItem(Context context, int height)
   {
      super(context);
      Resources resources = context.getResources();

      m_height = height;
      m_timeInitials = resources.getStringArray(R.array.time_initials);
      m_numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

      setLayerType(LAYER_TYPE_HARDWARE, null);
      setPadding(Utilities.EIGHT_DP, Utilities.EIGHT_DP, Utilities.EIGHT_DP, Utilities.EIGHT_DP);

      initPaints(resources);
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      /* If the canvas is meant to draw a bitmap but it is null, draw nothing.
         NOTE: This value must change if the AdapterTags heights are changing. */

      /* TODO: This also needs a better implementation as it fails sometimes. */
      if(200 < getHeight() && null == m_image)
      {
         return;
      }

      /* If our paints have been cleared from memory, remake them. */
      if(null == m_paints[0])
      {
         initPaints(getResources());
      }

      float verticalPosition = drawBase(canvas);
      verticalPosition = drawBitmap(canvas, verticalPosition);
      if(null != m_item.m_desLines && 0 != m_item.m_desLines.length && null != m_item.m_desLines[0])
      {
         drawDes(canvas, verticalPosition);
      }
   }

   void initPaints(Resources resources)
   {
      for(int i = 0; m_paints.length > i; i++)
      {
         m_paints[i] = configurePaint(resources, FONT_SIZES[i], FONT_COLORS[i]);
      }
   }

   static
   Paint configurePaint(Resources resources, int dimenResource, int colorResource)
   {
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setTextSize(resources.getDimension(dimenResource));
      paint.setColor(resources.getColor(colorResource));
      return paint;
   }

   void setBitmap(Bitmap bitmap)
   {
      m_image = bitmap;
      if(null != bitmap)
      {
         invalidate();
      }
   }

   float drawBase(Canvas canvas)
   {
      /* Padding top. */
      float verticalPosition = getPaddingTop() + 20.0F;
      boolean rtl = Utilities.isTextRtl(m_item.m_title);

      int startPadding = rtl ? SCREEN - getPaddingStart() : getPaddingStart();
      int endPadding = rtl ? getPaddingStart() : SCREEN - getPaddingStart();

      Paint.Align start = rtl ? Paint.Align.RIGHT : Paint.Align.LEFT;
      Paint.Align end = rtl ? Paint.Align.LEFT : Paint.Align.RIGHT;

      /* Draw the time. */
      m_paints[1].setTextAlign(end);
      canvas.drawText(getTime(m_item.m_time), endPadding, verticalPosition, m_paints[1]);

      String[] info = {m_item.m_title, m_item.m_url};

      /* Draw the title and the url. */
      for(int i = 0; 2 > i; i++)
      {
         m_paints[i].setTextAlign(start);
         canvas.drawText(info[i], startPadding, verticalPosition, m_paints[i]);
         verticalPosition += m_paints[i].getTextSize();
      }

      return verticalPosition;
   }

   void drawDes(Canvas canvas, float verticalPos)
   {
      if(!m_item.m_desLines[0].isEmpty())
      {
         boolean rtl = Utilities.isTextRtl(m_item.m_desLines[0]);

         m_paints[2].setTextAlign(rtl ? Paint.Align.RIGHT : Paint.Align.LEFT);
         int horizontalPos = rtl ? SCREEN - getPaddingRight() : getPaddingLeft();

         for(String des : m_item.m_desLines)
         {
            canvas.drawText(des, horizontalPos, verticalPos, m_paints[2]);
            verticalPos += m_paints[2].getTextSize();
         }
      }
   }

   float drawBitmap(Canvas canvas, float verticalPosition)
   {
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0.0F, verticalPosition, m_paints[0]);
         return verticalPosition + IMAGE_HEIGHT + 32;
      }
      else
      {
         return verticalPosition + Utilities.getDp(4.0F);
      }
   }

   private
   String getTime(long time)
   {
      Long timeAgo = System.currentTimeMillis() - time;

      /* Format the time. */
      Long[] periods = {timeAgo / 86400000, timeAgo / 3600000 % 24, timeAgo / 60000 % 60};

      StringBuilder builder = new StringBuilder(48);
      for(int i = 0; periods.length > i; i++)
      {
         if(0L != periods[i])
         {
            builder.append(m_numberFormat.format(periods[i]));
            builder.append(m_timeInitials[i]);
            builder.append(' ');
         }
      }
      builder.deleteCharAt(builder.length() - 1);
      return builder.toString();
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}