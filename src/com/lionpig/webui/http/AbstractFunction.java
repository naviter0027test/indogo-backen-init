package com.lionpig.webui.http;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.RoleRelayModel;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public abstract class AbstractFunction implements IFunction {
	
	private RoleRelayModel roleRelayModel = null;
	public void setRoleRelayModel(RoleRelayModel roleRelayModel) {
		this.roleRelayModel = roleRelayModel;
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		L l = fi.getLanguage();
		S s = fi.getSql();
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.init)) {
			return onInit(req, resp, fi, params, action, l, s);
		} else if (action.equals(C.insert)) {
			if (roleRelayModel != null && !roleRelayModel.allow_insert)
				throw new NotAllowedException(101, l.insert_not_allowed());
			return onInsert(req, resp, fi, params, action, l, s);
		} else if (action.equals(C.update)) {
			if (roleRelayModel != null && !roleRelayModel.allow_update)
				throw new NotAllowedException(102, l.update_not_allowed());
			return onUpdate(req, resp, fi, params, action, l, s);
		} else if (action.equals(C.delete)) {
			if (roleRelayModel != null && !roleRelayModel.allow_delete)
				throw new NotAllowedException(103, l.delete_not_allowed());
			return onDelete(req, resp, fi, params, action, l, s);
		} else if (action.equals(C.get)) {
			return onGet(req, resp, fi, params, action, l, s);
		} else
			return onExecute(req, resp, fi, params, action, l, s);
	}
	
	protected abstract String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
	protected abstract String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
	protected abstract String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
	protected abstract String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
	protected abstract String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
	protected abstract String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi, Hashtable<String, String> params, String action, L l, S s) throws Exception;
}
