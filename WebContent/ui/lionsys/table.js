/*global jQuery, $, sendRequest, funcGetTableData, alert, funcSetTableData, funcLoadingStart, funcLoadingEnd*/

/**
 * global jQuery
 * version: 1.0
 * @requires jQuery v1.4.2 or later
 * 
 * Copyright 2010, Henry Hung
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

jQuery.fn.createTable = function() {
	var arg = arguments[0] || {};
	var ajaxFunction = sendRequest.table;
	if (arg.ajaxFunction !== undefined && $.isFunction(arg.ajaxFunction)) {
		ajaxFunction = arg.ajaxFunction;
	}
	
	var executeInsert;
	if (arg.executeInsert !== undefined && $.isFunction(arg.executeInsert)) {
		executeInsert = arg.executeInsert;
	}
	
	var executeUpdate;
	if (arg.executeUpdate !== undefined && $.isFunction(arg.executeUpdate)) {
		executeUpdate = arg.executeUpdate;
	}
	
	var executeDelete;
	if (arg.executeDelete !== undefined && $.isFunction(arg.executeDelete)) {
		executeDelete = arg.executeDelete;
	}
	
	var init = function($table){
		$table.empty();
		
		//
		// --- jQuery OBJECTS --- BEGIN
		//
		var $tableControl;
		var $tableHeader;
		var $tableHeaderSort;
		var $tableBody;
		var $pageTotalRows;
		var $pageButton;
		var $loading;
		var $filterButton;
		var $tableFilter;
		//
		// --- jQuery OBJECTS --- END
		//
		
		//
		// --- VARIABLES --- BEGIN
		//
		var maxNumberPerPage = 15;
		var maxPage = 0;
		var maxRowCount = -1;
		var currentPage = 1;
		var className = '';
		var filterString = '';
		var filterSql = '';
		var filterSqlValues = '';
		var sqlTempSessionId = '';
		var sqlTempPageId = '';
		var sqlTempJoinSql = '';
		var hideFilter = true;
		var disableFilter = true;
		var isShiftPressed = false;
		// insert, update, delete enable or disable flag
		var isDisableInsert = false;
		var isDisableUpdate = false;
		var isDisableDelete = false;
		var labelInsert = 'Insert';
		var labelUpdate = 'Update';
		var labelDelete = 'Delete';
		// default view is for data maintenance with insert, update, delete
		// view mode for viewing only without insert, update, delete buttons.
		var isViewMode = false;
		// default selection is multiple
		// single select mode can only select one row at a time.
		var isSingleSelectMode = false;
		// can only be used with single select mode set to true
		// enable interaction with double click
		var singleSelectModeDblClick;
		// disable page by query all data, page number must be <= 0
		var isDisablePaging = false;
		// after set table data event
		var afterSetTableDataEvent;
		// after set table column event
		var afterSetTableColumnEvent;
		var imagePreviewWidth;
		var imagePreviewHeight;
		var summaryColumns = '';
		var stringColumns = '';
		var isShowCheckBox = false;
		var isDisableExport = false;
		//
		// --- VARIABLES --- END
		//
		
		//
		// --- ASSIGN INPUT VARIABLES --- BEGIN
		//
		if (isNaN(arg.page)) {
			currentPage = 1;
		}
		else {
			currentPage = Number(arg.page);
		}
		
		if (isNaN(arg.maxRow)) {
			maxNumberPerPage = 15;
		}
		else {
			maxNumberPerPage = Number(arg.maxRow);
		}
		
		className = arg.className;
		// insert, update, delete button enable disable flag
		isDisableInsert = arg.disableInsert === true;
		isDisableUpdate = arg.disableUpdate === true;
		isDisableDelete = arg.disableDelete === true;
		isShowCheckBox = arg.showCheckBox === true;
		isDisableExport = arg.disableExport === true;
		if (arg.labelInsert) {
			labelInsert = arg.labelInsert;
		}
		if (arg.labelUpdate) {
			labelUpdate = arg.labelUpdate;
		}
		if (arg.labelDelete) {
			labelDelete = arg.labelDelete;
		}
		// view mode value
		isViewMode = arg.viewMode === true;
		// single mode value
		isSingleSelectMode = arg.singleSelectMode === true;
		// single mode double click interaction
		if ($.isFunction(arg.singleSelectModeDblClick)) {
			singleSelectModeDblClick = arg.singleSelectModeDblClick;
		}
		// disable paging
		isDisablePaging = arg.disablePaging === true;
		// 
		if ($.isFunction(arg.afterSetTableDataEvent)) {
			afterSetTableDataEvent = arg.afterSetTableDataEvent;
		}
		//
		if ($.isFunction(arg.afterSetTableColumnEvent)) {
			afterSetTableColumnEvent = arg.afterSetTableColumnEvent;
		}
		imagePreviewWidth = arg.imagePreviewWidth;
		imagePreviewHeight = arg.imagePreviewHeight;
		if (arg.summaryColumns != undefined)
			summaryColumns = arg.summaryColumns;
		if (arg.stringColumns != undefined)
			stringColumns = arg.stringColumns;
		//
		// --- ASSIGN INPUT VARIABLES --- END
		//
		
		// hook window key event for detecting shift
		$table.keydown(function(e){
			if (e.keyCode === 17) {
				isShiftPressed = true;
			}
		}).keyup(function(e){
			if (e.keyCode === 17) {
				isShiftPressed = false;
			}
		});
		
		var funcShowFilter = function(){
			if (!hideFilter) {
				return;
			}
			hideFilter = false;
			$tableFilter.show();
			$filterButton.button('disable').removeClass('ui-state-hover').removeClass('ui-state-focus');
		};
		
		var funcHideFilter = function(){
			if (hideFilter) {
				return;
			}
			hideFilter = true;
			$tableFilter.hide();
			$filterButton.button('enable');
		};
		
		var funcRefreshPageInfo = function() {
			if (isDisablePaging) {
				maxRowCount = $tableBody.children('tr').length;
				maxPage = 1;
			}
			else {
				var x = $tableBody.children('tr[maxrow]:eq(0)').attr('maxrow');
				if (isNaN(x)) {
					maxRowCount = 0;
				}
				else {
					maxRowCount = parseInt(x, 10);
					if (maxRowCount < $tableBody.children('tr').length) {
						maxRowCount = 0;
					}
				}
				maxPage = Math.ceil(maxRowCount/maxNumberPerPage);
			}
			
			if (maxPage > 0) {
				$tableControl.find('.page-number').button('option', 'label', 'page: ' + currentPage + ' / ' + maxPage);
				$pageTotalRows.text('total rows: ' + maxRowCount).parent().parent().show();
			}
			else {
				$tableControl.find('.page-number').button('option', 'label', 'page: ' + currentPage + ' / ?');
				$pageTotalRows.text('total rows: ?').parent().parent().hide();
			}
		};
		
		var funcSortTableColumn = function(){
			var direction = $(this).attr('DIRECTION');
			if (direction === 'asc') { 
				direction = 'desc';
			}
			else { 
				direction = 'asc';
			}
			
			if (!isShiftPressed) {
				$tableHeaderSort.removeClass('default asc desc').addClass('default').attr('DIRECTION', 'default');
			}
			$(this).removeClass('default asc desc').addClass(direction).attr('DIRECTION', direction);
			funcGetTableData(currentPage, filterString);
		};
		
		//
		// --- GET TABLE COLUMN --- BEGIN
		//
		var funcGetTableColumn = function(){
			// prepare POST params
			var post = {
				'Action': 'col',
				'ClassName' : className
			};
			var columnCount = 0;
			ajaxFunction({
				data: post,
				success: function(data){
					if (data.substring(0, 5) === 'error'){
						alert(data);
						return;
					}
					
					$tableHeader.empty().append(data).find('tr td[DIRECTION]').each(function(){
						var e = $(this);
						var direction = e.attr('DIRECTION');
						if (direction === 'default' || direction === 'asc' || direction === 'desc') {
							e.addClass('sort ' + direction);
						}
					});
					
					var allcols = $tableHeader.find('tr td');
					columnCount = allcols.length;
					
					$tableHeaderSort = $tableHeader.find('tr td.sort');
					$tableHeaderSort.click(funcSortTableColumn);
					if (!isViewMode) {
						$tableHeader.find('tr').prepend('<td class="winbond-table-cell-control-box"><button type="button" class="winbond-table-cell-control-box-insert">' + labelInsert + '</button></td><td>&nbsp;</td>');
					}
					if (isShowCheckBox) {
						if (isSingleSelectMode) {
							$tableHeader.find('tr').prepend('<td class="winbond-table-cell-control-box"></td>');
						} else {
							$tableHeader.find('tr').prepend('<td class="winbond-table-cell-control-box"><input type="checkbox" class="winbond-table-cell-control-box-checkall" style="cursor: pointer;"/></td>');
							$tableHeader.find('input.winbond-table-cell-control-box-checkall').change(function() {
								var isChecked = $(this).is(':checked');
								$tableBody.find('td.winbond-table-cell-control-box input.winbond-table-cell-control-box-check').prop('checked', isChecked);
							});
						}
					}
					
					//
					// --- EXECUTE INSERT ---
					//
					$tableHeader.find('.winbond-table-cell-control-box-insert').button({
						icons: {
							primary: 'ui-icon-plus'
						},
						text: false
					}).click(function() {
						if (executeInsert !== undefined) {
							executeInsert(function(data) {
								if (data !== undefined) {
									var v = funcSetTableData(data, currentPage);
									$pageButton.filter('.mi-reload').removeClass('ui-state-disabled');
									if (isDisableUpdate) {
										$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-update').button('disable');
									}
									if (isDisableDelete) {
										$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-delete').button('disable');
									}
									return v;
								}
							});
						}
					});
					
					disableFilter = true;
					$tableHeader.find('tr td[COLUMN_NAME]').each(function(entryIndex, entry) {
						var td = $(this);
						if (td.attr('CAN_FILTER') === 'true') {
							disableFilter = false;
							var dataType = td.attr('DATA_TYPE');
							var option;
							var input;
							if (dataType === 'DATETIME' || dataType === 'DATE') {
								option = '<select class="filter-operator" org="0"><option selected>=</option><option>&lt;</option><option>&gt;</option><option>&lt;=</option><option>&gt;=</option><option>&lt;&gt;</option><option>BETWEEN</option></select>';
								if (dataType === 'DATETIME') {
									input = '<div><input type="text" class="winbond-table-filter-datetime filter-input" value="" org=""/><span class="filter-input-2"><br/>AND<br/><input type="text" class="winbond-table-filter-datetime filter-input" value="" org=""/></span></div>';
								}
								else {
									input = '<div><input type="text" class="winbond-table-filter-date filter-input" value="" org=""/><span class="filter-input-2"><br/>AND<br/><input type="text" class="winbond-table-filter-date filter-input" value="" org=""/></span></div>';
								}
							}
							else if (dataType === 'NUMBER') {
								option = '<select class="filter-operator" org="0"><option selected>=</option><option>&lt;</option><option>&gt;</option><option>&lt;=</option><option>&gt;=</option><option>&lt;&gt;</option><option>IN</option><option>IN_31</option><option>BETWEEN</option></select>';
								input = '<div><input class="filter-input" type="text" value="" org="" /><span class="filter-input-2"><br/>AND<br/><input type="text" class="filter-input" value="" org=""/></span></div>';
							}
							else {
								option = '<select class="filter-operator" org="0"><option selected>=</option><option>&lt;</option><option>&gt;</option><option>&lt;=</option><option>&gt;=</option><option>&lt;&gt;</option><option>LIKE</option><option>IN</option><option>BETWEEN</option></select>';
								input = '<div><input class="filter-input" type="text" value="" org="" /><span class="filter-input-2"><br/>AND<br/><input type="text" class="filter-input" value="" org=""/></span></div>';
							}
							
							$tableFilter.append('<tr c="' + td.attr('COLUMN_NAME') + '" d="' + dataType + '"><td class="title">' + td.text() + '</td><td>' + option + '</td><td>' + input + '</td></tr>');
						}
					});
					
					$tableFilter.find('.winbond-table-filter-datetime').createDateTimePicker();
					$tableFilter.find('.winbond-table-filter-date').createDatePicker();
					$tableFilter.find('.filter-input-2').hide();
					$tableFilter.find('select').change(function(){
						var o = $(this);
						var tr = o.parent().parent();
						var selected = o.find('option:selected').text();
						var dataType = tr.attr('d');
						if (selected === 'BETWEEN') {
							tr.find('.filter-input-2').show();
						}
						else {
							tr.find('.filter-input-2').hide();
						}
					});
					$tableFilter.append('<tfoot><tr><td></td><td colspan=2><button type="button" class="mi-filter-save">Filter</button><button type="button" class="mi-filter-cancel">Cancel</button><button type="button" class="mi-filter-reset">Reset</button></td></tr></tfoot>');
					$tableFilter.find('button.mi-filter-save').click(function() {
						var filter = '';
						$tableFilter.find('select.filter-operator').each(function(entryIndex, entry){
							var o = $(entry);
							var tr = $(entry).parent().parent();
							var operator = o.find('option:selected').text();
							var dataType = tr.attr('d');
							var columnName = tr.attr('c');
							var v1;
							var v = tr.find('.filter-input');
							v1 = $(v.get(0)).prop('value');
							if (v1 !== '') {
								if (operator === 'BETWEEN') {
									var v2 = $(v.get(1)).prop('value');
									if (v2 !== '') {
										$(v.get(0)).attr('org', v1);
										$(v.get(1)).attr('org', v2);
										filter += columnName + String.fromCharCode(31) + dataType + String.fromCharCode(31) + operator + String.fromCharCode(31) + v1 + String.fromCharCode(31) + v2 + String.fromCharCode(31);
									}
								}
								else {
									$(v.get(0)).attr('org', v1);
									filter += columnName + String.fromCharCode(31) + dataType + String.fromCharCode(31) + operator + String.fromCharCode(31) + v1 + String.fromCharCode(31);
								}
							}
						});
						funcGetTableData(1, filter);
						funcHideFilter();
					});
					$tableFilter.find('button.mi-filter-cancel').click(function() {
						$tableFilter.find('.filter-input').each(function(entryIndex, entry){
							var o = $(entry);
							o.val(o.attr('org'));
						});
						$tableFilter.find('.winbond-table-filter-datetime').createDateTimePicker();
						$tableFilter.find('.winbond-table-filter-date').createDatePicker();
						funcHideFilter();
					});
					$tableFilter.find('button.mi-filter-reset').click(function() {
						$tableFilter.find('.filter-input').val('').attr('org', '');
						$tableFilter.find('.winbond-table-filter-datetime').createDateTimePicker();
						$tableFilter.find('.winbond-table-filter-date').createDatePicker();
					});
					$tableFilter.find('input.filter-input').keydown(function(e) {
						if (e.keyCode === 13) {
							$tableFilter.find('button.mi-filter-save').click();
						}
					});
				},
				beforeSend: function(){
					funcLoadingStart();
				},
				complete: function(){
					funcLoadingEnd();
					if (columnCount > 0) {
						if (afterSetTableColumnEvent === undefined) {
							funcGetTableData(currentPage, filterString);
						}
						else {
							$tableControl.find('.mi-filter').hide();
							afterSetTableColumnEvent();
						}
					}
				}
			});
		};
		//
		// --- GET TABLE COLUMN --- END
		//
		
		//
		// --- SET TABLE DATA --- BEGIN
		//
		var funcSetTableData = function(data, page) {
			currentPage = page;
			var v = $(data);
			
			v.filter('tr').not('.dont-select').hover(function() {
				$(this).addClass('hover');
			}, function() {
				$(this).removeClass('hover');
			});
			
			// create maintenance tool
			if (!isViewMode) {
				v.filter('tr').prepend('<td class="winbond-table-cell-control-box"><button type="button" class="winbond-table-cell-control-box-update">' + labelUpdate + '</button></td>' +
						'<td class="winbond-table-cell-control-box"><button type="button" class="winbond-table-cell-control-box-delete">' + labelDelete + '</button></td>');
			}
			if (isShowCheckBox) {
				if (isSingleSelectMode) {
					v.find('td').click(function() {
						var cb = $(this).parent().find('input.winbond-table-cell-control-box-check');
						cb.prop('checked', true);
					});
					v.filter('tr').prepend('<td class="winbond-table-cell-control-box"><input type="radio" name="single_select_mode" class="winbond-table-cell-control-box-check" style="cursor: pointer;"/></td>');
				} else {
					v.find('td').click(function() {
						var cb = $(this).parent().find('input.winbond-table-cell-control-box-check');
						var is_checked = cb.prop('checked');
						cb.prop('checked', !is_checked);
					});
					v.filter('tr').prepend('<td class="winbond-table-cell-control-box"><input type="checkbox" class="winbond-table-cell-control-box-check" style="cursor: pointer;"/></td>');
				}
			}
			
			// update row
			v.find('.winbond-table-cell-control-box-update').button({
				icons: {
					primary: 'ui-icon-wrench'
				},
				text: false
			}).click(function(){
				if (executeUpdate !== undefined) {
					var row = $(this).parent().parent();
					executeUpdate(
						row.children('td:gt(1)'),
						function(data, deleteInsteadUpdate) {
							if (typeof deleteInsteadUpdate === 'undefined') { deleteInsteadUpdate = false; }
							
							if (deleteInsteadUpdate === true) {
								row.remove();
								return;
							}
							
							if (data !== undefined) {
								if (isShowCheckBox) {
									row.children('td:gt(2)').remove();
								} else {
									row.children('td:gt(1)').remove();
								}
								row.append($(data).children('td'));
								row.find('img').imagePreview(imagePreviewWidth, imagePreviewHeight);
								if (afterSetTableDataEvent !== undefined) {
									var v = afterSetTableDataEvent(row);
									
									if (!isDisableUpdate) {
										$tableBody.find('tr.disable-update td button.winbond-table-cell-control-box-update').button('disable');
									}
									if (!isDisableDelete) {
										$tableBody.find('tr.disable-delete td button.winbond-table-cell-control-box-delete').button('disable');
									}
									
									return v;
								}
							}
						}
					);
				}
			}); // update row
			
			// delete row
			v.find('.winbond-table-cell-control-box-delete').button({
				icons: {
					primary: 'ui-icon-trash'
				},
				text: false
			}).click(function() {
				if (executeDelete !== undefined) {
					var row = $(this).parent().parent();
					executeDelete(
						row.children('td:gt(1)'),
						function(data) {
							if (data !== undefined) {
								var dataTD = $(data).children('td');
								if (dataTD.length > 0) {
									if (isShowCheckBox) {
										row.children('td:gt(2)').remove();
									} else {
										row.children('td:gt(1)').remove();
									}
									row.append(dataTD);
									if (afterSetTableDataEvent !== undefined) {
										var v = afterSetTableDataEvent(row);
										
										if (!isDisableUpdate) {
											$tableBody.find('tr.disable-update td button.winbond-table-cell-control-box-update').button('disable');
										}
										if (!isDisableDelete) {
											$tableBody.find('tr.disable-delete td button.winbond-table-cell-control-box-delete').button('disable');
										}
										
										return v;
									}
								}
								else {
									row.remove();
									
									$tableBody.children('tr').removeClass('odd');
									$tableBody.children('tr').removeClass('even');
									$tableBody.children('tr:odd').addClass('odd');
									$tableBody.children('tr:even').addClass('even');
									
									if ($tableBody.children('tr').length === 0) {
										$pageButton.filter('.mi-reload').click();
									}
									else {
										funcRefreshPageInfo();
									}
								}
							}
						}
					);
				}
			}); // delete row
			
			v.find('img').imagePreview(imagePreviewWidth, imagePreviewHeight);
			
			if (isSingleSelectMode && singleSelectModeDblClick !== undefined) {
				v.filter('tr').dblclick(singleSelectModeDblClick);
			}
			
			// set table data
			$tableBody.append(v);
			$tableBody.children('tr:odd').addClass('odd');
			$tableBody.children('tr:even').addClass('even');
			
			funcRefreshPageInfo();
			
			if (afterSetTableDataEvent !== undefined) {
				afterSetTableDataEvent(v);
			}
			
			return v;
		};
		//
		// --- SET TABLE DATA --- END
		//

		//
		// --- GET TABLE DATA --- BEGIN
		//
		var funcGetTableData = function(page, inFilter){
			if (isDisablePaging) {
				page = -1;
			}
			// get columns
			var column = '$2$' + String.fromCharCode(31);
			$tableHeader.find('tr td[COLUMN_NAME]').each(function(){
				column += $(this).attr('COLUMN_NAME') + String.fromCharCode(31) + $(this).attr('DATA_TYPE') + String.fromCharCode(31) + $(this).attr('VIRTUAL') + String.fromCharCode(31);
			});
			// get sort sequence
			var sort = '';
			$tableHeaderSort.filter(':not(.default)').each(function(){
				sort += $(this).attr('COLUMN_NAME') + String.fromCharCode(31) + $(this).attr('DIRECTION') + String.fromCharCode(31);
			});
			// prepare POST params
			var post = {
				'Action': 'row',
				'ClassName': className,
				'Page': page,
				'Offset': maxNumberPerPage,
				'Sort': sort,
				'Column': column,
				'Filter': inFilter,
				'FilterSql': filterSql,
				'FilterSqlValues': filterSqlValues,
				'SessionId': sqlTempSessionId,
				'PageId': sqlTempPageId,
				'JoinSql': sqlTempJoinSql
			};
			// get table data
			ajaxFunction({
				data: post,
				success: function(data){
					if (data.substring(0,5) === 'error') {
						alert(data);
						return;
					}
					filterString = inFilter;
					$tableBody.empty();
					$tableHeader.find('input.winbond-table-cell-control-box-checkall').prop('checked', false);
					funcSetTableData(data, page);
				},
				beforeSend: funcLoadingStart,
				complete: funcLoadingEnd
			});
		};
		//
		// --- GET TABLE DATA --- END
		//
		
		var funcLoadingStart = function(){
			// disable page button
			$tableControl.find('button').button('disable').removeClass('ui-state-hover').removeClass('ui-state-focus');
			// disable sort
			if ($tableHeaderSort) {
				$tableHeaderSort.unbind('click');
			}
			$tableHeader.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-insert').button('disable');
			$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-update, .winbond-table-cell-control-box .winbond-table-cell-control-box-delete').button('disable');
			// show loading icon
			$loading.show();
		};
		
		var funcLoadingEnd = function(){
			//
			// --- CONTROL BOX BUTTON STATE --- BEGIN ---
			//
			// enable all buttons
			$tableControl.find('button').button('enable');
			// disable button for page control based on page criteria
			var count = $tableBody.children('tr').length;
			if (count < maxNumberPerPage || (maxPage > 0 && currentPage >= maxPage)) {
				$pageButton.filter('.mi-page-next, .mi-page-next-10').button('disable');
			}
			
			if (maxPage <= 0 || currentPage === maxPage) {
				$pageButton.filter('.mi-last').button('disable');
			}
			
			if (currentPage === 1) {
				$pageButton.filter('.mi-home, .mi-page-prev, .mi-page-prev-10').button('disable');
			}
			
			//if ($tableBody.children('tr:eq(0)').length <= 0)
			//	$pageButton.filter('.mi-reload').addClass('ui-state-disabled');
			// if filter area opened, then disable button for open filter area
			if (hideFilter === false || disableFilter) {
				$filterButton.button('disable');
			}
			
			//
			// --- TABLE STATE ---
			//
			// enable header column sort action
			$tableHeaderSort.click(funcSortTableColumn);
			// enable insert, update, delete buttons
			var state = isDisableInsert ? 'disable' : 'enable';
			$tableHeader.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-insert').button(state);
			state = isDisableUpdate ? 'disable' : 'enable';
			$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-update').button(state);
			state = isDisableDelete ? 'disable' : 'enable';
			$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-delete').button(state);
			
			if (!isDisableUpdate) {
				$tableBody.find('tr.disable-update td button.winbond-table-cell-control-box-update').button('disable');
			}
			if (!isDisableDelete) {
				$tableBody.find('tr.disable-delete td button.winbond-table-cell-control-box-delete').button('disable');
			}
			
			// hide loading icon
			$loading.hide();
			
			if (isDisableExport) {
				$tableControl.find('button.export-to-excel').button('disable');
			}
		};
		
		// define table control box for page navigation
		var c = '<tr><td><table class="winbond-table-control"><tr>' +
		'<td><button type="button" class="winbond-table-control-button mi-filter">Filter</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-reload">Reload Page</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-home">Page 1</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-page-prev-10">Previous 10 Pages</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-page-prev">Previous Page</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-page-next">Next Page</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-page-next-10">Next 10 Pages</button></td>' +
		'<td><button type="button" class="winbond-table-control-button mi-last">Last Page</button></td>' +
		'<td><button type="button" class="page-number"></button></td>' +
		'<td nowrap class="page-total-rows"><div style="cursor: default" class="ui-widget-content ui-state-default ui-corner-all ui-button ui-button-text-only"><span style="cursor: default" class="ui-button-text"></span></div></td>' +
		'<td><button type="button" class="export-to-excel">Export</button></td>' +
		'<td><div class="winbond-table-loading-icon mi-loading" ></div></td>' +
		'</tr></table></td></tr><tr><td><table class="winbond-table-filter"></table></td></tr>';
		
		$table.append(c);
		$tableFilter = $table.find('table.winbond-table-filter').hide();
		$table.find('.mi-reload').hide();
		
		var $pageNumberDialog = $('<div title="Change Page"><table style="margin: auto 15px auto 15px;"><tr><td>Page:</td><td><input type="number" value="" style="width: 100px;"/></td></tr></table></div>').appendTo($table).dialog({
			autoOpen: false,
			modal: true,
			width: 200,
			minWidth: 200,
			height: 150,
			buttons: {
				'Ok': function() {
					var n = $(this).find('input').val();
					if (n.length > 0 && !isNaN(n)) {
						n = parseInt(n, 10);
						if (n >= 1) {
							if (n > maxPage && maxPage > 0) {
								n = maxPage;
							}
						}
						else {
							n = 1;
						}
						funcGetTableData(n, filterString);
					}
					$(this).dialog('close');
				},
				'Cancel': function() {
					$(this).dialog('close');
				}
			}
		});
		// set table column and data area
		$table.append('<tr><td><table class="winbond-table"><thead></thead><tbody></tbody></table></td></tr>');
		// set cache
		$tableControl = $table.find('.winbond-table-control');
		$table = $table.find('.winbond-table');
		$tableHeader = $table.children('thead');
		$tableBody = $table.children('tbody');
		
		$tableControl.find('.page-number').button().click(function() {
			$pageNumberDialog.dialog('open').find('input').val('').focus();
		});
		$tableControl.find('.export-to-excel').button().click(function() {
			// get sort sequence
			var sort = '';
			$tableHeaderSort.filter(':not(.default)').each(function(){
				sort += $(this).attr('COLUMN_NAME') + String.fromCharCode(31) + $(this).attr('DIRECTION') + String.fromCharCode(31);
			});
			// prepare POST params
			var post = {
				'Action': 'export',
				'ClassName': className,
				'Sort': sort,
				'Filter': filterString,
				'FilterSql': filterSql,
				'FilterSqlValues': filterSqlValues,
				'SessionId': sqlTempSessionId,
				'PageId': sqlTempPageId,
				'JoinSql': sqlTempJoinSql,
				'SummaryColumns': summaryColumns,
				'StringColumns': stringColumns
			};
			// get table data
			ajaxFunction({
				data: post,
				success: function(data){
					if (data !== '') {
						sendRequest.downloadFile(data);
					}
				},
				beforeSend: funcLoadingStart,
				complete: funcLoadingEnd
			});
		});
		if (isDisableExport) {
			$tableControl.find('.export-to-excel').button('disable');
		}
		$pageTotalRows = $tableControl.find('.page-total-rows > div > span');
		
		$tableControl.find('.mi-reload').button({
			icons: { primary: 'ui-icon-refresh' },
			text: false
		}).data('offset', 'reload');
		
		$tableControl.find('.mi-home').button({
			icons: { primary: 'ui-icon-seek-first' },
			text: false
		}).data('offset', 'home');
		
		$tableControl.find('.mi-page-prev-10').button({
			icons: { primary: 'ui-icon-seek-prev' },
			text: false
		}).data('offset', '-10');
		
		$tableControl.find('.mi-page-prev').button({
			icons: { primary: 'ui-icon-triangle-1-w' },
			text: false
		}).data('offset', '-1');
		
		$tableControl.find('.mi-page-next').button({
			icons: { primary: 'ui-icon-triangle-1-e' },
			text: false
		}).data('offset', '1');
		
		$tableControl.find('.mi-page-next-10').button({
			icons: { primary: 'ui-icon-seek-next' },
			text: false
		}).data('offset', '10');
		
		$tableControl.find('.mi-last').button({
			icons: { primary: 'ui-icon-seek-end' },
			text: false
		}).data('offset', 'last');
		
		$pageButton = $tableControl
			.find('.mi-home, .mi-page-prev-10, .mi-page-prev, .mi-page-next, .mi-page-next-10, .mi-reload, .mi-last')
			.click(function(){
				var o = $(this).data('offset');
				var i;
				if (o === 'home') {
					i = 1;
				}
				else if (o === 'reload') {
					i = currentPage;
				}
				else if (o === 'last' && maxPage > 0) {
					i = maxPage;
				}
				else {
					i = currentPage + parseInt(o, 10);
				}
				
				if (i < 1) {
					i = 1;
				}
				else if (maxPage > 0 && i > maxPage) {
					i = maxPage;
				}
				funcGetTableData(i, filterString);
			});
		
		// set filter action
		$filterButton = $tableControl.find('.mi-filter').button({
			icons: {
				primary: 'ui-icon-pencil'
			},
			text: false
		}).click(function(){
			funcShowFilter();
		});
		// set loading animation
		$loading = $tableControl.find('.mi-loading').hide();
		// get table column
		funcGetTableColumn();
		
		if (isDisablePaging) {
			$tableControl.find('.page-number').hide();
			$tableControl.find('.mi-home, .mi-page-prev-10, .mi-page-prev, .mi-page-next, .mi-page-next-10, .mi-last').each(function(entryIndex, entry) {
				$(entry).parent().hide();
			});
		}
		
		return {
			'funcReload': function() {
				$filterButton.click();
				$tableFilter.find('button.mi-filter-reset').click();
				$tableFilter.find('button.mi-filter-save').click();
			},
			'funcEmptyTableBody': function() {
				$tableBody.empty();
			},
			'funcGetTableFilter': function() {
				return filterString;
			},
			'funcGetTableBody': function() {
				return $tableBody;
			},
			'funcGetTableHeader': function() {
				return $tableHeader;
			},
			'searchFor': function(columnName, operator, value, f1, f2, sessionId, pageId, joinSql) {
				$filterButton.click();
				$tableFilter.find('button.mi-filter-reset').click();
				var row;
				if ($.isArray(columnName)) {
					var i;
					for (i = 0; i < columnName.length; i++) {
						row = $tableFilter.find('tr[c=' + columnName[i] + ']');
						if (row !== undefined && row.length > 0) {
							row.find('.filter-operator').val(operator[i]);
							if (operator[i] === 'BETWEEN') {
								var xx = value[i].split(String.fromCharCode(31));
								var rr = row.find('.filter-input');
								$(rr.get(0)).val(xx[0]);
								$(rr.get(1)).val(xx[1]);
							}
							else {
								row.find('.filter-input').val(value[i]);
							}
						}
					}
				}
				else {
					row = $tableFilter.find('tr[c=' + columnName + ']');
					if (row !== undefined && row.length > 0) {
						row.find('.filter-operator').val(operator);
						row.find('.filter-input').val(value);
					}
				}
				filterSql = f1;
				filterSqlValues = f2;
				sqlTempSessionId = sessionId;
				sqlTempPageId = pageId;
				sqlTempJoinSql = joinSql;
				$tableFilter.find('button.mi-filter-save').click();
				return true;
			},
			'funcUpdateRow': function(row, data) {
				row.children('td:gt(1)').remove();
				row.append($(data).children('td'));
				row.find('img').imagePreview(imagePreviewWidth, imagePreviewHeight);
					
				if (!isDisableUpdate) {
					if (row.hasClass('disable-update'))
						row.find('button.winbond-table-cell-control-box-update').button('disable');
					else
						row.find('button.winbond-table-cell-control-box-update').button('enable');
				}
				
				if (!isDisableDelete) {
					if (row.hasClass('disable-delete'))
						row.find('button.winbond-table-cell-control-box-delete').button('disable');
					else
						row.find('button.winbond-table-cell-control-box-delete').button('enable');
				}
			},
			'funcInsertRow': function(data) {
				if (data !== undefined) {
					funcSetTableData(data, currentPage);
					$pageButton.filter('.mi-reload').removeClass('ui-state-disabled');
					if (isDisableUpdate) {
						$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-update').button('disable');
					}
					if (isDisableDelete) {
						$tableBody.find('.winbond-table-cell-control-box .winbond-table-cell-control-box-delete').button('disable');
					}
				}
			},
			'funcGetFilter': function() {
				// get sort sequence
				var sort = '';
				$tableHeaderSort.filter(':not(.default)').each(function(){
					sort += $(this).attr('COLUMN_NAME') + String.fromCharCode(31) + $(this).attr('DIRECTION') + String.fromCharCode(31);
				});
				return {
					'Sort': sort,
					'Filter': filterString,
					'FilterSql': filterSql,
					'FilterSqlValues': filterSqlValues,
					'SessionId': sqlTempSessionId,
					'PageId': sqlTempPageId,
					'JoinSql': sqlTempJoinSql
				}
			},
			'funcGetCheckedRows': function() {
				var rows = [];
				$tableBody.find('input.winbond-table-cell-control-box-check:checked').each(function(i, e) {
					rows.push($(e).parent().parent());
				});
				return rows;
			}
		};
	};
	
	var listInit = [];
	var i;
	for (i = 0; i < this.length; i++) {
		listInit.push(init($(this[i])));
	}
	
	return listInit;
};

jQuery.fn.imagePreview = function(width, height) {
	// check if body already contains preview element
	var preview = $('#ImagePreview');
	if (preview.length === 0) {
		preview = $('<p id="ImagePreview" style="position: absolute; border: 1px solid black; padding: 5px; background: #333"><img src=""/></p>');
		preview.hide();
		preview.appendTo($('body'));
	}

	var isInputGiven = false;
	if (width) {
		if (height) {
			if (!isNaN(width) && !isNaN(height)) {
				isInputGiven = true;
			}
		}
	}
	if (isInputGiven) {
		preview.children('img').attr('width', width).attr('height', height);
	}
	
	var me = $(this);
	
	me.hover(function(e) {
		var url = $(this).attr('src');
		url = url.replace(new RegExp('_thumbnail.png$'), '.png');
		preview.children('img').attr('src', url);
		preview.show();
	}, function() {
		preview.hide();
	});
	
	me.mousemove(function(e) {
		preview.css('top', (e.pageY - 10) + "px").css('left', (e.pageX + 15) + "px");
	});
	
	return me;
};