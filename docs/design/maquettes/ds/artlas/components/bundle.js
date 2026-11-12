/* Artlas — icônes et utilitaires partagés par les aperçus. Aucun calcul métier : les valeurs (distance, durée, code) viennent de l'API. */
(function () {
  var ICONS = {
    "type-autre": "<path d=\"M12 3 21 12 12 21 3 12z\"/><circle cx=\"12\" cy=\"12\" r=\"2\"/>",
    "type-bibliotheque": "<path d=\"M12 6.5C10 5 7 4.5 3.5 5v13c3.5-.5 6.5 0 8.5 1.5 2-1.5 5-2 8.5-1.5V5C17 4.5 14 5 12 6.5z\"/><path d=\"M12 6.5v13\"/>",
    "type-cinema": "<path d=\"M3 10h18v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z\"/><path d=\"m3.2 10-.7-3 16.4-4 .8 3z\"/><path d=\"m7.6 5.9 2.2 3M12.6 4.7l2.2 3\"/>",
    "type-lieu-exposition": "<rect x=\"4\" y=\"4.5\" width=\"16\" height=\"12.5\" rx=\"1\"/><path d=\"m7 14 3.2-3.6 2.4 2.6 1.6-1.6L17 14\"/><circle cx=\"15\" cy=\"8.3\" r=\"1.2\"/><path d=\"M12 17v3.5M8.5 20.5h7\"/>",
    "type-monument": "<path d=\"M7 21V9h10v12\"/><path d=\"M6 9V4.5h2.5v2h2.3v-2h2.4v2h2.3v-2H18V9\"/><path d=\"M10.5 21v-3.5a1.5 1.5 0 0 1 3 0V21\"/><path d=\"M4.5 21h15\"/>",
    "type-musee": "<path d=\"M3 9.5 12 4l9 5.5\"/><path d=\"M4 9.5h16\"/><path d=\"M6.5 12.5v5M10.2 12.5v5M13.8 12.5v5M17.5 12.5v5\"/><path d=\"M3.5 20.5h17\"/>",
    "type-theatre": "<path d=\"M5 4h14v7a7 7 0 0 1-14 0z\"/><path d=\"M8.3 8.8h2.2M13.5 8.8h2.2\"/><path d=\"M9 13.8a4 4 0 0 0 6 0\"/>",
    "search": "<circle cx=\"11\" cy=\"11\" r=\"6.5\"/><path d=\"m16 16 4.5 4.5\"/>",
    "locate": "<circle cx=\"12\" cy=\"12\" r=\"7\"/><circle cx=\"12\" cy=\"12\" r=\"2.2\"/><path d=\"M12 2.5V5M12 19v2.5M2.5 12H5M19 12h2.5\"/>",
    "close": "<path d=\"m6 6 12 12M18 6 6 18\"/>",
    "route": "<circle cx=\"6\" cy=\"18\" r=\"2.2\"/><circle cx=\"18\" cy=\"6\" r=\"2.2\"/><path d=\"M8.2 18H15a3 3 0 0 0 0-6H9a3 3 0 0 1 0-6h6.8\"/>",
    "share": "<path d=\"M12 15V3.5\"/><path d=\"m7.5 8 4.5-4.5L16.5 8\"/><path d=\"M5 12.5V19a1.5 1.5 0 0 0 1.5 1.5h11A1.5 1.5 0 0 0 19 19v-6.5\"/>",
    "plus": "<path d=\"M12 5v14M5 12h14\"/>",
    "minus": "<path d=\"M5 12h14\"/>",
    "list": "<path d=\"M9 6.5h11M9 12h11M9 17.5h11\"/><circle cx=\"4.5\" cy=\"6.5\" r=\".9\"/><circle cx=\"4.5\" cy=\"12\" r=\".9\"/><circle cx=\"4.5\" cy=\"17.5\" r=\".9\"/>",
    "map": "<path d=\"M3.5 6.5 9 4l6 2.5L20.5 4v13.5L15 20l-6-2.5-5.5 2.5z\"/><path d=\"M9 4v13.5M15 6.5V20\"/>",
    "clock": "<circle cx=\"12\" cy=\"12\" r=\"8.5\"/><path d=\"M12 7.5V12l3 2\"/>",
    "walk": "<circle cx=\"13\" cy=\"4.5\" r=\"1.6\"/><path d=\"m10 21 2-6 2.5 2.5V21\"/><path d=\"m8 11 3-3.5h2.5l2 3.5 2.5 1\"/><path d=\"m11 7.5-1 5.5 2 2\"/>",
    "check": "<circle cx=\"12\" cy=\"12\" r=\"8.5\"/><path d=\"m8.3 12.3 2.4 2.4 5-5.2\"/>",
    "warning": "<path d=\"M12 4 21 19.5H3z\"/><path d=\"M12 10v4\"/><circle cx=\"12\" cy=\"16.8\" r=\".6\"/>",
    "error": "<circle cx=\"12\" cy=\"12\" r=\"8.5\"/><path d=\"m9 9 6 6M15 9l-6 6\"/>",
    "info": "<circle cx=\"12\" cy=\"12\" r=\"8.5\"/><path d=\"M12 11v5.5\"/><circle cx=\"12\" cy=\"8\" r=\".6\"/>",
    "external": "<path d=\"M14 4.5h5.5V10\"/><path d=\"m19.5 4.5-8 8\"/><path d=\"M17 14v4.5a1 1 0 0 1-1 1H5.5a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1H10\"/>",
    "drag": "<circle cx=\"9\" cy=\"7\" r=\".9\"/><circle cx=\"15\" cy=\"7\" r=\".9\"/><circle cx=\"9\" cy=\"12\" r=\".9\"/><circle cx=\"15\" cy=\"12\" r=\".9\"/><circle cx=\"9\" cy=\"17\" r=\".9\"/><circle cx=\"15\" cy=\"17\" r=\".9\"/>",
    "copy": "<rect x=\"8.5\" y=\"8.5\" width=\"11\" height=\"11\" rx=\"1.5\"/><path d=\"M15.5 8.5V6a1.5 1.5 0 0 0-1.5-1.5H6A1.5 1.5 0 0 0 4.5 6v8A1.5 1.5 0 0 0 6 15.5h2.5\"/>"
    };
  var TYPES = [
    { code: "MUSEE", slug: "musee", libelle: "Musée", pluriel: "Musées" },
    { code: "MONUMENT", slug: "monument", libelle: "Monument", pluriel: "Monuments" },
    { code: "LIEU_EXPOSITION", slug: "exposition", icon: "lieu-exposition", libelle: "Lieu d'exposition", pluriel: "Lieux d'exposition" },
    { code: "THEATRE", slug: "theatre", libelle: "Théâtre", pluriel: "Théâtres" },
    { code: "CINEMA", slug: "cinema", libelle: "Cinéma", pluriel: "Cinémas" },
    { code: "BIBLIOTHEQUE", slug: "bibliotheque", libelle: "Bibliothèque", pluriel: "Bibliothèques" },
    { code: "AUTRE", slug: "autre", libelle: "Autre lieu", pluriel: "Autres lieux" }
  ];
  function svg(name, size) {
    var s = size || 20;
    return '<svg class="atl-icon" aria-hidden="true" focusable="false" viewBox="0 0 24 24" width="' + s + '" height="' + s +
      '" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">' + (ICONS[name] || "") + "</svg>";
  }
  function typeOf(code) {
    for (var i = 0; i < TYPES.length; i++) if (TYPES[i].code === code || TYPES[i].slug === code) return TYPES[i];
    return TYPES[TYPES.length - 1];
  }
  function typeIcon(code, size) { var t = typeOf(code); return svg("type-" + (t.icon || t.slug), size); }
  function hydrateIcons(root) {
    var nodes = (root || document).querySelectorAll("[data-icon]");
    for (var i = 0; i < nodes.length; i++) {
      var n = nodes[i], name = n.getAttribute("data-icon"), size = n.getAttribute("data-size");
      n.innerHTML = name.indexOf("type:") === 0 ? typeIcon(name.slice(5), size) : svg(name, size);
    }
  }
  window.Artlas = { icons: ICONS, types: TYPES, svg: svg, typeOf: typeOf, typeIcon: typeIcon, hydrateIcons: hydrateIcons };
})();
