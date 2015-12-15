package com.qburst.quizapp.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.qburst.quizapp.R;
import com.qburst.quizapp.models.QASections;

public class QASectionsListAdapter extends BaseAdapter {
	
	private Context context;
	private List<QASections> sectionsList;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private ImageLoaderConfiguration config;

	public QASectionsListAdapter(Context context, List<QASections> sectionsList) {
		
		this.context = context;
		this.sectionsList = sectionsList;
		config = new ImageLoaderConfiguration.Builder(context)
				.defaultDisplayImageOptions(options).enableLogging()
				.denyCacheImageMultipleSizesInMemory().build();
		options = new DisplayImageOptions.Builder().cacheInMemory()
				.cacheOnDisc().showStubImage(R.drawable.ic_launcher).build();

		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	@Override
	public int getCount() {
		
		return sectionsList.size();
	}

	@Override
	public QASections getItem(int position) {
		
		return sectionsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(
					R.layout.sections_custom_layout_cell, parent, false);

			holder.setSectionName((TextView) convertView
					.findViewById(R.id.section_name));
			holder.sectionImage = (ImageView) convertView
					.findViewById(R.id.section_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.getSectionName().setText(getItem(position).getSectionName());
		imageLoader.displayImage(getItem(position).getImageUrl(),
				holder.sectionImage, options);
		return convertView;
	}

	public class ViewHolder {
		
		private TextView sectionName;
		private ImageView sectionImage;

		public TextView getSectionName() {
			return sectionName;
		}

		public void setSectionName(TextView sectionName) {
			this.sectionName = sectionName;
		}
	}
}
