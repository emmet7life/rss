package yay.poloure.simplerss;

import yay.poloure.simplerss.card_adapter;
import yay.poloure.simplerss.parsered;
import android.content.Context;
import android.content.DialogInterface;


import android.app.AlertDialog;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView; //not permanent
import android.widget.Toast;
import android.content.res.Configuration;

import android.widget.PopupWindow;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.Gravity;


import android.os.Environment;
import java.io.File;
import java.net.URL;

import android.os.AsyncTask;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.lang.Thread;


public class main_view extends FragmentActivity
{
	private DrawerLayout mDrawerLayout;
	/// Actionbar Toggle to open navigation drawer.
	private ActionBarDrawerToggle drawer_toggle;
	private View.OnClickListener refreshListener;
	private String[] mPlanetTitles;
	private ListView mDrawerList;

	private Button btnClosePopup;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		getActionBar().setIcon(R.drawable.rss_icon);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));

		MyFragmentPagerAdapter page_adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(page_adapter);
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawer_toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle("Simple RSS");
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("Navigation");
			}
		};

		mDrawerLayout.setDrawerListener(drawer_toggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawer_toggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}

	public static class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		final int PAGE_COUNT = 3;
 
		public MyFragmentPagerAdapter(FragmentManager fm){
			super(fm);
		}
 
		@Override
		public int getCount(){
			return PAGE_COUNT;
		}

 		@Override
		public Fragment getItem(int position){
			return ArrayListFragment.newInstance(position);
		}

		 @Override
		public String getPageTitle(int position){
			if(position == 0)
				return "All";
			else if(position == 1)
				return "Technology";
			else
				return "Android";
		}
		
	}

	public static class ArrayListFragment extends ListFragment
	{		
		static ArrayListFragment newInstance(int num)
		{
			ArrayListFragment f = new ArrayListFragment();
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);
			return f;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			String[] title;
			String[] description;
			String[] time;
			
			title = new String[] { "Android", "iPhone", "WindowsMobile",
					"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
					"Linux", "OS/2" };
			description = new String[] {"This is in section 1.", "This is in section 1.", "This is in section 1.",
					"This is in section 1.", "This is in section 1.", "This is in section 1.", "This is in section 1."
					, "This is in section 1.", "This is in section 1.", "This is in section 1."};
			time = new String[] {"12:12", "12:12", "12:12", "12:12", "12:12", "12:12", "12:12", "12:12",
					"12:12", "12:12"};
			String[] content = new String[title.length*3];
			for (int i = 0; i < title.length; i++)
			{
				content[3*i] = title[i];
				content[(3*i)+1] = time[i];
				content[(3*i)+2] = description[i];
			}

			setListAdapter(new card_adapter(getActivity(), content));
		}

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){
				View view = inflater.inflate(R.layout.fragment_main_dummy, container, false);
				return view;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menu_inflater = getMenuInflater();
		menu_inflater.inflate(R.menu.main_overflow, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(drawer_toggle.onOptionsItemSelected(item))
		{
			return true;
		}
		else if(item.getTitle().equals("add"))
		{
			
			show_add_dialog();
			return true;
		}
		else if(item.getTitle().equals("refresh"))
		{
			String file_path = get_filepath("all_feeds.txt");
			String[] feeds_array = read_feeds_to_array(0, file_path);
			String[] url_array = read_feeds_to_array(1, file_path);
			String feed_path = get_filepath(feeds_array[0] + ".store");
			download_file(url_array[0], feeds_array[0] + ".store", "nocheck");

			File wait = new File(feed_path);
			int j = 0;
			while((wait.exists() == false)&&(j<100))
			{
				try
				{
					Thread.sleep(50);
				}
				catch(Exception e)
				{
				}
				j++;
			}
			
			parsered papa = new parsered(feed_path);

			wait.delete();
			
			String[] values = read_csv_to_array("title", feed_path + ".content.txt");
			toast_message(values[0], 1);
			String[] add_array = new String[] {"dicks", "cocks", "wangs"}
			/*FragmentManager fragmentManager = this.getSupportFragmentManager();
			ListFragment fragment = (ListFragment) fragmentManager.findFragmentByTag("first");
			
			fragment.setListAdapter(new card_adapter(get_context(), values));*/
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void show_add_dialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View add_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		
		String[] array_spinner = read_file_to_array("group_list.txt");
		boolean all_exists = false;
		for(int i=0; i<array_spinner.length; i++)
		{
			if(array_spinner[i].equals("All"))
			{
				all_exists = true;
				break;
			}
		}
		if(all_exists == false)
			add_group("All");

		array_spinner = read_file_to_array("group_list.txt");

		Spinner group_spinner = (Spinner) add_rss_dialog.findViewById(R.id.group_spinner);
		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.group_spinner_text, array_spinner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);
		
		final AlertDialog alertDialog = new AlertDialog.Builder(this, 2)
				.setTitle("Add Feed")
				.setView(add_rss_dialog)
				.setCancelable(true)
				.setPositiveButton
				("Add",new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				("Cancel",new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Boolean rss = false;
						EditText URL_edit = (EditText) add_rss_dialog.findViewById(R.id.URL_edit);
						String URL_check = URL_edit.getText().toString();
						File in = new File(get_filepath("URLcheck.txt"));
						in.delete();
						download_file(URL_check, "URLcheck.txt", "check_mode");
						File wait = new File(get_filepath("URLcheck.txt"));
						int j = 0;
						while((wait.exists() == false)&&(j<120))
						{
							try
							{
								Thread.sleep(10);
							}
							catch(Exception e)
							{
							}
							j++;
						}
						if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
						{
							try
							{
								BufferedReader reader = new BufferedReader(new FileReader(in));
								try{
									reader.readLine();
									if((reader.readLine().contains("rss")) == true)
										rss = true;
								}
								catch(Exception e)
								{
									rss = false;
								}
							}
							catch(Exception e)
							{
								rss = false;
							}
							in.delete();
						}
						if(rss == false)
						{
							toast_message("Invalid RSS URL", 0);
						}
						else
						{
							add_feed(((EditText) add_rss_dialog.findViewById(R.id.name_edit)).getText().toString(), URL_check, "Add");
							///A function will add this rss title and url to								
							alertDialog.dismiss();
						}
					}
				});
			}
		});
		alertDialog.show();
	}

	public void toast_message(String message, int zero_or_one)
	{
		Context context = getApplicationContext();
		Toast message_toast = Toast.makeText(context, message, zero_or_one);
		message_toast.show();
	}

	private void download_file(String url, String file_name, String check)
	{
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(url, file_name, check);
	}

	private class DownloadFile extends AsyncTask<String, Integer, String>
	{
		@Override
		protected String doInBackground(String... sUrl)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				try
				{
					URL url = new URL(sUrl[0]);
					URLConnection connection = url.openConnection();
					connection.connect();

					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(get_filepath(sUrl[1]));

					if(sUrl[2].equals("check_mode"))
					{
						byte data[] = new byte[256];
						int count;
						count = input.read(data);
						output.write(data, 0, count);
					}
					else
					{
						byte data[] = new byte[1024];
						int count;
						while ((count = input.read(data)) != -1)
							output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
				}
				catch (Exception e)
				{
				}
			}
			return null;
		}
	}

	private String get_filepath(String filename)
	{
		return this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
	}

	public void append_string_to_file(String file_name, String string)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(get_filepath(file_name), true));
				out.write(string);
				out.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void remove_string_from_file(String file_name, String string)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				String line;

				File in = new File(get_filepath(file_name));
				File out = new File(get_filepath(file_name) + ".temp");

				BufferedReader reader = new BufferedReader(new FileReader(in));
				BufferedWriter writer = new BufferedWriter(new FileWriter(out));

				while((line = reader.readLine()) != null)
				{
					if(!(line.trim().equals(string)))
						writer.write(line + "\n");
				}

				out.renameTo(in);
				reader.close();
				writer.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		append_string_to_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void delete_feed(String feed_name, String feed_url, String feed_group)
	{
		remove_string_from_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		remove_string_from_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void add_group(String group_name)
	{
		append_string_to_file("group_list.txt", "\n" + group_name + "\n");
	}

	private void delete_group(String group_name)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			remove_string_from_file("group_list.txt", group_name);
			File file = new File(get_filepath(group_name + ".txt"));
			file.delete();
		}
	}

	private String[] read_file_to_array(String file_name)
	{
		String[] line_values;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				String line;
				int number_of_lines = 0, i = 0;
				File in = new File(get_filepath(file_name));

				BufferedReader reader = new BufferedReader(new FileReader(in));

				while((line = reader.readLine()) != null)
					number_of_lines++;

				reader.close();
				reader = new BufferedReader(new FileReader(in));
				
				line_values = new String[number_of_lines];
				
				while((line = reader.readLine()) != null)
				{
					line_values[i] = line;
					i++;
				}
			}
			catch (Exception e)
			{
				line_values = new String[0];
			}
		}
		else
			line_values = new String[0];
		
		return line_values;
	}

	private String[] read_feeds_to_array(int index, String file_path)
	{
		String[] content_values;
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(file_path);

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while((line = reader.readLine()) != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));

			content_values = new String[number_of_lines];

			while((line = reader.readLine()) != null)
			{
				if(index == 0)
				{
					content_values[i] = line.substring(0, line.indexOf('|', 0));
					i++;
				}
				if(index == 1)
				{
					int bar_index = line.indexOf('|', 0);
					line = line.substring(bar_index + 1, line.indexOf('|', bar_index + 1));
					content_values[i] = line;
					i++;
				}
			}
		}
		catch (Exception e)
		{
			content_values = new String[0];
		}

		return content_values;
	}

	private String[] read_csv_to_array(String content_type, String feed_path)
	{
		String[] content_values;
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(feed_path);

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while((line = reader.readLine()) != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));

			content_values = new String[number_of_lines];

			while((line = reader.readLine()) != null)
			{
				int content_start = line.indexOf(content_type) + content_type.length() + 1;
				line = line.substring(content_start, line.indexOf('|', content_start));
				content_values[i] = line;
				i++;
			}
		}
		catch (Exception e)
		{
			content_values = new String[0];
		}

		return content_values;
	}
}

