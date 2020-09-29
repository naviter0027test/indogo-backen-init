/**
 * version: 1.0
 * @requires jQuery v1.4.2 or later
 * 
 * Copyright 2010, Henry Hung
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

var GLOBAL_SID = '';
var GLOBAL_LANGUAGE = 'en';
var $ERROR_DIALOG;
var $ERROR_DIALOG_TAG;
var $ERROR_DIALOG_BTN;
var $ERROR_DIALOG_MSG;
var $ERROR_DIALOG_STACK;
var $WARNING_DIALOG;
var $WARNING_DIALOG_MSG;
var $USER_INFO;
var $STATUS_IDLE_TIME = undefined;
var $SERVER_VERSION;
var $LOGOUT_BUTTON = undefined;
var IDLE_TIMER_ID = undefined;
var IDLE_TIMER_COUNT = 0;
var MAX_IDLE_TIME = 10;
var ENVIRONMENT_NAME = undefined;
var SERVER_NAME = undefined;
var NAVIGATION_TITLE = undefined;
var EXPIRE_DATE = undefined;
var ROLE_NAME = undefined;
var ALIAS_ID = undefined;
var MODULE_ID = undefined;
var MODULE_NAME = undefined;
var char_31 = String.fromCharCode(31);
var char_30 = String.fromCharCode(30);

var funcGoToLoginPage = function() {
	window.location = "login.html";
};

var funcTimeout = function() {
	alert('call server timeout');
	funcGoToLoginPage();
};

var funcIdleTimerCheck = function() {
	IDLE_TIMER_COUNT = IDLE_TIMER_COUNT + 1;
	if (IDLE_TIMER_COUNT > MAX_IDLE_TIME && MAX_IDLE_TIME > 0) {
		if ($LOGOUT_BUTTON != undefined) {
			$LOGOUT_BUTTON.click();
		}
	}
	else {
		if ($STATUS_IDLE_TIME != undefined)
			$STATUS_IDLE_TIME.empty().append(IDLE_TIMER_COUNT);
	}
};

var page = {
	showErrorDialog: function(tag, msg, stack) {
		try {
			$ERROR_DIALOG_TAG.empty().append(tag);
			$ERROR_DIALOG_MSG.empty().append(msg.replace(/\n/g, '<br/>')).show();
			$ERROR_DIALOG_STACK.empty().append(stack.replace(/\n/g, '<br/>')).hide();
			$ERROR_DIALOG_BTN.val('detail');
			
			if (msg == stack)
				$ERROR_DIALOG_BTN.hide();
			else
				$ERROR_DIALOG_BTN.show();
			
			$ERROR_DIALOG.show();
			
			//$('body,html').animate({ scrollTop: 0 }, 'fast');
		}
		catch (e) {
			alert(e);
		}
	},
	showWarningDialog: function(msg) {
		$WARNING_DIALOG_MSG.empty().append(msg.replace(/\n/g, '<br/>'));
		$WARNING_DIALOG.show();
		$('body,html').animate({ scrollTop: 0 }, 'fast');
	},
	getTag: function(tagStart, tagEnd, data) {
		var iStart = data.indexOf(tagStart);
		if (iStart < 0)
			return undefined;
		var iEnd = data.indexOf(tagEnd);
		if (iEnd < 0)
			return undefined;
		var result = data.substring(iStart + tagStart.length, iEnd); 
		return result;
	}
};

var sendRequest = {
	successHandler: function(data, options) {
		try {
			var code = data.ajaxerrcode;
			if (code != 0) {
				var errmsg = data.ajaxerrmsg;
				var errstack = data.ajaxerrstack;
				
				if (options.data != undefined) {
					var show_error_dialog = true;
					if (options.data.suppress_error_dialog != undefined) {
						if (options.data.suppress_error_dialog == 1) {
							show_error_dialog = false;
						}
					}
					
					if (show_error_dialog) {
						if (options.data.F != undefined) {
							page.showErrorDialog(options.data.F, errmsg, errstack);
						} else {
							page.showErrorDialog('undefined', errmsg, errstack);
						}
					}
				}
				else {
					page.showErrorDialog('', errmsg, errstack);
				}
				if (code == 4) {
					funcTimeout();
				}
				if (options.error != undefined)
					if ($.isFunction(options.error))
						options.error(undefined, undefined, undefined, code, errmsg, errstack);
			}
			else {
				var $info = $(data.ajaxinfo);
				if ($info != undefined) {
					$USER_INFO.empty().append($info.filter('#U').text());
					$SERVER_VERSION.empty().append($info.filter('#Version').text());
					SERVER_NAME.empty().append($info.filter('#ServerName').text());
					ENVIRONMENT_NAME.empty().append($info.filter('#EnvironmentName').text());
					ROLE_NAME.empty().append($info.filter('#RoleName').text());
					MAX_IDLE_TIME = Number($info.filter('#MaxIdleTime').text());
					ALIAS_ID.empty().append($info.filter('#AliasId').text());
					
					var warningMessage = $info.filter('#WarningMessage').text();
					if (warningMessage.length > 0) {
						page.showWarningDialog(warningMessage);
					}
					
					if ($info.filter('#ModuleId').text().length == 0) {
						MODULE_ID.parent().hide();
						MODULE_NAME.parent().hide();
					}
					else {
						MODULE_ID.empty().append($info.filter('#ModuleId').text());
						MODULE_NAME.empty().append($info.filter('#ModuleName').text());
					}
				}
				if (options.success != undefined) {
					if ($.isFunction(options.success)) {
						options.success(data.ajaxdata);
					}
				}
			}
		}
		catch (e) {
			if (options.data != undefined) {
				if (options.data.F != undefined) {
					page.showErrorDialog(options.data.F, e + '', e + '');
				}
				else {
					page.showErrorDialog('undefined', e + '', e + '');
				}
			}
			else {
				page.showErrorDialog('', e + '', e + '');
			}
		}
	},
	errorHandler: function(XMLHttpRequest, textStatus, errorThrown, options) {
		page.showErrorDialog(options.data.F, XMLHttpRequest.responseText, XMLHttpRequest.responseText);
		if (options.error != undefined && $.isFunction(options.error))
			options.error(XMLHttpRequest, textStatus, errorThrown);
	},
	beforeSendHandler: function(XMLHttpRequest, options) {
		//if (options.showLoadingDialog != undefined)
			//if (options.showLoadingDialog == true)
				//$('#ueLoadingDialog').dialog('open');
		if (options.beforeSend != undefined)
			if ($.isFunction(options.beforeSend))
				options.beforeSend(XMLHttpRequest);
	},
	completeHandler: function(XMLHttpRequest, textStatus, options) {
		//if (options.showLoadingDialog != undefined)
			//if (options.showLoadingDialog == true)
				//$('#ueLoadingDialog').dialog('close');
		if (options.complete != undefined)
			if ($.isFunction(options.complete))
				options.complete(XMLHttpRequest, textStatus);
	},
	basic: function(options) {
		try {
			if (options.data.F == undefined) {
				alert('You must define "options.data.F" before using sendRequest.basic');
				return;
			}
			options.data.SID = GLOBAL_SID;
			options.data.language = GLOBAL_LANGUAGE;
			$ERROR_DIALOG.hide();
			$WARNING_DIALOG.hide();
			$.ajax({
				url: 'servlet/UrlEncodedFormData',
				data: options.data,
				type: 'POST',
				dataType: 'json',
				success: function(data){
					sendRequest.successHandler(data, options);
				},
				error: function(XMLHttpRequest, textStatus, errorThrown){
					sendRequest.errorHandler(XMLHttpRequest, textStatus, errorThrown, options);
				},
				beforeSend: function(XMLHttpRequest){
					sendRequest.beforeSendHandler(XMLHttpRequest, options);
				},
				complete: function(XMLHttpRequest, textStatus){
					sendRequest.completeHandler(XMLHttpRequest, textStatus, options);
				}
			});
		}
		catch (e) {
			if (options.data != undefined) {
				if (options.data.F != undefined) {
					page.showErrorDialog(options.data.F, e + '', e + '');
				}
				else {
					page.showErrorDialog('undefined', e + '', e + '');
				}
			}
			else {
				page.showErrorDialog('', e + '', e + '');
			}
		}
	},
	logout: function(options) {
		options.data = {'F':'Logout'};
		sendRequest.basic(options);
	},
	menu: function(options) {
		options.data = {'F':'Menu'};
		sendRequest.basic(options);
	},
	relay: function(options) {
		if (options.data == undefined) {
			alert('You must define "options.data" before using sendRequest.relay');
			return;
		}
		if (options.data.RelayId == undefined) {
			alert('You must define "options.data.RelayId" before using sendRequest.relay');
			return;
		}
		options.data.F = 'Relay';
		sendRequest.basic(options);
	},
	table: function(options) {
		if (options.data == undefined) {
			alert('You must define "options.data" before using sendRequest.table');
			return;
		}
		if (options.data.Action == undefined) {
			alert('You must define "options.data.Action" before using sendRequest.table');
			return;
		}
		if (options.data.ClassName == undefined) {
			alert('You must define "options.data.ClassName" before using sendRequest.table');
			return;
		}
		options.data.F = 'TablePage';
		sendRequest.basic(options);
	},
	getParam: function(options) {
		if (options.data == undefined) {
			alert('You must define "options.data" before using sendRequest.getParam');
			return;
		}
		if (options.data.ParamName == undefined) {
			alert('You must define "options.data.ParamName" before using sendRequest.getParam');
			return;
		}
		options.data.F = 'Param';
		options.data.Action = 'get';
		sendRequest.basic(options);
	},
	removeParam: function(options) {
		if (options.data == undefined) {
			alert('You must define "options.data" before using sendRequest.getParam');
			return;
		}
		if (options.data.ParamName == undefined) {
			alert('You must define "options.data.ParamName" before using sendRequest.getParam');
			return;
		}
		options.data.F = 'Param';
		options.data.Action = 'del';
		sendRequest.basic(options);
	},
	downloadFile: function(filename, keepFile) {
		var options = {
			'Filename': filename,
			'KeepFile': keepFile,
			'SID': GLOBAL_SID,
			'language': GLOBAL_LANGUAGE
		};
		window.location = 'servlet/DownloadServlet?' + $.param(options);
	},
	downloadTool: function(filename) {
		var options = {
			'Filename': filename,
			'Tool': 'Y',
			'SID': GLOBAL_SID,
			'language': GLOBAL_LANGUAGE
		};
		window.location = 'servlet/DownloadServlet?' + $.param(options);
	},
	downloadPdf: function(filename, keepFile) {
		var options = {
			'pdf': filename,
			'KeepFile': keepFile,
			'SID': GLOBAL_SID,
			'language': GLOBAL_LANGUAGE
		};
		window.open('servlet/DownloadServlet?' + $.param(options));
	}
};

jQuery.fn.formSubmit = function(options) {
	try {
		if (options.data.F == undefined) {
			alert ('You must define "options.data.F" before using formSubmit');
			return;
		}
		options.data.SID = GLOBAL_SID;
		options.data.language = GLOBAL_LANGUAGE;
		
		var form = $(this);
		form.find('input[type=hidden].auto-generated-hidden').remove();
		$.each(options.data, function(i, n){
		    var h = $('<input type="hidden" class="auto-generated-hidden" name="' + i + '"/>');
			h.val(n);
			form.append(h);
		});
		
		$ERROR_DIALOG.hide();
		form.ajaxSubmit({
			url: 'servlet/MultipartFormData',
			type: 'POST',
			dataType: 'json',
			success: function(data) {
				sendRequest.successHandler(data, options);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				sendRequest.errorHandler(XMLHttpRequest, textStatus, errorThrown, options);
			},
			beforeSend: function(XMLHttpRequest){
				sendRequest.beforeSendHandler(XMLHttpRequest, options);
			},
			complete: function(XMLHttpRequest, textStatus){
				sendRequest.completeHandler(XMLHttpRequest, textStatus, options);
			}
		});
	}
	catch (e) {
		if (options.data != undefined) {
			if (options.data.F != undefined) {
				page.showErrorDialog(options.data.F, e + '', e + '');
			}
			else {
				page.showErrorDialog('undefined', e + '', e + '');
			}
		}
		else {
			page.showErrorDialog('', e + '', e + '');
		}
	}
	return this;
};

$(function() {
	$USER_INFO = $('#ueUserInfo');
	$STATUS_IDLE_TIME = $('#ueIdleTime');
	$SERVER_VERSION = $('#ueServerVersion');
	SERVER_NAME = $('#ueServerName');
	ENVIRONMENT_NAME = $('#ueEnvironmentName');
	ROLE_NAME = $('#ueRoleName');
	ALIAS_ID = $('#ueAliasId');
	MODULE_ID = $('#ueModuleId');
	MODULE_NAME = $('#ueModuleName');
	NAVIGATION_TITLE = $('#winbond-menu-nav-title');
	$ERROR_DIALOG = $('#ueErrorDialog').hide();
	$ERROR_DIALOG_MSG = $('#ueErrorMsg');
	$ERROR_DIALOG_STACK = $('#ueErrorStack');
	$ERROR_DIALOG_TAG = $('#ueErrorTag');
	$ERROR_DIALOG_BTN = $('#btnErrorDialog').click(function(){
		if ($(this).val() == 'detail') {
			$ERROR_DIALOG_MSG.hide();
			$ERROR_DIALOG_STACK.show();
			$ERROR_DIALOG_BTN.val('short');
		} else {
			$ERROR_DIALOG_MSG.show();
			$ERROR_DIALOG_STACK.hide();
			$ERROR_DIALOG_BTN.val('detail');
		}
	});
	$('#btnErrorDialogClose').click(function() {
		$ERROR_DIALOG.hide();
	});
	
	$WARNING_DIALOG = $('#ueWarningDialog').hide();
	$WARNING_DIALOG_MSG = $('#ueWarningMsg');
	$('#btnWarningDialogClose').click(function() {
		$WARNING_DIALOG.hide();
	});
	
	GLOBAL_SID = Cookies.get('SID');
	if (GLOBAL_SID == undefined || GLOBAL_SID.length == 0) {
		page.showErrorDialog('SID Check', 'There is something wrong with the HTML page, please contact administrator <strong>(SID Missing)</strong>', 'There is something wrong with the HTML page, please contact administrator <strong>(SID Missing)</strong>');
		return;
	}
	
	var l = Cookies.get('language');
	if (l != undefined) {
		GLOBAL_LANGUAGE = l;
	}
	
	$(document).ajaxStart(function(){
		var a = $('#winbond-menu-loading');
		if (a.data('StartCount') == undefined)
			a.data('StartCount', 0);
		var startCount = a.data('StartCount');
		startCount = startCount + 1;
		a.data('StartCount', startCount);
		if (startCount == 1) {
			a.data('ActiveElement', document.activeElement);
			$('#ueLoadingDialog').dialog('open');
		}
		a.show();
		IDLE_TIMER_COUNT = 0;
		clearInterval(IDLE_TIMER_ID);
	}).ajaxStop(function(){
		var a = $('#winbond-menu-loading');
		var startCount = a.data('StartCount');
		startCount = startCount - 1;
		a.data('StartCount', startCount);
		if (startCount <= 0) {
			a.hide();
			$('#ueLoadingDialog').dialog('close');
			var activeElement = $(a.data('ActiveElement'));
			if (!activeElement.hasClass('ui-state-disabled') && !activeElement.is(':button'))
				activeElement.focus();
		}
		IDLE_TIMER_COUNT = 0;
		IDLE_TIMER_ID = setInterval(funcIdleTimerCheck, 1000);
	});

	$("#ueLoadingDialogProgressBar").progressbar({
		value: 100
	});

	$('#ueLoadingDialog').dialog({
		autoOpen: false,
		modal: true,
		closeOnEscape: false,
		draggable: false,
		resizable: false,
		height: 80
	}).dialog('widget').find('.ui-dialog-titlebar-close').hide();
	
	$(this).mousemove(function() {
		IDLE_TIMER_COUNT = 0;
	});
	
	$LOGOUT_BUTTON = $('#ueLogout').parent().parent().parent().click(function(){
		sendRequest.logout({
			complete: function() {
				funcGoToLoginPage();
			}
		});
		return false;
	});
	
	var loadPage = function(data, navigation) {
		var jqData = $(data);
		
		var menuNav = $('#winbond-menu-nav');
		menuNav.empty();
		if ($.isArray(navigation)) {
			var nav = '';
			for (var i = 0; i < navigation.length; i++)
				nav += navigation[i] + ' &#187; ';
			menuNav.append(nav);
		}
		
		if (jqData.attr('href').indexOf('#GROUP') > -1)
			return;
		
		$ERROR_DIALOG.hide();
		$('#winbond-page').empty();
		
		$('body > div.ui-dialog').each(function(i, e) {
			var entry = $(e);
			if (entry.attr('aria-describedby') != 'ueLoadingDialog' && entry.attr('aria-describedby') != 'ueSwitchLanguageDialog') {
				entry.find('div.ui-dialog-content').remove();
			}
		});
		
		document.title = jqData.text();
		$.ajax({
			url: jqData.attr('href'),
			data: {},
			type: 'GET',
			success: function(htmlPage) {
				var code = $('#winbond-page').append(htmlPage).find('#ajax-message-error-code').val();
				$('form').submit(function(e) {
					e.preventDefault();
				});
				if (code != undefined && Number(code) == 4)
					funcTimeout();
				
				$('#winbond-page table.query').each(function(i, e) {
					var table = $(e);
					table.find('input').keydown(function(e) {
						if (e.which == 13) {
							table.find('button.ok').click();
						}
					});
					table.find('select').change(function() {
						table.find('button.ok').click();
					});
				});
			}
		});
	};
	
	var qsMenuRowId = $.query.get('menu_row_id');
	if (qsMenuRowId != undefined && qsMenuRowId.toString().length > 0) {
		$('#ueMenuDialog, #footer, #ueMenuDialogMargin').hide();
		sendRequest.basic({
			data: {
				'F': 'Menu',
				'MenuRowId': qsMenuRowId.toString()
			},
			success: loadPage
		});
	}
	else {
		// create menu
		sendRequest.menu({
			success: function(data) {
				$('#winbond-menu').createToolbar(data, loadPage);
			}
		});
	}
	
	$('#ueSwitchLanguageDialog').dialog({
		modal: true,
		width: 200,
		autoOpen: false,
		buttons: {
			'Ok': function() {
				var language = $('#tableSwitchLanguageDialog input[name=language]:checked').val();
				Cookies.set('language', language);
				location.reload();
			},
			'Cancel': function() {
				$('#ueSwitchLanguageDialog').dialog('close');
			}
		}
	});
	
	$('#btnChangeLanguage').button({icon: 'ui-icon-comment', showLabel: false}).click(function() {
		var language = Cookies.get('language');
		if (language == undefined)
			language = 'en';
		$('#tableSwitchLanguageDialog input[name=language][value=' + language + ']').prop('checked', true);
		$('#ueSwitchLanguageDialog').dialog('open');
	});
});

// createDateTimePicker
jQuery.fn.createDateTimePicker = function(){
	return $(this).attr('readonly', true).css('width', '160px').datepicker({
		dateFormat: 'yy/mm/dd',
		showOn: 'both',
		buttonImage: 'ui/jquery-ui/calendar.gif',
		buttonImageOnly: true,
		showButtonPanel: true,
		changeMonth: true,
		changeYear: true,
		yearRange: 'c-50:c+50'
	});
}; // createDateTimePicker

jQuery.fn.createDatePickerWritable = function(){
	return $(this).css('width', '160px').datepicker({
		dateFormat: 'yy/mm/dd',
		showOn: 'both',
		buttonImage: 'ui/jquery-ui/calendar.gif',
		buttonImageOnly: true,
		showButtonPanel: true,
		changeMonth: true,
		changeYear: true,
		yearRange: 'c-50:c+50'
	});
};

// createDatePicker
jQuery.fn.createDatePicker = function(){
	return $(this).attr('readonly', true).css('width', '160px').datepicker({
		dateFormat: 'yy/mm/dd',
		showOn: 'both',
		buttonImage: 'ui/jquery-ui/calendar.gif',
		buttonImageOnly: true,
		showButtonPanel: true,
		changeMonth: true,
		changeYear: true,
		yearRange: 'c-50:c+50'
	});
}; // createDatePicker

jQuery.fn.navbar = function(o) {
	if ($.isArray(o)) {
		var views = o;
		var max = o.length;
		$(this).empty().addClass('view').addClass('ui-corner-all').attr('max', max).attr('current', '-1');
		for (var i = 0; i < max - 1; i++) {
			$(this).append('<span class="view view-' + i + ' after">' + views[i] + '</span><img src="ui/lionsys/img/east.gif"/>');
		}
		$(this).append('<span class="view view-' + (max-1) + ' after">' + views[(max-1)] + '</span>');
		return $(this);
	}
	else if (o == 'next') {
		var current = Number($(this).attr('current'));
		var max = Number($(this).attr('max'));
		if (current < max - 1) {
			if (current >= 0)
				$(this).find('span.view-' + current).removeClass('current').addClass('before');
			current++;
			$(this).attr('current', current).find('span.view-' + current).removeClass('after').addClass('current');
		}
	}
};

jQuery.fn.createNumericTextInput = function() {
	return $(this).keydown(function(e) {
		if (!e.shiftKey && ((e.keyCode >= 48 && e.keyCode <= 57) || (e.keyCode >= 96 && e.keyCode <= 105))) {
			
		}
		else if (e.keyCode == 8 ||
				e.keyCode == 35 ||
				e.keyCode == 36 ||
				e.keyCode == 46 ||
				e.keyCode == 9 ||
				e.keyCode == 27 ||
				e.keyCode == 189 ||
				e.keyCode == 109 ||
				(e.keyCode >= 37 && e.keyCode <= 40) ||
				(e.ctrlKey && e.keyCode == 86) ||
				(e.ctrlKey && e.keyCode == 67)) {}
		else
			return false;
	});
};

jQuery.fn.incrementOnEnter = function() {
	return $(this).keydown(function(e) {
		if (e.which == 13) {
			var value = $(this).autoNumeric('get');
			if (value == undefined || value.length == 0) {
				value = '0';
			}
			value = parseInt(value, 10) + 1;
			$(this).autoNumeric('set', value);
		}
	});
};

jQuery.join = function(array, separator) {
	if (array.length == 0)
		return '';
	else {
		if (separator == undefined)
			return array.join(char_31);
		else
			return array.join(separator);
	}
};

function nl2br (str, is_xhtml) {   
    var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';    
    return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1'+ breakTag +'$2');
}

function toCurrency(value) {
	return value.toFixed(0).replace(/(\d)(?=(\d{3})+$)/g, "$1,");
}

function leftPad(value, maxLength) {
	return String('0'.repeat(maxLength) + value).slice(-maxLength);
}

jQuery.widget( "custom.combobox", {
	_create: function() {
	  this.wrapper = $( "<span>" )
	    .addClass( "custom-combobox" )
	    .insertAfter( this.element );
	
	  this.element.hide();
	  this._createAutocomplete();
	  this._createShowAllButton();
	},

    _createAutocomplete: function() {
      var selected = this.element.children( ":selected" ),
        value = selected.val() ? selected.text() : "";

      this.input = $( "<input>" )
        .appendTo( this.wrapper )
        .val( value )
        .attr( "title", "" )
        .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left" )
        .autocomplete({
          delay: 0,
          minLength: 0,
          source: $.proxy( this, "_source" )
        })
        .tooltip({
          classes: {
            "ui-tooltip": "ui-state-highlight"
          }
        });

      this._on( this.input, {
        autocompleteselect: function( event, ui ) {
          ui.item.option.selected = true;
          this._trigger( "select", event, {
            item: ui.item.option
          });
        },

        autocompletechange: "_removeIfInvalid"
      });
    },

    _createShowAllButton: function() {
      var input = this.input,
        wasOpen = false;

      $( "<a>" )
        .attr( "tabIndex", -1 )
        .attr( "title", "Show All Items" )
        .tooltip()
        .appendTo( this.wrapper )
        .button({
          icons: {
            primary: "ui-icon-triangle-1-s"
          },
          text: false
        })
        .removeClass( "ui-corner-all" )
        .addClass( "custom-combobox-toggle ui-corner-right" )
        .on( "mousedown", function() {
          wasOpen = input.autocomplete( "widget" ).is( ":visible" );
        })
        .on( "click", function() {
          input.trigger( "focus" );

          // Close if already visible
          if ( wasOpen ) {
            return;
          }

          // Pass empty string as value to search for, displaying all results
          input.autocomplete( "search", "" );
        });
    },

    _source: function( request, response ) {
      var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
      response( this.element.children( "option" ).map(function() {
        var text = $( this ).text();
        if ( this.value && ( !request.term || matcher.test(text) ) )
          return {
            label: text,
            value: text,
            option: this
          };
      }) );
    },

    _removeIfInvalid: function( event, ui ) {

      // Selected an item, nothing to do
      if ( ui.item ) {
        return;
      }

      // Search for a match (case-insensitive)
      var value = this.input.val(),
        valueLowerCase = value.toLowerCase(),
        valid = false;
      this.element.children( "option" ).each(function() {
        if ( $( this ).text().toLowerCase() === valueLowerCase ) {
          this.selected = valid = true;
          return false;
        }
      });

      // Found a match, nothing to do
      if ( valid ) {
        return;
      }

      // Remove invalid value
      this.input
        .val( "" )
        .attr( "title", value + " didn't match any item" )
        .tooltip( "open" );
      this.element.val( "" );
      this._delay(function() {
        this.input.tooltip( "close" ).attr( "title", "" );
      }, 2500 );
      this.input.autocomplete( "instance" ).term = "";
    },

    _destroy: function() {
      this.wrapper.remove();
      this.element.show();
    }
  });