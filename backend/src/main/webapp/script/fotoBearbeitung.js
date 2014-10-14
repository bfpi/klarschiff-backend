// @author Stefan Audersch (Fraunhofer IGD)
// @author Marcus Köller (Fraunhofer IGD)
// @author Martin Gielow (Fraunhofer IGD)

var x1,y1;
var xMove,yMove;
var currRect;
var i = 0;
var isMouseDown;

var undoStack = [];
var currStackItem;

var active = false;

function UndoStackItem(top, left, height, width, obj) {
	this.rect = new Rectangle(top, left, height, width);
	this.domObj = obj;
}
function Rectangle(top, left, height, width) {
	this.top = top;
	this.left = left;
	this.height = height;
	this.width = width;
}

//enable Zensoring:
$(function () {
	$("#editEnableAnchor").click(function() {
		$("#editEnableAnchor").hide();
		$("#editDisabledAnchor").show();
		$("#editDisabledAnchor ~ button").show(300);
		active = true;
	});
});

$(function () {

	
	//Catch mousedown events on the picture or drawn rectangles
	//$("img#picture").parent().append("<div id='recStack2' style='display:absolut;'><!--&nbsp;--></div>");
	
	$("img#picture, div#recStack")
		.mousedown(function(e) {
			if(active){
				isMouseDown = true;
				e.preventDefault();
				i++;
				
				if (undoStack.length > 0) {
					var targetLength;
					
					if (currStackItem != undoStack[undoStack.length-1]) {
						if (currStackItem.length == 0) {
							targetLength = 0;
						} else {
							targetLength = undoStack.indexOf(currStackItem) + 1;
						}
						while (undoStack.length > targetLength) {
							undoStack.pop();
						}
					}
				}
				
				// adding new censoring box
				var box = $(
					'<div style="background: black; border:1px #DDDDDD solid; position:absolute; margin:0"></div>'
				).hide();
				
				$("div#recStack").append(box);
				
				x1 = e.pageX;
				y1 = e.pageY;
				
				// save reference to the new box
				currStackItem = new UndoStackItem(x1, x1, 1, 1, box);
				undoStack.push(currStackItem);
			}
		})
	
	// Adjust size of the censoring box while dragging the mouse
	
	$("img#picture, div#recStack")
		.mousemove(function(e) {
			if(active){
				if (isMouseDown && currStackItem != null && currStackItem.length != 0) {
					var t, l, w, h;
				
					xMove = e.pageX;
					yMove = e.pageY;
					
					w = Math.abs(xMove - x1);
					h = Math.abs(yMove - y1);
					if (xMove >= x1) {
						l = x1;
					} else {
						l = xMove;
					}
					if (yMove >= y1) {
						t = y1;
					} else {
						t = yMove;
					}
					
					currStackItem.domObj.css({
						top : t,
						left : l,
						height : h,
						width : w
					}).fadeIn("slow");
					
					currStackItem.rect = new Rectangle(t, l, h, w);
				}
			}
		});
		
	// Stop size adjustment on mouseup event
	
	$(document)
		.mouseup(function(e) {
			if(active){
				isMouseDown = false;
				$("#current").attr({
					id : 'rect' + i
				});
			}
		});
		
	
	// Register custom functions for links
	
	// Undo
	$("#undoAnchor").click(function() {
		if (currStackItem.length != 0) {
			currStackItem.domObj.hide();
			var index = undoStack.indexOf(currStackItem)-1;
			if (index >= 0) {
				currStackItem = undoStack[index];
			} else {
				currStackItem = [];
			}
		}
	});
	
	// Redo
	$("#redoAnchor").click(function() {
		if (undoStack.length == 0) return;
		
		if (currStackItem == null || currStackItem.length == 0) {
				currStackItem = undoStack[0];
		} else if (currStackItem != undoStack[undoStack.length-1]) {
				var index = undoStack.indexOf(currStackItem)+1;
				currStackItem = undoStack[index];
		}
		currStackItem.domObj.fadeIn("slow");
	});
    
    // Rotate
	$("#rotateAnchor").click(function() {
        $("#submitAnchor").parent().append("<input type='hidden' name='action' value='fotoRotate'/>");
        if($("#fotoEditForm").length) {
			$("#fotoEditForm").submit();
		}
		else {
			$("form").submit();
		}
	});
	
	// Clear
	$("#editDisabledAnchor").click(function() {
		clearRecStack();
		$("#editDisabledAnchor").hide();
		$("#editEnableAnchor").show();
		$("#editDisabledAnchor ~ button").hide(300);
		active = false;
	});
	
	function clearRecStack() {
		$("div#recStack").children().remove();
		undoStack = [];
		currStackItem = null;
	}
	
	// Submit
	$("#submitAnchor").click(function(e) {
		
		if (currStackItem == null || currStackItem.length == 0) return;
		$.md('Sollen die Änderungen im Foto gespeichert werden? <div style="font-size: 0.8em; margin-top:3px"> Hinweis: Die Speicherung kann nicht wieder rückgängig gemacht werden.</div>',{
			fullscreen: false,
			cssDir: webAppUrl + "script/jquery/modalDialog/css",
			showClose: false,
			showMinimize: false,
			showFullscreen: false,
			type: 'prompt',
			modalBG: '#000000',
			width: 300,
			buttons:{
				'Ja':function(){ submit(); },
				'Nein':function(){$.md.hide(); return false; }
			}
		});
		$('#md-close').hide();
		$('#md-title').html("Foto speichern");
	});
	
	function submit() {
		
		var picture = $("img#picture");
		var t = picture.position().top;
		var l = picture.position().left;
		var w = picture.width();
		var h = picture.height();
	//	console.log("new imageTop: " + t + " imageLeft:" + l);
	//	console.log("imageW: " + w + " imageH:" + h);
		var rectCoords = "";
		for (var i = 0; i < undoStack.length; i++) {
			var rect = undoStack[i].rect;
			rectCoords += (rect.left-l) + "," + (rect.top-t) + "," + (rect.width+2) + "," + (rect.height+2) + ";" ;//+2 to compensate border
			if (undoStack[i] == currStackItem) break;
		}
		$("#submitAnchor").parent().append("<input type='hidden' name='censorRectangles' value='"+rectCoords+"'/>");
		$("#submitAnchor").parent().append("<input type='hidden' name='censoringWidth' value='"+w+"'/>");
		$("#submitAnchor").parent().append("<input type='hidden' name='censoringHeight' value='"+h+"'/>");
		$("#submitAnchor").parent().append("<input type='hidden' name='action' value='fotoSave'/>");
		
		if($("#fotoEditForm").length) {
			$("#fotoEditForm").submit();
		}
		else {
			$("form").submit();
		}
		$.md.hide();
	}	
});