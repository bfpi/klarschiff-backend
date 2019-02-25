
$(function () {

  function addAussendienstZustaendigkeitObserver() {
    $('#mehrfachauswahl').on("click", "ul li a", function (elem) {
      var target = $(elem.target);
      var a = target.parents("a");
      var li = $(target.parents("li"));
      var location = window.location.pathname.split('/')[1];
      if (a[0].className === "add") {
        li.find("a img").attr("src", "/" + location + "/images/go-next-hover2.png");
        li.find("input").attr("name", "zugewiesen[]");
        a[0].className = "remove";
        $('#zugewiesen').append($('<div>').append(li).html());
      } else {
        li.find("a img").attr("src", "/" + location + "/images/go-previous.png");
        li.find("input").attr("name", "moeglich[]");
        a[0].className = "add";
        $('#verfuegbar').append($('<div>').append(li).html());
      }
    });
  }

  addAussendienstZustaendigkeitObserver();

});
