
$(function () {
  var max = 12;
  var min = 8;
  var fontSize;
  var width1;
  var width2;

  function addResultTableSelectAllFunction() {
    var vorgang_checkboxen = $('table.resulttable tr td input[type=checkbox][name=vorgangAuswaehlen]');
    $('table.resulttable tr th input[type=checkbox]#alleVorgaengeAuswaehlen').on("change", function (elem) {
      var target = $(elem.target);
      vorgang_checkboxen.prop("checked", (target.attr('checked') === "checked"));
    });
  }

  function width(elem) {
    if (elem.innerWidth) {
      return elem.innerWidth();
    } else if (elem.offsetWidth) {
      return elem.offsetWidth();
    } else {
      return 0;
    }
  }

  function getFontSize(elem) {
    return parseFloat(elem.css('font-size'), 10);
  }

  function setFontSize(elem) {
    elem.css('font-size', fontSize);
  }

  function resizeResultTable() {
    fontSize = getFontSize($('table.resulttable'));
    width1 = width($('div#root_style_content'));
    width2 = width($('table.resulttable'));

    while (width1 - 20 > width2 && fontSize < max) {
      fontSize = fontSize + 0.5;
      setFontSize($('table.resulttable'));
      width2 = width($('table.resulttable'));
    }
    while (width1 < width2 && fontSize > min) {
      fontSize = fontSize - 0.5;
      setFontSize($('table.resulttable'));
      width2 = width($('table.resulttable'));
    }
  }

  if ($('table.resulttable') !== undefined) {
    addResultTableSelectAllFunction();
    resizeResultTable();
    window.onresize = resizeResultTable;
  }
});
