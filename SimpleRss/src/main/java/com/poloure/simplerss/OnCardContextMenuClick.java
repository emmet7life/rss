package com.poloure.simplerss;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

class OnCardContextMenuClick implements DialogInterface.OnClickListener
{
   private final CharSequence m_url;
   private final Context      m_context;

   OnCardContextMenuClick(CharSequence url, Context context)
   {
      m_url = url;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      if(0 == position)
      {
         ClipboardManager clipboard = (ClipboardManager) m_context.getSystemService(
               Context.CLIPBOARD_SERVICE);

         if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
         {
            ClipData clip = ClipData.newPlainText("Url", m_url);
            clipboard.setPrimaryClip(clip);
         }
         else
         {
            clipboard.setText(m_url);
         }
      }
      else if(1 == position)
      {
         Uri uri = Uri.parse((String) m_url);
         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
         m_context.startActivity(intent);
      }
   }
}