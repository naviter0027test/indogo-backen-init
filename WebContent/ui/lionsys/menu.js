/**
 * version: 1.0
 * @requires jQuery v1.4.2 or later
 * 
 * Copyright 2010, Henry Hung
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

$.fn.createToolbar = function(data, action) {
	var caller = $(this);
	var action = action;
	
	if (action) {
		if (!$.isFunction(action))
			throw 'action must be function type';
	}
	else {
		throw 'action must be provided';
	}
	
	caller.append(data);
	caller.addClass('ui-widget menu-area');
	
	var toolbar = caller.find('table.menu-toolbar');
	if (toolbar.length == 0)
		throw 'Cannot found class menu-toolbar';
	if (toolbar.length > 1)
		throw 'Too much menu-toolbar';
	
	var menuItems = caller.find('table.menu-item');
	
	toolbar.find('tr td[name]').addClass('menu-toolbar-has-child');
	menuItems.find('tr td[name]').addClass('menu-item-has-child');
	
	menuItems.hide();
	
	var openedMenuItems = [];
	var killMenus = function() {
		for (var i = openedMenuItems.length - 1; i >= 0; i--)
			openedMenuItems.pop();
		menuItems.hide();
		toolbar.find('tr td.ui-state-active').removeClass('ui-state-active');
		menuItems.find('tr td.ui-state-active').removeClass('ui-state-active');
	};
	
	$(document).click(killMenus);
	
	var navigation = [];
	
	toolbar.find('tr td').addClass('ui-state-default')
		.hover(
			function() {
				$(this).addClass('ui-state-hover');
			},
			function() {
				$(this).removeClass('ui-state-hover');
			}
		)
		.click(function() {
			var toolbarItem = $(this);
			
			navigation = [toolbarItem.text()];

			killMenus();
			
			toolbarItem.addClass('ui-state-active');
			
			// open current menu item
			var menuItemId = toolbarItem.attr('name');
			if (menuItemId == undefined || menuItemId.length == 0) {
				killMenus();
				var href = toolbarItem.find('a');
				if (href.length > 0)
					href = href.get(0);
				else
					href = undefined;
				action(href, navigation);
				return false;
			}
			
			var mi = menuItems.filter('#' + menuItemId);
			
			mi.show().position({
				of: toolbarItem,
				my: 'left top',
				at: 'left bottom',
				offset: '0',
				collision: 'none none'
			}).position({
				of: toolbarItem,
				my: 'left top',
				at: 'left bottom',
				offset: '0',
				collision: 'none none'
			});
			
			return false;
		});
	
	menuItems.addClass('ui-widget ui-widget-content').find('tr td').addClass('ui-state-default')
		.hover(
			function() {
				$(this).addClass('ui-state-hover');
			},
			function() {
				$(this).removeClass('ui-state-hover');
			}
		)
		.click(function() {
			var menuItem = $(this);
			
			navigation.push(menuItem.text());
			
			// search for siblings ui-state-active
			menuItem.parent().parent().find('tr td.ui-state-active[name]').each(function(entryIndex, entry) {
				var v = $(entry).removeClass('ui-state-active').attr('name');
				var x = -1;
				var y = menuItems.filter('#' + v);
				for (var i = openedMenuItems.length - 1; i >= 0; i--) {
					if (openedMenuItems[i] == y.get(0)) {
						x = i;
						break;
					}
				}
				if (x > -1) {
					for (var i = openedMenuItems.length - 1; i >= x; i--) {
						var o = openedMenuItems.pop();
						$(o).hide().find('tr td.ui-state-active').removeClass('ui-state-active');
					}
				}
			});
			
			menuItem.addClass('ui-state-active');
			
			var menuItemId = menuItem.attr('name');
			if (menuItemId == undefined || menuItemId.length == 0) {
				killMenus();
				var href = menuItem.find('a');
				if (href.length > 0)
					href = href.get(0);
				else
					href = undefined;
				action(href, navigation);
				return false;
			}
			
			var mi = menuItems.filter('#' + menuItemId);
			
			var index = -1;
			for (var i = openedMenuItems.length - 1; i >= 0; i--) {
				if (openedMenuItems[i] == mi.get(0)) {
					index = i;
					break;
				}
			};
			
			if (index > -1) {
				for (var i = openedMenuItems.length - 1; i >= index; i--) {
					var o = openedMenuItems.pop();
					$(o).hide().find('tr td.ui-state-active').removeClass('ui-state-active');
				}
			}
			
			mi.show().position({
				of: menuItem,
				my: 'left top',
				at: 'right top'
			}).position({
				of: menuItem,
				my: 'left top',
				at: 'right top'
			});
			
			openedMenuItems.push(mi.get(0));
			
			return false;
		});
	
	return this;
};