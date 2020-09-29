package com.lionpig.webui.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.lionpig.model.chart.ChartModel;
import com.lionpig.model.chart.SeriesModel;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class ChartTool implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.init)) {
			Timestamp end_date = Helper.getTimestamp(params, C.end_date, true);
			int timeline = Helper.getInt(params, C.timeline, true);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(end_date);
			cal.add(Calendar.DATE, -timeline);
			Timestamp start_date = new Timestamp(cal.getTimeInMillis());
			
			ChartModel[] charts = getCharts(fi);
			
			StringBuilder sb = new StringBuilder();
			sb.append(charts.length);
			for (int i = 0; i < charts.length; i++) {
				ChartModel m = charts[i];
				populateDaily(fi, m, start_date, end_date);
				sb.append(C.char_31).append(m.chart_id)
				.append(C.char_31).append(m.chart_name)
				.append(C.char_31).append(StringUtils.join(m.x_labels, C.char_30))
				.append(C.char_31).append(m.series.length);
				for (SeriesModel seriesModel : m.series) {
					sb.append(C.char_31).append(seriesModel.series_id)
					.append(C.char_31).append(seriesModel.series_name)
					.append(C.char_31).append(StringUtils.join(seriesModel.points, C.char_30));
				}
			}
			return sb.toString();
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}
	
	public ChartModel[] getCharts(FunctionItem fi) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		List<ChartModel> list = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select chart_id, chart_name from chart order by chart_seq")) {
			try (ResultSet r = pstmt.executeQuery()) {
				while (r.next()) {
					ChartModel m = new ChartModel();
					m.chart_id = r.getInt(1);
					m.chart_name = r.getString(2);
					list.add(m);
				}
			}
		}
		ChartModel[] charts = new ChartModel[list.size()];
		for (int i = 0; i < charts.length; i++) {
			charts[i] = list.get(i);
		}
		return charts;
	}
	
	public void populateDaily(FunctionItem fi, ChartModel chartModel, Timestamp start_time, Timestamp end_time) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatement pstmt = conn.prepareStatement("select distinct date_format(b.point_id, '%Y-%m-%d') as point_id from chart_series a, chart_daily b where a.chart_id = ? and a.chart_id = b.chart_id and a.series_id = b.series_id and b.point_id between ? and ? order by point_id")) {
			pstmt.setInt(1, chartModel.chart_id);
			pstmt.setTimestamp(2, start_time);
			pstmt.setTimestamp(3, end_time);
			try (ResultSet r = pstmt.executeQuery()) {
				List<String> list = new ArrayList<>();
				while (r.next()) {
					list.add(r.getString(1));
				}
				
				chartModel.x_labels = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					chartModel.x_labels[i] = list.get(i);
				}
			}
		}
		
		try (PreparedStatement pstmt = conn.prepareStatement("select series_id, series_name from chart_series where chart_id = ? and series_id in (select distinct series_id from chart_daily where chart_id = ? and point_id between ? and ?) order by series_name")) {
			pstmt.setInt(1, chartModel.chart_id);
			pstmt.setInt(2, chartModel.chart_id);
			pstmt.setTimestamp(3, start_time);
			pstmt.setTimestamp(4, end_time);
			try (ResultSet r = pstmt.executeQuery()) {
				List<SeriesModel> list = new ArrayList<>();
				while (r.next()) {
					SeriesModel seriesModel = new SeriesModel();
					seriesModel.series_id = r.getInt(1);
					seriesModel.series_name = r.getString(2);
					seriesModel.points = new double[chartModel.x_labels.length];
					for (int i = 0; i < seriesModel.points.length; i++) {
						seriesModel.points[i] = 0;
					}
					list.add(seriesModel);
				}
				
				chartModel.series = new SeriesModel[list.size()];
				for (int i = 0; i < list.size(); i++) {
					chartModel.series[i] = list.get(i);
				}
			}
		}
		
		try (PreparedStatement pstmt = conn.prepareStatement("select series_id, date_format(point_id, '%Y-%m-%d') as point_id, point_value from chart_daily where chart_id = ? and point_id between ? and ?")) {
			pstmt.setInt(1, chartModel.chart_id);
			pstmt.setTimestamp(2, start_time);
			pstmt.setTimestamp(3, end_time);
			try (ResultSet r = pstmt.executeQuery()) {
				Map<Integer, Integer> seriesIdPositions = new HashMap<>();
				for (int i = 0; i < chartModel.series.length; i++) {
					seriesIdPositions.put(chartModel.series[i].series_id, i);
				}
				
				Map<String, Integer> pointIdPositions = new HashMap<>();
				for (int i = 0; i < chartModel.x_labels.length; i++) {
					pointIdPositions.put(chartModel.x_labels[i], i);
				}
				
				while (r.next()) {
					int series_id = r.getInt(1);
					String point_id = r.getString(2);
					double point_value = r.getDouble(3);
					
					int seriesIdPos = seriesIdPositions.get(series_id);
					int pointIdPos = pointIdPositions.get(point_id);
					
					chartModel.series[seriesIdPos].points[pointIdPos] = point_value;
				}
			}
		}
	}
	
}
