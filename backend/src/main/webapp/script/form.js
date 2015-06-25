
$(function () {

  if ($("form[data-autosave]") !== undefined) {

    $(document).on("change", "form[data-autosave] *", function (e) {
      if ($(e.currentTarget).is("input, textarea, select")) {
        var status_update_error_text = "Beim Aktualisieren der Sortierung ist ein Fehler aufgetreten.";
        $.ajax({
          dataType: "json",
          type: "POST",
          url: e.currentTarget.form.action,
          data: $(e.currentTarget.form).serialize(),
          success: function (text) {
            if (text != true) {
              alert(status_update_error_text);
            }
          }, error: function (text) {
            if (text != true) {
              alert(status_update_error_text);
            }
          }
        });
      }
    });
  }
});