package doext.choosephotos;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;
import doext.choosephotos.ImageGridAdapter.TextCallback;
import doext.implement.ConstantValue;
import doext.implement.MyAsyncTask;
import doext.preview.ShowPictureViewActivity;

public class ImageGridActivity extends Activity implements DoIModuleTypeID {
	private List<ImageItem> dataList;
	private GridView gridView;
	private ImageGridAdapter adapter;
	private Button btn_cancel;
	private Button btn_preview;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ImageGridActivity.this, "一次最多只能选择" + ConstantValue.MAX_COUNT + "张图片", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int album_image_activity_id = DoResourcesHelper.getIdentifier("album_image_activity", "layout", this);
		setContentView(album_image_activity_id);
		dataList = (List<ImageItem>) getIntent().getSerializableExtra("imagelist");
		initView();
	}

	private void initView() {
		int gridview_id = DoResourcesHelper.getIdentifier("gridview", "id", this);
		gridView = (GridView) findViewById(gridview_id);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(ImageGridActivity.this, dataList, mHandler);
		gridView.setAdapter(adapter);
		adapter.setTextCallback(new TextCallback() {
			public void onListen(int count) {
				if (count > 0) {
					btn_cancel.setText("完成(" + count + "/" + ConstantValue.MAX_COUNT + ")");
					btn_preview.setText("预览(" + count + ")");
				} else {
					btn_cancel.setText("完成");
					btn_preview.setText("预览");
				}
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter.notifyDataSetChanged();
			}

		});
		int cancel_id = DoResourcesHelper.getIdentifier("cancel", "id", this);
		btn_cancel = (Button) findViewById(cancel_id);
		btn_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new MyAsyncTask(ImageGridActivity.this, getIntent()).execute();
			}
		});

		int preview_id = DoResourcesHelper.getIdentifier("preview", "id", this);
		btn_preview = (Button) findViewById(preview_id);
		btn_preview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getIntent();
				intent.setClass(ImageGridActivity.this, ShowPictureViewActivity.class);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		BitmapUtils.selectPaths.clear();
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}
}
