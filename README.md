Demo for react-stockcharts on ClojureScript(Reagent) with shadow-cljs
----

[ClojureScript](https://clojurescript.org/) can be used to implement web apps instead of (or with) Javascript.  
However, it might not be easy enough to use node moudles for it.  
The ClojureScript compiler now has :npm-deps option to import node modules, but it doesn't perfectly.

[shadow-cljs](https://github.com/thheller/shadow-cljs) would provide better integrity in that case.  
I hope this example can be helpful to you.

### Usage

```bash
npm install
npx shadow-cljs watch app
```

### License

MIT
