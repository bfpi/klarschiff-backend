function iframeDialog(title, url) {
	dialogWidth=$(window).width()-60;;
	if (dialogWidth>850) dialogWidth=850;
	
	heightBody=Math.max($('body').height(), $(window).height());
	left=($('body').width()-dialogWidth)/2;
	heightWindow=$(window).height()-60;

	firstElem = $($('body').children()[0]);
	mdFrame = '<div id="md-frame" style="display:none"></div>';
	
	mdMask = ('<div id="md-mask" class="md-close-onclick" style="display: block; background-color: rgb(0, 0, 0); height:'+heightBody+'px; opacity: 0.8;">&nbsp;</div>');
	dialogHtml = '<div id="md" style="display: block; width:'+dialogWidth+'px; top:30px; left:'+left+'px; height:'+heightWindow+'px; background-color:#E1E1E1;">'
	+ '  <div id="md-header" class="errheader promptheader">'
	+ '    <div id="md-title">'+title+'</div>'
	+ '    <div class="md-icon md-close-onclick" id="md-close" style="display: relative;"></div>'
	+ '  </div>'
	+ '  <div id="md-prompt" class="err prompt">'
	+ '    <div id="md-content-inner">'
	+ '      <iframe src="'+url+'" width="100%" height="'+(heightWindow-60)+'" scrolling="auto" marginheight="0" marginwidth="0" frameborder="0">Hilfe</iframe>'
	+ '    	 <div style="text-align: center; margin: 2px;">'
	+ '			<input id="md-close-button" class="md-close-onclick" value="schließen" type="submit" id="proceed" />'
	+ '    	 </div>'
	+ '    </div>'
	+ '  </div>'
	+ '</div>';
	
	firstElem.before(mdFrame);
	$('#md-frame').append(mdMask);
	$('#md-frame').append(dialogHtml);
	
	$('.md-close-onclick').click(function() {
		$('#md-frame').hide('slow', function () {
			$('#md-frame').remove();
		});
	});
	
	$('#md-frame').show('slow');
}
