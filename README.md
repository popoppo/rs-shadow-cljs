Demo for react-stockcharts on ClojureScript(Reagent) with shadow-cljs
----

[ClojureScript](https://clojurescript.org/) can be used to implement web apps instead of (or with) Javascript.
However, it might not be easy enough to use node moudles for it.
The ClojureScript compiler now has :npm-deps option to import node modules, but it doesn't perfectly.

[shadow-cljs](https://github.com/thheller/shadow-cljs) can provide better integrity in that case.
This demo would be a good example of how to use shadow-cljs.

### Usage

```bash
shadow-cljs watch app
```

### License

MIT
