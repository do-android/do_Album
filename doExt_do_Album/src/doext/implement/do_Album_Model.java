package doext.implement;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoActivityResultListener;
import core.interfaces.DoIPageView;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.choosephotos.BitmapUtils;
import doext.choosephotos.ChoosePhotosActivity;
import doext.define.do_Album_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_Album_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Album_Model extends DoSingletonModule implements do_Album_IMethod, DoActivityResultListener {

	private DoIPageView pageView;
	private DoIScriptEngine scriptEngine;
	private String callbackFuncName;

	public do_Album_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		// ...do something
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("select".equals(_methodName)) {
			this.select(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		} else if ("save".equals(_methodName)) {
			this.save(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 保存图片到相册；
	 * 
	 * @throws Exception
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void save(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _path = DoJsonHelper.getString(_dictParas, "path", "");// 图片路径
		String _name = DoJsonHelper.getString(_dictParas, "name", "default.jpg"); // 图片名称
		int _width = DoJsonHelper.getInt(_dictParas, "width", 0); // 选择后的图片的宽度，不填默认图片宽度
		int _height = DoJsonHelper.getInt(_dictParas, "height", 0); // 选择后的图片的高度，不填默认图片高度
		int _quality = DoJsonHelper.getInt(_dictParas, "quality", 100); // 清晰度1-100,缺省是100表示原始的图片质量
		if ("".equals(_name.trim())) {
			_name = "default.jpg";
		}
		_quality = _quality > 100 ? 100 : _quality;
		_quality = _quality < 1 ? 1 : _quality;

		DoInvokeResult _result = new DoInvokeResult(do_Album_Model.this.getUniqueKey());

		File _dirName = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dcim/Camera");
		if (!_dirName.exists()) {
			_dirName.mkdirs();
		}
		String _fileFullName = _dirName.getAbsolutePath() + "/" + _name;
		String _filePath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentPage().getCurrentApp(), _path);
		if (DoIOHelper.existFile(_filePath)) {
			if (_width <= 0 || _height <= 0) {
				DoIOHelper.fileCopy(_filePath, _fileFullName);
			} else {
				Bitmap bitmap = BitmapUtils.resizeRealImage(_filePath, _width, _height);
				FileOutputStream photoOutputStream = new FileOutputStream(new File(_fileFullName));
				bitmap.compress(Bitmap.CompressFormat.JPEG, _quality, photoOutputStream);
			}
//			galleryAddPic(DoServiceContainer.getPageViewFactory().getAppContext(), _fileFullName);
			_result.setResultBoolean(true);
		} else {
			_result.setResultBoolean(false);
		}
		_scriptEngine.callback(_callbackFuncName, _result);
	}

//	private void galleryAddPic(Context context, String mCurrentPhotoPath) {
//		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//		File f = new File(mCurrentPhotoPath);
//		Uri contentUri = Uri.fromFile(f);
//		mediaScanIntent.setData(contentUri);
//		context.sendBroadcast(mediaScanIntent);
//	}

	/**
	 * 从相册选择照片；
	 * 
	 * @throws Exception
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void select(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		BitmapUtils.selectPaths.clear();
		ConstantValue.MAX_COUNT = 9;
		ConstantValue.ISCUT = false;
		int _maxCount = DoJsonHelper.getInt(_dictParas, "maxCount", 9); // 总共最多选几张
		int _width = DoJsonHelper.getInt(_dictParas, "width", -1); // 选择后的图片的宽度，不填默认图片宽度
		int _height = DoJsonHelper.getInt(_dictParas, "height", -1); // 选择后的图片的高度，不填默认图片高度
		int _quality = DoJsonHelper.getInt(_dictParas, "quality", 100); // 清晰度1-100,缺省是100表示原始的图片质量
		boolean _iscut = DoJsonHelper.getBoolean(_dictParas, "iscut", false);
		ConstantValue.MAX_COUNT = _maxCount;
		if (_maxCount == 1) {
			ConstantValue.ISCUT = _iscut;
		}
		this.scriptEngine = _scriptEngine;
		this.callbackFuncName = _callbackFuncName;
		Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		pageView = _scriptEngine.getCurrentPage().getPageView();
		pageView.registActivityResultListener(this);
		Intent i = new Intent(_activity, ChoosePhotosActivity.class);
		i.putExtra("width", _width);
		i.putExtra("height", _height);
		i.putExtra("quality", _quality);
		i.putExtra("tagerDir", _scriptEngine.getCurrentApp().getDataFS().getRootPath());
		_activity.startActivityForResult(i, 100);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent != null) {
			ArrayList<String> _results = intent.getStringArrayListExtra("result");
			if (scriptEngine != null && callbackFuncName != null && _results != null && _results.size() > 0) {
				DoInvokeResult _invoke = new DoInvokeResult(this.getUniqueKey());
				_invoke.setResultArray(new JSONArray(_results));
				scriptEngine.callback(callbackFuncName, _invoke);
			}
		}
		if (pageView != null) {
			pageView.unregistActivityResultListener(this);
		}
	}
}