package yay.poloure.simplerss;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener
{

	private final String TAG = getClass().getName();

	private static final String ANDROIDNS="http://schemas.android.com/apk/res/android";
	private static final String ROBOBUNNYNS="http://robobunny.com";
	private static final int DEFAULT_VALUE = 50;

	private static final String[] names = new String[]{"15m", "30m", "45m", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h", "16h", "1d", "2d", "7d", "1M"};

	private int mMaxValue		= 100;
	private int mMinValue		= 0;
	private int mCurrentValue;
	private String mUnitsLeft	= "";
	private String mUnitsRight	= "";
	private SeekBar mSeekBar;
	private static final int defaultValue = 50;

	private TextView mStatusText;

	public SeekBarPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPreference(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs)
	{
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	private void setValuesFromXml(AttributeSet attrs)
	{
		mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		mMinValue = attrs.getAttributeIntValue(ROBOBUNNYNS, "min", 0);

		mUnitsLeft = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsLeft", "");
		String units = getAttributeStringValue(attrs, ROBOBUNNYNS, "units", "");
		mUnitsRight = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsRight", units);

		try
		{
			String newInterval = attrs.getAttributeValue(ROBOBUNNYNS, "interval");
		}
		catch(Exception e)
		{
			Log.e(TAG, "Invalid interval value", e);
		}

	}

	private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue)
	{
		String value = attrs.getAttributeValue(namespace, name);
		if(value == null)
			value = defaultValue;

		return value;
	}

	@Override
	protected View onCreateView(ViewGroup parent)
	{

		RelativeLayout layout =  null;

		try
		{
			LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			layout = (RelativeLayout)mInflater.inflate(R.layout.seek_bar_preference, parent, false);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Error creating seek bar preference", e);
		}

		return layout;

	}

	@Override
	public void onBindView(View view)
	{
		super.onBindView(view);

		try
		{
			ViewParent oldContainer = mSeekBar.getParent();
			ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

			if (oldContainer != newContainer)
			{
				if (oldContainer != null)
					((ViewGroup) oldContainer).removeView(mSeekBar);
				newContainer.removeAllViews();
				newContainer.addView(mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
			}
		}
		catch(Exception ex) {
			Log.e(TAG, "Error binding view: " + ex.toString());
		}

		updateView(view);
	}

	private void updateView(View view)
	{

		try
		{
			RelativeLayout layout = (RelativeLayout)view;

			mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
			mStatusText.setText(String.valueOf(mCurrentValue));
			mStatusText.setMinimumWidth(30);

			mSeekBar.setProgress(mCurrentValue - mMinValue);

			TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);

			TextView unitsLeft = (TextView)layout.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);

		}
		catch(Exception e){
			Log.e(TAG, "Error updating seek bar preference", e);
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		int newValue = progress + mMinValue;
		int point = 0;

		if(newValue > mMaxValue)
			newValue = mMaxValue;
		else if(newValue < mMinValue)
			newValue = mMinValue;

		for(int i = 0; i < 20; i++)
		{
			if((progress > i*5 - 3)&&(progress < i*5 + 3))
			{
				newValue = i*5;
				point = i;
				break;
			}
			else if(progress > 96)
			{
				point = 19;
				newValue = 100;
			}
		}

		// change rejected, revert to the previous value
		/*if(!callChangeListener(newValue))
		{
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}*/

		// change accepted, store it
		mCurrentValue = newValue;
		/// Convert to text
		mStatusText.setText(names[point]);
		persistInt(newValue);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		notifyChanged();
	}


	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index)
	{
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if(restoreValue)
			mCurrentValue = getPersistedInt(mCurrentValue);

		else
		{
			int temp = 0;
			try {
				temp = (Integer)defaultValue;
			}
			catch(Exception ex) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}

			persistInt(temp);
			mCurrentValue = temp;
		}

	}

}
