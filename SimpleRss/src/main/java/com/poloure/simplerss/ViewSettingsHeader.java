package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

class ViewSettingsHeader extends TextView
{
   private static final int LINE_COLOR = Color.parseColor("#CCCCCC");
   private static final float PADDING_VERTICAL = 6.0F;
   private static final float PADDING_HORIZONTAL = 4.0F;
   private static final float TEXT_SIZE = 18.0F;
   private static final int UNIT_DIP = TypedValue.COMPLEX_UNIT_DIP;
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

   private
   ViewSettingsHeader(Context context)
   {
      super(context);
   }

   static
   View newInstance(Context context)
   {
      return newInstance(context, 0, -1.0F);
   }

   static
   View newInstance(Context context, int lineColor, float textSize)
   {
      TextView textView = new ViewSettingsHeader(context);

      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int width = displayMetrics.widthPixels;

      ColorDrawable greyLine = new ColorDrawable(0 == lineColor ? LINE_COLOR : lineColor);
      greyLine.setBounds(0, 0, width, 4);

      float vPadding = TypedValue.applyDimension(UNIT_DIP, PADDING_VERTICAL, displayMetrics);
      float hPadding = TypedValue.applyDimension(UNIT_DIP, PADDING_HORIZONTAL, displayMetrics);

      int verticalPadding = Math.round(vPadding);
      int horizontalPadding = Math.round(hPadding);

      textView.setLayoutParams(LAYOUT_PARAMS);
      textView.setPadding(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding);
      textView.setCompoundDrawables(null, null, null, greyLine);
      textView.setCompoundDrawablePadding(8);
      textView.setTextSize(UNIT_DIP, 0.0F > textSize ? TEXT_SIZE : textSize);
      return textView;
   }
}