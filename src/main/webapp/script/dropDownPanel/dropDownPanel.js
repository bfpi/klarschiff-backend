//@author Stefan Audersch (Fraunhofer IGD)

jQuery.dropDownPanel = function (panel, content, tab) {
	
	if ($.cookie('klarschiff_'+panel)=='true') {
		$('#'+content).show();
	} else {
		$('#'+content).hide();
	}
	$('#'+tab).click(function() {
		  $('#'+content).slideToggle(300, function() {
			  $.cookie('klarschiff_'+panel, $('#'+content).is(":visible"), { expires: 365, path: '/'});
		  });
		});
	
	return null;
};
