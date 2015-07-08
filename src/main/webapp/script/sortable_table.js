
$(function () {

  function setTableSortable() {
    var error_text = "Beim Aktualisieren der Sortierung ist ein Fehler aufgetreten.";

    $("table.sortable tbody").sortable({
      handle: 'span.sorting-arrows',
      helper: function (e, ui) {
        ui.children().each(function () {
          $(this).width($(this).width());
        });
        return ui;
      },
      placeholder: "sortable-highlight",
      start: function (event, ui) {
        var column_count = 0;
        $('td, th', ui.helper).each(function () {
          column_count += 1;
        });
        ui.placeholder.html("'<td colspan='" + column_count + "'>&nbsp;</td>'");
      },
      stop: function (event, ui) {
        var tbody = $(ui.item).parent("tbody.ui-sortable");
        var table = tbody.parent("table[data-updateUrl]");
        if (table.data("updateurl") != undefined) {
          var ids = [];
          tbody.find(".id span").each(function () {
            ids.push($(this).data('id'));
          });

          $.ajax({
            dataType: "json",
            type: "POST",
            url: table.data("updateurl"),
            data: {ids: ids},
            success: function (text) {
              if (text != true) {
                alert(error_text);
              }
            }, error: function (text) {
              if (text != true) {
                alert(error_text);
              }
            }
          });
        }
      }
    }).disableSelection();
  }

  setTableSortable();

});
