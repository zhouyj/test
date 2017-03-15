package com.feedss.manage.util.ueditor;

import com.feedss.manage.util.ueditor.define.ActionMap;
import com.feedss.manage.util.ueditor.define.AppInfo;
import com.feedss.manage.util.ueditor.define.BaseState;
import com.feedss.manage.util.ueditor.define.State;
import com.feedss.manage.util.ueditor.hunter.FileManager;
import com.feedss.manage.util.ueditor.hunter.ImageHunter;
import com.feedss.manage.util.ueditor.upload.Uploader;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ActionEnter {
	
	private ConfigManager configManager = null;
	
	private HttpServletRequest request = null;
	
	private String actionType = null;
	
	public ActionEnter(ConfigManager configManager){
		this.configManager = configManager;
	}
	
	public String exec (HttpServletRequest request) {
		
		this.request = request;
		this.actionType = request.getParameter( "action" );
		String callbackName = this.request.getParameter("callback");
		
		if ( callbackName != null ) {

			if ( !validCallbackName( callbackName ) ) {
				return new BaseState( false, AppInfo.ILLEGAL ).toJSONString();
			}
			
			return callbackName+"("+this.invoke()+");";
			
		} else {
			return this.invoke();
		}

	}
	
	public String invoke() {
		
		if ( actionType == null || !ActionMap.mapping.containsKey( actionType ) ) {
			return new BaseState( false, AppInfo.INVALID_ACTION ).toJSONString();
		}
		
		if ( this.configManager == null || !this.configManager.valid() ) {
			return new BaseState( false, AppInfo.CONFIG_ERROR ).toJSONString();
		}
		
		State state = null;
		
		int actionCode = ActionMap.getType( this.actionType );
		
		Map<String, Object> conf = null;
		
		switch ( actionCode ) {
		
			case ActionMap.CONFIG:
				return this.configManager.getAllConfig().toString();
				
			case ActionMap.UPLOAD_IMAGE:
			case ActionMap.UPLOAD_SCRAWL:
			case ActionMap.UPLOAD_VIDEO:
			case ActionMap.UPLOAD_FILE:
				conf = this.configManager.getConfig( actionCode );
				state = new Uploader( request, conf ).doExec();
				break;
				
			case ActionMap.CATCH_IMAGE:
				conf = configManager.getConfig( actionCode );
				String[] list = this.request.getParameterValues( (String)conf.get( "fieldName" ) );
				state = new ImageHunter( conf ).capture( list );
				break;
				
//			case ActionMap.LIST_IMAGE:
//			case ActionMap.LIST_FILE:
//				conf = configManager.getConfig( actionCode );
//				int start = this.getStartIndex();
//				String marker = this.getMarker();
//				state = new FileManager( conf ).listFile( start,marker );
//				break;
		}
		
		return state.toJSONString();
		
	}
	
	public int getStartIndex () {
		
		String start = this.request.getParameter( "start" );
		try {
			return Integer.parseInt( start );
		} catch ( Exception e ) {
			return 0;
		}
		
	}
	public String getMarker () {
		
		String marker = this.request.getParameter( "marker" );
		return marker;
	}
	
	/**
	 * callback参数验证
	 */
	public boolean validCallbackName ( String name ) {
		
		if ( name.matches( "^[a-zA-Z_]+[\\w0-9_]*$" ) ) {
			return true;
		}
		
		return false;
		
	}
	
}
