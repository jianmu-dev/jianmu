import CodeMirror from 'codemirror';

(function (mod) {
  mod(CodeMirror);
})(function (CodeMirror) {
  CodeMirror.defineMode('log', function () {
    return {
      token: function (stream) {
        stream.next();

        return null;
      },
    };
  });

  CodeMirror.defineMIME('text/x-log', 'log');
  CodeMirror.defineMIME('text/log', 'log');
});