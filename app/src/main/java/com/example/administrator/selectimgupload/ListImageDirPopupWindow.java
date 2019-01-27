package com.example.administrator.selectimgupload;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;


public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFolder>
{
	private ListView mListDir;
	private CommonAdapter<ImageFolder> adapter;

	public ListImageDirPopupWindow(int width, int height,
                                   List<ImageFolder> datas, View convertView)
	{
		super(convertView, width, height, true, datas);
	}

	@Override
	public void initViews()
	{
		mListDir = (ListView) findViewById(R.id.id_list_dir);
		adapter = new CommonAdapter<ImageFolder>(context, mDatas,
				R.layout.photo_list_dir_item)
		{
			@Override
			public void convert(ViewHolder helper, ImageFolder item)
			{
				helper.setText(R.id.id_dir_item_name, item.getName());
				helper.setImageByUrl(context,R.id.id_dir_item_image,
						item.getFirstImagePath());
				helper.setText(R.id.id_dir_item_count, item.getCount() + "张");
			}
		};
		mListDir.setAdapter(adapter);
	}

	public interface OnImageDirSelected
	{
		void selected(ImageFolder floder);
	}

	private OnImageDirSelected mImageDirSelected;

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected)
	{
		this.mImageDirSelected = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		mListDir.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
			{

				if (mImageDirSelected != null)
				{
					mImageDirSelected.selected(mDatas.get(position));
				}
			}
		});
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}

	public void notifyDataChanged(){
		adapter.notifyDataSetChanged();
	}
}
