
$(function () {
  
  function addResultTableTrClick() {
    $('table.resulttable tr[data-tr-click-target]').on("click", "td:not(.skip-tr-click)", function (elem) {
      window.location.href = $(elem.currentTarget).parent("tr[data-tr-click-target]").data("trClickTarget");
    });
  }

  addResultTableTrClick();

});
