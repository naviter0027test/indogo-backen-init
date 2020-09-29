package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.InventoryModel;
import com.indogo.model.onlineshopping.StockTransferItemModel;
import com.indogo.model.onlineshopping.StockTransferModel;
import com.indogo.model.onlineshopping.StockTransferStatus;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class StockTransferCreate extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.print)) {
			long transfer_id = Helper.getLong(params, C.transfer_id, true);
			
			Connection conn = fi.getConnection().getConnection();
			
			StringBuilder sb = new StringBuilder();
			
			int shop_id_from, shop_id_to;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.stock_transfer_select_for_print()))) {
				pstmt.setLong(1, transfer_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						shop_id_from = r.getInt(1);
						shop_id_to = r.getInt(2);
					} else {
						throw new Exception(l.data_not_exist(C.transfer_id, String.valueOf(transfer_id)));
					}
				}
			}
			
			String shop_name_from, shop_name_to;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.shop_get_for_inventory()))) {
				pstmt.setInt(1, shop_id_from);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						shop_name_from = r.getString(1);
					} else {
						throw new Exception(l.data_not_exist(C.shop_id_from, String.valueOf(shop_id_from)));
					}
				}
				
				pstmt.setInt(1, shop_id_to);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						shop_name_to = r.getString(1);
					} else {
						throw new Exception(l.data_not_exist(C.shop_id_to, String.valueOf(shop_id_to)));
					}
				}
			}
			
			sb.append(shop_name_from)
			.append(C.char_31).append(shop_name_to);
			
			List<StockTransferItemModel> items = new ArrayList<>();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.stock_transfer_item_get_all()))) {
				pstmt.setLong(1, transfer_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					while (r.next()) {
						StockTransferItemModel m = new StockTransferItemModel();
						m.item_id = r.getInt(1);
						m.qty = r.getInt(2);
						items.add(m);
					}
				}
			}
			
			sb.append(C.char_31).append(items.size());
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.item_get_for_inventory()))) {
				for (StockTransferItemModel item : items) {
					sb.append(C.char_31);
					
					pstmt.setInt(1, item.item_id);
					try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
						if (r.next()) {
							String item_name = r.getString(1);
							String item_desc = r.getString(7);
							String category_name = r.getString(4);
							String color_name = r.getString(5);
							String size_name = r.getString(6);
							
							sb.append(item.qty)
							.append(C.char_30).append(item_name)
							.append(C.char_30).append(item_desc)
							.append(C.char_30).append(category_name)
							.append(C.char_30).append(color_name)
							.append(C.char_30).append(size_name);
						}
					}
				}
			}
			
			return sb.toString();
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l.stock_transfer_title())
		.append(C.char_31).append(l.shop_name_from())
		.append(C.char_31).append(l.shop_name_to())
		.append(C.char_31).append(l.item_list())
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(l.transfer_qty())
		.append(C.char_31).append(l.current_qty())
		.append(C.char_31).append(l.item_image())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(l.color_name())
		.append(C.char_31).append(l.size_name())
		.append(C.char_31).append(l.comment())
		.append(C.char_31).append(l.button_add_inventory())
		.append(C.char_31).append(l.button_browse_inventory())
		.append(C.char_31).append(l.button_create())
		.append(C.char_31).append(l.transfer_id())
		.append(C.char_31).append(l.button_search())
		.append(C.char_31).append(l.button_close())
		.append(C.char_31).append(l.button_title_add_checked_item_to_list());
		
		sb.append(C.char_31).append(Shop.getShopListForUser(fi));
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.shop_list_all()))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder cc = new StringBuilder();
				int size = 0;
				while (r.next()) {
					cc.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2));
					size++;
				}
				sb.append(C.char_31).append(size)
				.append(cc);
			}
		}
		
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id_from = Helper.getInt(params, C.shop_id_from, true, l.shop_name_from());
		int shop_id_to = Helper.getInt(params, C.shop_id_to, true, l.shop_name_to());
		String comment = Helper.getString(params, C.comment, false, l.comment());
		String[] items = Helper.getStringArray(params, C.items, true);
		
		StockTransferItemModel[] stockTransferItemModels = new StockTransferItemModel[items.length];
		for (int i = 0; i < stockTransferItemModels.length; i++) {
			String[] cells = StringUtils.splitPreserveAllTokens(items[i], C.char_30);
			
			StockTransferItemModel m = new StockTransferItemModel();
			m.item_id = Integer.parseInt(cells[0]);
			m.qty = Integer.parseInt(cells[1]);
			
			stockTransferItemModels[i] = m;
		}
		
		StockTransferModel stockTransferModel = new StockTransferModel();
		stockTransferModel.shop_id_from = shop_id_from;
		stockTransferModel.shop_id_to = shop_id_to;
		stockTransferModel.comment = comment;
		stockTransferModel.items = stockTransferItemModels;
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, stockTransferModel);
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
		
		return String.valueOf(stockTransferModel.transfer_id);
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void insert(FunctionItem fi, StockTransferModel stockTransferModel) throws Exception {
		S sql = fi.getSql();
		L l = fi.getLanguage();
		
		if (stockTransferModel.shop_id_from == stockTransferModel.shop_id_to) {
			throw new Exception(l.exception_cannot_transfer_to_same_shop());
		}
		
		stockTransferModel.status = StockTransferStatus.created;
		
		Connection conn = fi.getConnection().getConnection();
		
		List<Integer> itemIds = new ArrayList<>();
		for (StockTransferItemModel stockTransferItemModel : stockTransferModel.items) {
			itemIds.add(stockTransferItemModel.item_id);
		}
		Collections.sort(itemIds);
		HashMap<Integer, InventoryModel> inventories = new HashMap<>();
		try (PreparedStatementWrapper pstmtInventoryLock = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			for (int itemId : itemIds) {
				pstmtInventoryLock.setInt(1, stockTransferModel.shop_id_from);
				pstmtInventoryLock.setInt(2, itemId);
				ResultSet r = pstmtInventoryLock.executeQuery();
				try {
					if (r.next()) {
						int itemQty = r.getInt(1);
						InventoryModel m = new InventoryModel();
						m.item_id = itemId;
						m.shop_id = stockTransferModel.shop_id_from;
						m.item_qty = itemQty;
						inventories.put(itemId, m);
					} else {
						throw new Exception(l.exception_inventory_item_not_found(stockTransferModel.shop_id_from, itemId));
					}
				} finally {
					r.close();
				}
			}
		}
		
		stockTransferModel.create_time = stockTransferModel.lm_time = fi.getConnection().getCurrentTime();
		stockTransferModel.lm_user = fi.getSessionInfo().getUserName();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
		String seqPrefix = dateFormat.format(stockTransferModel.create_time);
		String seqName = new StringBuilder().append(C.stock_transfer_id).append(C.underscore).append(seqPrefix).toString();
		long seq = fi.getConnection().getSeq(seqName, true);
		stockTransferModel.transfer_id = Long.parseLong(new StringBuilder().append(seqPrefix).append(StringUtils.leftPad(String.valueOf(seq), 6, '0')).toString());
		
		try (PreparedStatementWrapper pstmtStockTransfer = new PreparedStatementWrapper(conn.prepareStatement("insert into stock_transfer (transfer_id, create_time, shop_id_from, shop_id_to, status_id, comment, lm_time, lm_user, lm_time_1, lm_user_1) values (?,?,?,?,?,?,?,?,?,?)"))) {
			pstmtStockTransfer.setLong(1, stockTransferModel.transfer_id);
			pstmtStockTransfer.setTimestamp(2, stockTransferModel.create_time);
			pstmtStockTransfer.setInt(3, stockTransferModel.shop_id_from);
			pstmtStockTransfer.setInt(4, stockTransferModel.shop_id_to);
			pstmtStockTransfer.setInt(5, stockTransferModel.status.getId());
			pstmtStockTransfer.setString(6, stockTransferModel.comment);
			pstmtStockTransfer.setTimestamp(7, stockTransferModel.lm_time);
			pstmtStockTransfer.setString(8, stockTransferModel.lm_user);
			pstmtStockTransfer.setTimestamp(9, stockTransferModel.lm_time);
			pstmtStockTransfer.setString(10, stockTransferModel.lm_user);
			pstmtStockTransfer.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.stock_transfer_item_insert()))) {
			for (StockTransferItemModel m : stockTransferModel.items) {
				pstmt.setLong(1, stockTransferModel.transfer_id);
				pstmt.setInt(2, m.item_id);
				pstmt.setInt(3, m.qty);
				pstmt.executeUpdate();
			}
		}
		
		// reduce goods quantity
		try (PreparedStatementWrapper pstmtInventoryUpdate = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
				for (StockTransferItemModel stockTransferItemModel : stockTransferModel.items) {
					InventoryModel m = inventories.get(stockTransferItemModel.item_id);
					
					int old_qty = m.item_qty;
					int diff_qty = -stockTransferItemModel.qty;
					int new_qty = old_qty + diff_qty;
					
					m.item_qty = m.item_qty - stockTransferItemModel.qty;
					pstmtInventoryUpdate.setInt(1, m.item_qty);
					pstmtInventoryUpdate.setTimestamp(2, stockTransferModel.lm_time);
					pstmtInventoryUpdate.setString(3, stockTransferModel.lm_user);
					pstmtInventoryUpdate.setInt(4, stockTransferModel.shop_id_from);
					pstmtInventoryUpdate.setInt(5, m.item_id);
					pstmtInventoryUpdate.executeUpdate();
					
					pstmt.setLong(1, Helper.randomSeq());
					pstmt.setInt(2, InventoryHistoryReason.stock_transfer_create.getId());
					pstmt.setInt(3, stockTransferModel.shop_id_from);
					pstmt.setInt(4, m.item_id);
					pstmt.setInt(5, old_qty);
					pstmt.setInt(6, diff_qty);
					pstmt.setInt(7, new_qty);
					pstmt.setTimestamp(8, stockTransferModel.lm_time);
					pstmt.setString(9, stockTransferModel.lm_user);
					pstmt.setLong(10, null);
					pstmt.setLong(11, null);
					pstmt.setLong(12, null);
					pstmt.setLong(13, stockTransferModel.transfer_id);
					pstmt.executeUpdate();
				}
			}
		}
	}
	
