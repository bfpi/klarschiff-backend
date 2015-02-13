
$(function () {

  function addAussendienstZustaendigkeitObserver() {
    $('#aussendienst_zustaendigkeiten').on("click", "ul li a", function (elem) {
      var target = $(elem.target);
      var a = target.parents("a");
      var li = $(target.parents("li"));
      if (a[0].className === "add") {
        li.find("a img").attr("src", "/backend/images/go-next-hover2.png");
        li.find("input").attr("name", "zugewiesen[]");
        a[0].className = "remove";
        $('#aktuellerBenutzer').append($('<div>').append(li).html());
      } else {
        li.find("a img").attr("src", "/backend/images/go-previous.png");
        li.find("input").attr("name", "moeglich[]");
        a[0].className = "add";
        $('#aussendienstTeams').append($('<div>').append(li).html());
      }
    });
  }

  addAussendienstZustaendigkeitObserver();

});
