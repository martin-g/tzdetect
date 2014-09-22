package org.apache;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private String tzId = null;
	private final Label lzLabel;
	AbstractDefaultAjaxBehavior a =  new AbstractDefaultAjaxBehavior() {
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(AjaxRequestTarget target) {
			StringValue v = getRequest().getRequestParameters().getParameterValue("rulez");
			try {
				JSONObject o = new JSONObject(v.toString());
				int offset = o.getInt("offset") * 60 * 1000;
				for (String id : TimeZone.getAvailableIDs(offset)) {
					TimeZone tz = TimeZone.getTimeZone(id);
					boolean tzPresent = o.getBoolean("present");
					if (tzPresent && tz.useDaylightTime()) {
						JSONArray startarr = o.getJSONArray("start");
						JSONArray endarr = o.getJSONArray("end");
						/*for (int i = 0; i < arr.length(); ++i)*/ { //Java does not support TZ with multiple DST periods
							JSONObject start = startarr.getJSONObject(0);
							JSONObject end = endarr.getJSONObject(0);
							Calendar c = Calendar.getInstance(tz);
							c.set(start.getInt("year"), start.getInt("month"), start.getInt("day"), start.getInt("hour"), start.getInt("minute"), 1);
							long so1 = tz.getOffset(c.getTimeInMillis());
							c.add(Calendar.SECOND, -2);
							long so2 = tz.getOffset(c.getTimeInMillis());
							//TODO end can be checked here
							/*
							c.set(end.getInt("year"), end.getInt("month"), end.getInt("day"), end.getInt("hour"), end.getInt("minute"), 1);
							long eo1 = tz.getOffset(c.getTimeInMillis());
							c.add(Calendar.SECOND, -2);
							long eo2 = tz.getOffset(c.getTimeInMillis());
							*/
							if (so2 == offset && so1 != so2) {
								System.err.println("MATCH:: " + id);
								tzId = id;
								break; //all matched can be retrieved here
							}
						}
					} else if (!tzPresent && !tz.useDaylightTime()) {
						//no DST get first matching 
						tzId = id;
						break;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			target.add(lzLabel);
		}
	};
	
	//resolved
	public HomePage(final PageParameters parameters) {
		super(parameters);

		add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

		add(a);
		lzLabel = new Label("tzid", new PropertyModel<String>(this, "tzId"));
		add(lzLabel.setOutputMarkupId(true));
    }
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript(a.getCallbackFunctionBody(CallbackParameter.resolved("rulez", "getRules()"))));
	}
}
