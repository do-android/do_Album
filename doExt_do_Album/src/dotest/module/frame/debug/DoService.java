package dotest.module.frame.debug;

import java.util.Map;

import core.DoServiceContainer;
import core.helper.jsonparse.DoJsonNode;
import core.object.DoInvokeResult;
import core.object.DoModule;

public class DoService {
	
	public interface EventCallBack{
		public void eventCallBack(String _data);
	}
	
	public static DoScriptEngine scriptEngine;
	private static int uniqueKeyCursor = 0;
	  
	 public static void Init(){
         DoServiceContainer.setLogEngine(new DoLogEngine());
         DoServiceContainer.setPageViewFactory(new DoPageViewFactory());
         scriptEngine = new DoScriptEngine();
     }

     public static void setPropertyValue(DoModule _module, String _propertyID, String _value){
         DoInvokeResult _invokeResult = scriptEngine.CreateInvokeResult(null);
         try{
        	 //设置text参数
             DoJsonNode _jsonPara = new DoJsonNode();
             _jsonPara.setOneText(_propertyID, _value);
             DoJsonNode jsonNode = new DoJsonNode();
             jsonNode.setOneNode("data", _jsonPara);
             _module.invokeSyncMethod("set", jsonNode, scriptEngine, _invokeResult);
             DoServiceContainer.getLogEngine().writeDebug("设置属性成功：" + _propertyID + "\n" + _invokeResult.getResult());
         }catch (Exception _err){
        	 DoServiceContainer.getLogEngine().writeError("设置属性失败：" + _propertyID , _err);
         }
     }

     public static void syncMethod(DoModule _module, String _methodName, Map<String, String> _paras){
         DoInvokeResult _invokeResult = scriptEngine.CreateInvokeResult(null);
         try{
             DoJsonNode _jsonPara = new DoJsonNode();
             if (_paras != null){
                 for (String _key :_paras.keySet()){
                     _jsonPara.setOneText(_key, _paras.get(_key));
                 }
             }
            _module.invokeSyncMethod(_methodName, _jsonPara, scriptEngine, _invokeResult);
             DoServiceContainer.getLogEngine().writeDebug("调用同步方法成功：" + _methodName + "\n" + _invokeResult.getResult());
         }catch (Exception _err){
             DoServiceContainer.getLogEngine().writeError("调用同步方法失败：" + _methodName, _err);
         }
     }

     public static void ansyncMethod(DoModule _module, String _methodName, Map<String, String> _paras, EventCallBack _eventCallBack){
         try{
             DoJsonNode _jsonPara = new DoJsonNode();
             if (_paras != null){
                 for (String _key :_paras.keySet()){
                     _jsonPara.setOneText(_key, _paras.get(_key));
                 }
             }
             String _uniqueKey = "doCallBackKey_" + (uniqueKeyCursor ++);
             scriptEngine.AddCallBack(_uniqueKey, _eventCallBack);
             _module.invokeAsyncMethod(_methodName, _jsonPara, scriptEngine, _uniqueKey);
             DoServiceContainer.getLogEngine().writeDebug("调用异步方法：" + _methodName );
         } catch (Exception _err){
             DoServiceContainer.getLogEngine().writeError("调用异步方法失败：" + _methodName, _err);
         }
     }

     public static void subscribeEvent(DoModule _module, String _eventID, EventCallBack _eventCallBack){
         try{
        	 String _messageName = _eventID;
             scriptEngine.AddCallBack(_eventID, _eventCallBack);
             _module.getEventCenter().subscribe(_messageName, _eventID, scriptEngine);
             DoServiceContainer.getLogEngine().writeDebug("订阅消息成功：" + _eventID);
         }catch (Exception _err) {
             DoServiceContainer.getLogEngine().writeError("设置属性失败：\n", _err);
         }
     }
}
