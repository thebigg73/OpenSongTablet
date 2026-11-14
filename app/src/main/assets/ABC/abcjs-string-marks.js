/* abcjs-string-marks.js
 * Load AFTER the patched abcjs-basic-min.js.
 * Lets ABC carry per-note guitar string marks:  [!s4!E !s2!c]   (1 = highest string)
 * Marks are read from the ABC text after parsing and attached to the pitches; the patched
 * TAB code uses them. Unpatched/other ABC software just sees an unknown decoration.
 * It wraps ABCJS.renderAbc, so existing calls need no changes. ABCJS.Editor calls an
 * internal copy of renderAbc and is NOT covered by the wrapper: for the Editor, add
 * afterParsing: window.abcStringMarksAfterParsing to its abcjsParams.
 */
(function (root) {
  var TOKEN = /!s(\d)!|"[^"]*"|![^!]*!|([\^_=]*[A-Ga-g][,']*)/g;

  function applyMarks(tune, n, str) {
    if (!tune || !tune.lines || !str || str.indexOf("!s") < 0) return;
    tune.lines.forEach(function (l) {
      if (!l.staff) return;
      l.staff.forEach(function (st) {
        st.voices.forEach(function (v) {
          v.forEach(function (el) {
            if (el.el_type !== "note" || !el.pitches) return;
            var txt = str.slice(el.startChar, el.endChar).replace(/\{[^}]*\}/g, "");
            var pending = null, idx = 0, m;
            TOKEN.lastIndex = 0;
            while ((m = TOKEN.exec(txt))) {
              if (m[1]) { pending = +m[1]; continue; }
              if (m[2]) {
                if (pending && el.pitches[idx]) el.pitches[idx].fs = pending;
                pending = null; idx++;
              }
            }
          });
        });
      });
    });
  }

  function install(A) {
    if (!A || A.__stringMarks || typeof A.renderAbc !== "function") return false;
    var orig = A.renderAbc;
    A.renderAbc = function (output, abc, params) {
      var p = Object.assign({}, params || {});
      var prev = p.afterParsing;
      p.afterParsing = function (tune, n, s) {
        try { applyMarks(tune, n, s); } catch (e) { if (root.console) console.warn("string marks:", e); }
        return prev ? prev(tune, n, s) : undefined;
      };
      var args = Array.prototype.slice.call(arguments);
      args[2] = p;
      return orig.apply(this, args);
    };
    A.__stringMarks = true;
    return true;
  }

  install(root.ABCJS || root.abcjs);
  root.installAbcStringMarks = install;
  // For ABCJS.Editor (which calls an internal renderAbc that the wrapper above cannot see):
  // pass this in the Editor's abcjsParams:  afterParsing: window.abcStringMarksAfterParsing
  root.abcStringMarksAfterParsing = applyMarks;
})(typeof window !== "undefined" ? window : globalThis);