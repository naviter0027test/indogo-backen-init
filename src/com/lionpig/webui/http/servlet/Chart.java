package com.lionpig.webui.http.servlet;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.lionpig.webui.database.ConnectionFactory;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.util.AjaxMessage;

public class Chart extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4280053859088752372L;
	
	private static final String ALLOWED_CHART_TYPE = "'LINE','BAR'";
	private static final String DEFAULT_CATEGORY_DATASET = "'LINE','BAR'";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		
		String SID = req.getParameter("SID");
		if (SID == null || SID.length() == 0)
			return;
		
		PrintWriter pw = resp.getWriter();
		IConnection conn = null;
		LogInfo logInfo = null;
		try {
			conn = ConnectionFactory.getInstance().createConnection(getServletContext());
			
			if (!conn.checkSessionId(SID, true, req.getLocalAddr(), req.getLocalName(), req.getLocalPort()))
				throw new FunctionException(201, "Session not exist");
			
			SessionInfo SIDInfo = conn.getUserInfo(SID);
			logInfo = new LogInfo(SIDInfo.getUserRowId(), SIDInfo.getUserName(), SID, "Chart");
			
			Hashtable<String, String> params = new Hashtable<String, String>();
			try {
				StringBuilder sb = new StringBuilder();
				@SuppressWarnings("rawtypes")
				Enumeration en = req.getParameterNames();
				while (en.hasMoreElements()) {
					String K = en.nextElement().toString();
					String V = req.getParameter(K);
					sb.append(K).append("=").append(V).append("\n");
					params.put(K, V);
				}
				logInfo.setVerbose(LogInfo.LOG_DEBUG);
				logInfo.setMessage(sb.toString());
				conn.log(logInfo);
			}
			catch (Exception ignore) {}
			
			File tempFolder = new File(getServletContext().getRealPath(SIDInfo.getTempFolderPath()));
			
			String reply = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
					"<html><head><title>Chart</title></head>\n" +
					"<body>\n" +
					this.getChart(SID, tempFolder, params) +
					"\n</body></html>";
			
			logInfo.setMessage(reply);
			logInfo.setVerbose(LogInfo.LOG_TRACE);
			conn.log(logInfo);
			
			pw.write(reply);
		}
		catch (FunctionException E) {
			String msg = AjaxMessage.parseError(E.getErrorCode(), E, true);
			if (conn != null && logInfo != null) {
				logInfo.setVerbose(LogInfo.LOG_ERROR);
				logInfo.setMessage(msg);
				conn.log(logInfo);
			}
			pw.write(msg);
		}
		catch (Exception E) {
			String msg = AjaxMessage.parseError(1500, E, true);
			if (conn != null && logInfo != null) {
				logInfo.setVerbose(LogInfo.LOG_ERROR);
				logInfo.setMessage(msg);
				conn.log(logInfo);
			}
			pw.write(msg);
		}
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			}
			catch (Exception ignore) {}
		}
	}
	
	private String getChart(String SID, File tempFolder, Hashtable<String, String> params) throws Exception {
		String chartType = params.get("ChartType");
		if (chartType == null || chartType.length() == 0)
			throw new Exception("Please provide ChartType");
		
		String dsFilename = params.get("DataSourceFilename");
		if (dsFilename == null || dsFilename.length() == 0)
			throw new Exception("Please provide DataSourceFilename");
		
		String dataSourceType = params.get("DataSourceType");
		if (dataSourceType == null || dataSourceType.length() == 0)
			throw new Exception("Please provide DataSourceType");
		
		String title = params.get("Title");
		if (title == null)
			title = "";
		
		if (ALLOWED_CHART_TYPE.indexOf("'" + chartType + "'") < 0)
			throw new Exception("Chart type [" + chartType + "] not supported");
		
		int width = 1024;
		if (params.get("Width") != null)
			width = Integer.parseInt(params.get("Width"));
		
		int height = 768;
		if (params.get("Height") != null)
			height = Integer.parseInt(params.get("Height"));
		
		String[] columnNames;
		CategoryDataset dataset;
		if (DEFAULT_CATEGORY_DATASET.indexOf("'" + chartType + "'") > -1) {
			DefaultCategoryDataset dcd = new DefaultCategoryDataset();
			BufferedReader br = new BufferedReader(new FileReader(new File(tempFolder, dsFilename)));
			try {
				String line = br.readLine();
				if (line == null)
					throw new Exception("No data found in filename [" + dsFilename + "]");
			
				columnNames = line.split(",");
				String[] cols;
				while ((line = br.readLine()) != null) {
					cols = line.split(",");
					if (cols.length < 3)
						continue;
					dcd.setValue(Double.parseDouble(cols[0]), cols[1], cols[2]);
				}
			}
			finally {
				br.close();
			}
			
			dataset = dcd;
		}
		else
			throw new Exception("Dataset for chart type [" + chartType + "] not supported");

		JFreeChart chart;
		if (chartType.equals("LINE")) {
			chart = ChartFactory.createLineChart(
					title,
					columnNames[2],
					columnNames[0],
					dataset,
					PlotOrientation.VERTICAL,
					false,
					true,
					false);
			
			CategoryPlot plot = (CategoryPlot)chart.getPlot();
			plot.setBackgroundPaint(Color.LIGHT_GRAY);
			plot.setRangeGridlinePaint(Color.WHITE);
			plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			
			if (params.containsKey("UpperBound"))
				plot.getRangeAxis().setUpperBound(Double.parseDouble(params.get("UpperBound")));
			if (params.containsKey("LowerBound"))
				plot.getRangeAxis().setLowerBound(Double.parseDouble(params.get("LowerBound")));
			
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setBaseShapesVisible(true);
			renderer.setBaseItemLabelsVisible(true);
		}
		else if (chartType.equals("BAR")) {
			chart = ChartFactory.createBarChart(
					title,
					columnNames[2],
					columnNames[0],
					dataset,
					PlotOrientation.VERTICAL,
					false,
					true,
					false);
			
			CategoryPlot plot = (CategoryPlot)chart.getPlot();
			plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		}
		else
			throw new Exception("Chart type [" + chartType + "] not supported");
		
		String uniqueId = UUID.randomUUID().toString().replaceAll("-", "");
		String map;
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(tempFolder, uniqueId + ".png"), false));
		try {
			ChartRenderingInfo info = new ChartRenderingInfo();
			info.clear();
			ChartUtilities.writeChartAsPNG(out, chart, width, height, info);
			map = ChartUtilities.getImageMap(uniqueId,
					info,
					new ToolTipTagFragmentGenerator() {
						public String generateToolTipFragment(String toolTipText) {
							return " title=\"" + toolTipText + "\" alt=\"" + toolTipText + "\"";
						}
					},
					new StandardURLTagFragmentGenerator());
		}
		finally {
			out.flush();
			out.close();
		}
		return "<img style=\"border:0\" src=\"DownloadServlet?SID=" + SID + "&Filename=" + uniqueId + ".png&KeepFile=N\" usemap=\"#" + uniqueId + "\"/>\n" + map;
	}
}
