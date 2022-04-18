/* eslint-disable no-use-before-define */
// 引入全局实例
import CodeMirror from 'codemirror';
import 'codemirror/addon/search/searchcursor.js';
import 'codemirror/addon/search/jump-to-line.js';
import 'codemirror/addon/dialog/dialog.js';
import 'codemirror/addon/dialog/dialog.css';

(function () {
  CodeMirror.defineOption('search', { bottom: false });

  function searchOverlay(query, caseInsensitive) {
    if (typeof query === 'string')
      query = new RegExp(query.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, '\\$&'), caseInsensitive ? 'gi' : 'g');
    else if (!query.global)
      query = new RegExp(query.source, query.ignoreCase ? 'gi' : 'g');

    return {
      token: function (stream) {
        query.lastIndex = stream.pos;
        const match = query.exec(stream.string);
        if (match && match.index === stream.pos) {
          stream.pos += match[0].length || 1;
          return 'searching';
        } else if (match) {
          stream.pos = match.index;
        } else {
          stream.skipToEnd();
        }
      },
    };
  }

  function SearchState() {
    this.posFrom = this.posTo = this.lastQuery = this.query = null;
    this.overlay = null;
  }

  function getSearchState(cm) {
    return cm.state.search || (cm.state.search = new SearchState());
  }

  function queryCaseInsensitive(query) {
    return typeof query === 'string' && query === query.toLowerCase();
  }

  function getSearchCursor(cm, query, pos) {
    // Heuristic: if the query string is all lowercase, do a case insensitive search.
    return cm.getSearchCursor(query, pos, { caseFold: queryCaseInsensitive(query), multiline: true });
  }

  function persistentDialog(cm, text, deflt, onEnter, onKeyDown) {
    cm.openDialog(text, onEnter, {
      value: deflt,
      selectValueOnOpen: true,
      closeOnEnter: false,
      onClose: function () {
        clearSearch(cm);
      },
      onKeyDown: onKeyDown,
      bottom: cm.options.search.bottom,
    });
  }

  function parseString(string) {
    return string.replace(/\\([nrt\\])/g, function (match, ch) {
      if (ch === 'n') return '\n';
      if (ch === 'r') return '\r';
      if (ch === 't') return '\t';
      if (ch === '\\') return '\\';
      return match;
    });
  }

  function parseQuery(query) {
    const isRE = query.match(/^\/(.*)\/([a-z]*)$/);
    if (isRE) {
      try {
        query = new RegExp(isRE[1], isRE[2].indexOf('i') === -1 ? '' : 'i');
        // eslint-disable-next-line no-empty
      } catch (e) {
      } // Not a regular expression after all, do a string search
    } else {
      query = parseString(query);
    }
    if (typeof query === 'string' ? query === '' : query.test(''))
      query = /x^/;
    return query;
  }

  function startSearch(cm, state, query) {
    state.queryText = query;
    state.query = parseQuery(query);
    cm.removeOverlay(state.overlay, queryCaseInsensitive(state.query));
    state.overlay = searchOverlay(state.query, queryCaseInsensitive(state.query));
    cm.addOverlay(state.overlay);
    if (cm.showMatchesOnScrollbar) {
      if (state.annotate) {
        state.annotate.clear();
        state.annotate = null;
      }
      state.annotate = cm.showMatchesOnScrollbar(state.query, queryCaseInsensitive(state.query));
    }
  }

  function doSearch(cm, rev, immediate) {
    const state = getSearchState(cm);
    if (state.query) return findNext(cm, rev);
    let q = cm.getSelection() || state.lastQuery;
    if (q instanceof RegExp && q.source === 'x^') q = null;

    let hiding = null;
    const searchNext = function (query, event) {
      CodeMirror.e_stop(event);
      if (!query) return;
      if (query !== state.queryText) {
        startSearch(cm, state, query);
        state.posFrom = state.posTo = cm.getCursor();
      }
      if (hiding) hiding.style.opacity = 1;
      findNext(cm, event.shiftKey, function (_, to) {
        let dialog;
        if (to.line < 3 && document.querySelector &&
          (dialog = cm.display.wrapper.querySelector('.CodeMirror-dialog')) &&
          dialog.getBoundingClientRect().bottom - 4 > cm.cursorCoords(to, 'window').top)
          (hiding = dialog).style.opacity = .4;
      });
    };
    persistentDialog(cm, getQueryDialog(cm), q, searchNext, function (event, query) {
      const keyName = CodeMirror.keyName(event);
      const extra = cm.getOption('extraKeys');
      let cmd = (extra && extra[keyName]) || CodeMirror.keyMap[cm.getOption('keyMap')][keyName];
      if (!cmd) {
        switch (keyName) {
          case 'Up':
            cmd = 'findPrev';
            break;
          case 'Down':
            cmd = 'findNext';
            break;
        }
      }

      if (cmd === 'findNext' || cmd === 'findPrev' ||
        cmd === 'findPersistentNext' || cmd === 'findPersistentPrev') {
        CodeMirror.e_stop(event);
        startSearch(cm, getSearchState(cm), query);
        cm.execCommand(cmd);
      } else if (cmd === 'find' || cmd === 'findPersistent') {
        CodeMirror.e_stop(event);
        searchNext(query, event);
      }
    });
    if (immediate && q) {
      startSearch(cm, state, q);
      findNext(cm, rev);
    }
  }

  function findNext(cm, rev, callback) {
    cm.operation(function () {
      const state = getSearchState(cm);
      let cursor = getSearchCursor(cm, state.query, rev ? state.posFrom : state.posTo);
      if (!cursor.find(rev)) {
        cursor = getSearchCursor(cm, state.query, rev ? CodeMirror.Pos(cm.lastLine()) : CodeMirror.Pos(cm.firstLine(), 0));
        if (!cursor.find(rev)) return;
      }
      cm.setSelection(cursor.from(), cursor.to());
      cm.scrollIntoView({ from: cursor.from(), to: cursor.to() }, 20);
      state.posFrom = cursor.from();
      state.posTo = cursor.to();
      if (callback) callback(cursor.from(), cursor.to());
    });
  }

  function clearSearch(cm) {
    cm.operation(function () {
      const state = getSearchState(cm);
      state.lastQuery = state.query;
      if (!state.query) return;
      state.query = state.queryText = null;
      cm.removeOverlay(state.overlay);
      if (state.annotate) {
        state.annotate.clear();
        state.annotate = null;
      }
    });
  }


  function getQueryDialog(cm) {
    return `
      <input type="text" style="width: 100%" class="CodeMirror-search-field" 
        placeholder="${cm.phrase('请输入查找内容，支持正则表达式，格式为/reg/')}"/>
    `;
  }

  CodeMirror.commands.findPersistent = function (cm) {
    clearSearch(cm);
    doSearch(cm, false);
  };
  CodeMirror.commands.findPersistentNext = function (cm) {
    doSearch(cm, false, true);
  };
  CodeMirror.commands.findPersistentPrev = function (cm) {
    doSearch(cm, true, true);
  };
  CodeMirror.commands.findNext = doSearch;
  CodeMirror.commands.findPrev = function (cm) {
    doSearch(cm, true);
  };
})();
