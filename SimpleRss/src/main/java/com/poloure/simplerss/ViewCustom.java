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

class ViewCustom extends View
{
   static final int IMAGE_HEIGHT = 360;
   static final Paint[] PAINTS = new Paint[3];
   private static final int[] COLORS = {255, 165, 205};
   private static final float[] SIZES = {16.0F, 12.0F, 14.0F};
   private static final int screen = Resources.getSystem().getDisplayMetrics().widthPixels;

   static
   {
      for(int i = 0; 3 > i; i++)
      {
         PAINTS[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
         PAINTS[i].setTextSize(Utilities.getSp(SIZES[i]));
         PAINTS[i].setARGB(COLORS[i], 0, 0, 0);
      }
   }

   private Bitmap m_image;
   FeedItem m_item;
   private final int m_height;

   @Override
   public
   void onDraw(Canvas canvas)
   {
      /* If the canvas is meant to draw a bitmap but it is null, draw nothing.
         NOTE: This value must change if the AdapterTags heights are changing. */
      if(200 < getHeight() && null == m_image)
      {
         return;
      }

      float verticalPosition = drawBase(canvas);
      verticalPosition = drawBitmap(canvas, verticalPosition);
      if(null != m_item.m_desLines && 0 != m_item.m_desLines.length && null != m_item.m_desLines[0])
      {
         drawDes(canvas, verticalPosition);
      }
   }

   ViewCustom(Context context, int height)
   {
      super(context);
      m_height = height;

      setLayerType(LAYER_TYPE_HARDWARE, null);
      Utilities.setPaddingEqual(this, Utilities.EIGHT_DP);
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

      /* Draw the title. */
      //if((char) 0x200F == m_item.m_title.charAt(0))
      if(Utilities.isRtl(m_item.m_title.charAt(0)))
      {
         PAINTS[0].setTextAlign(Paint.Align.RIGHT);
         PAINTS[1].setTextAlign(Paint.Align.RIGHT);

         canvas.drawText(m_item.m_title, screen - getPaddingRight(), verticalPosition, PAINTS[0]);
         verticalPosition += PAINTS[0].getTextSize();

         canvas.drawText(m_item.m_url, screen - getPaddingRight(), verticalPosition, PAINTS[1]);

         /* Reset the paints. */
         PAINTS[0].setTextAlign(Paint.Align.LEFT);
         PAINTS[1].setTextAlign(Paint.Align.LEFT);
      }
      else
      {
         canvas.drawText(m_item.m_title, getPaddingLeft(), verticalPosition, PAINTS[0]);
         verticalPosition += PAINTS[0].getTextSize();

         canvas.drawText(m_item.m_url, getPaddingLeft(), verticalPosition, PAINTS[1]);
      }

      return verticalPosition + PAINTS[1].getTextSize();
   }

   void drawDes(Canvas canvas, float verticalPosition)
   {
      if(m_item.m_desLines[0].isEmpty())
      {
         return;
      }

      float position = verticalPosition;

      boolean rtl = Utilities.isRtl(m_item.m_desLines[0].charAt(0));
      if(rtl)
      {
         PAINTS[2].setTextAlign(Paint.Align.RIGHT);
      }
      for(int i = 0; 3 > i; i++)
      {
         canvas.drawText(m_item.m_desLines[i], rtl ? screen - getPaddingRight() : getPaddingLeft(),
               position, PAINTS[2]);
         position += PAINTS[2].getTextSize();
      }
      PAINTS[2].setTextAlign(Paint.Align.LEFT);
   }

   float drawBitmap(Canvas canvas, float verticalPosition)
   {
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0.0F, verticalPosition, PAINTS[0]);
         return verticalPosition + IMAGE_HEIGHT + 32;
      }
      else
      {
         return verticalPosition + Utilities.getDp(4.0F);
      }
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
