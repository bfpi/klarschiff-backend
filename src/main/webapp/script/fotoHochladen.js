// @author Alexander Kruth (BFPI)

$(function () {
	$("#uploadButton").click(function() {
		$("#uploadButton").hide();
		$("#uploadForm").css('display', 'inline');
	});
    $("#uploadCancelButton").click(function() {
		$("#uploadButton").show();
		$("#uploadForm").hide();
	});
});