//	private String createQrCode(FunctionItem fi, int transfer_id) throws Exception {
//		String qrcode_base_directory = fi.getConnection().getGlobalConfig(C.stock_transfer, C.qrcode_base_directory);
//		String qrcode_base_url = fi.getConnection().getGlobalConfig(C.stock_transfer, C.qrcode_base_url);
//		
//		String filename = Stringify.concat(UUID.randomUUID().toString().replaceAll("-", ""), C.dot, C.png);
//		int size = 100;
//		File myFile = new File(fi.getTempFolder(), filename);
//		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
//		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//
//		// Now with zxing version 3.2.1 you could change border size (white border size
//		// to just 1)
//		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
//		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
//
//		QRCodeWriter qrCodeWriter = new QRCodeWriter();
//		BitMatrix byteMatrix = qrCodeWriter.encode(Stringify.concat(qrcode_base_url, String.valueOf(transfer_id)), BarcodeFormat.QR_CODE, size, size, hintMap);
//		int CrunchifyWidth = byteMatrix.getWidth();
//		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
//		image.createGraphics();
//
//		Graphics2D graphics = (Graphics2D) image.getGraphics();
//		graphics.setColor(Color.WHITE);
//		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
//		graphics.setColor(Color.BLACK);
//
//		for (int i = 0; i < CrunchifyWidth; i++) {
//			for (int j = 0; j < CrunchifyWidth; j++) {
//				if (byteMatrix.get(i, j)) {
//					graphics.fillRect(i, j, 1, 1);
//				}
//			}
//		}
//		ImageIO.write(image, C.png, myFile);
//		
//		StandardFileSystemManager manager = new StandardFileSystemManager();
//		try {
//			manager.init();
//			
//			FileSystemOptions opts = new FileSystemOptions();
//			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
//			builder.setStrictHostKeyChecking(opts, "no");
//			builder.setUserDirIsRoot(opts, true);
//			builder.setTimeout(opts, 10000);
//			
//			FileObject localFile = manager.resolveFile(myFile.getAbsolutePath());
//			
//			FileObject productionFolder = manager.resolveFile(qrcode_base_directory, opts);
//			if (!productionFolder.exists()) {
//				productionFolder.createFolder();
//			}
//			FileObject productionThumbnail = manager.resolveFile(productionFolder, filename, opts);
//			localFile.moveTo(productionThumbnail);
//		} finally {
//			manager.close();
//		}
//		
//		return filename;
//	}

}
