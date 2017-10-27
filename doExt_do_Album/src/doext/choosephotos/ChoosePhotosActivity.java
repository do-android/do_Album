package doext.choosephotos;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;

public class ChoosePhotosActivity extends Activity implements DoIModuleTypeID {
	private List<ImageBucket> dataList;
	private GridView gridView;
	private ImageBucketAdapter adapter;// 自定义的适配器
	private AlbumHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int album_bucket_activity_id = DoResourcesHelper.getIdentifier("album_bucket_activity", "layout", this);
		setContentView(album_bucket_activity_id);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		int _type = getIntent().getIntExtra("type", 0);
		dataList = helper.getImagesBucketList(_type);
		initView();
	}

	/**
	 * 初始化view视图
	 */
	private void initView() {
		int gridview_id = DoResourcesHelper.getIdentifier("gridview", "id", this);
		gridView = (GridView) findViewById(gridview_id);
		adapter = new ImageBucketAdapter(ChoosePhotosActivity.this, dataList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = getIntent();
				ImageBucket bucket = dataList.get(position);
				// 判断选择的是视频库还是照片库
				if ("Video".equals(bucket.bucketName) && helper.getVoideBucket() == bucket) {
					helper.currentType = "video";
				} else {
					helper.currentType = "image";
				}
				intent.setClass(ChoosePhotosActivity.this, ImageGridActivity.class);
				intent.putExtra("imagelist", bucket.imageList);
				startActivityForResult(intent, 100);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case 100:
			setResult(Activity.RESULT_OK, data);
			finish();
			break;
		}
	}

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}

}
