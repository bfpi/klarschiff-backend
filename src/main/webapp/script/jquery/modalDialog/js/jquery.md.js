/*
 * jQuery MD plugin 1.7
 * Released: July 3, 2011
 * 
 * Copyright (c) 2011 Steve Koehler
 * Email: steve@tiny-threads.com
 * 
 * Developed: Steve Koehler
 * http://www.tiny-threads.com/blog/2011/06/07/jquery-modal-dialog-plugin/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 *  Licensed under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 *
 * @license http://www.opensource.org/licenses/mit-license.php
 * @project jquery.md
 *
 * If you have enjoyed MD, then we encourage you to donate at http://www.tiny-threads.com
 */
 
(function( $ ){
	$.md = function(msg,options){
		if(navigator.appName=='Microsoft Internet Explorer')
			$.md.ie=true;
		else
			$.md.ie=false;
		
        $.md.defaults = {
            timeout: 0
            , showClose: true
            , width: 525
            , buttons: {}
            , type: 'err'
            , title: ''
			, cssDir: 'css'
			, position: 'center'
			, modal:true
			, modalBG: '#ffffff'
			, fullscreen: false
			, showMinimize: true
			, showFullscreen: true
        };
        $.md.DialogTypes = new Array("error", "warning", "success", "prompt");
        $.md.DialogTitles = {
            "err": "Error"
            , "warning": "Warning!"
            , "success": "Success"
            , "prompt": "Please Choose"
        };
		
		//set minimized flag to off
		$.md.minimized=false;
         
        // display the whole thing
        $.md.showDialog(msg,options);
   
	};
	
	// Creates and shows the modal dialog
	$.md.showDialog= function  (msg, options) {
			
		// Merge default title (per type), default settings, and user defined settings
		var settings = $.extend( $.md.defaults, options);
		settings.type=settings.type.toLowerCase();
		 
		//if an invalid dialog type, default to error
		if(!$.inArray(settings.type, $.md.DialogTypes) || settings.type=='error') {
			settings.type='err';
		}
		//if no title is entered, default to selected dialog type
		if(settings.title=='' || typeof settings.title == 'undefined'){
			settings.title=$.md.DialogTitles[settings.type];
		}
		// If there's no timeout, make sure the close button is show (or the dialog can't close)
		settings.timeout = (typeof(settings.timeout) == "undefined") ? 0 : settings.timeout;
		settings.showClose = ((typeof(settings.showClose) == "undefined") | !settings.timeout) ? true : !!settings.showClose;
		
		// Check if the dialog elements exist and create them if not
		if (!document.getElementById('md')) {
			var d = new Date();
			
			//load stylesheet into the page to avoid incorrect displays
			$.ajax({
				 async: false,
				 type: 'GET',
				 url: settings.cssDir+'/jquery.md.css?'+d.getTime(),
				 success: function(data) {
					$('body').prepend(
						"<style type='text/css'>"+data+"</style>" +
						"<div id='md-mask'>&nbsp;</div>"+
						"<div id='md'>"+
						"<div class='errheader' id='md-header'>" +
							"<div id='md-title'></div>" +
							"<div id='md-close' class='md-icon'></div>" +
							"<div id='md-minimize' class='md-icon'></div>" +
							"<div id='md-restore' class='md-icon'></div>" +
							"<div id='md-fullscreen' class='md-icon'></div>" +
						"</div>" +
						"<div class='err' id='md-content'>" +
							"<div id='md-content-inner' />" +
							"<div id='md-button-container'>" +
								"<input class='errbutton' type='button' class='md-button' onclick='$.md.hide();' value='Close'>" +
							"</div>" +
						"</div>"+
						"</div>"
					);
				 }
			});
			
			
			
			// Set the click events for the icons and mask            
			$("#md-close").click($.md.hide);
			$('#md-minimize').click($.md.minimize);
			$('#md-restore').click($.md.restore);
			$('#md-fullscreen').click($.md.toggleFullscreen);
			$('#md-mask').click($.md.hide);
			
			//set local vars for common elements to increase speed
			$.md.mask=$('#md-mask');
			$.md.dl=$('#md');
			$.md.content=$('#md-content');
			
			$('#md').hide();
			$('#md-mask').hide();
		}
		
		//reset the minimized flag and settings
		$.md.minimized=false;
		$.md.content.show();
		$('#md-restore').hide();
		
		//switch minimize buttons on and off
		if(!settings.showMinimize)
			$('#md-minimize').hide();
		else
			$('#md-minimize').show();
		
		//switch fullscreen button on and off
		if(!settings.showFullscreen)
			$('#md-fullscreen').hide();
		else
			$('#md-fullscreen').show();
		
		//switch close and buttons on and off
		if (!settings.showClose) {
			$('#md-close').hide();
		} else {
			$('#md-close').show();
		}

		//set title and content
		$('#md-title').html(settings.title);
		$('#md-content-inner').html(msg);
		
		// erase the old buttons            
		$('#md-button-container').html('');
		var btn = settings.buttons;
		 
		
		// add the new buttons
		// Die Variable wurde von x in btnKey umbenannt, da es beim IE mit dem Bezeichner (bisher noch aus unverständlichen Gründen)
		// zu Problemen kommt
		for(btnKey in btn){
			$('#md-button-container').append("<input type='button' class='md-button' id='md-button-"+btnKey+"' value='"+btnKey+"'>");
			$('#md-button-'+btnKey).click(btn[btnKey]);
		}
//		for(x in btn){
//			$('#md-button-container').append("<input type='button' class='md-button' id='md-button-"+x+"' value='"+x+"'>");
//			$('#md-button-'+x).click(btn[x]);
//		}
		
		//set width
		$.md.dl.css('width', settings.width);
		 
		
		 
		// remove the dialog type classes and replace them with the new type
		for(i=0;i<$.md.DialogTypes.length;i++){
			$('#md-header').removeClass($.md.DialogTypes[i]+ 'header');
			$.md.content.removeClass($.md.DialogTypes[i]);
		}
		$('#md-header').addClass(settings.type + "header");
		$.md.content.addClass(settings.type);
		$('.md-button').addClass(settings.type + "button");
		
		//set mask color
		$.md.mask.css('background-color',settings.modalBG);
		
		// set timeout
		if (settings.timeout) {
			window.setTimeout("$.md.hide();", (settings.timeout * 1000));
		}
		 
		// get the mask height. Use document height unless it's smaller than the window
		if($(document).height() > $(window).height())
			$.md.mask.height($(document).height());
		else
			$.md.mask.height($(window).height());
			 
		// fade the dialog in
		$.md.dl.fadeIn("normal");
		 
		$.md.setPosition(settings.position);
		
		//only fade in mask if set to true
		if(settings.modal)
			$.md.mask.fadeTo("slow",0.8);
		
		//adjust dialog box size on window resize
		$(window).resize($.md.adjustDimensions);
		
		//set to fullscreen if set to true
		if(settings.fullscreen)
			$.md.fullscreen();
		 
		return true;
	};
	
	//set the position of the dialog box
	$.md.setPosition=function(position){
		var pos = position.split(' ',2);
		var top;
		var left;
		var dl = $.md.dl;
		
		//get the vertical
		if(pos[0] =='top')
			top = 0;
		else if(pos[0] =='bottom')
			top = Math.abs($(window).height() - dl.height());
		else if(String(parseInt(pos[0])) == pos[0])
			top = pos[0];
		else
			top = Math.abs($(window).height() - dl.height()) / 2;
			
		//get the horizontal
		if(pos[1] =='left')
			left = 0;
		else if(pos[1] =='right')
			left = ($(window).width() - dl.width());
		else if(String(parseInt(pos[1])) == pos[1])
			left = pos[1];
		else
			left = ($(window).width() - dl.width()) / 2;
		
		//set the position
		dl.css('left', left);
		dl.css('top', top);
		
		//dl.css('top', (top >= 25) ? top : 25);
	};
	
	//toggle the dialog box between minimized and restored
	$.md.toggleMinimize=function(){
		//if the dialog box is minimized, restore it, or vice versa
		if($.md.minimized){
			$.md.restore();
		}
		else{
			$.md.minimize();
		}
	}
	
    //hide dialog box content
	$.md.minimize=function(){
		$('#md-restore').show();
		$('#md-minimize').hide();
		$.md.content.css('min-height','0'); //hide the min-height for sliding to work right
		$.md.content.slideUp();
		$.md.minimized=true;
	}
	
	//restore the dialog box back to original dimensions
	$.md.restore=function(){
		$('#md-restore').hide();
		$('#md-minimize').show();
		$.md.content.slideDown();
		$.md.minimized=false;
	}
	
	//toggle between normal and fullscreen
	$.md.toggleFullscreen=function(){
		if($.md.isFullscreen)
			$.md.removeFullscreen();
		else
			$.md.fullscreen();
	}
	
	// make fullscreen
	$.md.fullscreen=function(){
		$.md.isFullscreen=true;
		$.md.adjustDimensions();
	}
	
	// restore to previous size
	$.md.removeFullscreen=function(){
		$.md.isFullscreen=false;
		$.md.adjustDimensions();
	}
	
	// resizing of the dialog box for fullscreen and normal sizes
	$.md.adjustDimensions=function(){
		if($.md.isFullscreen){
			$.md.height=$('#md-content').height();
			$.md.width=$.md.dl.width();
			$.md.top=$.md.dl.css('top');
			$.md.left=$.md.dl.css('left');
			$.md.dl.width($(window).width());
			$.md.dl.css('top','0');
			$.md.dl.css('left','0');
			$.md.content.height($(window).height() - $('#md-header').height());
		}
		else{
			$.md.content.height($.md.height);
			$.md.dl.width($.md.width);
			$.md.dl.css('top',$.md.top);
			$.md.dl.css('left',$.md.left);
		}
	}
		
    //hide the dialog window
    $.md.hide=function () {
            $.md.dl.fadeOut("normal", function () { $(this).hide(0); });
            $.md.mask.fadeOut("normal", function () { $(this).hide(0); });
            return true;
    };
})( jQuery );