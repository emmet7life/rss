package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;

public class card_adapter extends BaseAdapter
{
	private List<String> content_titles = new ArrayList();
	private List<String> content_des = new ArrayList();
	private List<String> content_links = new ArrayList();
	//private List<Drawable> content_icons = new ArrayList();

	LayoutInflater inflater;

	private final Context context;

	public card_adapter(Context context)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add_list(String new_title, String new_des, String new_link/*, Drawable new_icons*/)
	{
		content_titles.add(new_title);
		content_des.add(new_des);
		content_links.add(new_link);
		//content_icons.add(new_icon);
	}

	public void clear_list(){
		content_titles = new ArrayList();
		content_des = new ArrayList();
		content_links = new ArrayList();
		//content_icons = new ArrayList();
	}

	public List<String> return_titles(){
		return content_titles;
	}

	@Override
	public int getCount(){
		return content_titles.size();
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public String getItem(int position){
		return content_titles.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.card_layout, parent, false);
				holder = new ViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title);
				holder.time_view = (TextView) convertView.findViewById(R.id.time);
				holder.description_view = (TextView) convertView.findViewById(R.id.description);
				//holder.image_view = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			holder.title_view.setText(content_titles.get(position));
			holder.time_view.setText(content_links.get(position));
			holder.description_view.setText(content_des.get(position));
			/*try{
				holder.image_view.setImageDrawable(content_icons.get(position));
			}
			catch(Exception e){}*/
			
			return convertView;
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		//ImageView image_view;
	}
} 
