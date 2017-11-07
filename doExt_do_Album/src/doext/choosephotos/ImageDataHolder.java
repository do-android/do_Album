package doext.choosephotos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDataHolder {
	public static final String DO_CURRENT_FOLDER_ITEM = "do_current_folder_item";

	private static ImageDataHolder mInstance;
	private Map<String, List<ImageItem>> data;

	public static ImageDataHolder getInstance() {
		if (mInstance == null) {
			synchronized (ImageDataHolder.class) {
				if (mInstance == null) {
					mInstance = new ImageDataHolder();
				}
			}
		}
		return mInstance;
	}

	private ImageDataHolder() {
		data = new HashMap<String, List<ImageItem>>();
	}

	public void save(String id, List<ImageItem> object) {
		if (data != null) {
			data.put(id, object);
		}
	}

	public Object retrieve(String id) {
		if (data == null || mInstance == null) {
			throw new RuntimeException("你必须先初始化");
		}
		return data.get(id);
	}
}
